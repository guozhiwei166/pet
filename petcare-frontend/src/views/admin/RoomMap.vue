<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../../api/http'
import api from '../../api'
import { useUserStore } from '../../stores/user'

interface MapRoom {
  id: number; roomNo: string; floor: number; status: string; note: string
  roomTypeName: string; physicalStatus?: string; species?: string
  orderId?: number; petId?: number; petName?: string; contactName?: string
  startDate?: string; endDate?: string; rangeText?: string; occupied?: boolean; overdue?: boolean
  reservations?: { orderId: number; petName?: string; contactName?: string; contactPhone?: string; rangeText: string }[]
  reservationCount?: number; deletable?: boolean
}
interface Floor { floor: number; rooms: MapRoom[] }
interface RoomTypeOption { id: number; name: string; status?: number; species?: string }

const route = useRoute()
const router = useRouter()
const store = useUserStore()

const floors = ref<Floor[]>([])
const drawer = ref(false)
const current = ref<MapRoom | null>(null)

// ---------- 添加房间（管理员为房型选楼层/房间号建房） ----------
const addVisible = ref(false)
const roomTypes = ref<RoomTypeOption[]>([])
const addForm = ref<{ roomTypeId: number | null; species: string; floor: number; startNo: string; count: number }>({
  roomTypeId: null, species: 'DOG', floor: 1, startNo: '', count: 3,
})

/** 当前分区下的房型（房型下拉只列本分区，猫/狗由上面的分区选项决定） */
const speciesRoomTypes = computed(() =>
  roomTypes.value.filter(t => (t.species || 'DOG') === addForm.value.species))

/** 切换分区：原选中的房型若不属于新分区则清空，避免建出跨分区的房间 */
function onSpeciesChange() {
  const cur = roomTypes.value.find(t => t.id === addForm.value.roomTypeId)
  if (cur && (cur.species || 'DOG') !== addForm.value.species) addForm.value.roomTypeId = null
}

/** 预选房型（从房型管理跳转 / 未配置房型提醒）：同时把分区切到该房型所属分区 */
function presetRoomType(typeId: number | null) {
  const t = typeId ? roomTypes.value.find(x => x.id === typeId) : undefined
  addForm.value.species = (t?.species || 'DOG') === 'CAT' ? 'CAT' : 'DOG'
  addForm.value.roomTypeId = t?.id ?? null
}
const adding = ref(false)

const STATUS: Record<string, { text: string; cls: string }> = {
  AVAILABLE: { text: '空闲', cls: 'st-available' },
  RESERVED: { text: '已预约', cls: 'st-reserved' },
  OCCUPIED: { text: '入住中', cls: 'st-occupied' },
  CLEANING: { text: '清洁中', cls: 'st-cleaning' },
  MAINTENANCE: { text: '维修中', cls: 'st-maintenance' },
}

const stats = computed(() => {
  const all = floors.value.flatMap(f => f.rooms)
  return {
    total: all.length,
    occupied: all.filter(r => r.status === 'OCCUPIED').length,
    available: all.filter(r => r.status === 'AVAILABLE').length,
    cleaning: all.filter(r => r.status === 'CLEANING').length,
  }
})

/** 管理员才能添加房间（后端 room:manage 兜底） */
const canAddRoom = computed(() => store.isAdmin)

/** 尚无任何房间的房型（提示去配置，避免寄养人订不到） */
const noRoomTypes = computed(() => {
  const withRooms = new Set(floors.value.flatMap(f => f.rooms).map(r => r.roomTypeName))
  return roomTypes.value.filter(t => !withRooms.has(t.name))
})

async function load() {
  const data = await api.roomMap()
  floors.value = data
  // 顺带刷新房型下拉（all=true 含下架房型，便于为历史房型补房）
  try {
    roomTypes.value = await api.allRoomTypes()
  } catch { /* 忽略 */ }
}

function open(room: MapRoom) {
  current.value = room
  drawer.value = true
}

/** 本层空闲房间数 */
const floorAvailable = (f: Floor) => f.rooms.filter(r => r.status === 'AVAILABLE').length
/** 本层是否住满（无空闲无清洁中） */
const floorFull = (f: Floor) => floorAvailable(f) === 0 && !f.rooms.some(r => r.status === 'CLEANING')

/** 当前楼层最大数字房号（用于推荐续号），如 101-120 → 120 */
function floorMaxNo(floor: number): number {
  const nos = (floors.value.find(f => f.floor === floor)?.rooms || [])
    .map(r => r.roomNo.replace(/[^0-9]/g, ''))
    .filter(n => n.length > 0)
    .map(Number)
  return nos.length ? Math.max(...nos) : 0
}

/** 打开添加房间弹窗；typeId 用于从房型管理跳转时预选 */
async function openAdd(typeId?: number) {
  if (!roomTypes.value.length) await load()
  const qTypeId = typeId || (route.query.typeId ? Number(route.query.typeId) : null)
  addForm.value = { roomTypeId: null, species: 'DOG', floor: 1, startNo: '', count: 3 }
  presetRoomType(qTypeId && roomTypes.value.some(t => t.id === qTypeId) ? qTypeId : null)
  suggestStartNo() // 按默认楼层 1 推荐
  addVisible.value = true
}

/** 在指定楼层快捷加房（该楼层已有房间则默认续号） */
function openAddForFloor(floor: number) {
  if (!roomTypes.value.length) load()
  const qTypeId = route.query.typeId ? Number(route.query.typeId) : null
  addForm.value = { roomTypeId: null, species: 'DOG', floor, startNo: '', count: 3 }
  presetRoomType(qTypeId && roomTypes.value.some(t => t.id === qTypeId) ? qTypeId : null)
  suggestStartNo()
  addVisible.value = true
}

/** 根据所选楼层推荐起始房号：该层最大房号 + 1；无则 floor*100+1 */
function suggestStartNo() {
  const max = floorMaxNo(addForm.value.floor)
  addForm.value.startNo = max > 0 ? String(max + 1) : String(addForm.value.floor * 100 + 1)
}

function onFloorChange() {
  suggestStartNo()
}

async function confirmAdd() {
  if (!addForm.value.roomTypeId) return ElMessage.warning('请选择房型')
  if (!addForm.value.startNo.trim()) return ElMessage.warning('请填写起始房间号')
  if (!addForm.value.count || addForm.value.count < 1) return ElMessage.warning('请填写创建数量')
  adding.value = true
  try {
    const created = await api.batchCreateRooms({
      roomTypeId: addForm.value.roomTypeId!,
      floor: addForm.value.floor,
      startNo: addForm.value.startNo.trim(),
      count: addForm.value.count,
    })
    const typeName = roomTypes.value.find(t => t.id === addForm.value.roomTypeId)?.name || ''
    ElMessage.success(`已在 ${addForm.value.floor} 层创建 ${created.length} 间「${typeName}」房间：${created.join('、')}`)
    addVisible.value = false
    load()
  } finally {
    adding.value = false
  }
}

async function changeStatus(status: string) {
  if (!current.value) return
  if (status === 'MAINTENANCE') {
    await ElMessageBox.confirm(`将 ${current.value.roomNo} 房标记为维修中？`, '确认操作', { type: 'warning' })
  }
  await http.put(`/rooms/${current.value.id}/status`, { status })
  ElMessage.success('状态已更新')
  drawer.value = false
  load()
}

// ---------- 批量删除（多选） ----------
const batchMode = ref(false)
const selected = ref<Set<number>>(new Set())
const deleting = ref(false)
const selectedCount = computed(() => selected.value.size)

function toggleBatch() {
  batchMode.value = !batchMode.value
  selected.value = new Set()
  if (batchMode.value) drawer.value = false
}

function exitBatch() {
  batchMode.value = false
  selected.value = new Set()
}

const isSelected = (id: number) => selected.value.has(id)

/** 普通模式点房间开抽屉；批量模式下点选/取消（仅没有预约/在住订单的房间可删） */
function onRoomClick(r: MapRoom) {
  if (!batchMode.value) return open(r)
  if (!r.deletable) {
    const why = r.reservations?.length ? `存在 ${r.reservations.length} 条未来预约` : '当前有宠物入住'
    return ElMessage.warning(`「${r.roomNo}」${why}，不能删除`)
  }
  const next = new Set(selected.value)
  next.has(r.id) ? next.delete(r.id) : next.add(r.id)
  selected.value = next
}

/** 楼层全选 / 取消全选（只针对本层可删除的房间） */
function toggleFloor(f: Floor) {
  const free = f.rooms.filter(r => r.deletable)
  if (!free.length) return ElMessage.warning(`${f.floor} 层没有可删除的房间（都存在预约或在住）`)
  const allChosen = free.every(r => selected.value.has(r.id))
  const next = new Set(selected.value)
  free.forEach(r => (allChosen ? next.delete(r.id) : next.add(r.id)))
  selected.value = next
}

async function confirmBatchDelete() {
  if (!selected.value.size) return ElMessage.warning('请先选择要删除的房间')
  const ids = [...selected.value]
  const nos = floors.value.flatMap(f => f.rooms).filter(r => selected.value.has(r.id)).map(r => r.roomNo)
  await ElMessageBox.confirm(
    `确定删除选中的 ${ids.length} 间空闲房间？\n${nos.join('、')}`,
    '批量删除房间', { type: 'warning', confirmButtonText: '确认删除', cancelButtonText: '取消' })
  deleting.value = true
  try {
    const res = await api.batchDeleteRooms(ids)
    let msg = `已删除 ${res.deleted?.length || 0} 间房间`
    if (res.skipped?.length) msg += `；${res.skipped.length} 间因非空闲已跳过（${res.skipped.join('、')}）`
    ElMessage.success(msg)
    exitBatch()
    load()
  } finally {
    deleting.value = false
  }
}

/** 删除空闲房间（管理员） */
async function removeRoom() {
  if (!current.value) return
  await ElMessageBox.confirm(`确定删除空闲房间 ${current.value.roomNo}？`, '删除房间', { type: 'warning' })
  await api.deleteRoom(current.value.id)
  ElMessage.success('房间已删除')
  drawer.value = false
  load()
}

onMounted(async () => {
  await load()
  // 支持从「房型管理 → 配置房间」跳转：?typeId=xx 自动打开建房弹窗
  if (route.query.typeId) {
    await new Promise(r => setTimeout(r, 100))
    openAdd()
  }
})
</script>

<template>
  <div>
    <div class="stat-row">
      <div class="stat"><b class="num">{{ stats.total }}</b><span>房间总数</span></div>
      <div class="stat"><b class="num occupied">{{ stats.occupied }}</b><span>入住中</span></div>
      <div class="stat"><b class="num ok">{{ stats.available }}</b><span>空闲</span></div>
      <div class="stat"><b class="num clean">{{ stats.cleaning }}</b><span>清洁中</span></div>
      <div class="legend">
        <span><i class="dot st-available" />空闲</span>
        <span><i class="dot st-reserved" />已预约</span>
        <span><i class="dot st-occupied" />入住中</span>
        <span><i class="dot st-cleaning" />清洁中</span>
        <span><i class="dot st-maintenance" />维修中</span>
      </div>
      <el-button v-if="canAddRoom && !batchMode" icon="Delete" @click="toggleBatch">批量删除</el-button>
      <template v-if="batchMode">
        <span class="batch-count">已选 <b class="num">{{ selectedCount }}</b> 间（仅空闲房可删）</span>
        <el-button @click="exitBatch">退出批量</el-button>
        <el-button type="danger" icon="Delete" :disabled="!selectedCount" :loading="deleting"
                   @click="confirmBatchDelete">删除所选</el-button>
      </template>
      <el-button v-if="canAddRoom && !batchMode" type="primary" icon="Plus" @click="openAdd()">添加房间</el-button>
    </div>

    <!-- 未配置房间的房型提醒 -->
    <div v-if="roomTypes.length && noRoomTypes.length" class="no-room-tip">
      💡 以下房型还没有房间，寄养人无法预订：
      <el-link v-for="t in noRoomTypes" :key="t.id" type="primary" :underline="false"
               style="margin-left: 10px" @click="openAdd(t.id)">
        「{{ t.name }}」去配置 →
      </el-link>
    </div>

    <div v-for="f in floors" :key="f.floor" class="pc-card" style="margin-top: 16px">
      <div class="floor-head">
        <h4 style="margin: 0">{{ f.floor }} 层</h4>
        <span class="floor-stat sub">
          <template v-if="floorFull(f)"><b style="color:#D96C61">🔥 本层已住满</b></template>
          <template v-else>{{ floorAvailable(f) }} 间空闲 / {{ f.rooms.length }} 间</template>
        </span>
        <el-button v-if="batchMode" size="small" text type="primary" @click="toggleFloor(f)">本层全选/取消</el-button>
        <el-button v-else-if="canAddRoom" size="small" text type="primary" @click="openAddForFloor(f.floor)">＋ 在此层加房</el-button>
      </div>
      <div class="grid">
        <div v-for="r in f.rooms" :key="r.id" class="room-cell"
             :class="[STATUS[r.status]?.cls, { picking: batchMode, picked: isSelected(r.id), dimmed: batchMode && !r.deletable }]"
             @click="onRoomClick(r)">
          <b class="num">{{ r.roomNo }}</b>
          <span class="room-type">{{ r.roomTypeName }}</span>
          <span v-if="r.status === 'RESERVED'" class="room-status">预约【<span class="num">{{ r.rangeText }}</span>】</span>
          <span v-else class="room-status">{{ STATUS[r.status]?.text }}
            <template v-if="r.overdue">（超期未离店）</template>
          </span>
          <span v-if="r.status !== 'RESERVED' && r.reservationCount" class="room-sub num">
            未来预约 {{ r.reservationCount }} 条
          </span>
        </div>
      </div>
    </div>

    <el-drawer v-model="drawer" :title="current?.roomNo + ' 号房'" size="400px">
      <template v-if="current">
        <div class="d-row"><span>房型</span><b>{{ current.roomTypeName }}</b></div>
        <div class="d-row"><span>楼层</span><b>{{ current.floor }} 层</b></div>
        <div class="d-row"><span>当前状态</span>
          <el-tag :type="current.status === 'AVAILABLE' ? 'success' : current.status === 'OCCUPIED' ? 'primary'
                   : current.status === 'RESERVED' ? 'warning' : 'info'"
                  effect="light">{{ STATUS[current.status]?.text }}</el-tag>
        </div>
        <template v-if="current.orderId">
          <div class="d-row"><span>{{ current.occupied ? '入住宠物' : '预约宠物' }}</span>
            <b>{{ current.petName || '—' }}{{ current.contactName ? '（' + current.contactName + '）' : '' }}</b></div>
          <div class="d-row"><span>{{ current.occupied ? '在住时段' : '预约时段' }}</span>
            <b class="num">{{ current.rangeText }}</b></div>
          <div v-if="current.overdue" class="sub" style="color: #D96C61">该订单已超期未离店，请及时办理离店</div>
          <el-button v-if="current.petId" type="primary" plain size="small" style="margin-top: 8px"
                     @click="router.push(`${store.isAdmin ? '/admin' : '/staff'}/pets/${current.petId}`)">查看宠物档案</el-button>
        </template>
        <template v-else>
          <div class="d-row"><span>当前占用</span><b class="sub">无</b></div>
        </template>

        <el-divider>预约记录（{{ current.reservations?.length || 0 }} 条未来预约）</el-divider>
        <div v-if="!current.reservations?.length" class="sub">暂无未来预约</div>
        <div v-for="r in current.reservations" :key="r.orderId" class="res-item">
          <div class="res-line">预约【<span class="num">{{ r.rangeText }}</span>】</div>
          <div class="sub">
            {{ r.petName || '—' }}<template v-if="r.contactName"> · {{ r.contactName }}</template>
            <template v-if="r.contactPhone"> · {{ r.contactPhone }}</template>
          </div>
        </div>

        <el-divider>快捷操作</el-divider>
        <div class="ops">
          <el-button v-if="current.physicalStatus === 'CLEANING'" type="success" size="small"
                     @click="changeStatus('AVAILABLE')">清洁完成，转为空闲</el-button>
          <el-button v-if="current.physicalStatus === 'AVAILABLE' && current.status !== 'OCCUPIED'" type="warning" size="small" plain
                     @click="changeStatus('MAINTENANCE')">标记维修中</el-button>
          <el-button v-if="current.physicalStatus === 'MAINTENANCE'" type="success" size="small"
                     @click="changeStatus('AVAILABLE')">维修完成</el-button>
          <el-button v-if="current.deletable && canAddRoom" type="danger" size="small" plain
                     @click="removeRoom">删除此房间</el-button>
          <div v-if="current.status === 'OCCUPIED'" class="sub">入住中的房间需办理离店后才能变更状态</div>
          <div v-else-if="!current.deletable" class="sub">该房间存在预约或在住订单，不能删除</div>
        </div>
      </template>
    </el-drawer>

    <!-- 添加房间弹窗：选房型 → 选楼层 → 起始房号自动建议可改 → 数量 -->
    <el-dialog v-model="addVisible" title="添加房间（选择楼层与房间号）" width="480px">
      <div class="add-tip" v-if="route.query.typeId">为新房型配置房间：选择楼层后，起始房间号会自动按该层续号建议，可手动修改。</div>
      <el-form label-width="96px">
        <el-form-item label="分区" required>
          <el-radio-group v-model="addForm.species" @change="onSpeciesChange">
            <el-radio-button value="DOG">🐶 犬区</el-radio-button>
            <el-radio-button value="CAT">🐱 猫区</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="房型" required>
          <el-select v-model="addForm.roomTypeId" filterable
                     :placeholder="speciesRoomTypes.length ? '选择要配置的房型' : '该分区暂无房型'"
                     :disabled="!speciesRoomTypes.length" style="width: 100%">
            <el-option v-for="t in speciesRoomTypes" :key="t.id"
                       :label="t.name + (t.status === 0 ? '（已下架）' : '')" :value="t.id" />
          </el-select>
          <span v-if="!speciesRoomTypes.length" class="sub" style="margin-left: 10px">
            请先到「房型管理」新增{{ addForm.species === 'CAT' ? '猫区' : '犬区' }}房型
          </span>
        </el-form-item>
        <el-form-item label="楼层" required>
          <el-select v-model="addForm.floor" style="width: 140px" @change="onFloorChange">
            <el-option v-for="f in 9" :key="f" :label="f + ' 层' + (floors.some(x => x.floor === f) ? `（现有 ${floors.find(x => x.floor === f)?.rooms.length || 0} 间）` : '（新楼层）')" :value="f" />
          </el-select>
        </el-form-item>
        <el-form-item label="起始房间号" required>
          <el-input v-model="addForm.startNo" style="width: 180px" placeholder="如 121 / 401" />
          <span class="sub" style="margin-left: 10px">房号将自动递增（121、122…）</span>
        </el-form-item>
        <el-form-item label="创建数量">
          <el-input-number v-model="addForm.count" :min="1" :max="50" />
          <span class="sub" style="margin-left: 10px">已占用的房号会自动跳过</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addVisible = false">取消</el-button>
        <el-button type="primary" :loading="adding" @click="confirmAdd">创建房间</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.stat-row { display: flex; gap: 14px; align-items: stretch; flex-wrap: wrap; }
.stat { background: #fff; border-radius: 14px; padding: 14px 22px; display: flex; flex-direction: column;
  align-items: center; box-shadow: 0 1px 2px rgba(48,54,51,.04); }
.stat b { font-size: 22px; font-weight: 900; }
.stat b.occupied { color: var(--pc-primary-deep); }
.stat b.ok { color: #4FA482; }
.stat b.clean { color: var(--pc-ink-2); }
.stat span { font-size: 11px; color: var(--pc-ink-2); }
.legend { margin-left: auto; display: flex; align-items: center; gap: 12px; font-size: 12px; color: var(--pc-ink-2); }
.legend .dot { display: inline-block; width: 10px; height: 10px; border-radius: 3px; margin-right: 4px; }
.no-room-tip { background: #FDF6EC; border-radius: 12px; padding: 10px 16px; font-size: 13px;
  color: #A97B46; margin-top: 14px; }
.floor-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px; gap: 12px; }
.floor-stat { font-size: 12px; }
.floor-head h4 { flex-shrink: 0; }
.floor-head .el-button { margin-left: auto; }
.grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(120px, 1fr)); gap: 12px; }
.room-cell { border-radius: 12px; padding: 12px; cursor: pointer; border: 1.5px solid transparent;
  display: flex; flex-direction: column; gap: 3px; transition: all .15s; }
.room-cell:hover { transform: translateY(-2px); }
/* 批量删除模式：可选的房间加虚线框与选中态，不可删的置灰 */
.room-cell.picking { cursor: crosshair; }
.room-cell.picking:hover { transform: none; }
.room-cell.picked { position: relative; outline: 3px solid var(--pc-primary); outline-offset: 1px; }
.room-cell.picked::after { content: '✓'; position: absolute; top: 2px; right: 6px; font-weight: 900; color: var(--pc-primary-deep); }
.room-cell.dimmed { opacity: .4; cursor: not-allowed; }
.batch-count { font-size: 13px; color: var(--pc-ink-2); align-self: center; }
.room-sub { font-size: 10.5px; opacity: .75; }
.res-item { padding: 7px 0; border-bottom: 1px dashed var(--pc-line); }
.res-item:last-child { border-bottom: none; }
.res-line { font-size: 13px; font-weight: 600; }
.room-cell b { font-size: 16px; }
.room-type { font-size: 11px; opacity: .85; }
.room-status { font-size: 12px; font-weight: 700; }
.st-available { background: #EAF2ED; } .st-available .room-status { color: #3FA482; }
.st-reserved { background: #FBEEDD; } .st-reserved .room-status { color: #C97B2C; }
.st-occupied { background: #6FA58B; } .st-occupied b, .st-occupied .room-status { color: #fff; }
.st-occupied .room-type { color: rgba(255,255,255,.85); }
.st-cleaning { background: #EFEEE9; } .st-cleaning .room-status { color: var(--pc-ink-2); }
.st-maintenance { background: var(--pc-danger-soft); } .st-maintenance .room-status { color: var(--pc-danger); }
.d-row { display: flex; justify-content: space-between; font-size: 13px; margin: 10px 0; align-items: center; }
.d-row span { color: var(--pc-ink-2); }
.add-tip { background: var(--pc-primary-soft); border-radius: 10px; padding: 10px 14px;
  font-size: 12.5px; color: #3E6B54; margin-bottom: 14px; line-height: 1.7; }
</style>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../../api/http'
import { ORDER_STATUS } from '../../api'

interface OrderInfo {
  id: number; orderNo: string; startDate: string; endDate: string; days: number
  totalAmount: number; status: string; contactName?: string; contactPhone?: string
  roomId?: number | null; roomTypeId: number
}
interface Candidate {
  id: number; roomNo: string; floor: number; status: string; physicalStatus: string
  currentOrderId?: number | null; currentPetName?: string | null; currentRangeText?: string | null
  overdue?: boolean
  reservations: { orderId: number; petName?: string; contactName?: string; rangeText: string; status: string }[]
  conflicts: { orderId: number; petName?: string; rangeText: string }[]
  assignable: boolean; needConfirm: boolean; blockReason?: string | null
}

const status = ref('ALL')
const list = ref<OrderInfo[]>([])
const loading = ref(false)

const ROOM_STATUS_TEXT: Record<string, string> = {
  AVAILABLE: '空闲', RESERVED: '已预约', OCCUPIED: '入住中', CLEANING: '清洁中', MAINTENANCE: '维修中',
}
const ROOM_STATUS_TYPE: Record<string, string> = {
  AVAILABLE: 'success', RESERVED: 'warning', OCCUPIED: 'primary', CLEANING: 'info', MAINTENANCE: 'danger',
}

async function load() {
  loading.value = true
  try {
    const data = await http.get<never, { records: OrderInfo[] }>('/orders', { params: { page: 1, size: 50, status: status.value } })
    list.value = data.records
  } finally { loading.value = false }
}

// ---------- 手动分配房间 ----------
const assignVisible = ref(false)
const assignLoading = ref(false)
const assigning = ref(false)
const assignOrder = ref<OrderInfo | null>(null)
const candidates = ref<any>(null)
const pickedRoomId = ref<number | null>(null)

const pickedRoom = computed<Candidate | null>(() =>
  (candidates.value?.rooms || []).find((r: Candidate) => r.id === pickedRoomId.value) || null)
const assignableRooms = computed(() => (candidates.value?.rooms || []).filter((r: Candidate) => r.assignable).length)

async function openAssign(row: OrderInfo) {
  assignOrder.value = row
  pickedRoomId.value = null
  candidates.value = null
  assignVisible.value = true
  assignLoading.value = true
  try {
    candidates.value = await http.get<never, any>(`/orders/${row.id}/room-candidates`)
    if (row.roomId && (candidates.value.rooms || []).some((r: Candidate) => r.id === row.roomId)) {
      pickedRoomId.value = row.roomId
    }
  } catch { assignVisible.value = false } finally { assignLoading.value = false }
}

async function submitAssign(force = false) {
  const order = assignOrder.value
  const room = pickedRoom.value
  if (!order || !room) return ElMessage.warning('请选择要分配的房间')
  // 软冲突：未来预约时段重叠 / 房间清洁中 → 确认后带 force 重试
  if (!force && room.needConfirm) {
    const c = room.conflicts?.[0]
    const msg = c
      ? `该房间已有未来预约【${c.rangeText}】${c.petName ? '（' + c.petName + '）' : ''}，确认继续分配吗？`
      : `该房间当前${ROOM_STATUS_TEXT[room.physicalStatus] || ''}，确认继续分配吗？`
    try {
      await ElMessageBox.confirm(msg, '房间冲突提示', { type: 'warning', confirmButtonText: '确认分配', cancelButtonText: '重新选择' })
    } catch { return }
    return submitAssign(true)
  }
  assigning.value = true
  try {
    await http.post(`/orders/${order.id}/assign-room`, { roomId: room.id, force })
    ElMessage.success(`已为订单 ${order.orderNo} 分配 ${room.roomNo} 房`)
    assignVisible.value = false
    load()
  } catch (e: any) {
    const msg = e?.message || ''
    // 后端复核仍有软冲突（并发/数据变化）：再确认一次后强分配（员工会被后端直接拒绝）
    if (!force && /已有预约|清洁中/.test(msg)) {
      try {
        await ElMessageBox.confirm(`${msg}，确认继续分配吗？`, '房间冲突提示',
          { type: 'warning', confirmButtonText: '确认分配', cancelButtonText: '重新选择' })
      } catch { return }
      return submitAssign(true)
    }
  } finally { assigning.value = false }
}

async function refund(row: OrderInfo, approve: boolean) {
  if (approve) {
    await ElMessageBox.confirm(`同意订单 ${row.orderNo} 的退款申请？`, '退款审批', { type: 'warning' })
    await http.post(`/orders/${row.id}/refund-approve`, { approve: true })
  } else {
    await http.post(`/orders/${row.id}/refund-approve`, { approve: false })
  }
  ElMessage.success(approve ? '已退款' : '已驳回')
  load()
}

onMounted(load)
</script>

<template>
  <div class="pc-card">
    <div class="head">
      <span class="sub">订单状态由本页与管理流程统一裁决；确认订单需手动选择房间完成分配</span>
      <el-select v-model="status" style="width: 150px" @change="load">
        <el-option label="全部" value="ALL" />
        <el-option v-for="(v, k) in ORDER_STATUS" :key="k" :label="v.text" :value="k" />
      </el-select>
    </div>

    <el-table :data="list" stripe style="margin-top: 12px" v-loading="loading">
      <el-table-column prop="orderNo" label="订单号" width="180" />
      <el-table-column label="寄养日期" width="200">
        <template #default="{ row }" class="num">{{ row.startDate }} ~ {{ row.endDate }}（{{ row.days }}晚）</template>
      </el-table-column>
      <el-table-column prop="contactName" label="联系人" width="90" />
      <el-table-column prop="contactPhone" label="电话" width="120" />
      <el-table-column label="金额" width="90">
        <template #default="{ row }"><span class="price num">￥{{ row.totalAmount }}</span></template>
      </el-table-column>
      <el-table-column label="房间" width="90">
        <template #default="{ row }">
          <el-tag v-if="row.roomId" type="success" effect="light" size="small">已分配</el-tag>
          <el-tag v-else type="warning" effect="light" size="small">未分配</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="110">
        <template #default="{ row }">
          <el-tag :type="(ORDER_STATUS[row.status]?.type as any)" effect="light">{{ ORDER_STATUS[row.status]?.text }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" min-width="230" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 'PAID'" type="primary" size="small" @click="openAssign(row)">确认并分配房间</el-button>
          <el-button v-else-if="['CONFIRMED', 'CHECKED_IN', 'BOARDING'].includes(row.status)" size="small" plain
                     @click="openAssign(row)">{{ row.roomId ? '改分配房间' : '分配房间' }}</el-button>
          <template v-if="row.status === 'REFUNDING'">
            <el-button type="success" size="small" @click="refund(row, true)">同意退款</el-button>
            <el-button type="danger" plain size="small" @click="refund(row, false)">驳回</el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>
  </div>

  <!-- 手动分配房间弹窗（员工：仅可操作权限范围内的房间；管理员：可查看并分配全部房间） -->
  <el-dialog v-model="assignVisible" :title="`分配房间 · ${assignOrder?.orderNo || ''}`" width="860px">
    <div v-if="candidates" class="assign-head">
      <div class="ah-row">
        <span>毛孩子</span><b>{{ candidates.petName || '—' }}</b>
        <span class="sep">|</span><span>入住人</span><b>{{ candidates.contactName || '—' }}</b>
        <span class="sep">|</span><span>房型</span><b>{{ candidates.roomTypeName }}</b>
      </div>
      <div class="ah-row">
        <span>寄养时段</span><b class="num">{{ candidates.roomRangeText }}</b>
        <span class="sep">|</span><span>可分配房间</span><b class="num">{{ assignableRooms }}</b>
        <el-tag size="small" effect="plain" style="margin-left: 8px">
          {{ candidates.role === 'ADMIN' ? '管理员：可分配全部房间' : '员工：仅可分配空闲且无预约冲突的房间' }}
        </el-tag>
      </div>
    </div>

    <el-table :data="candidates?.rooms || []" height="380" v-loading="assignLoading" style="margin-top: 10px">
      <el-table-column width="48">
        <template #default="{ row }">
          <el-radio v-model="pickedRoomId" :value="row.id" :disabled="!row.assignable"><span></span></el-radio>
        </template>
      </el-table-column>
      <el-table-column prop="roomNo" label="房号" width="76" />
      <el-table-column label="楼层" width="66">
        <template #default="{ row }">{{ row.floor }} 层</template>
      </el-table-column>
      <el-table-column label="当前状态" width="96">
        <template #default="{ row }">
          <el-tag :type="(ROOM_STATUS_TYPE[row.status] as any)" effect="light" size="small">
            {{ ROOM_STATUS_TEXT[row.status] }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="占用 / 未来预约" min-width="240">
        <template #default="{ row }">
          <div v-if="row.currentRangeText" class="sub">
            <el-tag v-if="row.overdue" type="danger" effect="light" size="small">超期未离店</el-tag>
            {{ row.currentPetName || '在住' }}：<span class="num">{{ row.currentRangeText }}</span>
          </div>
          <div v-for="r in row.reservations" :key="r.orderId" class="sub">
            预约【<span class="num">{{ r.rangeText }}</span>】{{ r.petName || '' }}
          </div>
          <span v-if="!row.currentRangeText && !row.reservations.length" class="sub">—</span>
        </template>
      </el-table-column>
      <el-table-column label="可分配性" width="150">
        <template #default="{ row }">
          <el-tag v-if="row.assignable && !row.needConfirm" type="success" effect="light" size="small">可直接分配</el-tag>
          <el-tag v-else-if="row.assignable" type="warning" effect="light" size="small">需二次确认</el-tag>
          <el-tag v-else type="info" effect="light" size="small">{{ row.blockReason }}</el-tag>
        </template>
      </el-table-column>
    </el-table>

    <template #footer>
      <el-button @click="assignVisible = false">取消</el-button>
      <el-button type="primary" :loading="assigning" :disabled="!pickedRoomId" @click="submitAssign()">
        确认分配{{ pickedRoom?.needConfirm ? '（需二次确认）' : '' }}
      </el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.head { display: flex; justify-content: space-between; align-items: center; }
.assign-head { display: flex; flex-direction: column; gap: 6px; background: #F7F5F0; border-radius: 12px; padding: 10px 14px; }
.ah-row { display: flex; align-items: center; gap: 6px; font-size: 13px; color: var(--pc-ink-2); }
.ah-row b { color: var(--pc-ink); }
.sep { color: #D8CFC6; margin: 0 6px; }
</style>

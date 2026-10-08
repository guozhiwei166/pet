<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../../api/http'
import api from '../../api'

interface RoomType {
  id?: number; name: string; species?: string; price: number; area?: number; maxPets?: number
  facilities?: string; description?: string; photo?: string; status?: number; sort?: number
}

const router = useRouter()
const list = ref<RoomType[]>([])
const roomCountMap = ref<Record<string, number>>({}) // 房型名 → 房间数
const visible = ref(false)
const form = ref<RoomType>({} as RoomType)
const photoUrl = ref('')
const uploading = ref(false)

const roomCount = (rt: RoomType) => roomCountMap.value[rt.name] || 0

async function load() {
  list.value = await http.get<never, RoomType[]>('/room-types', { params: { all: true } })
  // 房间地图数据统计各房型的现有房间数
  try {
    const map: any[] = await api.roomMap()
    const m: Record<string, number> = {}
    for (const f of map) for (const r of f.rooms || []) {
      m[r.roomTypeName] = (m[r.roomTypeName] || 0) + 1
    }
    roomCountMap.value = m
  } catch { /* 忽略 */ }
}

/** 跳转房间地图为该房型配置房间（选楼层与房间号） */
function goConfigRoom(rt: RoomType) {
  router.push(`/admin/room-map?typeId=${rt.id}`)
}

function openAdd() {
  form.value = { name: '', species: 'DOG', price: undefined as unknown as number, area: 8, maxPets: 1, facilities: '', description: '', photo: '', status: 1, sort: 0 }
  photoUrl.value = ''
  visible.value = true
}
function openEdit(row: RoomType) {
  form.value = { ...row }
  photoUrl.value = row.photo || ''
  visible.value = true
}

async function uploadPhoto(opt: { file: Blob }) {
  uploading.value = true
  try {
    const fd = new FormData()
    fd.append('file', opt.file)
    fd.append('bizType', 'ROOM_TYPE')
    const data = await api.upload(fd)
    photoUrl.value = data.url
    form.value.photo = data.url
  } catch (e: any) {
    ElMessage.error(e?.message || '图片上传失败')
  } finally {
    uploading.value = false
  }
}

function removePhoto() {
  photoUrl.value = ''
  form.value.photo = ''
}

async function save() {
  if (!form.value.name || !form.value.price) return ElMessage.warning('请填写名称与价格')
  if (!form.value.species) form.value.species = 'DOG'
  const isNew = !form.value.id
  let savedId: number | undefined
  if (isNew) {
    // 创建后拿返回的 id（接口当前返回空 body，用重新拉列表定位）
    await http.post('/room-types', form.value)
    await load()
    savedId = list.value.find(x => x.name === form.value.name)?.id
  } else {
    await http.put(`/room-types/${form.value.id}`, form.value)
    savedId = form.value.id
  }
  ElMessage.success(isNew ? '房型已创建' : '保存成功')
  visible.value = false
  await load()
  // 新增房型引导配置房间：跳转房间地图选择楼层与房间号创建
  if (isNew) {
    try {
      await ElMessageBox.confirm(
        `房型「${form.value.name}」已创建，当前还没有房间。\n是否现在去房间地图选择楼层与房间号，为该房型创建房间？`,
        '配置房间', { confirmButtonText: '去配置房间', cancelButtonText: '稍后再说', type: 'info' })
      router.push(`/admin/room-map?typeId=${savedId || ''}`)
    } catch { /* 用户选择稍后 */ }
  }
}

async function toggle(row: RoomType) {
  await http.put(`/room-types/${row.id}`, { ...row, status: row.status === 1 ? 0 : 1 })
  ElMessage.success(row.status === 1 ? '已下架' : '已上架')
  load()
}

async function del(row: RoomType) {
  await ElMessageBox.confirm(`确定删除「${row.name}」？`, '删除确认', { type: 'warning' })
  await http.delete(`/room-types/${row.id}`)
  ElMessage.success('已删除')
  load()
}

onMounted(load)
</script>

<template>
  <div class="pc-card">
    <div class="page-head">
      <div>
        <h2 class="page-title">房型管理</h2>
        <p class="page-desc">房型价格参与预订金额计算；新增房型后可去房间地图选择楼层与房间号创建房间（没有房间的房型，寄养人无法预订）</p>
      </div>
      <el-button type="primary" icon="Plus" @click="openAdd">新增房型</el-button>
    </div>

    <el-table :data="list" stripe>
      <el-table-column label="图片" width="90">
        <template #default="{ row }">
          <el-image v-if="row.photo" :src="row.photo" fit="cover" style="width:52px;height:40px;border-radius:6px" preview-teleported :preview-src-list="[row.photo]" />
          <span v-else class="sub">—</span>
        </template>
      </el-table-column>
      <el-table-column prop="name" label="房型" min-width="120" />
      <el-table-column label="分区" width="90">
        <template #default="{ row }">
          <el-tag :type="row.species === 'CAT' ? 'warning' : 'primary'" effect="light" size="small">
            {{ row.species === 'CAT' ? '🐱 猫区' : '🐶 犬区' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="price" label="价格(元/天)" width="110">
        <template #default="{ row }"><span class="price num">￥{{ row.price }}</span></template>
      </el-table-column>
      <el-table-column prop="area" label="面积(㎡)" width="90" />
      <el-table-column prop="maxPets" label="可容宠物" width="90" />
      <el-table-column label="房间数" width="90">
        <template #default="{ row }">
          <el-tag v-if="roomCount(row) === 0" type="danger" effect="light" size="small">0 间</el-tag>
          <span v-else class="num" style="color: var(--pc-primary-deep); font-weight: 700">{{ roomCount(row) }} 间</span>
        </template>
      </el-table-column>
      <el-table-column prop="facilities" label="设施" min-width="200" show-overflow-tooltip />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="light">{{ row.status === 1 ? '上架' : '下架' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <el-button text type="primary" size="small" @click="goConfigRoom(row)">
            {{ roomCount(row) === 0 ? '⚠ 配置房间' : '配置房间' }}
          </el-button>
          <el-button text type="primary" size="small" @click="openEdit(row)">编辑</el-button>
          <el-button text size="small" @click="toggle(row)">{{ row.status === 1 ? '下架' : '上架' }}</el-button>
          <el-button text type="danger" size="small" @click="del(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>

  <el-dialog v-model="visible" :title="form.id ? '编辑房型' : '新增房型'" width="500px">
    <el-form label-width="100px">
      <el-form-item label="房型图片">
        <el-upload :show-file-list="false" accept="image/*" :http-request="uploadPhoto" :disabled="uploading">
          <div class="photo-ring">
            <img v-if="photoUrl" :src="photoUrl" alt="" />
            <span v-else class="photo-ph">+ 上传图片</span>
          </div>
        </el-upload>
        <el-button v-if="photoUrl" text type="danger" size="small" style="margin-left:10px" @click="removePhoto">移除</el-button>
      </el-form-item>
      <el-form-item label="房型名称" required>
        <el-input v-model="form.name" placeholder="如：豪华房" />
      </el-form-item>
      <el-form-item label="分区" required>
        <el-radio-group v-model="form.species">
          <el-radio-button value="DOG">🐶 犬区</el-radio-button>
          <el-radio-button value="CAT">🐱 猫区</el-radio-button>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="价格(元/天)" required><el-input-number v-model="form.price" :min="0" :precision="0" /></el-form-item>
      <el-form-item label="面积(㎡)"><el-input-number v-model="form.area" :min="1" :precision="1" /></el-form-item>
      <el-form-item label="可容宠物"><el-input-number v-model="form.maxPets" :min="1" :max="5" /></el-form-item>
      <el-form-item label="设施"><el-input v-model="form.facilities" placeholder="逗号分隔，如：独立空间,空调" /></el-form-item>
      <el-form-item label="介绍"><el-input v-model="form.description" type="textarea" :rows="2" /></el-form-item>
      <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="save">保存</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.photo-ring {
  width: 120px; height: 80px; border-radius: 10px; border: 2px dashed var(--pc-primary);
  display: flex; align-items: center; justify-content: center; overflow: hidden; cursor: pointer;
  background: var(--pc-primary-soft);
}
.photo-ring img { width: 100%; height: 100%; object-fit: cover; }
.photo-ph { font-size: 13px; color: var(--pc-primary-deep); font-weight: 600; }
</style>

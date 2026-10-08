<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'

const rooms = ref([])
const visible = ref(false)
const form = ref({})

const speciesOptions = [
  { value: 'DOG', label: '🐶 狗狗' },
  { value: 'CAT', label: '🐱 猫咪' },
  { value: 'ALL', label: '🐱🐶 猫犬不限' },
]

async function load() { rooms.value = await api.myRooms() }

function openAdd() {
  form.value = { name: '', species: 'DOG', maxWeight: 30, totalCount: 1, pricePerDay: null,
    needSterilized: 0, allowAggressive: 0, tags: '', description: '' }
  visible.value = true
}
function openEdit(row) { form.value = { ...row, tags: row.tags || '' }; visible.value = true }

async function save() {
  if (!form.value.name || !form.value.pricePerDay) return ElMessage.warning('请填写房型名称与价格')
  if (form.value.id) await api.updateRoom(form.value)
  else await api.saveRoom(form.value)
  ElMessage.success('保存成功')
  visible.value = false
  load()
}

async function del(row) {
  await ElMessageBox.confirm(`确定删除「${row.name}」吗？`, '删除房型', { type: 'warning' })
  await api.delRoom(row.id)
  ElMessage.success('已删除')
  load()
}

onMounted(load)
</script>

<template>
  <div class="page-card">
    <div class="head">
      <span class="head-tip">💡 房型的体重上限、绝育要求会参与「智能排笼」匹配，填写越准，匹配越合适。</span>
      <el-button type="primary" @click="openAdd">+ 新增房型</el-button>
    </div>

    <div class="room-grid">
      <div v-for="r in rooms" :key="r.id" class="room-item">
        <div class="room-top">
          <b>{{ r.name }}</b>
          <span class="price">￥{{ r.pricePerDay }}/天</span>
        </div>
        <div class="room-tags">
          <el-tag size="small" effect="plain">{{ { CAT: '猫咪', DOG: '狗狗', ALL: '猫犬不限' }[r.species] }}</el-tag>
          <el-tag size="small" effect="plain" type="info">≤{{ r.maxWeight }}kg</el-tag>
          <el-tag size="small" effect="plain" type="warning" v-if="r.needSterilized">需绝育</el-tag>
          <el-tag size="small" effect="plain" type="success" v-if="r.allowAggressive">可接收特殊性格</el-tag>
        </div>
        <div class="sub">{{ r.description }}</div>
        <div class="room-bottom">
          <span class="sub">库存 {{ r.totalCount }} 间</span>
          <div>
            <el-button text type="primary" size="small" @click="openEdit(r)">编辑</el-button>
            <el-button text type="danger" size="small" @click="del(r)">删除</el-button>
          </div>
        </div>
      </div>
    </div>
    <el-empty v-if="!rooms.length" description="还没有房型，先新增一个吧" />
  </div>

  <el-dialog v-model="visible" :title="form.id ? '编辑房型' : '新增房型'" width="520px">
    <el-form label-width="100px">
      <el-form-item label="房型名称" required>
        <el-input v-model="form.name" placeholder="如：豪华犬房" />
      </el-form-item>
      <el-form-item label="接收物种">
        <el-radio-group v-model="form.species">
          <el-radio-button v-for="s in speciesOptions" :key="s.value" :value="s.value">{{ s.label }}</el-radio-button>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="体重上限">
        <el-input-number v-model="form.maxWeight" :min="1" :max="100" :precision="1" />
        <span class="sub" style="margin-left: 8px">kg，参与智能排笼</span>
      </el-form-item>
      <el-form-item label="笼位数量">
        <el-input-number v-model="form.totalCount" :min="1" :max="99" />
      </el-form-item>
      <el-form-item label="每日价格" required>
        <el-input-number v-model="form.pricePerDay" :min="0" :precision="0" />
        <span class="sub" style="margin-left: 8px">元/天</span>
      </el-form-item>
      <el-form-item label="要求已绝育">
        <el-switch v-model="form.needSterilized" :active-value="1" :inactive-value="0" />
      </el-form-item>
      <el-form-item label="接收特殊性格">
        <el-switch v-model="form.allowAggressive" :active-value="1" :inactive-value="0" />
        <span class="sub" style="margin-left: 8px">开启后可接收有攻击倾向的宠物</span>
      </el-form-item>
      <el-form-item label="特色标签">
        <el-input v-model="form.tags" placeholder="逗号分隔，如：独立空调,每日视频" />
      </el-form-item>
      <el-form-item label="房型描述">
        <el-input v-model="form.description" type="textarea" :rows="2" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="save">保存</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.head { display: flex; justify-content: space-between; align-items: center; }
.head-tip { font-size: 13px; color: #9B938D; }
.room-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 14px; margin-top: 16px; }
.room-item { border: 1px solid #F5EFE8; border-radius: 12px; padding: 16px; display: flex; flex-direction: column; gap: 8px; }
.room-top { display: flex; justify-content: space-between; align-items: center; }
.price { color: var(--ps-primary); font-weight: 700; }
.room-tags { display: flex; gap: 6px; flex-wrap: wrap; }
.room-bottom { display: flex; justify-content: space-between; align-items: center; margin-top: auto; }
.sub { font-size: 12px; color: #9B938D; }
</style>

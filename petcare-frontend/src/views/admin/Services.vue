<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../../api/http'

interface ServiceItem {
  id?: number; name: string; price?: number; unit?: string
  description?: string; status?: number; sort?: number
}

const list = ref<ServiceItem[]>([])
const visible = ref(false)
const form = ref<ServiceItem>({} as ServiceItem)

async function load() {
  list.value = await http.get<never, ServiceItem[]>('/services', { params: { all: true } })
}

function openAdd() {
  form.value = { name: '', price: undefined as unknown as number, unit: '次', description: '', status: 1, sort: 0 }
  visible.value = true
}
function openEdit(row: ServiceItem) { form.value = { ...row }; visible.value = true }

async function save() {
  if (!form.value.name || !form.value.price) return ElMessage.warning('请填写名称与价格')
  if (form.value.id) await http.put(`/services/${form.value.id}`, form.value)
  else await http.post('/services', form.value)
  ElMessage.success('保存成功')
  visible.value = false
  load()
}

async function del(row: ServiceItem) {
  await ElMessageBox.confirm(`确定删除「${row.name}」？`, '删除确认', { type: 'warning' })
  await http.delete(`/services/${row.id}`)
  ElMessage.success('已删除')
  load()
}

onMounted(load)
</script>

<template>
  <div class="pc-card">
    <div class="page-head">
      <div>
        <h2 class="page-title">增值服务管理</h2>
        <p class="page-desc">主人在预订时可勾选增值服务，金额实时计入订单</p>
      </div>
      <el-button type="primary" icon="Plus" @click="openAdd">新增服务</el-button>
    </div>

    <el-table :data="list" stripe>
      <el-table-column prop="name" label="服务名称" min-width="120" />
      <el-table-column label="价格" width="130">
        <template #default="{ row }"><span class="price num">￥{{ row.price }}</span> / {{ row.unit }}</template>
      </el-table-column>
      <el-table-column prop="description" label="说明" min-width="220" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="light">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button text type="primary" size="small" @click="openEdit(row)">编辑</el-button>
          <el-button text type="danger" size="small" @click="del(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>

  <el-dialog v-model="visible" :title="form.id ? '编辑服务' : '新增服务'" width="460px">
    <el-form label-width="90px">
      <el-form-item label="服务名称" required><el-input v-model="form.name" /></el-form-item>
      <el-form-item label="价格" required>
        <el-input-number v-model="form.price" :min="0" :precision="0" />
        <el-input v-model="form.unit" style="width: 90px; margin-left: 10px" placeholder="单位" />
      </el-form-item>
      <el-form-item label="说明"><el-input v-model="form.description" type="textarea" :rows="2" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="save">保存</el-button>
    </template>
  </el-dialog>
</template>

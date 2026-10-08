<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'

const tabs = [
  { label: '全部', value: 'ALL' },
  { label: '待审核', value: 'PENDING' },
  { label: '已通过', value: 'APPROVED' },
  { label: '已驳回', value: 'REJECTED' },
]
const active = ref('ALL')
const list = ref([])
const total = ref(0)
const page = ref(1)

const AUDIT = { PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger' }

async function load() {
  const data = await api.adminMerchants({ page: page.value, size: 10, status: active.value })
  list.value = data.records
  total.value = data.total
}
function changeTab() { page.value = 1; load() }

async function audit(row, pass) {
  try {
    if (pass) {
      await ElMessageBox.confirm(`通过「${row.shopName}」的入驻申请？通过后即可发布房型接单。`, '审核通过', { type: 'success' })
      await api.auditMerchant(row.id, { pass: true })
    } else {
      const { value } = await ElMessageBox.prompt('请输入驳回原因（会通知商家）', '驳回申请', { inputPlaceholder: '如：资质材料不完整' })
      await api.auditMerchant(row.id, { pass: false, reason: value })
    }
    ElMessage.success('已处理')
    load()
  } catch (e) { /* 用户取消 */ }
}

onMounted(load)
</script>

<template>
  <div class="page-card">
    <el-radio-group v-model="active" @change="changeTab">
      <el-radio-button v-for="t in tabs" :key="t.value" :value="t.value">{{ t.label }}</el-radio-button>
    </el-radio-group>

    <el-table :data="list" stripe style="margin-top: 14px">
      <el-table-column prop="id" label="ID" width="56" />
      <el-table-column prop="shopName" label="店铺名称" min-width="160" />
      <el-table-column prop="phone" label="电话" width="125" />
      <el-table-column prop="address" label="地址" min-width="180" show-overflow-tooltip />
      <el-table-column label="物种" width="100">
        <template #default="{ row }">
          {{ row.serviceSpecies?.split(',').map(s => ({ CAT: '猫', DOG: '狗' }[s])).join('/') }}
        </template>
      </el-table-column>
      <el-table-column prop="license" label="资质" min-width="150" show-overflow-tooltip />
      <el-table-column prop="rating" label="评分" width="70" />
      <el-table-column label="审核状态" width="100">
        <template #default="{ row }">
          <el-tag :type="AUDIT[row.auditStatus]" effect="light">
            {{ { PENDING: '待审核', APPROVED: '已通过', REJECTED: '已驳回' }[row.auditStatus] }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="170" fixed="right">
        <template #default="{ row }">
          <template v-if="row.auditStatus === 'PENDING'">
            <el-button type="primary" size="small" @click="audit(row, true)">通过</el-button>
            <el-button type="danger" plain size="small" @click="audit(row, false)">驳回</el-button>
          </template>
          <span v-else class="sub">{{ row.auditReason || '—' }}</span>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination background layout="prev, pager, next, total" :total="total"
                   :page-size="10" v-model:current-page="page" @current-change="load" />
  </div>
</template>

<style scoped>
.sub { font-size: 12px; color: #9B938D; }
</style>

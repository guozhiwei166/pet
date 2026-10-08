<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../../api'

const date = ref(new Date().toLocaleDateString('sv-SE'))
const list = ref<any[]>([])
const total = ref(0)

const STATUS: Record<string, { text: string; type: string }> = {
  PENDING: { text: '待确认', type: 'warning' },
  CONFIRMED: { text: '已确认', type: 'primary' },
  DONE: { text: '已完成', type: 'success' },
  CANCELLED: { text: '已取消', type: 'info' },
}

async function load() {
  list.value = (await api.allServiceBookings(date.value)) || []
  total.value = list.value.filter(b => b.status !== 'CANCELLED').length
}
onMounted(load)

async function act(b: any, action: string) {
  await api.transitionBooking(b.id, action)
  ElMessage.success(action === 'CONFIRM' ? '已确认预约' : '已完成服务')
  load()
}
</script>

<template>
  <div>
    <div class="page-head">
      <div>
        <h2 class="page-title">服务预约处理</h2>
        <p class="page-desc">洗澡 / 美容 / 遛宠等门店预约的确认与完成</p>
      </div>
      <el-date-picker v-model="date" type="date" value-format="YYYY-MM-DD" style="width: 150px" @change="load" />
    </div>

    <div class="pc-card">
      <div style="margin-bottom: 12px" class="sub">当日共 <b style="color: var(--pc-primary-deep)">{{ total }}</b> 笔预约</div>
      <el-table :data="list" stripe>
        <el-table-column label="时间" width="90">
          <template #default="{ row }"><b class="num">{{ row.bookTime }}</b></template>
        </el-table-column>
        <el-table-column prop="serviceName" label="服务" width="110" />
        <el-table-column prop="petId" label="宠物" width="90">
          <template #default="{ row }">宠物#{{ row.petId }}</template>
        </el-table-column>
        <el-table-column label="价格" width="90">
          <template #default="{ row }">￥{{ row.price }}</template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="(STATUS[row.status]?.type as any)" effect="light">{{ STATUS[row.status]?.text }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140">
          <template #default="{ row }">
            <el-button v-if="row.status === 'PENDING'" type="primary" size="small" @click="act(row, 'CONFIRM')">确认</el-button>
            <el-button v-if="row.status === 'CONFIRMED'" type="success" size="small" @click="act(row, 'DONE')">完成</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!list.length" description="当天没有服务预约" />
    </div>
  </div>
</template>

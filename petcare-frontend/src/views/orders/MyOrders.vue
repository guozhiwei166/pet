<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api, { ORDER_STATUS, type OrderPage } from '../../api'

const router = useRouter()
const tabs = ['ALL', 'PENDING_PAYMENT', 'CONFIRMED', 'BOARDING', 'COMPLETED', 'CANCELLED']
const tabLabels: Record<string, string> = {
  ALL: '全部', PENDING_PAYMENT: '待支付', CONFIRMED: '已预约', BOARDING: '寄养中', COMPLETED: '已完成', CANCELLED: '已取消',
}
const active = ref('ALL')
const currentPage = ref(1)
const page = ref<OrderPage | null>(null)
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    page.value = await api.orders(active.value, currentPage.value)
  } finally { loading.value = false }
}

function changeTab() { currentPage.value = 1; load() }

onMounted(load)
</script>

<template>
  <div>
    <div class="page-head">
      <div>
        <h2 class="page-title">我的订单</h2>
        <p class="page-desc">订单状态由门店与管理端统一维护</p>
      </div>
    </div>
    <div class="pc-card">
      <el-tabs v-model="active" @tab-change="changeTab">
        <el-tab-pane v-for="(label, key) in tabLabels" :key="key" :label="label" :name="key" />
      </el-tabs>
      <el-table v-loading="loading" :data="page?.records || []" stripe>
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="startDate" label="寄养日期" width="200">
          <template #default="{ row }" class="num">{{ row.startDate }} ~ {{ row.endDate }}（{{ row.days }}晚）</template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="金额" width="100">
          <template #default="{ row }"><span class="price num">￥{{ row.totalAmount }}</span></template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="(ORDER_STATUS[row.status]?.type as any)" effect="light">{{ ORDER_STATUS[row.status]?.text }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="110" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="router.push(`/orders/${row.id}`)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-if="page && page.total > 10" background layout="prev, pager, next, total"
                     :total="page.total" :page-size="10" v-model:current-page="currentPage" @current-change="load"
                     style="margin-top: 14px; justify-content: flex-end" />
    </div>
  </div>
</template>

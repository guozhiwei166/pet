<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api, { ORDER_STATUS } from '../api'

const router = useRouter()
const dash = ref(null)

const cards = [
  { key: 'pendingConfirm', label: '待接单', icon: '🔔', bg: '#FFF1E6' },
  { key: 'todayCheckin', label: '今日待入住', icon: '🧳', bg: '#E8F3FF' },
  { key: 'boarding', label: '寄养中', icon: '🐾', bg: '#FFE8DD' },
  { key: 'todayCheckout', label: '待离店', icon: '🏡', bg: '#E6F7EF' },
]

onMounted(async () => {
  dash.value = await api.dashboard()
})
</script>

<template>
  <div v-if="dash">
    <div class="stat-grid">
      <div v-for="c in cards" :key="c.key" class="stat-card cursor" @click="router.push('/orders')">
        <div class="stat-icon" :style="{ background: c.bg }">{{ c.icon }}</div>
        <div>
          <div class="stat-value">{{ dash[c.key] }}</div>
          <div class="stat-label">{{ c.label }}</div>
        </div>
      </div>
    </div>

    <div class="page-card" style="margin-top: 16px">
      <div class="card-head">
        <b>最近订单</b>
        <el-button type="primary" text @click="router.push('/orders')">全部订单 →</el-button>
      </div>
      <el-table :data="dash.recentOrders" stripe>
        <el-table-column prop="orderNo" label="订单号" width="190" />
        <el-table-column prop="petName" label="宠物" width="90" />
        <el-table-column prop="roomName" label="房型" width="130" />
        <el-table-column label="寄养日期" width="200">
          <template #default="{ row }">{{ row.startDate }} ~ {{ row.endDate }}</template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="金额" width="100">
          <template #default="{ row }">￥{{ row.totalAmount }}</template>
        </el-table-column>
        <el-table-column label="状态">
          <template #default="{ row }">
            <el-tag :type="ORDER_STATUS[row.status]?.type" effect="light">{{ ORDER_STATUS[row.status]?.text }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="quick-tip">💡 每日护理打卡后，主人会在 App 内收到寄养日记更新通知——坚持打卡是店铺好评的关键。</div>
  </div>
</template>

<style scoped>
.stat-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 14px; }
.cursor { cursor: pointer; }
.card-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.quick-tip { margin-top: 16px; font-size: 13px; color: #9B938D; }
</style>

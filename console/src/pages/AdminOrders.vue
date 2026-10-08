<script setup>
import { ref, onMounted } from 'vue'
import api, { ORDER_STATUS } from '../api'

const status = ref('ALL')
const list = ref([])
const total = ref(0)
const page = ref(1)

async function load() {
  const data = await api.orders({ page: page.value, size: 10, status: status.value })
  list.value = data.records
  total.value = data.total
}

onMounted(load)
</script>

<template>
  <div class="page-card">
    <div class="filter">
      <span>订单状态：</span>
      <el-select v-model="status" style="width: 180px" @change="page = 1; load()">
        <el-option label="全部" value="ALL" />
        <el-option v-for="(v, k) in ORDER_STATUS" :key="k" :label="v.text" :value="k" />
      </el-select>
    </div>

    <el-table :data="list" stripe style="margin-top: 14px">
      <el-table-column prop="orderNo" label="订单号" width="185" />
      <el-table-column prop="shopName" label="商家" min-width="150" show-overflow-tooltip />
      <el-table-column label="宠物" width="110">
        <template #default="{ row }">{{ row.petName }}（{{ row.petSpecies === 'CAT' ? '猫' : '狗' }}）</template>
      </el-table-column>
      <el-table-column prop="roomName" label="房型" width="120" />
      <el-table-column label="寄养日期" width="200">
        <template #default="{ row }">{{ row.startDate }} ~ {{ row.endDate }}</template>
      </el-table-column>
      <el-table-column prop="totalAmount" label="金额" width="90">
        <template #default="{ row }">￥{{ row.totalAmount }}</template>
      </el-table-column>
      <el-table-column label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="ORDER_STATUS[row.status]?.type" effect="light">{{ ORDER_STATUS[row.status]?.text }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="下单时间" width="170" />
    </el-table>

    <el-pagination background layout="prev, pager, next, total" :total="total"
                   :page-size="10" v-model:current-page="page" @current-change="load" />
  </div>
</template>

<style scoped>
.filter { display: flex; align-items: center; gap: 8px; font-size: 14px; color: #5B5551; }
</style>

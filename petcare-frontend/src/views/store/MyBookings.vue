<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../../api'

const list = ref<any[]>([])

async function load() { list.value = await api.myServiceBookings() }
onMounted(load)

async function cancel(b: any) {
  await ElMessageBox.confirm(`确定取消「${b.serviceName}」预约？`, '取消预约', { type: 'warning' })
  await api.transitionBooking(b.id, 'CANCEL')
  ElMessage.success('已取消')
  load()
}

const STATUS: Record<string, { text: string; type: string }> = {
  PENDING: { text: '待确认', type: 'warning' },
  CONFIRMED: { text: '已确认', type: 'primary' },
  DONE: { text: '已完成', type: 'success' },
  CANCELLED: { text: '已取消', type: 'info' },
}
</script>

<template>
  <div>
    <div class="page-head">
      <div>
        <h2 class="page-title">我的服务预约</h2>
        <p class="page-desc">门店美容、洗澡、遛宠等预约记录</p>
      </div>
      <el-button type="primary" round @click="$router.push('/store')">去门店预约</el-button>
    </div>

    <div class="pc-card">
      <el-table :data="list" stripe>
        <el-table-column prop="petId" label="宠物" width="120">
          <template #default="{ row }">{{ row.petId ? `宠物#${row.petId}` : '—' }}</template>
        </el-table-column>
        <el-table-column prop="serviceName" label="服务" width="120" />
        <el-table-column label="日期" width="140">
          <template #default="{ row }" class="num">{{ row.bookDate }} {{ row.bookTime }}</template>
        </el-table-column>
        <el-table-column label="金额" width="110">
          <template #default="{ row }"><span class="price num">￥{{ row.price }}</span></template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="(STATUS[row.status]?.type as any)" effect="light">{{ STATUS[row.status]?.text }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
        <el-table-column label="操作" width="90">
          <template #default="{ row }">
            <el-button v-if="row.status === 'PENDING'" text type="danger" size="small" @click="cancel(row)">取消</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!list.length" description="还没有预约过门店服务" />
    </div>
  </div>
</template>

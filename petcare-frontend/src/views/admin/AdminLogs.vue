<script setup lang="ts">
import { ref, onMounted } from 'vue'
import http from '../../api/http'

interface LogRow {
  id: number; username?: string; module?: string; action?: string
  detail?: string; ip?: string; createTime?: string
}

const list = ref<LogRow[]>([])
const total = ref(0)
const page = ref(1)

async function load() {
  const data = await http.get<never, { records: LogRow[]; total: number }>('/admin/logs', {
    params: { page: page.value, size: 15 },
  })
  list.value = data.records
  total.value = data.total
}

onMounted(load)
</script>

<template>
  <div class="pc-card">
    <div class="page-head">
      <div>
        <h2 class="page-title">操作日志</h2>
        <p class="page-desc">登录、订单、入住离店、档案修改、异常处理等关键操作全量留痕</p>
      </div>
    </div>
    <el-table :data="list" stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="username" label="操作人" width="130" />
      <el-table-column prop="module" label="模块" width="100" />
      <el-table-column prop="action" label="操作" width="110" />
      <el-table-column prop="detail" label="详情" min-width="240" show-overflow-tooltip />
      <el-table-column prop="ip" label="IP" width="120" />
      <el-table-column prop="createTime" label="时间" width="170" />
    </el-table>
    <el-pagination background layout="prev, pager, next, total" :total="total" :page-size="15"
                   v-model:current-page="page" @current-change="load" style="margin-top: 14px" />
  </div>
</template>

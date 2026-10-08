<script setup>
import { ref, onMounted } from 'vue'
import api from '../api'

const keyword = ref('')
const list = ref([])
const total = ref(0)
const page = ref(1)

async function load() {
  const data = await api.adminUsers({ page: page.value, size: 10, keyword: keyword.value })
  list.value = data.records
  total.value = data.total
}

function search() { page.value = 1; load() }

onMounted(load)
</script>

<template>
  <div class="page-card">
    <div class="search-row">
      <el-input v-model="keyword" placeholder="搜索昵称 / 手机号" clearable style="width: 260px" @keyup.enter="search" />
      <el-button type="primary" @click="search">搜索</el-button>
    </div>

    <el-table :data="list" stripe style="margin-top: 14px">
      <el-table-column prop="id" label="ID" width="64" />
      <el-table-column prop="nickname" label="昵称" min-width="130" />
      <el-table-column prop="phone" label="手机号" width="130" />
      <el-table-column label="角色" width="110">
        <template #default="{ row }">
          <el-tag :type="{ OWNER: 'primary', MERCHANT: 'warning', ADMIN: 'danger' }[row.role]" effect="light">
            {{ { OWNER: '宠物主人', MERCHANT: '商家', ADMIN: '管理员' }[row.role] }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="light">
            {{ row.status === 1 ? '正常' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="注册时间" width="170" />
    </el-table>

    <el-pagination background layout="prev, pager, next, total" :total="total"
                   :page-size="10" v-model:current-page="page" @current-change="load" />
  </div>
</template>

<style scoped>
.search-row { display: flex; gap: 10px; }
</style>

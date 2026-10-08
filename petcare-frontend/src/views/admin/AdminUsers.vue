<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../../api/http'

interface UserRow {
  id: number; username: string; nickname: string; phone?: string
  status: number; roleName: string; createTime?: string
}

const keyword = ref('')
const users = ref<UserRow[]>([])
const employees = ref<any[]>([])

async function load() {
  users.value = await http.get<never, UserRow[]>('/admin/users', { params: { keyword: keyword.value } })
  try { employees.value = await http.get<never, any[]>('/admin/stats/staff') } catch { employees.value = [] }
}

async function resetPwd(u: UserRow) {
  const { value } = await ElMessageBox.prompt(`为「${u.nickname}」设置新密码（6-20位）`, '重置密码', {
    inputPlaceholder: '输入新密码', inputPattern: /^\S{6,20}$/, inputErrorMessage: '密码需为6-20位',
    confirmButtonText: '确认重置', type: 'warning' })
  await http.put(`/admin/users/${u.id}/password`, { password: value })
  ElMessage.success('密码已重置，请通知用户用新密码登录')
}
async function toggle(u: UserRow) {
  await ElMessageBox.confirm(
    u.status === 1 ? `禁用「${u.nickname}」？禁用后无法登录。` : `启用「${u.nickname}」？`,
    '确认操作', { type: 'warning' })
  await http.put(`/admin/users/${u.id}/status`)
  ElMessage.success('已更新')
  load()
}

onMounted(load)
</script>

<template>
  <div>
    <div class="pc-card" style="margin-bottom: 16px">
      <div class="page-head" style="margin-bottom: 10px">
        <h3>用户管理</h3>
        <div style="display:flex; gap:10px">
          <el-input v-model="keyword" placeholder="搜索昵称 / 用户名" style="width: 220px" clearable @keyup.enter="load" />
          <el-button type="primary" @click="load">搜索</el-button>
        </div>
      </div>
      <el-table :data="users" stripe>
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="nickname" label="昵称" width="140" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="roleName" label="角色" width="110">
          <template #default="{ row }">
            <el-tag :type="row.role === 'ROLE_ADMIN' ? 'danger' : row.role === 'ROLE_STAFF' ? 'warning' : 'primary'"
                    effect="light">{{ row.roleName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="light">{{ row.status === 1 ? '正常' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="170" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button v-if="row.role !== 'ROLE_ADMIN'" size="small" round plain
                       :type="row.status === 1 ? 'danger' : 'success'" @click="toggle(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button v-if="row.role !== 'ROLE_ADMIN'" size="small" round plain type="warning"
                       @click="resetPwd(row)">重置密码</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="pc-card">
      <h3 style="margin-bottom: 10px">员工任务完成情况</h3>
      <el-table :data="employees" stripe>
        <el-table-column prop="name" label="员工" width="140" />
        <el-table-column prop="total" label="任务总数" width="110" />
        <el-table-column prop="done" label="已完成" width="110" />
        <el-table-column label="完成率" min-width="240">
          <template #default="{ row }">
            <el-progress :percentage="Number(row.rate)" :stroke-width="14" color="#6FA58B" />
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!employees.length" description="暂无员工任务数据" :image-size="60" />
    </div>
  </div>
</template>

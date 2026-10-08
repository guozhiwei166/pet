<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../../api'

const list = ref<any[]>([])

async function load() { list.value = await api.allStories() }
onMounted(load)

async function toggle(row: any) {
  const toHide = row.status === 1
  await ElMessageBox.confirm(toHide ? `下架「${row.title}」？` : `恢复「${row.title}」？`, '确认操作', { type: 'warning' })
  await api.toggleStory(row.id, toHide ? 0 : 1)
  ElMessage.success('已更新')
  load()
}
</script>

<template>
  <div class="pc-card">
    <div class="page-head">
      <div>
        <h2 class="page-title">故事墙管理</h2>
        <p class="page-desc">审核与下架用户发布的故事</p>
      </div>
    </div>
    <el-table :data="list" stripe>
      <el-table-column prop="title" label="标题" min-width="160" />
      <el-table-column prop="author" label="作者" width="100" />
      <el-table-column prop="content" label="内容" min-width="260" show-overflow-tooltip />
      <el-table-column prop="likes" label="点赞" width="80" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="light">{{ row.status === 1 ? '展示中' : '已下架' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="发布时间" width="170" />
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button text :type="row.status === 1 ? 'danger' : 'success'" size="small" @click="toggle(row)">
            {{ row.status === 1 ? '下架' : '恢复' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../../api/http'

interface Exc { id: number; petName?: string; type: string; level: string; description: string; status: string; createTime: string; handleNote?: string }

const status = ref('ALL')
const list = ref<Exc[]>([])

const LEVEL: Record<string, string> = { MILD: '轻微', NORMAL: '一般', SEVERE: '严重' }
const STATUS_TEXT: Record<string, string> = { PENDING: '待处理', PROCESSING: '处理中', OBSERVING: '观察中', RESOLVED: '已解决', CLOSED: '已关闭' }
const TYPE_TEXT: Record<string, string> = { MENTAL: '精神', APPETITE: '食欲', VOMIT: '呕吐', DIARRHEA: '腹泻', INJURY: '外伤', BEHAVIOR: '行为', OTHER: '其他' }

async function load() {
  list.value = await http.get<never, Exc[]>('/exceptions', { params: { status: status.value } })
}

async function transition(row: Exc, action: string) {
  let note = ''
  if (action !== 'close') {
    const r = await ElMessageBox.prompt(`将事件流转为「${{ process: '处理中', observe: '观察中', resolve: '已解决' }[action]}」，可填写处理说明`, '异常处理', {
      inputPlaceholder: '处理说明（选填）', inputValue: row.handleNote || '',
    })
    note = r.value
  }
  await http.post(`/exceptions/${row.id}/${action}`, { note })
  ElMessage.success('已流转')
  load()
}

onMounted(load)
</script>

<template>
  <div class="pc-card">
    <div class="head">
      <span class="sub">事件流转：待处理 → 处理中 → 观察中 → 已解决 → 关闭</span>
      <el-select v-model="status" style="width: 140px" @change="load">
        <el-option label="全部" value="ALL" />
        <el-option v-for="(t, k) in STATUS_TEXT" :key="k" :label="t" :value="k" />
      </el-select>
    </div>

    <el-table :data="list" stripe style="margin-top: 12px">
      <el-table-column prop="petName" label="宠物" width="90" />
      <el-table-column label="级别" width="80">
        <template #default="{ row }">
          <el-tag :type="row.level === 'SEVERE' ? 'danger' : row.level === 'NORMAL' ? 'warning' : 'info'" effect="light">
            {{ LEVEL[row.level] }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="类型" width="100">
        <template #default="{ row }">
          {{ (TYPE_TEXT as Record<string, string>)[row.type] || row.type }}
        </template>
      </el-table-column>
      <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
      <el-table-column label="状态" width="90">
        <template #default="{ row }"><el-tag effect="light">{{ STATUS_TEXT[row.status] }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="handleNote" label="处理说明" min-width="140" show-overflow-tooltip />
      <el-table-column prop="createTime" label="上报时间" width="170" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 'PENDING'" type="primary" size="small" @click="transition(row, 'process')">接收处理</el-button>
          <el-button v-if="row.status === 'PROCESSING'" type="warning" size="small" plain @click="transition(row, 'observe')">转入观察</el-button>
          <el-button v-if="row.status === 'OBSERVING' || row.status === 'PROCESSING'" type="success" size="small" plain @click="transition(row, 'resolve')">标记解决</el-button>
          <el-button v-if="row.status === 'RESOLVED'" size="small" @click="transition(row, 'close')">关闭事件</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-if="!list.length" description="没有异常事件，一切正常" :image-size="70" />
  </div>
</template>

<style scoped>
.head { display: flex; justify-content: space-between; align-items: center; }
</style>

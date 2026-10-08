<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../api'

const list = ref([])
const visible = ref(false)
const replyId = ref(null)
const replyText = ref('')

async function load() { list.value = await api.myReviews() }

function openReply(row) {
  replyId.value = row.id
  replyText.value = row.reply || ''
  visible.value = true
}

async function doReply() {
  await api.replyReview(replyId.value, replyText.value)
  ElMessage.success('回复成功')
  visible.value = false
  load()
}

onMounted(load)
</script>

<template>
  <div class="page-card">
    <el-table :data="list" stripe>
      <el-table-column prop="nickname" label="用户" width="120" />
      <el-table-column label="评分" width="160">
        <template #default="{ row }"><el-rate :model-value="row.rating" disabled /></template>
      </el-table-column>
      <el-table-column prop="content" label="评价内容" min-width="240" />
      <el-table-column prop="createTime" label="时间" width="170" />
      <el-table-column label="商家回复" min-width="200">
        <template #default="{ row }">
          <span v-if="row.reply">{{ row.reply }}</span>
          <el-button v-else text type="primary" size="small" @click="openReply(row)">回复</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-if="!list.length" description="暂无评价" />
  </div>

  <el-dialog v-model="visible" title="回复评价" width="440px">
    <el-input v-model="replyText" type="textarea" :rows="3" placeholder="感谢语、改进承诺都可以，回复会展示在商家详情页" />
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="doReply">发布回复</el-button>
    </template>
  </el-dialog>
</template>

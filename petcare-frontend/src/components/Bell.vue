<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api, { type NotificationItem } from '../api'
import { useUserStore } from '../stores/user'
import { notificationTarget } from '../utils/notify'

const router = useRouter()
const store = useUserStore()
const list = ref<NotificationItem[]>([])
const unread = ref(0)
const visible = ref(false)

async function load() {
  if (!store.token) return
  try {
    unread.value = await api.unreadCount()
    if (visible.value) list.value = await api.notifications()
  } catch { /* 静默 */ }
}

async function openPanel() {
  visible.value = !visible.value
  if (visible.value) list.value = await api.notifications()
}

async function openItem(n: NotificationItem) {
  if (!n.isRead) await api.readNotification(n.id)
  visible.value = false
  const target = notificationTarget(n, store.isStaff, store.isAdmin)
  if (target) router.push(target)
  else load()
}

async function readAll() {
  await api.readAllNotifications()
  ElMessage.success('全部已读')
  load()
}

function timeText(t: string) { return t?.slice(5, 16) }

let pollTimer: ReturnType<typeof setInterval> | undefined
onMounted(() => {
  load()
  pollTimer = setInterval(load, 20000)
})
onUnmounted(() => clearInterval(pollTimer))
</script>

<template>
  <div class="bell-wrap">
    <el-badge :value="unread > 0 ? unread : ''" class="bell" @click="openPanel">
      <el-icon :size="19"><Bell /></el-icon>
    </el-badge>
    <div v-if="visible" class="panel pc-card-md">
      <div class="panel-head">
        <b>通知</b>
        <el-link type="primary" :underline="false" style="font-size:12px" @click="readAll">全部已读</el-link>
      </div>
      <div v-for="n in list.slice(0, 8)" :key="n.id" class="n-item" :class="{ unread: !n.isRead }" @click="openItem(n)">
        <b>{{ n.title }}</b>
        <div class="sub">{{ n.content }}</div>
        <div class="sub num">{{ timeText(n.createTime) }}</div>
      </div>
      <div v-if="!list.length" class="sub" style="text-align:center; padding: 14px">暂无通知</div>
    </div>
  </div>
</template>

<style scoped>
.bell-wrap { position: relative; }
.bell { cursor: pointer; display: flex; align-items: center; color: var(--pc-ink-2); }
.panel { position: absolute; right: 0; top: 36px; width: 320px; max-height: 420px; overflow: auto; z-index: 50; }
.panel-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px; }
.n-item { padding: 9px 8px; border-radius: 10px; cursor: pointer; border-bottom: 1px solid var(--pc-line); }
.n-item:hover { background: var(--pc-primary-soft); }
.n-item.unread b::before { content: '●'; color: var(--pc-primary); margin-right: 5px; font-size: 10px; }
.n-item .sub { margin: 3px 0 2px; line-height: 1.5; }
</style>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '../api'
import { useUserStore } from '../stores/user'
import { formatTime, isToday } from '../utils/datetime'
import { notificationTarget } from '../utils/notify'

const router = useRouter()
const store = useUserStore()
const list = ref<any[]>([])
const loading = ref(false)

const typeMeta: Record<string, { icon: string; cls: string; label: string }> = {
  DAILY: { icon: 'Document', cls: 'mint', label: '日报' },
  REMINDER: { icon: 'AlarmClock', cls: 'warm', label: '提醒' },
  EXCEPTION: { icon: 'WarningFilled', cls: 'rose', label: '异常' },
  SYSTEM: { icon: 'InfoFilled', cls: 'gray', label: '系统' },
}

const unreadCount = computed(() => list.value.filter(n => !n.isRead).length)
const groups = computed(() => {
  const today: any[] = []
  const earlier: any[] = []
  for (const n of list.value) {
    ;(isToday(n.createTime) ? today : earlier).push(n)
  }
  const out: { label: string; items: any[] }[] = []
  if (today.length) out.push({ label: '今天', items: today })
  if (earlier.length) out.push({ label: '更早', items: earlier })
  return out
})

async function load() {
  loading.value = true
  try {
    list.value = await api.notifications()
  } catch (e: any) {
    ElMessage.error(e?.message || '消息加载失败')
  } finally {
    loading.value = false
  }
}

const fmtTime = (t: string) => formatTime(t)

async function open(n: any) {
  if (!n.isRead) {
    await api.readNotification(n.id)
    n.isRead = 1
  }
  const target = notificationTarget(n, store.isStaff, store.isAdmin)
  if (target) router.push(target)
}

async function readAll() {
  await api.readAllNotifications()
  load()
}
onMounted(load)
</script>

<template>
  <div class="msg-page">
    <div class="msg-head pc-card">
      <div>
        <h2 class="page-title">消息中心</h2>
        <p class="page-desc" v-if="unreadCount">有 {{ unreadCount }} 条未读消息</p>
        <p class="page-desc" v-else>全部已读，没有遗漏</p>
      </div>
      <el-button text type="primary" :disabled="!unreadCount" @click="readAll">全部标为已读</el-button>
    </div>

    <div v-for="g in groups" :key="g.label" class="group">
      <div class="group-label">{{ g.label }}</div>
      <div class="msg-list pc-card">
        <div v-for="n in g.items" :key="n.id" class="n-item" :class="{ unread: !n.isRead }" @click="open(n)">
          <span class="n-icon" :class="typeMeta[n.type]?.cls || 'gray'">
            <el-icon><component :is="typeMeta[n.type]?.icon || 'InfoFilled'" /></el-icon>
          </span>
          <div class="n-main">
            <div class="n-title-row">
              <span class="n-type" :class="typeMeta[n.type]?.cls || 'gray'">{{ typeMeta[n.type]?.label || '系统' }}</span>
              <b>{{ n.title }}</b>
              <span v-if="!n.isRead" class="unread-dot"></span>
            </div>
            <div class="n-content sub">{{ n.content }}</div>
            <div class="n-time sub num">{{ fmtTime(n.createTime) }} <span v-if="n.refId" class="go">› 点击查看</span></div>
          </div>
        </div>
      </div>
    </div>
    <div v-if="!groups.length" class="pc-card msg-empty">
      <el-empty description="暂无消息，去逛逛吧～" />
    </div>
  </div>
</template>

<style scoped>
.msg-page { max-width: 760px; margin: 0 auto; display: flex; flex-direction: column; gap: 16px; }
.msg-head { display: flex; justify-content: space-between; align-items: center; }
.group-label { font-size: 13px; color: var(--pc-ink-2); font-weight: 700; margin-bottom: 8px; padding-left: 2px; }
.msg-list { padding: 6px 16px; }
.n-item { display: flex; gap: 13px; padding: 14px 6px; border-radius: 12px; cursor: pointer;
  align-items: flex-start; border-bottom: 1px solid #F4F1EC; }
.n-item:last-child { border-bottom: none; }
.n-item:hover { background: #FAF7F2; }
.n-main { flex: 1; min-width: 0; }
.n-title-row { display: flex; align-items: center; gap: 8px; }
.n-title-row b { font-size: 14.5px; }
.n-type { font-size: 11px; font-weight: 800; padding: 1px 8px; border-radius: 4px; flex-shrink: 0; }
.n-type.mint { background: var(--pc-primary-soft); color: var(--pc-primary-deep); }
.n-type.warm { background: var(--pc-accent-soft); color: #C97B2C; }
.n-type.rose { background: var(--pc-danger-soft); color: var(--pc-danger); }
.n-type.gray { background: #F0EDE8; color: var(--pc-ink-2); }
.n-content { margin: 5px 0 4px; line-height: 1.6; }
.n-time { display: flex; gap: 4px; align-items: center; }
.go { color: var(--pc-primary-deep); }
.unread-dot { width: 8px; height: 8px; border-radius: 50%; background: var(--pc-accent); flex-shrink: 0; }
.n-item.unread { background: #FBF6EF; }
.n-icon { width: 40px; height: 40px; border-radius: 12px; display: flex; align-items: center;
  justify-content: center; flex-shrink: 0; }
.n-icon.mint { background: var(--pc-primary-soft); color: var(--pc-primary-deep); }
.n-icon.warm { background: var(--pc-accent-soft); color: #C97B2C; }
.n-icon.rose { background: var(--pc-danger-soft); color: var(--pc-danger); }
.n-icon.gray { background: #F0EDE8; color: var(--pc-ink-2); }
.msg-empty { text-align: center; padding: 30px; }
</style>

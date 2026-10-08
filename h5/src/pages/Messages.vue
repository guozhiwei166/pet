<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '../api'
import { useUserStore } from '../store/user'

const router = useRouter()
const store = useUserStore()
const list = ref([])

const typeMeta = {
  ORDER: { icon: 'orders-o', cls: 'sky' },
  DIARY: { icon: 'photograph', cls: 'warm' },
  SYSTEM: { icon: 'info-o', cls: 'mint' },
}

async function load() {
  list.value = await api.messages()
  const n = await api.unreadCount()
  store.setUnread(n)
}

async function open(m) {
  if (!m.isRead) await api.readMessage(m.id)
  if (m.refId && m.type === 'ORDER') return router.push(`/orders/${m.refId}`)
  load()
}

async function readAll() {
  await api.readAll()
  load()
}

onMounted(load)
</script>

<template>
  <div class="page">
    <van-nav-bar title="消息中心">
      <template #right>
        <span class="read-all" @click="readAll">全部已读</span>
      </template>
    </van-nav-bar>

    <div v-for="m in list" :key="m.id" class="clay msg-card press" :class="{ unread: !m.isRead }" @click="open(m)">
      <div class="msg-icon" :class="typeMeta[m.type]?.cls || 'mint'">
        <van-icon :name="typeMeta[m.type]?.icon || 'info-o'" />
      </div>
      <div class="msg-body">
        <div class="msg-head">
          <b>{{ m.title }}</b>
          <span v-if="!m.isRead" class="dot"></span>
        </div>
        <div class="sub msg-content">{{ m.content }}</div>
        <div class="sub msg-time">{{ m.createTime }}</div>
      </div>
    </div>
    <div v-if="!list.length" class="empty-wrap">
      <div class="sub" style="text-align:center">暂无消息</div>
    </div>
  </div>
</template>

<style scoped>
.read-all { font-size: 13px; color: var(--brand); font-weight: 600; }
.msg-card { margin: 12px 16px; padding: 14px; display: flex; }
.msg-card.unread { border-left: 4px solid var(--brand); }
.msg-icon { width: 42px; height: 42px; border-radius: 14px; display: flex; align-items: center;
  justify-content: center; font-size: 19px; flex-shrink: 0; margin-right: 12px; }
.msg-icon.warm { background: var(--brand-soft); color: var(--brand-deep); }
.msg-icon.sky { background: var(--sky-soft); color: var(--sky); }
.msg-icon.mint { background: var(--mint-soft); color: var(--mint); }
.msg-body { flex: 1; min-width: 0; }
.msg-head { display: flex; justify-content: space-between; align-items: center; font-size: 14.5px; }
.dot { width: 8px; height: 8px; border-radius: 50%; background: var(--brand); }
.msg-content { margin: 6px 0 5px; line-height: 1.6; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
</style>

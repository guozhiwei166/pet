<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import api from './api'
import { useUserStore } from './store/user'

const route = useRoute()
const store = useUserStore()

/** 未读消息轮询：15 秒拉取一次，驱动底部导航角标 */
let pollTimer = null
async function refreshUnread() {
  if (!store.token || route.path === '/login') return
  try { store.setUnread(await api.unreadCount()) } catch (e) { /* 静默失败 */ }
}
onMounted(() => {
  pollTimer = setInterval(refreshUnread, 15000)
})
onUnmounted(() => clearInterval(pollTimer))
</script>

<template>
  <router-view />
  <van-tabbar route v-if="route.meta.tabbar" safe-area-inset-bottom class="tabbar">
    <van-tabbar-item to="/" icon="wap-home-o">首页</van-tabbar-item>
    <van-tabbar-item to="/orders" icon="orders-o">订单</van-tabbar-item>
    <van-tabbar-item to="/messages" icon="chat-o" :badge="store.unread > 0 ? store.unread : ''">消息</van-tabbar-item>
    <van-tabbar-item to="/profile" icon="user-o">我的</van-tabbar-item>
  </van-tabbar>
</template>

<style scoped>
.tabbar { height: calc(58px + env(safe-area-inset-bottom)); box-shadow: 0 -6px 24px rgba(190, 106, 58, .1); }
.tabbar :deep(.van-tabbar-item__icon) { font-size: 21px; }
.tabbar :deep(.van-tabbar-item__text) { font-size: 11px; font-weight: 600; }
</style>

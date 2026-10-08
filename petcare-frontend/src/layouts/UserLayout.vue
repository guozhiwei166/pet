<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import Bell from '../components/Bell.vue'
import api from '../api'

const route = useRoute()
const router = useRouter()
const store = useUserStore()

const menus = [
  { path: '/', label: '首页', icon: 'HomeFilled' },
  { path: '/pets', label: '我的宠物', icon: 'Histogram' },
  { path: '/boarding', label: '我要寄养', icon: 'Suitcase' },
  { path: '/orders', label: '我的订单', icon: 'Tickets' },
  { path: '/store', label: '门店服务', icon: 'Shop' },
  { path: '/service-bookings', label: '服务预约', icon: 'Calendar' },
  { path: '/coupons', label: '领券中心', icon: 'Ticket' },
  { path: '/stories', label: '故事墙', icon: 'ChatLineSquare' },
  { path: '/notifications', label: '消息', icon: 'Bell' },
]

async function logout() {
  try { await api.logout() } catch { /* 忽略 */ }
  store.logout()
  router.push('/login')
}

/** 下拉菜单统一走 command，避免菜单项自身 click 与 command 各触发一次（退出登录被执行两次） */
function onCommand(cmd: string) {
  if (cmd === 'profile') router.push('/profile')
  else if (cmd === 'logout') logout()
}
</script>

<template>
  <el-container class="layout">
    <el-header class="header" height="62px">
      <div class="brand" @click="router.push('/')">
        <svg viewBox="0 0 24 24" width="26" height="26" fill="#6FA58B" aria-hidden="true">
          <ellipse cx="7" cy="8.2" rx="2.1" ry="2.7"/><ellipse cx="17" cy="8.2" rx="2.1" ry="2.7"/>
          <ellipse cx="3.6" cy="13" rx="1.9" ry="2.4"/><ellipse cx="20.4" cy="13" rx="1.9" ry="2.4"/>
          <path d="M12 11c3.6 0 6.4 2.7 6.4 5.6 0 2.1-1.7 3.4-3.6 3.4-1.1 0-1.9-.4-2.8-.4-.9 0-1.7.4-2.8.4-1.9 0-3.6-1.3-3.6-3.4C5.6 13.7 8.4 11 12 11z"/>
        </svg>
        <span>猫狗宠物寄养系统</span>
      </div>
      <el-menu :default-active="route.path" mode="horizontal" router class="nav" :ellipsis="false">
        <el-menu-item v-for="m in menus" :key="m.path" :index="m.path">{{ m.label }}</el-menu-item>
      </el-menu>
      <div class="right">
        <Bell />
        <el-dropdown @command="onCommand">
          <span class="user">
            <img v-if="store.userInfo?.avatar" :src="store.userInfo.avatar" alt="头像" class="nav-avatar" />
            <el-avatar v-else :size="30" style="background:#EAF2ED;color:#5A8F76;font-weight:800">
              {{ store.nickname.slice(0, 1) }}
            </el-avatar>
            {{ store.nickname }}
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">👤 个人中心</el-dropdown-item>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>
    <el-main class="main">
      <router-view />
    </el-main>
  </el-container>
</template>

<style scoped>
.layout { min-height: 100vh; }
.header { display: flex; align-items: center; gap: 14px; background: #fff;
  border-bottom: 1px solid var(--pc-line); position: sticky; top: 0; z-index: 20; padding: 0 18px; }
.brand { display: flex; align-items: center; gap: 8px; font-size: 17px; font-weight: 900;
  letter-spacing: 1px; cursor: pointer; }
.nav { border-bottom: none !important; flex: 1; }
.nav :deep(.el-menu-item) { font-weight: 600; height: 60px; line-height: 60px; padding: 0 13px; }
.nav :deep(.el-menu-item.is-active) { border-bottom: 3px solid var(--pc-primary); }
.right { display: flex; align-items: center; gap: 14px; }
.bell { cursor: pointer; display: flex; align-items: center; color: var(--pc-ink-2); }
.user { cursor: pointer; display: inline-flex; align-items: center; gap: 8px; font-size: 14px; font-weight: 600; outline: none; }
.nav-avatar { width: 30px; height: 30px; border-radius: 50%; object-fit: cover; }
.main { max-width: 1200px; margin: 0 auto; width: 100%; padding: 22px 20px 40px; }
</style>

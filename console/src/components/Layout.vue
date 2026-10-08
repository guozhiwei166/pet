<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../store/user'

const route = useRoute()
const router = useRouter()
const store = useUserStore()

const merchantMenus = [
  { path: '/dashboard', label: '经营看板', icon: 'Odometer' },
  { path: '/orders', label: '订单管理', icon: 'Tickets' },
  { path: '/rooms', label: '房型管理', icon: 'House' },
  { path: '/reviews', label: '评价管理', icon: 'ChatDotRound' },
  { path: '/shop', label: '店铺信息', icon: 'Shop' },
]
const adminMenus = [
  { path: '/admin/dashboard', label: '数据大屏', icon: 'DataAnalysis' },
  { path: '/admin/merchants', label: '商家审核', icon: 'Stamp' },
  { path: '/admin/users', label: '用户管理', icon: 'User' },
  { path: '/admin/orders', label: '订单监管', icon: 'Tickets' },
]
const menus = computed(() => (store.isAdmin ? adminMenus : merchantMenus))
const roleText = computed(() => (store.isAdmin ? '平台管理中心' : '商家中心'))

async function logout() {
  store.logout()
  router.push('/login')
}
</script>

<template>
  <el-container class="layout">
    <el-aside width="216px" class="aside">
      <div class="logo">
        <div class="logo-mark">
          <svg viewBox="0 0 24 24" width="22" height="22" fill="#fff" aria-hidden="true">
            <ellipse cx="7" cy="8.2" rx="2.1" ry="2.7"/><ellipse cx="17" cy="8.2" rx="2.1" ry="2.7"/>
            <ellipse cx="3.6" cy="13" rx="1.9" ry="2.4"/><ellipse cx="20.4" cy="13" rx="1.9" ry="2.4"/>
            <path d="M12 11c3.6 0 6.4 2.7 6.4 5.6 0 2.1-1.7 3.4-3.6 3.4-1.1 0-1.9-.4-2.8-.4-.9 0-1.7.4-2.8.4-1.9 0-3.6-1.3-3.6-3.4C5.6 13.7 8.4 11 12 11z"/>
          </svg>
        </div>
        <div>
          <div class="logo-name">毛孩驿站</div>
          <div class="logo-sub">{{ roleText }}</div>
        </div>
      </div>

      <el-menu :default-active="route.path" router class="menu">
        <el-menu-item v-for="m in menus" :key="m.path" :index="m.path">
          <el-icon><component :is="m.icon" /></el-icon>
          <span>{{ m.label }}</span>
        </el-menu-item>
      </el-menu>

      <div class="aside-tip">毕业设计演示系统<br />PawStay v2.0</div>
    </el-aside>

    <el-container>
      <el-header class="header">
        <span class="title">{{ route.meta.title }}</span>
        <el-dropdown @command="logout">
          <span class="user">
            <span class="user-avatar">{{ store.nickname.slice(0, 1) }}</span>
            {{ store.nickname }}
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="out">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.layout { height: 100vh; }
.aside { background: #2E2A26; display: flex; flex-direction: column; }
.logo { display: flex; align-items: center; gap: 11px; padding: 20px 18px 16px; }
.logo-mark { width: 38px; height: 38px; border-radius: 13px; flex-shrink: 0;
  background: linear-gradient(140deg, #FF9A62, #FF6B3D); display: flex; align-items: center; justify-content: center; }
.logo-name { color: #FFF6F0; font-weight: 800; font-size: 16px; letter-spacing: 1px; }
.logo-sub { color: #8F867D; font-size: 11px; margin-top: 1px; }
.menu { border-right: none; flex: 1; background: transparent; padding: 4px 10px; }
.menu :deep(.el-menu-item) { color: #B9B0A6; border-radius: 12px; margin: 3px 0; height: 46px;
  transition: all .2s; }
.menu :deep(.el-menu-item:hover) { background: #3A342E; color: #FFF6F0; }
.menu :deep(.el-menu-item.is-active) { background: var(--el-color-primary); color: #fff;
  box-shadow: 0 8px 18px -6px rgba(244, 81, 30, .5); }
.aside-tip { padding: 14px 20px; font-size: 11px; color: #6E665E; line-height: 1.8; }

.header { display: flex; justify-content: space-between; align-items: center; background: #fff;
  border-bottom: 1px solid var(--ps-line); height: 60px; }
.title { font-weight: 800; font-size: 16.5px; }
.user { cursor: pointer; font-size: 14px; display: inline-flex; align-items: center; gap: 7px; color: var(--ps-ink-2); outline: none; }
.user-avatar { width: 30px; height: 30px; border-radius: 50%; background: var(--el-color-primary-light-8);
  color: var(--el-color-primary-dark-2); display: inline-flex; align-items: center; justify-content: center;
  font-weight: 800; font-size: 13px; }
.main { padding: 20px; }
</style>

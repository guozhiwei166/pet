<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import api from '../api'
import Bell from '../components/Bell.vue'

const route = useRoute()
const router = useRouter()
const store = useUserStore()

const menus = [
  { path: '/staff/workbench', label: '今日工作台', icon: 'AlarmClock' },
  { path: '/staff/room-map', label: '房间地图', icon: 'OfficeBuilding' },
  { path: '/staff/bookings', label: '服务预约', icon: 'Calendar' },
  { path: '/staff/pets', label: '宠物档案', icon: 'Histogram' },
  { path: '/staff/exceptions', label: '异常上报', icon: 'WarningFilled' },
  { path: '/staff/profile', label: '个人中心', icon: 'User' },
]

async function logout() {
  try { await api.logout() } catch { /* 忽略 */ }
  store.logout()
  router.push('/login')
}
</script>

<template>
  <el-container class="layout">
    <el-aside width="200px" class="aside">
      <div class="logo">
        <div class="logo-mark">🐾</div>
        <div>
          <div class="logo-name">猫狗宠物寄养系统</div>
          <div class="logo-sub">员工端</div>
        </div>
      </div>
      <el-menu :default-active="route.path" router class="menu">
        <el-menu-item v-for="m in menus" :key="m.path" :index="m.path">
          <el-icon><component :is="m.icon" /></el-icon>
          <span>{{ m.label }}</span>
        </el-menu-item>
      </el-menu>
      <div class="aside-tip">{{ store.nickname }}，加油！</div>
    </el-aside>
    <el-container>
      <el-header class="header" height="58px">
        <span class="title">{{ route.meta.title }}</span>
        <div style="display: flex; align-items: center; gap: 16px">
          <Bell />
          <el-dropdown @command="logout">
            <span class="user">{{ store.nickname }}</span>
            <template #dropdown>
              <el-dropdown-menu><el-dropdown-item command="out">退出登录</el-dropdown-item></el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="main"><router-view /></el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.layout { height: 100vh; }
.aside { background: #FBF9F4; border-right: 1px solid var(--pc-line); display: flex; flex-direction: column; }
.logo { display: flex; align-items: center; gap: 10px; padding: 18px 16px 14px; }
.logo-mark { width: 36px; height: 36px; border-radius: 12px; background: var(--pc-primary-soft);
  display: flex; align-items: center; justify-content: center; font-size: 18px; }
.logo-name { font-weight: 900; font-size: 14px; }
.logo-sub { font-size: 11px; color: var(--pc-ink-3, #A69C92); }
.menu { border-right: none; flex: 1; background: transparent; }
.menu :deep(.el-menu-item.is-active) { background: var(--pc-primary-soft); border-radius: 10px; }
.aside-tip { padding: 12px 16px; font-size: 12px; color: var(--pc-ink-2, #7B827D); }
.header { display: flex; justify-content: space-between; align-items: center; background: #fff;
  border-bottom: 1px solid var(--pc-line); }
.title { font-weight: 800; font-size: 16px; }
.nav-avatar { width: 30px; height: 30px; border-radius: 50%; object-fit: cover; }
.user { cursor: pointer; font-size: 13.5px; font-weight: 600; outline: none; }
.main { padding: 18px; background: var(--pc-bg); overflow: auto; }
</style>

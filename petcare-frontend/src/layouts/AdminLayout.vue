<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import api from '../api'
import Bell from '../components/Bell.vue'

const route = useRoute()
const router = useRouter()
const store = useUserStore()

const menus = computed(() => {
  const all = [
    { path: '/admin/dashboard', label: '驾驶舱', icon: 'DataAnalysis', adminOnly: true },
    { path: '/admin/room-map', label: '房间地图', icon: 'OfficeBuilding' },
    { path: '/admin/room-types', label: '房型管理', icon: 'House' },
    { path: '/admin/services', label: '服务管理', icon: 'Present' },
    { path: '/admin/orders', label: '订单管理', icon: 'Tickets' },
    { path: '/admin/pets', label: '宠物档案', icon: 'Histogram' },
    { path: '/admin/exceptions', label: '异常中心', icon: 'WarningFilled' },
    { path: '/admin/marketing', label: '营销中心', icon: 'Present', adminOnly: true },
    { path: '/admin/stories', label: '故事审核', icon: 'ChatLineSquare', adminOnly: true },
    { path: '/admin/users', label: '用户与员工', icon: 'User', adminOnly: true },
    { path: '/admin/logs', label: '操作日志', icon: 'Document', adminOnly: true },
  ]
  // 员工登录管理端时，仅显示其有权限访问的菜单（驾驶舱/营销/故事审核/用户/日志为 ADMIN 专属）
  return store.isAdmin ? all : all.filter(m => !m.adminOnly)
})

async function logout() {
  try { await api.logout() } catch { /* 忽略 */ }
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
          <div class="logo-name">猫狗宠物寄养系统</div>
          <div class="logo-sub">管理中心</div>
        </div>
      </div>
      <el-menu :default-active="route.path" router class="menu">
        <el-menu-item v-for="m in menus" :key="m.path" :index="m.path">
          <el-icon><component :is="m.icon" /></el-icon>
          <span>{{ m.label }}</span>
        </el-menu-item>
      </el-menu>
      <div class="aside-tip">猫狗宠物寄养系统 毕业设计 v1.0</div>
    </el-aside>

    <el-container>
      <el-header class="header" height="60px">
        <span class="title">{{ route.meta.title }}</span>
        <div style="display: flex; align-items: center; gap: 18px">
          <Bell />
          <el-dropdown @command="(cmd: string) => cmd === 'profile' ? router.push('/admin/profile') : logout()">
            <span class="user">
              <img v-if="store.userInfo?.avatar" :src="store.userInfo.avatar" class="nav-avatar" alt="头像" />
              <el-avatar v-else :size="30" style="background:#EAF2ED;color:#5A8F76;font-weight:800">
                {{ store.nickname.slice(0, 1) }}
              </el-avatar>
              {{ store.nickname }}（{{ store.userInfo?.roleName }}）
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">👤 个人中心</el-dropdown-item>
                <el-dropdown-item command="out">退出登录</el-dropdown-item>
              </el-dropdown-menu>
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
.aside { background: #2E3532; display: flex; flex-direction: column; }
.logo { display: flex; align-items: center; gap: 11px; padding: 20px 18px 16px; }
.logo-mark { width: 38px; height: 38px; border-radius: 13px; flex-shrink: 0;
  background: var(--pc-primary); display: flex; align-items: center; justify-content: center; }
.logo-name { color: #F7F5F0; font-weight: 800; font-size: 14px; letter-spacing: 0; }
.logo-sub { color: #8F9A93; font-size: 11px; margin-top: 1px; }
.menu { border-right: none; flex: 1; background: transparent; padding: 4px 10px; }
.menu :deep(.el-menu-item) { color: #AEBBB3; border-radius: 10px; margin: 3px 0; height: 46px; transition: all .2s; }
.menu :deep(.el-menu-item:hover) { background: #3A443F; color: #F7F5F0; }
.menu :deep(.el-menu-item.is-active) { background: var(--pc-primary); color: #fff;
  box-shadow: 0 8px 18px -6px rgba(111,165,139,.5); }
.aside-tip { padding: 14px 20px; font-size: 11px; color: #6E7A72; }
.header { display: flex; justify-content: space-between; align-items: center; background: #fff;
  border-bottom: 1px solid var(--pc-line); }
.title { font-weight: 800; font-size: 16.5px; }
.nav-avatar { width: 30px; height: 30px; border-radius: 50%; object-fit: cover; }
.user { cursor: pointer; display: inline-flex; align-items: center; gap: 8px; font-size: 13.5px;
  color: var(--pc-ink-2); outline: none; }
.main { padding: 20px; background: var(--pc-bg); }
</style>

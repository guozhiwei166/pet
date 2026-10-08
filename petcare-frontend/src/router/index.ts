import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: () => import('../views/auth/Login.vue') },
    { path: '/register', component: () => import('../views/auth/Register.vue') },
    {
      path: '/',
      component: () => import('../layouts/UserLayout.vue'),
      children: [
        { path: '', component: () => import('../views/Home.vue'), meta: { title: '首页' } },
        { path: 'pets', component: () => import('../views/pets/MyPets.vue'), meta: { title: '我的宠物' } },
        { path: 'pets/:id', component: () => import('../views/pets/PetProfile.vue'), meta: { title: '宠物档案' } },
        // Phase 4-8 页面
        { path: 'boarding', component: () => import('../views/boarding/Booking.vue'), meta: { title: '我要寄养' } },
        { path: 'orders', component: () => import('../views/orders/MyOrders.vue'), meta: { title: '我的订单' } },
        { path: 'orders/:id', component: () => import('../views/orders/OrderDetail.vue'), meta: { title: '订单详情' } },
        { path: 'notifications', component: () => import('../views/Notifications.vue'), meta: { title: '消息中心' } },
        { path: 'store', component: () => import('../views/store/Store.vue'), meta: { title: '门店' } },
        { path: 'coupons', component: () => import('../views/store/Coupons.vue'), meta: { title: '领券中心' } },
        { path: 'stories', component: () => import('../views/store/Stories.vue'), meta: { title: '故事墙' } },
        { path: 'profile', component: () => import('../views/Profile.vue'), meta: { title: '个人中心' } },
        { path: 'service-bookings', component: () => import('../views/store/MyBookings.vue'), meta: { title: '服务预约' } },
      ],
    },
    {
      path: '/admin',
      component: () => import('../layouts/AdminLayout.vue'),
      meta: { requiresStaff: true },
      children: [
        { path: '', redirect: '/admin/dashboard' },
        { path: 'dashboard', component: () => import('../views/admin/Dashboard.vue'), meta: { title: '驾驶舱', role: 'ROLE_ADMIN' } },
        { path: 'room-map', component: () => import('../views/admin/RoomMap.vue'), meta: { title: '房间地图' } },
        { path: 'room-types', component: () => import('../views/admin/RoomTypes.vue'), meta: { title: '房型管理' } },
        { path: 'services', component: () => import('../views/admin/Services.vue'), meta: { title: '服务管理' } },
        { path: 'orders', component: () => import('../views/admin/AdminOrders.vue'), meta: { title: '订单管理' } },
        { path: 'pets', component: () => import('../views/pets/MyPets.vue'), meta: { title: '宠物档案' } },
        { path: 'pets/:id', component: () => import('../views/pets/PetProfile.vue'), meta: { title: '宠物档案' } },
        { path: 'exceptions', component: () => import('../views/admin/Exceptions.vue'), meta: { title: '异常中心' } },
        { path: 'marketing', component: () => import('../views/admin/Marketing.vue'), meta: { title: '营销中心', role: 'ROLE_ADMIN' } },
        { path: 'stories', component: () => import('../views/admin/Stories.vue'), meta: { title: '故事审核', role: 'ROLE_ADMIN' } },
        { path: 'users', component: () => import('../views/admin/AdminUsers.vue'), meta: { title: '用户与员工' } },
        { path: 'logs', component: () => import('../views/admin/AdminLogs.vue'), meta: { title: '操作日志' } },
        { path: 'profile', component: () => import('../views/Profile.vue'), meta: { title: '个人中心' } },
      ],
    },
    {
      path: '/staff',
      component: () => import('../layouts/StaffLayout.vue'),
      meta: { requiresStaff: true },
      children: [
        { path: '', redirect: '/staff/workbench' },
        { path: 'workbench', component: () => import('../views/staff/Workbench.vue'), meta: { title: '今日工作台' } },
        { path: 'room-map', component: () => import('../views/admin/RoomMap.vue'), meta: { title: '房间地图' } },
        { path: 'pets', component: () => import('../views/pets/MyPets.vue'), meta: { title: '宠物档案' } },
        { path: 'pets/:id', component: () => import('../views/pets/PetProfile.vue'), meta: { title: '宠物档案' } },
        { path: 'exceptions', component: () => import('../views/staff/ExceptionReport.vue'), meta: { title: '异常上报' } },
        { path: 'bookings', component: () => import('../views/staff/StaffBookings.vue'), meta: { title: '服务预约' } },
        { path: 'profile', component: () => import('../views/Profile.vue'), meta: { title: '个人中心' } },
      ],
    },
  ],
})

router.beforeEach(to => {
  const store = useUserStore()
  const publicPages = ['/login', '/register']
  if (!publicPages.includes(to.path) && !store.token) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  const homeOf = store.isAdmin ? '/admin/dashboard' : store.isStaff ? '/staff/workbench' : '/'
  if (publicPages.includes(to.path) && store.token) return homeOf
  if (store.token) {
    // 用户端页面：仅 ROLE_USER 可进（admin/staff 一律回各自首页）
    if (!to.path.startsWith('/admin') && !to.path.startsWith('/staff')) {
      if (store.isAdmin || store.isStaff) return homeOf
    }
    // 管理区权限：房态操作 STAFF 可用，驾驶舱等 ADMIN 专属页面由 meta.role 控制
    if (to.path.startsWith('/admin')) {
      if (!store.isStaff) return '/'
      if (to.meta.role === 'ROLE_ADMIN' && !store.isAdmin) return '/admin/room-map'
    }
    // 员工区权限：仅 STAFF/ADMIN 可入
    if (to.path.startsWith('/staff') && !store.isStaff) return homeOf
  }
})

export default router

import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../store/user'

const routes = [
  { path: '/login', component: () => import('../pages/Login.vue') },
  {
    path: '/',
    component: () => import('../components/Layout.vue'),
    children: [
      { path: '', redirect: '/dashboard' },
      // 商家端
      { path: 'dashboard', component: () => import('../pages/MerchantDashboard.vue'), meta: { role: 'MERCHANT', title: '经营看板' } },
      { path: 'orders', component: () => import('../pages/MerchantOrders.vue'), meta: { role: 'MERCHANT', title: '订单管理' } },
      { path: 'rooms', component: () => import('../pages/MerchantRooms.vue'), meta: { role: 'MERCHANT', title: '房型管理' } },
      { path: 'reviews', component: () => import('../pages/MerchantReviews.vue'), meta: { role: 'MERCHANT', title: '评价管理' } },
      { path: 'shop', component: () => import('../pages/MerchantShop.vue'), meta: { role: 'MERCHANT', title: '店铺信息' } },
      // 管理端
      { path: 'admin/dashboard', component: () => import('../pages/AdminDashboard.vue'), meta: { role: 'ADMIN', title: '数据大屏' } },
      { path: 'admin/merchants', component: () => import('../pages/AdminMerchants.vue'), meta: { role: 'ADMIN', title: '商家审核' } },
      { path: 'admin/users', component: () => import('../pages/AdminUsers.vue'), meta: { role: 'ADMIN', title: '用户管理' } },
      { path: 'admin/orders', component: () => import('../pages/AdminOrders.vue'), meta: { role: 'ADMIN', title: '订单监管' } },
    ],
  },
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach(to => {
  const store = useUserStore()
  if (to.path === '/login') return true
  if (!store.token) return '/login'
  if (to.meta.role && to.meta.role !== store.role) return store.role === 'ADMIN' ? '/admin/dashboard' : '/dashboard'
  if (to.path === '/') return store.isAdmin ? '/admin/dashboard' : '/dashboard'
})

export default router

import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/login', component: () => import('../pages/Login.vue') },
  { path: '/', component: () => import('../pages/Home.vue'), meta: { tabbar: true } },
  { path: '/merchants/:id', component: () => import('../pages/MerchantDetail.vue') },
  { path: '/booking', component: () => import('../pages/Booking.vue') },
  { path: '/orders', component: () => import('../pages/Orders.vue'), meta: { tabbar: true } },
  { path: '/orders/:id', component: () => import('../pages/OrderDetail.vue') },
  { path: '/pets', component: () => import('../pages/Pets.vue') },
  { path: '/pets/edit', component: () => import('../pages/PetEdit.vue') },
  { path: '/messages', component: () => import('../pages/Messages.vue'), meta: { tabbar: true } },
  { path: '/profile', component: () => import('../pages/Profile.vue'), meta: { tabbar: true } },
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach(to => {
  const token = localStorage.getItem('ps_token')
  if (to.path !== '/login' && !token) return { path: '/login', query: { redirect: to.fullPath } }
  if (to.path === '/login' && token) return '/'
})

export default router

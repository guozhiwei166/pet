import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

const http = axios.create({ baseURL: '/api', timeout: 20000 })

http.interceptors.request.use(cfg => {
  const token = localStorage.getItem('ps_token')
  if (token) cfg.headers.Authorization = 'Bearer ' + token
  return cfg
})

http.interceptors.response.use(resp => {
  const body = resp.data
  if (body.code === 0) return body.data
  if (body.code === 401) {
    localStorage.clear()
    router.push('/login')
    return Promise.reject(body)
  }
  ElMessage.error(body.msg || '请求失败')
  return Promise.reject(body)
}, () => {
  ElMessage.error('网络异常，请稍后再试')
  return Promise.reject(new Error('network'))
})

export const ORDER_STATUS = {
  PENDING_CONFIRM: { text: '待接单', type: 'warning' },
  CONFIRMED: { text: '已接单·待入住', type: 'primary' },
  BOARDING: { text: '寄养中', type: 'danger' },
  COMPLETED: { text: '已完成', type: 'success' },
  CANCELLED: { text: '已取消', type: 'info' },
  REJECTED: { text: '已拒单', type: 'info' },
}

export default {
  login: p => http.post('/auth/login', p),
  // 商家端
  dashboard: () => http.get('/merchant/dashboard'),
  myRooms: () => http.get('/merchant/rooms'),
  saveRoom: p => http.post('/merchant/rooms', p),
  updateRoom: p => http.put('/merchant/rooms', p),
  delRoom: id => http.delete(`/merchant/rooms/${id}`),
  shop: () => http.get('/merchant/shop'),
  updateShop: p => http.put('/merchant/shop', p),
  myReviews: () => http.get('/reviews', { params: { merchantId: JSON.parse(localStorage.getItem('ps_user') || '{}').merchantId, limit: 50 } }),
  orders: p => http.get('/orders', { params: p }),
  replyReview: (id, reply) => http.post(`/reviews/${id}/reply`, { reply }),
  orderDetail: id => http.get(`/orders/${id}`),
  confirmOrder: id => http.post(`/orders/${id}/confirm`),
  rejectOrder: (id, reason) => http.post(`/orders/${id}/reject`, { reason }),
  checkinOrder: (id, weightIn) => http.post(`/orders/${id}/checkin`, { weightIn }),
  checkoutOrder: (id, weightOut) => http.post(`/orders/${id}/checkout`, { weightOut }),
  postDiary: (id, p) => http.post(`/orders/${id}/diary`, p),
  // 管理端
  adminStats: () => http.get('/admin/stats'),
  adminMerchants: p => http.get('/admin/merchants', { params: p }),
  auditMerchant: (id, p) => http.post(`/admin/merchants/${id}/audit`, p),
  adminUsers: p => http.get('/admin/users', { params: p }),
}

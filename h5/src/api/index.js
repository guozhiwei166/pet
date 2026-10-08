import axios from 'axios'
import { showToast } from 'vant'
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
    localStorage.removeItem('ps_token')
    if (router.currentRoute.value.path !== '/login') {
      showToast('请先登录')
      router.push('/login')
    }
    return Promise.reject(body)
  }
  showToast(body.msg || '请求失败')
  return Promise.reject(body)
}, err => {
  showToast('网络异常，请稍后再试')
  return Promise.reject(err)
})

/** 订单状态展示配置 */
export const STATUS_MAP = {
  PENDING_CONFIRM: { text: '待商家确认', color: '#FFA940' },
  CONFIRMED: { text: '已接单·待入住', color: '#409EFF' },
  BOARDING: { text: '寄养中', color: '#FF7D4D' },
  COMPLETED: { text: '已完成', color: '#52C41A' },
  CANCELLED: { text: '已取消', color: '#BFBFBF' },
  REJECTED: { text: '商家拒单', color: '#F5222D' },
}

export function fmtDate(d) {
  if (!d) return ''
  const t = new Date(d)
  const p = n => String(n).padStart(2, '0')
  return `${t.getFullYear()}-${p(t.getMonth() + 1)}-${p(t.getDate())}`
}

export default {
  // 认证
  login: p => http.post('/auth/login', p),
  register: p => http.post('/auth/register', p),
  // 首页 / 商家
  home: p => http.get('/home', { params: p }),
  merchantDetail: id => http.get(`/merchants/${id}`),
  // 宠物档案
  pets: () => http.get('/pets'),
  savePet: p => http.post('/pets', p),
  delPet: id => http.delete(`/pets/${id}`),
  // 智能排笼 + 订单
  matchRooms: p => http.get('/match/rooms', { params: p }),
  createOrder: p => http.post('/orders', p),
  orders: p => http.get('/orders', { params: p }),
  orderDetail: id => http.get(`/orders/${id}`),
  payOrder: id => http.post(`/orders/${id}/pay`),
  cancelOrder: id => http.post(`/orders/${id}/cancel`),
  orderDiary: id => http.get(`/orders/${id}/diary`),
  // 评价
  addReview: (id, p) => http.post(`/orders/${id}/review`, p),
  // 消息
  messages: () => http.get('/messages'),
  unreadCount: () => http.get('/messages/unread-count'),
  readMessage: id => http.post(`/messages/${id}/read`),
  readAll: () => http.post('/messages/read-all'),
}

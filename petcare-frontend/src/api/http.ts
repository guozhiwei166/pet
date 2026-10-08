import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

export interface ApiResult<T = unknown> {
  code: number
  message: string
  data: T
  timestamp: number
}

export interface UserInfo {
  id: number
  username: string
  nickname: string
  phone: string
  avatar: string
  role: 'ROLE_USER' | 'ROLE_STAFF' | 'ROLE_ADMIN'
  roleName: string
  securityQuestion?: string
}

const http = axios.create({ baseURL: '/api', timeout: 20000 })

http.interceptors.request.use(cfg => {
  const token = localStorage.getItem('pc_token')
  if (token) cfg.headers.Authorization = 'Bearer ' + token
  return cfg
})

http.interceptors.response.use(resp => {
  const body = resp.data as ApiResult
  if (body.code === 200) return body.data as never
  if (body.code === 401) {
    localStorage.removeItem('pc_token')
    localStorage.removeItem('pc_user')
    if (router.currentRoute.value.path !== '/login') {
      ElMessage.warning('请先登录')
      router.push('/login')
    }
    return Promise.reject(body)
  }
  ElMessage.error(body.message || '请求失败')
  return Promise.reject(body)
}, () => {
  ElMessage.error('网络异常，请稍后再试')
  return Promise.reject(new Error('network'))
})

export default http

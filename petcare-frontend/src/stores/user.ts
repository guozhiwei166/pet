import { defineStore } from 'pinia'
import type { UserInfo } from '../api/http'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('pc_token') || '',
    userInfo: JSON.parse(localStorage.getItem('pc_user') || 'null') as UserInfo | null,
  }),
  getters: {
    isLogin: s => !!s.token,
    role: s => s.userInfo?.role || 'ROLE_USER',
    isAdmin: s => s.userInfo?.role === 'ROLE_ADMIN',
    isStaff: s => s.userInfo?.role === 'ROLE_STAFF' || s.userInfo?.role === 'ROLE_ADMIN',
    nickname: s => s.userInfo?.nickname || '',
  },
  actions: {
    setAuth(token: string, user: UserInfo) {
      this.token = token
      this.userInfo = user
      localStorage.setItem('pc_token', token)
      localStorage.setItem('pc_user', JSON.stringify(user))
    },
    logout() {
      this.token = ''
      this.userInfo = null
      localStorage.removeItem('pc_token')
      localStorage.removeItem('pc_user')
    },
  },
})

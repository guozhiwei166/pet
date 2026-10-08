import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('ps_token') || '',
    userInfo: JSON.parse(localStorage.getItem('ps_user') || 'null'),
    unread: 0,
  }),
  getters: {
    isLogin: s => !!s.token,
    nickname: s => s.userInfo?.nickname || '毛孩家长',
    phone: s => s.userInfo?.phone || '',
    role: s => s.userInfo?.role || 'OWNER',
  },
  actions: {
    setAuth(token, user) {
      this.token = token
      this.userInfo = user
      localStorage.setItem('ps_token', token)
      localStorage.setItem('ps_user', JSON.stringify(user))
    },
    setUnread(n) { this.unread = n },
    logout() {
      this.token = ''
      this.userInfo = null
      localStorage.removeItem('ps_token')
      localStorage.removeItem('ps_user')
    },
  },
})

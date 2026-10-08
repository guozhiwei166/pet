import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('ps_token') || '',
    userInfo: JSON.parse(localStorage.getItem('ps_user') || 'null'),
  }),
  getters: {
    role: s => s.userInfo?.role,
    nickname: s => s.userInfo?.nickname || '',
    isAdmin: s => s.userInfo?.role === 'ADMIN',
  },
  actions: {
    setAuth(token, user) {
      this.token = token
      this.userInfo = user
      localStorage.setItem('ps_token', token)
      localStorage.setItem('ps_user', JSON.stringify(user))
    },
    logout() {
      this.token = ''
      this.userInfo = null
      localStorage.removeItem('ps_token')
      localStorage.removeItem('ps_user')
    },
  },
})

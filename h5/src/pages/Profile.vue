<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showConfirmDialog, showToast } from 'vant'
import api from '../api'
import { useUserStore } from '../store/user'

const router = useRouter()
const store = useUserStore()
const stats = ref({ pets: 0, orders: 0 })

onMounted(async () => {
  const [pets, orders] = await Promise.all([api.pets(), api.orders({ page: 1, size: 1, status: 'ALL' })])
  stats.value = { pets: pets.length, orders: orders.total }
})

async function logout() {
  try {
    await showConfirmDialog({ title: '退出登录', message: '确定要退出毛孩驿站吗？' })
    store.logout()
    router.replace('/login')
  } catch (e) { /* 用户取消 */ }
}
</script>

<template>
  <div class="page">
    <div class="hero hero-gradient">
      <div class="me-line">
        <div class="me-avatar">🐾</div>
        <div class="me-info">
          <div class="me-name">{{ store.nickname }}</div>
          <div class="me-sub">{{ store.phone }}</div>
        </div>
        <span class="role-chip">毛孩家长</span>
      </div>
      <div class="me-stats">
        <div><b class="num">{{ stats.pets }}</b><span>毛孩子</span></div>
        <div class="divider"></div>
        <div><b class="num">{{ stats.orders }}</b><span>寄养订单</span></div>
        <div class="divider"></div>
        <div><b class="num">0</b><span>优惠券</span></div>
      </div>
    </div>

    <div class="clay menu">
      <div class="menu-item press" @click="router.push('/pets')">
        <span class="mi-icon warm"><van-icon name="friends-o" /></span>我的毛孩子<van-icon name="arrow" class="mi-arrow" />
      </div>
      <div class="menu-item press" @click="router.push('/orders')">
        <span class="mi-icon sky"><van-icon name="orders-o" /></span>我的订单<van-icon name="arrow" class="mi-arrow" />
      </div>
      <div class="menu-item press" @click="router.push('/messages')">
        <span class="mi-icon mint"><van-icon name="bell" /></span>消息通知<van-icon name="arrow" class="mi-arrow" />
      </div>
    </div>

    <div class="clay menu">
      <div class="menu-item press" @click="showToast('毛孩驿站 v1.0 · 毕业设计作品')">
        <span class="mi-icon gray"><van-icon name="info-o" /></span>关于毛孩驿站<van-icon name="arrow" class="mi-arrow" />
      </div>
      <div class="menu-item press logout" @click="logout">
        <span class="mi-icon rose"><van-icon name="revoke" /></span>退出登录
      </div>
    </div>
  </div>
</template>

<style scoped>
.hero { padding: 28px 20px 22px; border-radius: 0 0 28px 28px; }
.me-line { display: flex; align-items: center; }
.me-avatar { width: 58px; height: 58px; border-radius: 50%; background: rgba(255,255,255,.28);
  display: flex; align-items: center; justify-content: center; font-size: 27px; margin-right: 14px; }
.me-info { flex: 1; }
.me-name { font-size: 19px; font-weight: 900; }
.me-sub { opacity: .88; font-size: 12.5px; margin-top: 3px; }
.role-chip { background: rgba(255,255,255,.25); border-radius: 999px; font-size: 12px; padding: 5px 13px; }
.me-stats { display: flex; align-items: center; margin-top: 20px; background: rgba(255,255,255,.16);
  border-radius: 18px; padding: 13px 0; }
.me-stats > div { flex: 1; display: flex; flex-direction: column; align-items: center; }
.me-stats b { font-size: 19px; }
.me-stats span { font-size: 11px; opacity: .9; margin-top: 2px; }
.divider { flex: 0 0 1px; height: 26px; background: rgba(255,255,255,.3); }

.menu { margin: 14px 16px 0; padding: 4px 0; }
.menu-item { display: flex; align-items: center; padding: 13px 17px; font-size: 14.5px; font-weight: 600;
  cursor: pointer; }
.menu-item:active { background: var(--brand-mist); }
.mi-icon { width: 34px; height: 34px; border-radius: 11px; display: inline-flex; align-items: center;
  justify-content: center; font-size: 17px; margin-right: 12px; }
.mi-icon.warm { background: var(--brand-soft); color: var(--brand-deep); }
.mi-icon.sky { background: var(--sky-soft); color: var(--sky); }
.mi-icon.rose { background: var(--rose-soft); color: var(--rose); }
.mi-icon.mint { background: var(--mint-soft); color: var(--mint); }
.mi-icon.gray { background: #F2ECE5; color: var(--ink-2); }
.mi-arrow { margin-left: auto; color: #D8CFC6; }
.logout { color: var(--rose); }
</style>

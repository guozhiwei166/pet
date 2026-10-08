<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { showToast } from 'vant'
import api from '../api'
import { useUserStore } from '../store/user'
import Illust from '../components/Illust.vue'

const router = useRouter()
const route = useRoute()
const store = useUserStore()

const mode = ref('login')
const phone = ref('')
const password = ref('')
const nickname = ref('')
const role = ref('OWNER')
const shopName = ref('')
const address = ref('')

const demoAccounts = [
  { label: '我是宠物主人', phone: '13800000001' },
  { label: '我是商家', phone: '13900000001' },
]

async function submit() {
  if (!/^1\d{10}$/.test(phone.value)) return showToast('请输入正确的手机号')
  if (password.value.length < 6) return showToast('密码至少6位')
  try {
    if (mode.value === 'login') {
      const data = await api.login({ phone: phone.value, password: password.value })
      store.setAuth(data.token, data.user)
      showToast({ type: 'success', message: '欢迎回来' })
      router.replace(route.query.redirect || '/')
    } else {
      await api.register({
        phone: phone.value, password: password.value, nickname: nickname.value,
        role: role.value, shopName: shopName.value, address: address.value,
      })
      showToast('注册成功，请登录')
      mode.value = 'login'
    }
  } catch (e) { /* 错误提示由拦截器统一处理 */ }
}

function fillDemo(acc) {
  mode.value = 'login'
  phone.value = acc.phone
  password.value = '123456'
}
</script>

<template>
  <div class="page-plain login-page">
    <!-- 插画主视觉 -->
    <div class="hero">
      <div class="hero-blob">
        <Illust scene="hero" />
      </div>
      <div class="brand-row">
        <div class="brand-name">毛孩驿站</div>
        <div class="brand-slogan">把毛孩子，托付给安心</div>
      </div>
    </div>

    <!-- 登录卡片 -->
    <div class="form-wrap clay-lg">
      <div class="mode-tabs">
        <span :class="{ active: mode === 'login' }" @click="mode = 'login'">登录</span>
        <span :class="{ active: mode === 'register' }" @click="mode = 'register'">注册</span>
      </div>

      <van-cell-group inset>
        <van-field v-model="phone" label="手机号" placeholder="请输入手机号" type="tel" maxlength="11"
                   left-icon="phone-o" />
        <van-field v-model="password" label="密码" placeholder="请输入密码" type="password" left-icon="shield-o" />
        <template v-if="mode === 'register'">
          <van-field v-model="nickname" label="昵称" placeholder="怎么称呼你？" left-icon="smile-o" />
          <van-field label="我的身份">
            <template #input>
              <van-radio-group v-model="role" direction="horizontal">
                <van-radio name="OWNER">宠物主人</van-radio>
                <van-radio name="MERCHANT">商家入驻</van-radio>
              </van-radio-group>
            </template>
          </van-field>
          <template v-if="role === 'MERCHANT'">
            <van-field v-model="shopName" label="店铺名称" placeholder="你的寄养店名" left-icon="shop-o" />
            <van-field v-model="address" label="店铺地址" placeholder="详细地址" left-icon="location-o" />
          </template>
        </template>
      </van-cell-group>

      <van-button type="primary" round block class="submit-btn press" @click="submit">
        {{ mode === 'login' ? '进入驿站' : '注册并加入' }}
      </van-button>

      <div class="trust-line">
        <span class="trust-item"><van-icon name="certificate" /> 持证商家</span>
        <span class="trust-item"><van-icon name="chat-square" /> 每日日记</span>
        <span class="trust-item"><van-icon name="signed" /> 安全退改</span>
      </div>

      <div class="demo-tip">
        <span class="sub">演示账号（密码 123456，点击填充）</span>
        <div class="demo-chips">
          <span v-for="a in demoAccounts" :key="a.phone" class="chip chip-warm press" @click="fillDemo(a)">
            {{ a.label }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  background:
    radial-gradient(130% 80% at 90% -8%, #FFD3B3 0%, rgba(255, 211, 179, 0) 52%),
    linear-gradient(168deg, #FFF3EA 0%, #FDE7D8 46%, var(--bg) 100%);
}
.hero { padding: 30px 24px 0; }
.hero-blob {
  height: 210px; border-radius: 32px; overflow: hidden;
  background: linear-gradient(170deg, #FFD9E8 0%, #FFE3C2 52%, #CBE9D6 100%);
  box-shadow: var(--clay);
}
.brand-row { text-align: center; margin-top: 20px; }
.brand-name { font-size: 27px; font-weight: 900; letter-spacing: 2px; }
.brand-slogan { color: var(--ink-2); font-size: 13px; margin-top: 6px; letter-spacing: 3px; }

.form-wrap { margin: 22px 16px 0; padding: 20px 0 22px; }
.mode-tabs { display: flex; gap: 26px; padding: 0 32px 14px; font-size: 16px; color: var(--ink-3); }
.mode-tabs span { transition: all .2s; cursor: pointer; }
.mode-tabs .active { color: var(--ink); font-weight: 900; font-size: 20px; position: relative; }
.mode-tabs .active::after {
  content: ''; position: absolute; left: 2px; bottom: -5px; width: 22px; height: 5px;
  border-radius: 3px; background: var(--brand);
}
:deep(.van-cell-group--inset) { margin: 0 22px; }
:deep(.van-field) { padding-top: 12px; padding-bottom: 12px; }
:deep(.van-field .van-icon) { color: var(--ink-3); font-size: 17px; margin-right: 2px; }
.submit-btn { width: calc(100% - 44px); margin: 20px 22px 0; height: 46px; font-size: 16px; }
.trust-line { display: flex; justify-content: center; gap: 22px; margin-top: 16px; }
.trust-item { display: inline-flex; align-items: center; gap: 4px; font-size: 11.5px; color: var(--ink-3); }
.trust-item .van-icon { color: var(--mint); font-size: 14px; }
.demo-tip { text-align: center; margin-top: 18px; }
.demo-chips { margin-top: 10px; display: flex; justify-content: center; gap: 10px; }
.demo-chips .chip { padding: 8px 16px; font-size: 13px; }
</style>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '../api'
import { useUserStore } from '../store/user'

const router = useRouter()
const store = useUserStore()
const loading = ref(false)
const form = ref({ phone: '', password: '' })

const demo = [
  { label: '商家账号', phone: '13900000001' },
  { label: '管理员账号', phone: '10000000000' },
]

async function submit() {
  if (!form.value.phone || !form.value.password) return ElMessage.warning('请输入账号密码')
  loading.value = true
  try {
    const data = await api.login(form.value)
    if (data.user.role === 'OWNER') return ElMessage.error('请使用商家或管理员账号登录（用户端请用手机 H5）')
    store.setAuth(data.token, data.user)
    ElMessage.success('欢迎回来')
    router.replace(data.user.role === 'ADMIN' ? '/admin/dashboard' : '/dashboard')
  } finally {
    loading.value = false
  }
}

function fill(d) {
  form.value.phone = d.phone
  form.value.password = '123456'
}
</script>

<template>
  <div class="login-page">
    <!-- 左侧品牌区 -->
    <div class="brand-panel">
      <div class="brand-inner">
        <div class="logo-row">
          <div class="logo-mark">
            <svg viewBox="0 0 24 24" width="26" height="26" fill="#fff" aria-hidden="true">
              <ellipse cx="7" cy="8.2" rx="2.1" ry="2.7"/><ellipse cx="17" cy="8.2" rx="2.1" ry="2.7"/>
              <ellipse cx="3.6" cy="13" rx="1.9" ry="2.4"/><ellipse cx="20.4" cy="13" rx="1.9" ry="2.4"/>
              <path d="M12 11c3.6 0 6.4 2.7 6.4 5.6 0 2.1-1.7 3.4-3.6 3.4-1.1 0-1.9-.4-2.8-.4-.9 0-1.7.4-2.8.4-1.9 0-3.6-1.3-3.6-3.4C5.6 13.7 8.4 11 12 11z"/>
            </svg>
          </div>
          <span class="logo-text">毛孩驿站 PawStay</span>
        </div>
        <h1>让每一次寄养<br />都值得被看见</h1>
        <p class="brand-desc">接单、打卡、寄养日记、经营数据——<br />商家端与平台管理端，一站式打理你的毛孩生意。</p>
        <ul class="points">
          <li><el-icon><CircleCheckFilled /></el-icon>每日护理打卡，寄养日记实时同步给主人</li>
          <li><el-icon><CircleCheckFilled /></el-icon>智能排笼，按体型性格自动匹配合适笼位</li>
          <li><el-icon><CircleCheckFilled /></el-icon>平台审核 + 数据大屏，生意状况一目了然</li>
        </ul>
      </div>
      <div class="brand-foot">本科毕业设计演示系统 · v2.0</div>
    </div>

    <!-- 右侧表单区 -->
    <div class="form-panel">
      <div class="form-box">
        <h2>欢迎回来</h2>
        <p class="form-sub">登录商家端 / 平台管理端</p>

        <el-form size="large" @submit.prevent>
          <el-form-item>
            <el-input v-model="form.phone" placeholder="手机号" prefix-icon="Iphone" maxlength="11" />
          </el-form-item>
          <el-form-item>
            <el-input v-model="form.password" type="password" placeholder="密码" prefix-icon="Lock"
                      show-password @keyup.enter="submit" />
          </el-form-item>
          <el-button type="primary" size="large" round :loading="loading" class="btn" @click="submit">
            登 录
          </el-button>
        </el-form>

        <div class="demo">
          <span class="demo-label">演示账号（密码 123456）</span>
          <div class="demo-chips">
            <el-tag v-for="d in demo" :key="d.phone" class="demo-tag" effect="plain" round @click="fill(d)">{{ d.label }}</el-tag>
          </div>
        </div>
        <div class="h5-hint">宠物主人请从用户端（localhost:5180）登录</div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-page { height: 100vh; display: flex; }
.brand-panel {
  flex: 1.15; position: relative; color: #fff; display: flex; flex-direction: column;
  background:
    radial-gradient(120% 80% at 88% -10%, #FFC08A 0%, rgba(255, 192, 138, 0) 55%),
    linear-gradient(155deg, #FF9A62 0%, #FF6B3D 58%, #F4511E 100%);
}
.brand-inner { flex: 1; display: flex; flex-direction: column; justify-content: center; padding: 0 9%; }
.logo-row { display: flex; align-items: center; gap: 12px; margin-bottom: 38px; }
.logo-mark { width: 42px; height: 42px; border-radius: 14px; background: rgba(255,255,255,.22);
  display: flex; align-items: center; justify-content: center; }
.logo-text { font-size: 18px; font-weight: 800; letter-spacing: 1px; }
.brand-panel h1 { font-size: 38px; font-weight: 900; line-height: 1.4; letter-spacing: 1px; }
.brand-desc { margin-top: 18px; font-size: 14.5px; line-height: 2; opacity: .92; }
.points { list-style: none; margin: 34px 0 0; padding: 0; display: flex; flex-direction: column; gap: 15px; }
.points li { display: flex; align-items: center; gap: 10px; font-size: 14px; opacity: .96; }
.points .el-icon { color: #FFE3A0; font-size: 18px; }
.brand-foot { text-align: center; padding: 22px; font-size: 12px; opacity: .7; }

.form-panel { flex: 1; display: flex; align-items: center; justify-content: center; background: var(--ps-bg); }
.form-box { width: 360px; background: #fff; border-radius: 26px; padding: 40px 36px 30px;
  box-shadow: var(--ps-clay-lg); }
.form-box h2 { margin: 0; font-size: 24px; font-weight: 900; }
.form-sub { color: var(--ps-ink-3); font-size: 13px; margin: 8px 0 26px; }
.btn { width: 100%; font-weight: 800; letter-spacing: 4px; margin-top: 4px; }
.demo { margin-top: 26px; text-align: center; }
.demo-label { font-size: 12px; color: var(--ps-ink-3); }
.demo-chips { margin-top: 10px; display: flex; justify-content: center; gap: 10px; }
.demo-tag { cursor: pointer; }
.h5-hint { margin-top: 20px; text-align: center; font-size: 11.5px; color: var(--ps-ink-3); }

@media (max-width: 860px) {
  .brand-panel { display: none; }
}
</style>

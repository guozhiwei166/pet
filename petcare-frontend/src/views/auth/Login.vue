<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '../../api'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const route = useRoute()
const store = useUserStore()

const loading = ref(false)
const forgotVisible = ref(false)
const forgotLoading = ref(false)
const forgotForm = ref({ phone: '', captchaId: '', captchaCode: '', newPassword: '', securityAnswer: '' })
const forgotCaptchaImg = ref('')

const captchaImage = ref('')
const captchaId = ref('')
const captchaCode = ref('')

async function refreshCaptcha() {
  try {
    const d: any = await api.captcha()
    captchaId.value = d.captchaId
    captchaImage.value = d.image
    captchaCode.value = ''
  } catch { /* 忽略 */ }
}

async function refreshForgotCaptcha() {
  const d: any = await api.captcha()
  forgotForm.value.captchaId = d.captchaId
  forgotCaptchaImg.value = d.image
}
function openForgot() {
  forgotVisible.value = true
  refreshForgotCaptcha()
}
async function resetPassword() {
  if (!/^1\d{10}$/.test(forgotForm.value.phone)) return ElMessage.warning('请输入正确的手机号')
  if (!forgotForm.value.captchaCode) return ElMessage.warning('请输入验证码')
  if (forgotForm.value.newPassword.length < 6) return ElMessage.warning('新密码至少6位')
  forgotLoading.value = true
  try {
    await api.resetPassword(forgotForm.value)
    ElMessage.success('密码已重置，请用新密码登录')
    forgotVisible.value = false
    forgotForm.value = { phone: '', captchaId: '', captchaCode: '', newPassword: '', securityAnswer: '' }
  } catch { refreshForgotCaptcha() } finally { forgotLoading.value = false }
}
const form = ref({ username: '', password: '' })
onMounted(refreshCaptcha)

async function submit() {
  if (!form.value.username || !form.value.password) return ElMessage.warning('请输入账号密码')
  if (!captchaCode.value) return ElMessage.warning('请输入验证码')
  loading.value = true
  try {
    const data = await api.login({ ...form.value, captchaId: captchaId.value, captchaCode: captchaCode.value })
    store.setAuth(data.token, data.user)
    ElMessage.success('欢迎回来')
    // 角色分流：管理员进管理端、员工进工作台、主人进用户首页
    // redirect 参数不可越过角色边界：管理员/员工一律不允许跳转到用户端页面
    const role = data.user.role
    const home = role === 'ROLE_ADMIN' ? '/admin/dashboard'
      : role === 'ROLE_STAFF' ? '/staff/workbench' : '/'
    let target = home
    if (route.query.redirect) {
      const r = String(route.query.redirect)
      const adminArea = r.startsWith('/admin')
      const staffArea = r.startsWith('/staff')
      if (role === 'ROLE_USER') {
        target = adminArea || staffArea || r.startsWith('/login') || r.startsWith('/register') ? home : r
      } else if (role === 'ROLE_ADMIN') {
        target = adminArea || staffArea ? r : home
      } else { // ROLE_STAFF：可回员工端，也可进管理端非 ADMIN 专属页
        target = staffArea || adminArea ? r : home
      }
    }
    router.replace(target)
  } catch {
    // 登录失败（账号密码或验证码错误）：验证码一次性，刷新新的
    refreshCaptcha()
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <!-- 左侧：品牌故事 + 真实宠物照片墙 -->
    <div class="brand-panel">
      <div class="photo-stack">
        <img src="/img/hero/golden.jpg" alt="金毛犬" class="photo p1" />
        <img src="/img/hero/cat.jpg" alt="猫咪" class="photo p2" />
        <img src="/img/hero/corgi.jpg" alt="柯基犬" class="photo p3" />
      </div>

      <div class="brand-inner">
        <div class="logo-row">
          <div class="logo-chip">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="#6FA58B" aria-hidden="true">
              <ellipse cx="7" cy="8.2" rx="2.1" ry="2.7"/><ellipse cx="17" cy="8.2" rx="2.1" ry="2.7"/>
              <ellipse cx="3.6" cy="13" rx="1.9" ry="2.4"/><ellipse cx="20.4" cy="13" rx="1.9" ry="2.4"/>
              <path d="M12 11c3.6 0 6.4 2.7 6.4 5.6 0 2.1-1.7 3.4-3.6 3.4-1.1 0-1.9-.4-2.8-.4-.9 0-1.7.4-2.8.4-1.9 0-3.6-1.3-3.6-3.4C5.6 13.7 8.4 11 12 11z"/>
            </svg>
          </div>
          <span class="logo-text">猫狗宠物寄养系统</span>
        </div>
        <h1>把毛孩子托付给<br />真正懂它的人</h1>
        <p class="slogan">寄养不是关笼子，而是另一种陪伴</p>
        <ul class="points">
          <li><el-icon><CircleCheckFilled /></el-icon>宠物数字档案，性格习惯一目了然</li>
          <li><el-icon><CircleCheckFilled /></el-icon>每日照护打卡，寄养日报实时同步</li>
          <li><el-icon><CircleCheckFilled /></el-icon>异常事件闭环，三方都安心</li>
        </ul>
      </div>
      <div class="glass-stat">
        <div><b class="num">98%</b><span>主人好评率</span></div>
        <div><b class="num">1200+</b><span>毛孩子住过</span></div>
        <div><b class="num">7×24</b><span>专人看护</span></div>
      </div>
    </div>

    <!-- 右侧：登录表单 -->
    <div class="form-panel">
      <div class="form-box">
        <h2>欢迎回来</h2>
        <p class="form-sub">登录猫狗宠物寄养系统，继续照顾你的毛孩子</p>
        <el-form size="large" @submit.prevent>
          <el-form-item>
            <el-input v-model="form.username" placeholder="用户名 / 手机号" prefix-icon="User" @keyup.enter="submit" />
          </el-form-item>
          <el-form-item>
            <el-input v-model="form.password" type="password" placeholder="密码" prefix-icon="Lock" show-password @keyup.enter="submit" />
          </el-form-item>
          <el-form-item>
            <div class="captcha-row">
              <el-input v-model="captchaCode" maxlength="4" placeholder="验证码" prefix-icon="Key" @keyup.enter="submit" />
              <img v-if="captchaImage" :src="captchaImage" class="captcha-img" alt="验证码" title="点击刷新" @click="refreshCaptcha" />
            </div>
          </el-form-item>
          <el-button type="primary" size="large" class="btn" :loading="loading" @click="submit">登 录</el-button>
        </el-form>
        <div class="register-line">
          还没有账号？<el-link type="primary" @click="router.push('/register')">立即注册</el-link>
          <span style="margin: 0 6px; color:#D8CFC6">|</span>
          <el-link type="primary" :underline="false" @click="openForgot">忘记密码？</el-link>
        </div>
      </div>
    </div>
  </div>

    <!-- 忘记密码弹窗 -->
    <el-dialog v-model="forgotVisible" title="找回密码" width="420px">
      <el-form label-width="80px">
        <el-form-item label="手机号">
          <el-input v-model="forgotForm.phone" maxlength="11" placeholder="注册时的手机号" />
        </el-form-item>
        <el-form-item label="验证码">
          <div style="display:flex; gap:10px; width:100%">
            <el-input v-model="forgotForm.captchaCode" maxlength="4" placeholder="输入右侧验证码" />
            <img v-if="forgotCaptchaImg" :src="forgotCaptchaImg" style="height:32px;border-radius:6px;cursor:pointer"
                 alt="验证码" @click="refreshForgotCaptcha" />
          </div>
        </el-form-item>
        <el-form-item label="密保答案">
          <el-input v-model="forgotForm.securityAnswer" type="password" show-password
                    placeholder="已设置密保则必填（未设置可留空）" />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="forgotForm.newPassword" type="password" show-password placeholder="6-20位" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="forgotVisible = false">取消</el-button>
        <el-button type="primary" :loading="forgotLoading" @click="resetPassword">重置密码</el-button>
      </template>
    </el-dialog>
</template>

<style scoped>
.login-page { min-height: 100vh; display: flex; background: #F7F5F0; }

/* ---- 左侧品牌区：奶油底 + 错落宠物照片 ---- */
.brand-panel {
  position: relative;
  flex: 1.15; color: #2F4639;
  display: flex; flex-direction: column; overflow: hidden;
  background:
    radial-gradient(90% 60% at 12% 0%, #EAF2ED 0%, rgba(234,242,237,0) 60%),
    radial-gradient(70% 50% at 90% 100%, #F7E9DC 0%, rgba(247,233,220,0) 60%),
    linear-gradient(160deg, #FDFCFA 0%, #F2F6F2 55%, #EAF2ED 100%);
}
.photo-stack { position: absolute; inset: 0; pointer-events: none; }
.photo {
  position: absolute; object-fit: cover; border-radius: 24px;
  box-shadow: 0 24px 48px -20px rgba(47,70,57,.45);
  opacity: .94;
}
.p1 { width: 44%; aspect-ratio: 3/4; top: 4%; left: -3%; transform: rotate(-6deg); }
.p2 { width: 40%; aspect-ratio: 1/1; top: 12%; right: -4%; transform: rotate(5deg); }
.p3 { width: 34%; aspect-ratio: 4/3; bottom: 8%; left: 10%; transform: rotate(-3deg); }
@media (min-width: 1200px) { .p1 { width: 40% } .p2 { width: 36% } .p3 { width: 30% } }

.brand-inner {
  position: relative; z-index: 2; flex: 1; display: flex;
  flex-direction: column; justify-content: center; padding: 40px 8% 20px;
  backdrop-filter: blur(2px);
}
.logo-row { display: flex; align-items: center; gap: 12px; margin-bottom: 26px; }
.logo-chip { width: 40px; height: 40px; border-radius: 14px; background: #fff;
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 8px 20px -8px rgba(111,165,139,.6); }
.logo-text { font-size: 19px; font-weight: 900; letter-spacing: 2px; color: #2F4639; }
.brand-panel h1 { font-size: 40px; font-weight: 900; line-height: 1.3; letter-spacing: 1px; }
.slogan { margin-top: 14px; font-size: 16px; color: #6FA58B; font-weight: 600; }
.points { list-style: none; margin: 30px 0 0; padding: 0; display: flex; flex-direction: column; gap: 14px; }
.points li { display: flex; align-items: center; gap: 10px; font-size: 14px; color: #40584A; }
.points .el-icon { color: #6FA58B; font-size: 17px; }

/* 底部玻璃拟态统计条 */
.glass-stat {
  position: relative; z-index: 2; display: flex; gap: 24px;
  margin: 0 8% 26px; padding: 14px 22px;
  background: rgba(255,255,255,.55); border: 1px solid rgba(255,255,255,.7);
  backdrop-filter: blur(12px); -webkit-backdrop-filter: blur(12px);
  border-radius: 20px; width: fit-content;
  box-shadow: 0 8px 24px -14px rgba(47,70,57,.3);
}
.glass-stat > div { display: flex; flex-direction: column; }
.glass-stat b { font-size: 18px; font-weight: 900; color: #2F4639; }
.glass-stat span { font-size: 11px; color: #6B7F72; }

/* ---- 右侧表单区 ---- */
.form-panel { flex: 1; display: flex; align-items: center; justify-content: center; padding: 24px; }
.form-box { width: 380px; background: #fff; border-radius: 24px; padding: 44px 38px 30px;
  box-shadow: 0 1px 2px rgba(48,54,51,.04), 0 24px 60px -24px rgba(47,70,57,.25); }
.form-box h2 { font-size: 24px; font-weight: 900; }
.form-sub { color: var(--pc-ink-2); font-size: 13px; margin: 8px 0 26px; }
.btn { width: 100%; font-weight: 800; letter-spacing: 4px; }
.captcha-row { display: flex; gap: 10px; width: 100%; }
.captcha-img { height: 40px; border-radius: 8px; cursor: pointer; flex-shrink: 0; }
.register-line { margin-top: 20px; text-align: center; font-size: 13px; color: var(--pc-ink-2); }

@media (max-width: 1024px) {
  .photo { opacity: .5; }
  .brand-panel h1 { font-size: 30px; }
}
@media (max-width: 860px) { .brand-panel { display: none; } }
</style>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '../../api'

const router = useRouter()
const step = ref(0)
const loading = ref(false)

const captchaImage = ref('')
const form = reactive({
  username: '', phone: '', password: '', confirmPassword: '',
  nickname: '', captchaId: '', captchaCode: '',
  petName: '', petSpecies: 'DOG', petBreed: '',
})

onMounted(refreshCaptcha)

async function refreshCaptcha() {
  const data = await api.captcha()
  form.captchaId = data.captchaId
  captchaImage.value = data.image
}

async function next() {
  if (step.value === 0) {
    if (!/^1\d{10}$/.test(form.phone)) return ElMessage.warning('请输入正确的手机号')
    if (!form.captchaCode) return ElMessage.warning('请输入验证码')
    // 即时校验验证码正确性（不销毁，提交注册时后端会再消费）
    try {
      await api.verifyCaptcha({ captchaId: form.captchaId, captchaCode: form.captchaCode })
    } catch {
      ElMessage.error('验证码不正确，请重新输入')
      form.captchaCode = ''
      refreshCaptcha()
      return
    }
    step.value = 1
  } else if (step.value === 1) {
    if (!/^[a-zA-Z0-9_]{3,20}$/.test(form.username)) return ElMessage.warning('用户名需为3-20位字母数字下划线')
    if (form.password.length < 6) return ElMessage.warning('密码至少6位')
    if (form.password !== form.confirmPassword) return ElMessage.warning('两次密码不一致')
    step.value = 2
  }
}

async function submit() {
  loading.value = true
  try {
    await api.register({
      username: form.username, phone: form.phone, password: form.password,
      nickname: form.nickname || form.username, captchaId: form.captchaId, captchaCode: form.captchaCode,
    })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch {
    // 验证码一次性：失败后刷新验证码，保留已填信息（账号密码不丢）
    refreshCaptcha()
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="register-page">
    <div class="box">
      <h2>加入猫狗宠物寄养系统</h2>
      <p class="sub">三步完成注册，为毛孩子建一个安心的家</p>

      <el-steps :active="step" align-center finish-status="success" class="steps">
        <el-step title="验证手机" />
        <el-step title="设置账号" />
        <el-step title="完成" />
      </el-steps>

      <el-form v-if="step === 0" size="large" label-position="top">
        <el-form-item label="手机号">
          <el-input v-model="form.phone" maxlength="11" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="验证码">
          <div class="captcha-row">
            <el-input v-model="form.captchaCode" maxlength="4" placeholder="右侧验证码" />
            <img v-if="captchaImage" :src="captchaImage" class="captcha-img" alt="验证码" title="点击刷新" @click="refreshCaptcha" />
          </div>
        </el-form-item>
        <el-button type="primary" size="large" class="btn" @click="next">下一步</el-button>
      </el-form>

      <el-form v-else-if="step === 1" size="large" label-position="top">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="3-20位字母数字下划线" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password placeholder="6-20位" />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input v-model="form.confirmPassword" type="password" show-password placeholder="再输入一次" />
        </el-form-item>
        <el-button round class="back" @click="step = 0">上一步</el-button>
        <el-button type="primary" size="large" class="btn" @click="next">下一步</el-button>
      </el-form>

      <div v-else class="done">
        <el-icon :size="46" color="#6FA58B"><CircleCheckFilled /></el-icon>
        <h3>信息已就绪</h3>
        <p class="sub">点击下方按钮完成注册（昵称可在个人中心修改）</p>
        <el-input v-model="form.nickname" placeholder="昵称（选填）" size="large" style="margin: 10px 0 18px" />
        <div class="done-actions">
          <el-button round class="back" @click="step = 1">上一步</el-button>
          <el-button type="primary" size="large" round class="btn-inline" :loading="loading" @click="submit">完成注册</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.register-page { min-height: 100vh; display: flex; align-items: center; justify-content: center;
  background: linear-gradient(150deg, #EAF2ED, #F7F5F0 60%); }
.box { width: 460px; background: #fff; border-radius: 20px; padding: 34px 36px 30px;
  box-shadow: 0 1px 2px rgba(48,54,51,.04), 0 18px 40px -18px rgba(48,54,51,.2); }
.box h2 { font-size: 22px; font-weight: 900; text-align: center; }
.box > .sub { text-align: center; margin: 8px 0 20px; }
.steps { margin-bottom: 26px; }
.captcha-row { display: flex; gap: 10px; width: 100%; }
.captcha-img { height: 40px; border-radius: 8px; cursor: pointer; }
.btn { width: 100%; margin-top: 6px; font-weight: 700; }
.back { width: 100px; margin-top: 6px; }
.done-actions { display: flex; gap: 12px; justify-content: center; margin-top: 6px; }
.btn-inline { width: 180px; font-weight: 700; }
.done { text-align: center; }
</style>

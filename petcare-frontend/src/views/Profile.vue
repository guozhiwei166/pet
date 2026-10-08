<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'
import { useUserStore } from '../stores/user'

const router = useRouter()
const store = useUserStore()

const uploading = ref(false)
const nickname = ref('')
const profileForm = ref({ nickname: '', avatar: '' })
const hasSecurity = ref(false)
const myQuestion = ref('')

const stats = ref({ boarding: 0, nights: 0, coupons: 0 })

/** 个人数据概览：寄养中订单数/累计晚数/可用券数 */
async function loadStats() {
  try {
    // 概览统计：第二个参数是页码，此前误把"100 条"传成了 page=100，导致永远拿到空数组
    const ordersPage: any = await api.orders('ALL', 1, 100)
    const all = ordersPage?.records || []
    stats.value.boarding = all.filter((o: any) => ['BOARDING', 'CHECKED_IN', 'CONFIRMED'].includes(o.status)).length
    // 累计晚数只统计实际发生/进行中的寄养，排除已取消与已退款订单
    stats.value.nights = all
      .filter((o: any) => !['CANCELLED', 'REFUNDED', 'REFUNDING'].includes(o.status))
      .reduce((acc: number, o: any) => acc + (o.days || 0), 0)
    const coupons: any[] = await api.myCoupons()
    stats.value.coupons = coupons.filter(c => c.status === 'UNUSED').length
  } catch { /* 忽略 */ }
}

const pwdVisible = ref(false)
const pwdForm = ref({ oldPassword: '', newPassword: '', confirmPassword: '', securityAnswer: '' })
const savingPwd = ref(false)

// 密保弹窗
const secVisible = ref(false)
const secForm = ref({ question: '', answer: '' })
const savingSec = ref(false)
const SEC_QUESTIONS = [
  '你的小学名称是什么？',
  '你的家乡在哪里？',
  '你最喜欢的宠物名字？',
  '你母亲的姓氏是什么？',
  '你的第一辆车是什么品牌？',
]

/** 登录角色：决定个人中心可见入口 */
const role = computed(() => store.userInfo?.role || 'ROLE_USER')

/** 主人入口（用户端功能，仅 ROLE_USER 显示） */
const userMenus = [
  { path: '/pets', icon: '🐾', label: '我的宠物' },
  { path: '/orders', icon: '📋', label: '我的订单' },
  { path: '/service-bookings', icon: '💈', label: '服务预约' },
  { path: '/coupons', icon: '🎟️', label: '我的优惠券' },
  { path: '/stories', icon: '💬', label: '我的故事' },
  { path: '/notifications', icon: '🔔', label: '消息通知' },
]
/** 员工入口 */
const staffMenus = [
  { path: '/staff/workbench', icon: '🧑‍💼', label: '今日工作台' },
  { path: '/staff/pets', icon: '🐾', label: '宠物档案' },
  { path: '/staff/exceptions', icon: '🚨', label: '异常上报' },
  { path: '/staff/bookings', icon: '💈', label: '服务预约' },
]
/** 管理员入口（工作台快捷） */
const adminMenus = [
  { path: '/admin/dashboard', icon: '📊', label: '运营驾驶舱' },
  { path: '/admin/orders', icon: '📋', label: '订单管理' },
  { path: '/admin/pets', icon: '🐾', label: '宠物档案' },
  { path: '/admin/room-map', icon: '🗺️', label: '房间地图' },
  { path: '/admin/room-types', icon: '🏠', label: '房型管理' },
  { path: '/admin/services', icon: '💈', label: '服务管理' },
  { path: '/admin/marketing', icon: '🎟️', label: '营销中心' },
  { path: '/admin/users', icon: '👥', label: '用户与员工' },
  { path: '/admin/logs', icon: '📜', label: '操作日志' },
]

const menus = computed(() => {
  if (role.value === 'ROLE_ADMIN') return adminMenus
  if (role.value === 'ROLE_STAFF') return staffMenus
  return userMenus
})
const menuTitle = computed(() => {
  if (role.value === 'ROLE_ADMIN') return '管理快捷入口'
  if (role.value === 'ROLE_STAFF') return '工作快捷入口'
  return '我的服务'
})

async function loadMe() {
  const u = await api.me()
  store.setAuth(store.token, u)
  profileForm.value = { nickname: u.nickname, avatar: u.avatar || '' }
  nickname.value = u.nickname
  hasSecurity.value = !!u.securityQuestion
  myQuestion.value = u.securityQuestion || ''
}
onMounted(() => { loadMe(); loadStats() })

async function onAvatarPick(file: File) {
  if (!file.type.startsWith('image/')) return ElMessage.warning('请选择图片文件')
  uploading.value = true
  try {
    const fd = new FormData()
    fd.append('file', file)
    fd.append('bizType', 'AVATAR')
    const up = await api.upload(fd)
    await api.updateProfile({ nickname: profileForm.value.nickname, avatar: up.url })
    ElMessage.success('头像已更新 🎉')
    await loadMe()
  } catch (e: any) {
    ElMessage.error(e?.message || '头像上传失败')
  } finally {
    uploading.value = false
  }
}

async function saveProfile() {
  if (!profileForm.value.nickname.trim()) return ElMessage.warning('昵称不能为空')
  await api.updateProfile({ nickname: profileForm.value.nickname.trim(), avatar: profileForm.value.avatar })
  ElMessage.success('资料已保存')
  await loadMe()
}

async function changePwd() {
  if (pwdForm.value.newPassword.length < 6) return ElMessage.warning('新密码至少6位')
  if (pwdForm.value.newPassword !== pwdForm.value.confirmPassword) return ElMessage.warning('两次输入的新密码不一致')
  // 已设密保则必须填写密保答案
  if (hasSecurity.value && !pwdForm.value.securityAnswer) return ElMessage.warning('已设置密保问题，请输入密保答案')
  savingPwd.value = true
  try {
    await api.changePassword({
      oldPassword: pwdForm.value.oldPassword,
      newPassword: pwdForm.value.newPassword,
      securityAnswer: pwdForm.value.securityAnswer || undefined,
    })
    ElMessage.success('密码已修改，请重新登录')
    pwdVisible.value = false
    pwdForm.value = { oldPassword: '', newPassword: '', confirmPassword: '', securityAnswer: '' }
    setTimeout(() => { store.logout(); router.push('/login') }, 800)
  } finally {
    savingPwd.value = false
  }
}

async function openSec() {
  secForm.value = { question: myQuestion.value || SEC_QUESTIONS[0], answer: '' }
  secVisible.value = true
}
async function saveSec() {
  if (!secForm.value.question) return ElMessage.warning('请选择密保问题')
  if (!secForm.value.answer.trim()) return ElMessage.warning('请输入密保答案')
  if (secForm.value.answer.trim().length < 2) return ElMessage.warning('密保答案至少2个字符')
  savingSec.value = true
  try {
    await api.saveSecurityQuestion({ question: secForm.value.question, answer: secForm.value.answer.trim() })
    ElMessage.success(hasSecurity.value ? '密保问题已更新' : '密保问题已设置')
    secVisible.value = false
    await loadMe()
  } finally {
    savingSec.value = false
  }
}

async function logout() {
  try {
    await ElMessageBox.confirm('确定退出登录吗？', '退出', { type: 'warning' })
    try { await api.logout() } catch { /* 忽略 */ }
    store.logout()
    router.push('/login')
  } catch { /* 取消 */ }
}

const avatarShow = () => profileForm.value.avatar || (store.userInfo?.avatar || '')
</script>

<template>
  <div class="profile-page">
    <!-- 个人信息卡 -->
    <div class="profile-hero pc-card">
      <div class="avatar-wrap">
        <div class="big-avatar">
          <img v-if="avatarShow()" :src="avatarShow()" alt="头像" />
          <span v-else>{{ nickname.slice(0, 1) || '🐾' }}</span>
        </div>
        <el-upload :show-file-list="false" accept="image/*" :http-request="(opt: any) => onAvatarPick(opt.file)"
                   :disabled="uploading">
          <el-button size="small" round class="change-avatar-btn" :loading="uploading">📷 更换头像</el-button>
        </el-upload>
      </div>
      <div class="profile-main">
        <h2>{{ nickname }}</h2>
        <div class="profile-meta">
          <span class="sub">{{ store.userInfo?.roleName }} · {{ store.userInfo?.phone }}</span>
        </div>
        <div class="profile-input">
          <el-input v-model="profileForm.nickname" maxlength="20" placeholder="昵称" style="width: 220px" />
          <el-button type="primary" round size="small" @click="saveProfile">保存昵称</el-button>
        </div>
      </div>
      <div class="profile-stats">
        <div><b class="num">{{ stats.boarding }}</b><span>寄养中</span></div>
        <div><b class="num">{{ stats.nights }}</b><span>累计晚数</span></div>
        <div><b class="num">{{ stats.coupons }}</b><span>可用券</span></div>
      </div>
    </div>

    <!-- 功能入口（按角色显示，不跨端跳转） -->
    <div class="menu-card pc-card">
      <div class="menu-title">{{ menuTitle }}</div>
      <div class="menu-grid">
        <div v-for="m in menus" :key="m.path" class="menu-item" @click="router.push(m.path)">
          <span class="mi-icon">{{ m.icon }}</span>{{ m.label }}
        </div>
      </div>
    </div>

    <!-- 账号设置 -->
    <div class="menu-card pc-card">
      <div class="menu-title">账号设置</div>
      <div class="menu-item" @click="openSec">
        <span class="mi-icon m7">❓</span>
        <div class="mi-main">
          密保问题
          <span class="sub">{{ hasSecurity ? '已设置：' + myQuestion : '未设置（找回密码需要）' }}</span>
        </div>
        <span class="arrow">›</span>
      </div>
      <div class="menu-item" @click="pwdVisible = true">
        <span class="mi-icon m7">🔒</span>
        <div class="mi-main">修改密码<span class="sub" v-if="hasSecurity">需验证密保答案</span></div>
        <span class="arrow">›</span>
      </div>
      <div class="menu-item logout-item" @click="logout"><span class="mi-icon m8">🚪</span>退出登录</div>
    </div>

    <!-- 改密弹窗 -->
    <el-dialog v-model="pwdVisible" title="修改密码" width="440px">
      <el-form label-width="100px">
        <el-form-item label="旧密码"><el-input v-model="pwdForm.oldPassword" type="password" show-password /></el-form-item>
        <el-form-item v-if="hasSecurity" label="密保答案">
          <el-input v-model="pwdForm.securityAnswer" placeholder="回答：{{ myQuestion }}" show-password />
          <div class="form-tip">修改密码需验证密保答案（{{ myQuestion }}）</div>
        </el-form-item>
        <el-form-item label="新密码"><el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="6-20位" /></el-form-item>
        <el-form-item label="确认新密码"><el-input v-model="pwdForm.confirmPassword" type="password" show-password /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingPwd" @click="changePwd">确认修改</el-button>
      </template>
    </el-dialog>

    <!-- 密保弹窗 -->
    <el-dialog v-model="secVisible" :title="hasSecurity ? '修改密保问题' : '设置密保问题'" width="440px">
      <el-form label-width="100px">
        <el-form-item label="密保问题">
          <el-select v-model="secForm.question" style="width: 100%" placeholder="选择密保问题">
            <el-option v-for="q in SEC_QUESTIONS" :key="q" :label="q" :value="q" />
          </el-select>
        </el-form-item>
        <el-form-item label="密保答案">
          <el-input v-model="secForm.answer" placeholder="请牢记，用于找回/修改密码" show-password />
        </el-form-item>
      </el-form>
      <div class="sec-tip">💡 设置密保后，修改密码与手机号找回密码都需要验证密保答案，账号更安全。</div>
      <template #footer>
        <el-button @click="secVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingSec" @click="saveSec">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.profile-page { display: flex; flex-direction: column; gap: 14px; max-width: 860px; margin: 0 auto; }
.profile-hero { display: flex; align-items: center; gap: 24px; background: linear-gradient(120deg, #EAF2ED, #fff 60%); }
.avatar-wrap { display: flex; flex-direction: column; align-items: center; gap: 10px; flex-shrink: 0; }
.big-avatar { width: 92px; height: 92px; border-radius: 50%; overflow: hidden; background: #fff;
  display: flex; align-items: center; justify-content: center; font-size: 40px; color: var(--pc-primary-deep);
  border: 3px solid #fff; box-shadow: 0 10px 24px -8px rgba(47,70,57,.3); }
.big-avatar img { width: 100%; height: 100%; object-fit: cover; }
.profile-main { flex: 1; min-width: 0; }
.profile-main h2 { font-size: 22px; font-weight: 900; margin: 0 0 4px; }
.profile-input { display: flex; gap: 8px; margin-top: 14px; }
.profile-stats { display: flex; gap: 22px; flex-shrink: 0; }
.profile-stats > div { display: flex; flex-direction: column; align-items: center; }
.profile-stats b { font-size: 18px; font-weight: 900; color: var(--pc-primary-deep); }
.profile-stats span { font-size: 11px; color: var(--pc-ink-2); }

.menu-card { padding: 10px 8px; }
.menu-title { font-size: 13px; font-weight: 800; color: var(--pc-ink-2); padding: 4px 12px 8px; }
.menu-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 2px; }
.menu-item { display: flex; align-items: center; gap: 12px; padding: 12px 14px; font-size: 14.5px; font-weight: 600;
  cursor: pointer; border-radius: 10px; }
.menu-item:hover { background: var(--pc-primary-soft); }
.mi-icon { width: 34px; height: 34px; border-radius: 10px; display: flex; align-items: center;
  justify-content: center; font-size: 17px; background: #F0EDE8; flex-shrink: 0; }
.mi-icon.m7 { background: #EAF2ED; } .mi-icon.m8 { background: #FDECEC; }
.mi-main { display: flex; flex-direction: column; gap: 2px; line-height: 1.3; }
.mi-main .sub { font-weight: 400; font-size: 12px; }
.arrow { margin-left: auto; color: #C4BCB2; font-size: 18px; }
.logout-item { color: #D96C61; }
.form-tip { font-size: 12px; color: #C97B2C; line-height: 1.5; }
.sec-tip { background: #FDF6EC; border-radius: 10px; padding: 10px 14px; font-size: 12.5px; color: #A97B46; }
</style>

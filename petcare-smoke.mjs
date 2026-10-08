
// --- 登录 helper：自动拉图形验证码（从 redis 读码）后登录 ---
async function captchaLogin(username, password) {
  const cap = await (await fetch(BASE + '/auth/captcha')).json()
  const cid = cap.data.captchaId
  let raw = ''
  try { raw = execSync('"' + REDIS_CLI + '" GET captcha:' + cid).toString().trim() } catch { }
  const code = raw.replace(/^"|"$/g, '')
  const r = await api('/auth/login', { method: 'POST', body: { username, password, captchaId: cid, captchaCode: code } })
  return r
}

/**
 * 猫狗宠物寄养系统 Phase 1 冒烟测试：node petcare-smoke.mjs
 * 覆盖：验证码 → 注册 → 登录 → me → 改资料 → 改密码 → 重登录 → 旧token失效 → 上传头像 → 登出黑名单 → 权限校验
 * 说明：验证码文本通过 redis-cli 读取（仅测试环境使用）
 */
import { execSync } from 'node:child_process'

const BASE = 'http://localhost:8082/api'
const REDIS_CLI = 'C:/tools/redis/redis-cli.exe'

function api(path, { method = 'GET', token, body, raw } = {}) {
  return fetch(BASE + path, {
    method,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: 'Bearer ' + token } : {}),
    },
    body: body === undefined ? undefined : (raw ? body : JSON.stringify(body)),
  }).then(async resp => ({ status: resp.status, body: await resp.json() }))
}

function readCaptcha(captchaId) {
  // RedisTemplate(GenericJackson2Json) 存储的字符串值形如 "AB3D"，去引号即验证码
  const raw = execSync(`"${REDIS_CLI}" GET captcha:${captchaId}`).toString().trim()
  return raw.replace(/^"|"$/g, '')
}

async function main() {
  // 0. 演示账号登录（DataInitializer）
  const admin = await captchaLogin('admin', 'admin123')
  if (admin.body.code !== 200) throw new Error('admin 登录失败: ' + admin.body.message)
  console.log('✓ admin 登录，角色:', admin.body.data.user.role)

  // 1. 验证码 + 注册（随机用户名，保证脚本幂等可重复运行）
  const uname = 'test' + String(Date.now()).slice(-8)
  const cap = await api('/auth/captcha')
  const captchaId = cap.body.data.captchaId
  const captchaCode = readCaptcha(captchaId)
  const reg = await api('/auth/register', {
    method: 'POST',
    body: { username: uname, phone: '137' + String(Date.now()).slice(-8), password: 'test123', nickname: '测试用户', captchaId, captchaCode },
  })
  if (reg.body.code !== 200) throw new Error('注册失败: ' + reg.body.message)
  console.log('✓ 验证码 + 注册成功:', reg.body.data.username)

  // 2. 错误验证码应被拒绝
  const cap2 = await api('/auth/captcha')
  const bad = await api('/auth/register', {
    method: 'POST',
    body: { username: 'testuser2', phone: '13700002222', password: 'test123', nickname: '测试', captchaId: cap2.body.data.captchaId, captchaCode: 'XXXX' },
  })
  if (bad.body.code === 200) throw new Error('错误验证码竟能注册')
  console.log('✓ 错误验证码被拦截:', bad.body.message)

  // 3. 登录 + me
  const login = await captchaLogin(uname, 'test123')
  const token = login.body.data.token
  console.log('✓ 登录成功，角色:', login.body.data.user.role)
  const me = await api('/auth/me', { token })
  if (me.body.data.username !== uname) throw new Error('me 接口异常')
  console.log('✓ /auth/me:', me.body.data.nickname)

  // 4. 改资料 + 改密码
  const prof = await api('/auth/profile', { method: 'PUT', token, body: { nickname: '测试用户改', avatar: '/upload/2026/09/a.jpg' } })
  if (prof.body.data.nickname !== '测试用户改') throw new Error('资料修改失败')
  console.log('✓ 资料修改:', prof.body.data.nickname)
  await api('/auth/password', { method: 'PUT', token, body: { oldPassword: 'test123', newPassword: 'test456' } })
  const relogin = await captchaLogin(uname, 'test456')
  if (relogin.body.code !== 200) throw new Error('改密后重登录失败: [' + relogin.body.code + '] ' + relogin.body.message)
  console.log('✓ 修改密码 + 重登录成功')

  // 5. 上传头像（1x1 PNG）
  const png = Buffer.from('iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==', 'base64')
  const form = new FormData()
  form.append('file', new Blob([png], { type: 'image/png' }), 'avatar.png')
  form.append('bizType', 'AVATAR')
  const upload = await fetch(BASE + '/files/upload', {
    method: 'POST', headers: { Authorization: 'Bearer ' + relogin.body.data.token }, body: form,
  }).then(r => r.json())
  if (upload.code !== 200) throw new Error('上传失败: ' + upload.message)
  const fileResp = await fetch('http://localhost:8082' + upload.data.url)
  if (fileResp.status !== 200) throw new Error('上传文件无法访问')
  console.log('✓ 头像上传并可访问:', upload.data.url)

  // 6. 登出黑名单：单独开会话验证，不影响后续流程使用的 token
  const tmpLogin = await captchaLogin(uname, 'test456')
  const tmpToken = tmpLogin.body.data.token
  await api('/auth/logout', { method: 'POST', token: tmpToken })
  const after = await api('/auth/me', { token: tmpToken })
  if (after.body.code === 200) throw new Error('登出后 token 仍可用')
  console.log('✓ 登出后旧 token 被黑名单拦截')

  // 7. 权限校验：普通用户访问管理接口应 403
  const userLogin = await captchaLogin('user', 'user123')
  const denied = await api('/admin/dashboard', { token: userLogin.body.data.token })
  if (denied.body.code !== 403) throw new Error('越权访问未被拦截: ' + denied.body.code)
  console.log('✓ 权限校验：普通用户访问管理接口被拒（403）')

  // 8. admin 可访问管理接口
  const adminStats = await api('/admin/dashboard', { token: admin.body.data.token })
  if (adminStats.body.code !== 404 && adminStats.body.code !== 200) throw new Error('admin 接口异常')
  console.log('✓ admin 访问管理接口未被拒绝（dashboard 属 Phase 8，404 属预期）')

  // 9. 宠物体系（Phase 2）；昵称在同一主人下唯一，故按时间戳取唯一名
  const petToken = relogin.body.data.token
  const petName = '豆豆' + String(Date.now()).slice(-4)
  const petResp = await api('/pets', {
    method: 'POST', token: petToken,
    body: { name: petName, species: 'DOG', breed: '金毛寻回犬', gender: 'M', birthDate: '2023-05-01',
            weight: 28.5, sterilized: 1, furColor: '金黄色', temperament: '温顺亲人', tagIds: [1, 2, 4] },
  })
  const pet = petResp.body.data
  if (petResp.body.code !== 200 || pet.name !== petName || pet.age === undefined) {
    throw new Error('创建宠物异常: ' + petResp.body.message)
  }
  console.log(`✓ 创建宠物档案: ${pet.name}（${pet.age}岁）标签=${pet.tags?.join('/')}`)

  // 9.1 同一主人下重名宠物必须被拒绝
  const dupResp = await api('/pets', {
    method: 'POST', token: petToken,
    body: { name: petName, species: 'DOG', breed: '金毛寻回犬', gender: 'M', birthDate: '2023-05-01', weight: 20 },
  })
  if (dupResp.body.code === 200) throw new Error('重名宠物竟然创建成功')
  console.log('✓ 重名宠物被拦截:', dupResp.body.message)

  const petDetail = (await api(`/pets/${pet.id}`, { token: petToken })).body.data
  if (!petDetail.tags.includes('亲人')) throw new Error('标签未生效')
  console.log('✓ 档案详情含性格标签:', petDetail.tags.join('、'))

  await api(`/pets/${pet.id}/vaccines`, {
    method: 'POST', token: petToken,
    body: { vaccineName: '犬联苗年度', inoculationDate: '2026-05-12', expireDate: '2027-05-12', org: '安安宠物医院' },
  })
  const vac = (await api(`/pets/${pet.id}/vaccines`, { token: petToken })).body.data
  if (!vac.length || vac[0].status !== '有效') throw new Error('疫苗记录异常')
  console.log('✓ 疫苗记录状态计算:', vac[0].vaccineName, '=', vac[0].status)

  const growth = (await api(`/pets/${pet.id}/growth`, { token: petToken })).body.data
  if (growth.totalBoardings !== 0) throw new Error('成长档案异常')
  console.log(`✓ 成长档案: 累计寄养${growth.totalBoardings}次，体重曲线${growth.weightCurve.length}个点`)

  // 员工可看全店宠物；用户越权改他人宠物应 403
  const staffLogin = await captchaLogin('staff', 'staff123')
  const staffPets = staffLogin.body.data ? (await api('/pets', { token: staffLogin.body.data.token })).body.data : []
  if (staffPets.length < 1) throw new Error('员工看不到宠物列表')
  console.log(`✓ 员工查看全店宠物: ${staffPets.length} 只`)

  const stranger = await captchaLogin('user', 'user123')
  const foreign = await fetch(BASE + `/pets/${pet.id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json', Authorization: 'Bearer ' + stranger.body.data.token },
    body: JSON.stringify({ name: '黑客改名', species: 'CAT' }),
  }).then(r => r.json())
  if (foreign.code !== 403) throw new Error('越权修改他人宠物未被拦截: ' + foreign.code)
  console.log('✓ 越权修改他人宠物被拦截（403）')

  console.log('\n============================')
  console.log('Phase 1+2 冒烟测试全部通过 ✅')
}

main().catch(e => {
  console.error('\n❌ 冒烟测试失败:', e.message)
  process.exit(1)
})

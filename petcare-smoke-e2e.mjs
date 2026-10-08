
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
 * 猫狗宠物寄养系统 完整演示链路 e2e（规格书第 93 节）
 * 运行：node petcare-smoke-e2e.mjs
 * 链路：注册 → 建宠物 → 查档案 → 预约 → 支付 → 管理员确认 → 员工入住 → 照护打卡 → 日报
 *      → 异常上报 → 处理 → 通知 → 关闭 → 离店 → 评价 → 成长档案 → 权限抽查
 */
const BASE = 'http://localhost:8082/api'
const REDIS_CLI = 'C:/tools/redis/redis-cli.exe'

function api(path, { method = 'GET', token, body } = {}) {
  return fetch(BASE + path, {
    method,
    headers: { 'Content-Type': 'application/json', ...(token ? { Authorization: 'Bearer ' + token } : {}) },
    body: body === undefined ? undefined : JSON.stringify(body),
  }).then(async r => {
    const body = await r.json()
    if (body.code !== 200 && body.code !== 404) throw new Error(`${path} -> [${body.code}] ${body.message}`)
    return body.data
  })
}

const d = n => {
  const t = new Date(Date.now() + n * 86400000)
  const p = x => String(x).padStart(2, '0')
  return `${t.getFullYear()}-${p(t.getMonth() + 1)}-${p(t.getDate())}`
}

function readCaptcha(captchaId) {
  const raw = execSync(`"${REDIS_CLI}" GET captcha:${captchaId}`).toString().trim()
  return raw.replace(/^"|"$/g, '')
}
import { execSync } from 'node:child_process'

async function main() {
  const admin = await captchaLogin('admin', 'admin123')
  const staff = await captchaLogin('staff', 'staff123')

  // 1. 用户注册（图形验证码 → Redis 读取）
  const cap = await api('/auth/captcha')
  const uname = 'demo' + String(Date.now()).slice(-8)
  await api('/auth/register', { method: 'POST', body: {
    username: uname, phone: '136' + String(Date.now()).slice(-8), password: 'demo123',
    nickname: '演示家长', captchaId: cap.captchaId, captchaCode: readCaptcha(cap.captchaId) } })
  console.log('✓ 用户注册:', uname)
  // 登录同样需要图形验证码（注册时的那张已被消费），复用 captchaLogin 重新取一张
  const user = await captchaLogin(uname, 'demo123')

  // 2. 创建宠物 + 档案完善
  const pet = await api('/pets', { method: 'POST', token: user.token, body: {
    name: '可乐', species: 'DOG', breed: '拉布拉多', gender: 'M', birthDate: '2022-08-15',
    weight: 26.0, sterilized: 1, furColor: '奶白色', temperament: '温顺亲人',
    notes: '雷雨天气容易焦虑，喂食时不要突然靠近', tagIds: [1, 2, 4] } })
  await api(`/pets/${pet.id}/vaccines`, { method: 'POST', token: user.token, body: {
    vaccineName: '犬四联', inoculationDate: '2026-06-01', expireDate: '2027-06-01', org: '安安宠物医院' } })
  const profile = await api(`/pets/${pet.id}`, { token: user.token })
  const vacs = await api(`/pets/${pet.id}/vaccines`, { token: user.token })
  if (!profile.tags.includes('亲人') || vacs.length !== 1) throw new Error('档案数据异常')
  console.log('✓ 宠物档案:', profile.name, '标签', profile.tags.join('/'), '疫苗', vacs.length, '条')

  // 3. 预约寄养（查库存 → 下单 → 模拟支付）
  const start = d(0), end = d(3)
  const avail = await api('/boarding/availability', { method: 'POST', token: user.token,
    body: { startDate: start, endDate: end } })
  const lux = avail.find(a => a.roomType.name === '豪华房')
  const services = await api('/services', { token: user.token })
  const order = await api('/boarding/orders', { method: 'POST', token: user.token, body: {
    petId: pet.id, roomTypeId: lux.roomType.id, startDate: start, endDate: end,
    services: [{ serviceId: services[0].id, quantity: 1 }, { serviceId: services[3].id, quantity: 1 }],
    contactName: '演示家长', contactPhone: '13800000001', remark: '怕打雷需要安抚' } })
  console.log(`✓ 预约下单: ${order.orderNo} 豪华房 ${order.days} 晚 合计￥${order.totalAmount}`)
  await api(`/orders/${order.id}/pay`, { method: 'POST', token: user.token })
  console.log('✓ 模拟支付成功')

  // 4. 管理员分配房间（手动选房 + 生成照护任务）
  const cands = await api(`/orders/${order.id}/room-candidates`, { token: admin.token })
  const roomPick = cands.rooms.find(r => r.assignable && !r.needConfirm) || cands.rooms.find(r => r.assignable)
  if (!roomPick) throw new Error('没有可分配的房间')
  await api(`/orders/${order.id}/assign-room`, { method: 'POST', token: admin.token, body: { roomId: roomPick.id } })
  const confirmed = await api(`/orders/${order.id}`, { token: user.token })
  console.log('✓ 管理员手动分配房间:', confirmed.room.roomNo)

  // 5. 员工办理入住 + 完成照护任务（喂食/健康观察）
  await api(`/orders/${order.id}/check-in`, { method: 'POST', token: staff.token })
  const dayTasks = (await api(`/care/tasks?date=${start}`, { token: staff.token }))
    .filter(t => t.orderId === order.id)
  for (const t of dayTasks.filter(t => t.taskType === 'FEEDING')) {
    await api(`/care/tasks/${t.id}/complete`, { method: 'POST', token: staff.token, body: {
      food: '拉布拉多专用粮', amount: '180g', appetite: '正常' } })
  }
  for (const t of dayTasks.filter(t => t.taskType === 'WALK')) {
    await api(`/care/tasks/${t.id}/complete`, { method: 'POST', token: staff.token, body: {
      durationMinutes: 35, mood: '很活跃' } })
  }
  for (const t of dayTasks.filter(t => t.taskType === 'HEALTH_CHECK')) {
    await api(`/care/tasks/${t.id}/complete`, { method: 'POST', token: staff.token, body: {
      weight: 26.2, temperature: 38.4, mentalState: '很活跃', appetite: '正常', water: '正常', poop: '正常' } })
  }
  const reports = await api(`/orders/${order.id}/daily-reports`, { token: user.token })
  const report = reports.find(r => r.reportDate === start)
  if (!report || !report.mealsSummary) throw new Error('日报未生成')
  console.log(`✓ 入住 + 打卡 + 日报: ${report.mealsSummary} / 活动${report.activityMinutes}分钟`)

  // 6. 异常事件闭环
  await api('/exceptions', { method: 'POST', token: staff.token, body: {
    petId: pet.id, orderId: order.id, type: 'APPETITE', level: 'NORMAL',
    description: '晚上食欲略少，已加餐观察' } })
  const pending = (await api('/exceptions?status=PENDING', { token: admin.token }))[0]
  await api(`/exceptions/${pending.id}/process`, { method: 'POST', token: admin.token, body: { note: '已加餐' } })
  await api(`/exceptions/${pending.id}/resolve`, { method: 'POST', token: admin.token, body: { note: '食欲恢复' } })
  await api(`/exceptions/${pending.id}/close`, { method: 'POST', token: admin.token })
  console.log('✓ 异常事件上报并处理完毕')

  // 7. 离店（体重回写）→ 评价 → 成长档案
  const checkoutTasks = (await api(`/care/tasks?date=${end}`, { token: staff.token }))
    .filter(t => t.orderId === order.id && t.taskType === 'CHECKOUT_CHECK')
  if (checkoutTasks[0]) await api(`/care/tasks/${checkoutTasks[0].id}/complete`, {
    method: 'POST', token: staff.token, body: { weight: 26.4, mentalState: '正常', appetite: '正常', water: '正常', poop: '正常' } })
  await api(`/orders/${order.id}/start-boarding`, { method: 'POST', token: staff.token })
  await api(`/orders/${order.id}/check-out`, { method: 'POST', token: staff.token, body: { weightOut: 26.4 } })
  // 收尾：清洁完成，房间回到空闲（避免演示库累积"清洁中"房间）
  const finished = await api(`/orders/${order.id}`, { token: staff.token })
  if (finished.room) {
    await api(`/rooms/${finished.room.id}/status`, { method: 'PUT', token: staff.token, body: { status: 'AVAILABLE' } })
  }
  await api(`/orders/${order.id}/review`, { method: 'POST', token: user.token, body: {
    ratingOverall: 5, ratingEnv: 5, ratingService: 5, ratingStaff: 5, ratingPetStatus: 5,
    content: '每天看日报特别安心，可乐回来状态很好', tags: '反馈及时,宠物状态好' } })
  await api(`/reviews/${(await api(`/pets/${pet.id}/reviews`, { token: user.token }))[0].id}/reply`,
    { method: 'POST', token: admin.token, body: { reply: '谢谢信任，欢迎可乐随时回来' } })
  const growth = await api(`/pets/${pet.id}/growth`, { token: user.token })
  console.log(`✓ 离店 + 五星评价 + 成长档案: 寄养${growth.totalBoardings}次/${growth.totalDays}天，体重曲线${growth.weightCurve.length}点`)

  // 8. 权限抽查
  let denied = false
  try { await api('/admin/users', { token: user.token }) } catch (e) { denied = String(e.message).includes('403') }
  if (!denied) throw new Error('越权访问未被拦截')
  console.log('✓ 权限抽查：普通用户访问管理接口被拒')

  console.log('\n============================')
  console.log('完整演示链路 e2e 全部通过 ✅（规格书第 93 节）')
}

main().catch(e => {
  console.error('\n❌ e2e 失败:', e.message)
  process.exit(1)
})

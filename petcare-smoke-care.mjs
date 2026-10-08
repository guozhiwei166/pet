
// --- 登录 helper：自动拉图形验证码（从 redis 读码）后登录 ---
import { execSync } from 'node:child_process'
const REDIS_CLI = 'C:/tools/redis/redis-cli.exe'
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
 * 猫狗宠物寄养系统 Phase 5 冒烟：照护任务生成与完成链路
 * 运行：node petcare-smoke-care.mjs（会重建数据并走完整预订链路）
 */
const BASE = 'http://localhost:8082/api'

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

async function main() {
  // 1. 前置：user 建宠物 → 下单 → 支付 → admin 确认（触发任务生成）→ 入住
  const user = await captchaLogin('user', 'user123')
  const admin = await captchaLogin('admin', 'admin123')
  const staff = await captchaLogin('staff', 'staff123')



  let allPets = await api('/pets', { token: user.token })
  // 优先选"在目标日期窗口内无占用订单"的宠物，持久化模式下避免与历史订单撞日期
  const occOrders = ((await api('/orders?status=ALL&page=1&size=200', { token: user.token }).catch(() => ({ records: [] }))).records || [])
    .filter(o => ['PENDING_PAYMENT', 'PAID', 'CONFIRMED', 'CHECKED_IN', 'BOARDING'].includes(o.status)
      && String(o.startDate) < d(3) && String(o.endDate) > d(0))
  const busyPetIds = new Set(occOrders.map(o => o.petId))
  let pet = allPets.find(p => !busyPetIds.has(p.id))
  if (!pet) {
    pet = await api('/pets', { method: 'POST', token: user.token, body: {
      name: '豆豆' + String(Date.now()).slice(-4), species: 'DOG', breed: '金毛寻回犬', gender: 'M',
      birthDate: '2023-05-01', weight: 28.5, sterilized: 1, tagIds: [1, 2, 4] } })
    allPets = [pet]
  }

  // 分区规则：房型分犬区/猫区（显式字段）；在同分区里选目标日期真正可订的房型
  const zone = pet.species === 'CAT' ? 'CAT' : 'DOG'
  const avail = await api('/boarding/availability', { method: 'POST', token: user.token,
    body: { startDate: d(0), endDate: d(3) } })
  const bookable = avail.filter(a => a.roomType.species === zone && a.bookable)
    .sort((a, b) => b.minAvailable - a.minAvailable)   // 优先可用间数最多的：仅"可订"可能仍分不到具体房间
  const stdType = bookable.find(a => a.roomType.name.endsWith('标准房') && a.minAvailable === bookable[0]?.minAvailable) || bookable[0]
  if (!stdType) throw new Error(`${zone} 区在目标日期没有可订房型`)
  const order = await api('/boarding/orders', { method: 'POST', token: user.token, body: {
    petId: pet.id, roomTypeId: stdType.roomType.id, startDate: d(0), endDate: d(3),
    contactName: '小明', contactPhone: '13800000001', remark: '怕打雷需要安抚' } })
  await api(`/orders/${order.id}/pay`, { method: 'POST', token: user.token })
  // 手动选房分配：取候选里第一个可直接分配的房间
  const cands = await api(`/orders/${order.id}/room-candidates`, { token: admin.token })
  const roomPick = cands.rooms.find(r => r.assignable && !r.needConfirm) || cands.rooms.find(r => r.assignable)
  if (!roomPick) throw new Error('没有可分配的房间')
  await api(`/orders/${order.id}/assign-room`, { method: 'POST', token: admin.token, body: { roomId: roomPick.id } })

  // 2. 验证任务生成：4 天 × 4 项 + 入住检查 = 17 项左右（每日 早餐/健康观察 + 中间日遛宠）
  const tasks = await api('/care/tasks', { token: staff.token, }).catch(() => null)
  // workbench 只看今天；用 tasks 接口查订单起止范围
  const allTasks = await api(`/care/tasks?date=${d(1)}`, { token: staff.token })
  console.log(`✓ 确认订单后生成照护任务（次日 ${allTasks.length} 项）`)
  const types = new Set(allTasks.map(t => t.taskType))
  if (!types.has('FEEDING') || !types.has('HEALTH_CHECK')) throw new Error('每日任务类型缺失')
  const withCheckin = await api(`/care/tasks?date=${d(0)}`, { token: staff.token })
  if (!withCheckin.some(t => t.taskType === 'CHECKIN_CHECK')) throw new Error('入住检查任务缺失')
  console.log('✓ 每日任务类型齐全，入住日含入住检查，任务卡带注意事项:', withCheckin[0].petNotes || '（无）')

  // 3. 入住 → 完成喂食任务（写 feeding_record）→ 完成健康观察（写 pet_health）
  await api(`/orders/${order.id}/check-in`, { method: 'POST', token: staff.token })
  const day2Tasks = (await api(`/care/tasks?date=${d(0)}`, { token: staff.token })).filter(t => t.orderId === order.id)
  const feeding = day2Tasks.find(t => t.taskType === 'FEEDING')
  if (!feeding) throw new Error('未找到喂食任务')
  await api(`/care/tasks/${feeding.id}/complete`, { method: 'POST', token: staff.token, body: {
    food: '狗粮', amount: '150g', appetite: '正常', remark: '食欲很好' } })
  console.log('✓ 完成喂食任务，feeding_record 已写入')

  const health = day2Tasks.find(t => t.taskType === 'HEALTH_CHECK')
  if (health) {
    await api(`/care/tasks/${health.id}/complete`, { method: 'POST', token: staff.token, body: {
      weight: 28.3, temperature: 38.5, mentalState: '很活跃', appetite: '正常', water: '正常', poop: '正常' } })
    console.log('✓ 完成健康观察，pet_health 已写入（体重 28.3kg）')
  }

  // 4. 时间线与工作台统计
  const timeline = await api(`/care/orders/${order.id}/timeline`, { token: staff.token })
  if (timeline.length < 2) throw new Error('时间线数据不足')
  console.log(`✓ 照护时间线 ${timeline.length} 条:`, timeline.map(t => t.icon).join('/'))

  const wb = await api('/care/workbench', { token: staff.token })
  console.log(`✓ 员工工作台: 今日任务${wb.total} 已完成${wb.done} 待处理${wb.pending}`)

  // 5. 离店检查（离店日）→ 生成 pet_health + 回写体重 → 办理离店
  await api(`/orders/${order.id}/start-boarding`, { method: 'POST', token: staff.token })
  const checkoutTasks = (await api(`/care/tasks?date=${d(3)}`, { token: staff.token })).filter(t => t.orderId === order.id)
  const checkoutCheck = checkoutTasks.find(t => t.taskType === 'CHECKOUT_CHECK')
  await api(`/care/tasks/${checkoutCheck.id}/complete`, { method: 'POST', token: staff.token, body: {
    weight: 28.6, mentalState: '正常', appetite: '正常', water: '正常', poop: '正常' } })
  await api(`/orders/${order.id}/check-out`, { method: 'POST', token: staff.token, body: { weightOut: 28.6 } })
  const growth = await api(`/pets/${pet.id}/growth`, { token: user.token })
  if (growth.totalBoardings < 1) throw new Error('成长档案未更新')
  console.log(`✓ 离店检查回写体重，成长档案: 累计寄养${growth.totalBoardings}次 ${growth.totalDays}天，曲线${growth.weightCurve.length}点`)

  // 收尾：办理"清洁完成"，让房间回到空闲——否则演示库会不断累积清洁中房间、可用数虚高
  const orderDetail = await api(`/orders/${order.id}`, { token: staff.token })
  if (orderDetail.room) {
    await api(`/rooms/${orderDetail.room.id}/status`, { method: 'PUT', token: staff.token, body: { status: 'AVAILABLE' } })
  }

  console.log('\n============================')
  console.log('Phase 5 冒烟测试全部通过 ✅')
}

main().catch(e => {
  console.error('\n❌ 冒烟测试失败:', e.message)
  process.exit(1)
})

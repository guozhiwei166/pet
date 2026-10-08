
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
 * 猫狗宠物寄养系统 Phase 6 冒烟：日报自动生成 + 异常事件生命周期 + 通知中心
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

  // 1. 下单→支付→确认→入住（分区规则：同分区内选目标日期可订的房型）
  const zone = pet.species === 'CAT' ? 'CAT' : 'DOG'
  const avail = await api('/boarding/availability', { method: 'POST', token: user.token,
    body: { startDate: d(0), endDate: d(3) } })
  const bookable = avail.filter(a => a.roomType.species === zone && a.bookable)
    .sort((a, b) => b.minAvailable - a.minAvailable)
  const comfortType = bookable.find(a => a.roomType.name.endsWith('舒适房') && a.minAvailable === bookable[0]?.minAvailable) || bookable[0]
  if (!comfortType) throw new Error(`${zone} 区在目标日期没有可订房型`)
  const order = await api('/boarding/orders', { method: 'POST', token: user.token, body: {
    petId: pet.id, roomTypeId: comfortType.roomType.id, startDate: d(0), endDate: d(3),
    contactName: '小明', contactPhone: '13800000001' } })
  await api(`/orders/${order.id}/pay`, { method: 'POST', token: user.token })
  // 手动选房分配：取候选里第一个可直接分配的房间
  const cands = await api(`/orders/${order.id}/room-candidates`, { token: admin.token })
  const roomPick = cands.rooms.find(r => r.assignable && !r.needConfirm) || cands.rooms.find(r => r.assignable)
  if (!roomPick) throw new Error('没有可分配的房间')
  await api(`/orders/${order.id}/assign-room`, { method: 'POST', token: admin.token, body: { roomId: roomPick.id } })
  await api(`/orders/${order.id}/check-in`, { method: 'POST', token: staff.token })

  // 2. 员工完成早餐与健康观察 → 日报应自动生成
  const tasks = (await api(`/care/tasks?date=${d(0)}`, { token: staff.token })).filter(t => t.orderId === order.id)
  for (const t of tasks.filter(t => t.taskType === 'FEEDING')) {
    await api(`/care/tasks/${t.id}/complete`, { method: 'POST', token: staff.token, body: {
      food: '狗粮', amount: '150g', appetite: '正常' } })
  }
  const hc = tasks.find(t => t.taskType === 'HEALTH_CHECK')
  await api(`/care/tasks/${hc.id}/complete`, { method: 'POST', token: staff.token, body: {
    weight: 28.3, mentalState: '很活跃', appetite: '正常', water: '正常', poop: '正常' } })

  const reports = await api(`/orders/${order.id}/daily-reports`, { token: user.token })
  const today = reports.find(r => r.reportDate === d(0))
  if (!today || !today.mealsSummary || !today.mentalState) throw new Error('日报未自动生成')
  console.log(`✓ 日报自动生成: ${d(0)} 饮食[${today.mealsSummary}] 精神[${today.mentalState}] 活动${today.activityMinutes}分钟`)

  // 3. 员工上报一般异常 → 管理员处理 → 关闭
  await api('/exceptions', { method: 'POST', token: staff.token, body: {
    petId: pet.id, orderId: order.id, type: 'APPETITE', level: 'NORMAL',
    description: '晚餐食欲偏少，已观察' } })
  const excs = await api('/exceptions?status=PENDING', { token: admin.token })
  if (!excs.length) throw new Error('异常未上报成功')
  const exc = excs[0]
  console.log(`✓ 异常上报: ${exc.petName} 级别=${exc.level} 状态=${exc.status}`)

  await api(`/exceptions/${exc.id}/process`, { method: 'POST', token: admin.token, body: { note: '已加餐观察' } })
  await api(`/exceptions/${exc.id}/observe`, { method: 'POST', token: admin.token })
  await api(`/exceptions/${exc.id}/resolve`, { method: 'POST', token: admin.token, body: { note: '食欲恢复' } })
  await api(`/exceptions/${exc.id}/close`, { method: 'POST', token: admin.token })
  const closed = (await api('/exceptions?status=CLOSED', { token: admin.token }))[0]
  if (!closed) throw new Error('异常未关闭')
  console.log('✓ 异常生命周期: PENDING→PROCESSING→OBSERVING→RESOLVED→CLOSED')

  // 4. 非法流转被拦截
  let blocked = false
  try { await api(`/exceptions/${exc.id}/process`, { method: 'POST', token: admin.token }) }
  catch (e) { blocked = String(e.message).includes('不允许') }
  if (!blocked) throw new Error('非法流转未被拦截')
  console.log('✓ 非法流转被拦截')

  // 5. 通知中心
  const notifs = await api('/notifications', { token: user.token })
  const unread = await api('/notifications/unread-count', { token: user.token })
  if (unread < 1) throw new Error('通知未产生')
  console.log(`✓ 通知中心: 共${notifs.length}条 未读${unread} 最新[${notifs[0].title}]`)
  await api('/notifications/read-all', { method: 'POST', token: user.token })
  const after = await api('/notifications/unread-count', { token: user.token })
  if (after !== 0) throw new Error('全部已读失败')
  console.log('✓ 全部已读生效')

  // 收尾：办理离店 + 清洁完成，房间回到空闲——否则演示库会累积"在住"房间导致可用数虚高
  await api(`/orders/${order.id}/start-boarding`, { method: 'POST', token: staff.token })
  await api(`/orders/${order.id}/check-out`, { method: 'POST', token: staff.token })
  const finished = await api(`/orders/${order.id}`, { token: staff.token })
  if (finished.room) {
    await api(`/rooms/${finished.room.id}/status`, { method: 'PUT', token: staff.token, body: { status: 'AVAILABLE' } })
  }

  console.log('\n============================')
  console.log('Phase 6 冒烟测试全部通过 ✅')
}

main().catch(e => {
  console.error('\n❌ 冒烟测试失败:', e.message)
  process.exit(1)
})

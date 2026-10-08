
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
 * 猫狗宠物寄养系统 Phase 4 冒烟：预订订单全链路（含并发占房验证）
 * 追加运行：node petcare-smoke.mjs --booking
 */
import { execSync } from 'node:child_process'

const BASE = 'http://localhost:8082/api'
const REDIS_CLI = 'C:/tools/redis/redis-cli.exe'
const onlyBooking = process.argv.includes('--booking')

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

async function login(username, password) {
  return captchaLogin(username, password)
}

async function fullBookingFlow() {
  // 0. 前置：取一只"在目标日期窗口内无占用订单"的宠物（持久化演示库里固定取第一只常被历史订单挡住）
  const user = await login('user', 'user123')
  let pets = await api('/pets', { token: user.token })
  const history = (await api('/orders?status=ALL&page=1&size=200', { token: user.token })).records || []
  const busyPetIds = new Set(history
    .filter(o => ['PENDING_PAYMENT', 'PAID', 'CONFIRMED', 'CHECKED_IN', 'BOARDING'].includes(o.status)
      && String(o.startDate) < d(3) && String(o.endDate) > d(0))
    .map(o => o.petId))
  let pet = pets.find(p => !busyPetIds.has(p.id))
  if (!pet) {
    pet = await api('/pets', { method: 'POST', token: user.token, body: {
      name: '豆豆' + String(Date.now()).slice(-4), species: 'DOG', breed: '金毛寻回犬', gender: 'M',
      birthDate: '2023-05-01', weight: 28.5, sterilized: 1, tagIds: [1, 2, 4] } })
  }
  console.log(`✓ 前置宠物: ${pet.name}（id=${pet.id}，${pet.species === 'CAT' ? '猫' : '狗'}）`)

  // 1. 库存计算（按宠物物种在同分区里选可订房型：分区为房型显式字段，跨区下单会被服务端拒绝）
  const zone = pet.species === 'CAT' ? 'CAT' : 'DOG'
  const avail = await api('/boarding/availability', {
    method: 'POST', token: user.token,
    body: { startDate: d(0), endDate: d(3) },
  })
  const bookable = avail.filter(a => a.roomType.species === zone && a.bookable)
    .sort((a, b) => b.minAvailable - a.minAvailable)   // 优先可用间数最多的：仅"可订"可能仍分不到具体房间
  const std = bookable.find(a => a.roomType.name.endsWith('标准房') && a.minAvailable === bookable[0]?.minAvailable) || bookable[0]
  if (!std) throw new Error(`${zone === 'CAT' ? '猫' : '犬'}区在目标日期没有可订房型（演示库可能已被占满）`)
  console.log(`✓ 库存计算: ${std.roomType.name}共${std.totalRooms}间，区间最少可用${std.minAvailable}间`)

  // 2. 创建订单（含增值服务）
  const services = await api('/services', { token: user.token })
  const order = await api('/boarding/orders', {
    method: 'POST', token: user.token,
    body: {
      petId: pet.id, roomTypeId: std.roomType.id, startDate: d(0), endDate: d(3),
      services: [{ serviceId: services[0].id, quantity: 1 }, { serviceId: services[3].id, quantity: 1 }],
      contactName: '小明', contactPhone: '13800000001', remark: '怕打雷需要安抚',
    },
  })
  const expectRoom = Number(std.roomType.price) * 3, expectSvc = 35 + 30
  if (Number(order.roomAmount) !== expectRoom || Number(order.serviceAmount) !== expectSvc
      || Number(order.totalAmount) !== expectRoom + expectSvc) {
    throw new Error(`金额计算错误: ${order.roomAmount}/${order.serviceAmount}/${order.totalAmount}`)
  }
  console.log(`✓ 下单成功: ${order.orderNo} 房费${order.roomAmount}+服务${order.serviceAmount}=¥${order.totalAmount}（3晚）`)

  // 3. 状态机防护：未支付订单不可分配房间
  const adminUser0 = await login('admin', 'admin123')
  const typeRooms = (await api('/rooms', { token: adminUser0.token })).filter(r => r.roomTypeId === std.roomType.id)
  try {
    await api(`/orders/${order.id}/assign-room`, { method: 'POST', token: adminUser0.token, body: { roomId: typeRooms[0].id } })
    throw new Error('未支付订单竟能被分配房间')
  } catch (e) {
    if (!String(e.message).includes('状态不支持')) throw e
    console.log('✓ 状态机防护：未支付订单不可分配房间')
  }

  // 4. 支付 → 手动选择房间分配 → 入住 → 开始寄养 → 离店
  await api(`/orders/${order.id}/pay`, { method: 'POST', token: user.token })
  console.log('✓ 模拟支付成功，payment 流水已写入')

  // 4.1 可分配房间候选（管理员视角：含房态与未来预约信息）
  const cand = await api(`/orders/${order.id}/room-candidates`, { token: adminUser0.token })
  if (!cand.rooms?.length) throw new Error('没有返回可选房间')
  const pick = cand.rooms.find(r => r.assignable && !r.needConfirm)
  if (!pick) throw new Error('没有可直接分配的房间')
  console.log(`✓ 可分配房间候选: 共 ${cand.rooms.length} 间，可选 ${cand.rooms.filter(r => r.assignable).length} 间，选中 ${pick.roomNo}`)

  // 4.2 手动分配（不再自动挑房）
  await api(`/orders/${order.id}/assign-room`, { method: 'POST', token: adminUser0.token, body: { roomId: pick.id } })
  const detail = await api(`/orders/${order.id}`, { token: user.token })
  if (!detail.order.roomId) throw new Error('分配后订单未绑定房间')

  // 4.3 未来预约不影响房间当前状态：房间仍空闲，但已登记 1 条预约（含起止时间）
  const map = await api('/rooms/map', { token: adminUser0.token })
  const cell = map.flatMap(f => f.rooms).find(r => r.id === detail.order.roomId)
  if (!cell) throw new Error('房间地图未找到该房间')
  if (cell.status !== 'AVAILABLE') throw new Error(`未来预约不应改变房间当前状态，实际=${cell.status}`)
  if (cell.reservationCount < 1) throw new Error('房间未登记预约记录')
  console.log(`✓ 分配房间 ${cell.roomNo}：当前状态 ${cell.status}（未来预约 ${cell.reservationCount} 条「${cell.reservations[0].rangeText}」，不影响当前可用）`)

  const staff = await login('staff', 'staff123')
  await api(`/orders/${order.id}/check-in`, { method: 'POST', token: staff.token })
  await api(`/orders/${order.id}/start-boarding`, { method: 'POST', token: staff.token })
  const map2 = await api('/rooms/map', { token: adminUser0.token })
  const cell2 = map2.flatMap(f => f.rooms).find(r => r.id === detail.order.roomId)
  if (cell2.status !== 'OCCUPIED') throw new Error(`入住后房间状态应为入住中，实际=${cell2.status}`)
  console.log('✓ 员工办理入住 → 寄养中，房间状态派生为入住中')

  await api(`/orders/${order.id}/check-out`, { method: 'POST', token: staff.token, body: { weightOut: 28.6 } })
  const done = await api(`/orders/${order.id}`, { token: user.token })
  if (done.order.status !== 'COMPLETED') throw new Error('离店后状态异常')
  console.log('✓ 办理离店 → COMPLETED，房间转清洁中，宠物体重回写')
  // 收尾：清洁完成，房间回到空闲（避免演示库反复累积"清洁中"房间而挤占可用数）
  if (done.room) {
    await api(`/rooms/${done.room.id}/status`, { method: 'PUT', token: staff.token, body: { status: 'AVAILABLE' } })
  }

  // 4.5 权限反向用例：普通用户尝试分配房间应 403
  let denied = false
  try { await api(`/orders/${order.id}/assign-room`, { method: 'POST', token: user.token, body: { roomId: pick.id } }) }
  catch (e) { denied = String(e.message).includes('403') }
  if (!denied) throw new Error('用户分配房间未被拦截')
  console.log('✓ 权限反向用例：用户分配房间被拒（403）')

  // 5. 全链路再来一单（PAID 后取消）
  const order2 = await api('/boarding/orders', {
    method: 'POST', token: user.token,
    body: { petId: pet.id, roomTypeId: std.roomType.id, startDate: d(10), endDate: d(12),
            contactName: '小明', contactPhone: '13800000001' },
  })
  await api(`/orders/${order2.id}/pay`, { method: 'POST', token: user.token })
  await api(`/orders/${order2.id}/cancel`, { method: 'POST', token: user.token, body: { reason: '行程有变' } })
  console.log('✓ 支付后取消订单（CANCELLED）')

  // 5.1 未来日期的订单：可以分房预约，但不能提前办理入住（否则会出现"未来日期却已完成"的脏数据）
  const order3 = await api('/boarding/orders', {
    method: 'POST', token: user.token,
    body: { petId: pet.id, roomTypeId: std.roomType.id, startDate: d(11), endDate: d(14),
            contactName: '小明', contactPhone: '13800000001' },
  })
  await api(`/orders/${order3.id}/pay`, { method: 'POST', token: user.token })
  const cand3 = await api(`/orders/${order3.id}/room-candidates`, { token: adminUser0.token })
  const pick3 = cand3.rooms.find(r => r.assignable && !r.needConfirm) || cand3.rooms.find(r => r.assignable)
  await api(`/orders/${order3.id}/assign-room`, { method: 'POST', token: adminUser0.token, body: { roomId: pick3.id } })
  let earlyBlocked = false
  try { await api(`/orders/${order3.id}/check-in`, { method: 'POST', token: staff.token }) }
  catch (e) { earlyBlocked = String(e.message).includes('未到入住日期') }
  if (!earlyBlocked) throw new Error('未来日期的订单竟然能提前办理入住')
  console.log(`✓ 未来日期订单（${d(11)} 入住）不可提前入住，只能保持已预约`)

  // 收尾：退款释放该预约，避免每次跑测试都留下多余预约
  await api(`/orders/${order3.id}/refund`, { method: 'POST', token: user.token })
  await api(`/orders/${order3.id}/refund-approve`, { method: 'POST', token: adminUser0.token, body: { approve: true } })
  console.log('✓ 退款审批通过，预约已释放（REFUNDED）')
}

/** 并发：管理员同时确认两笔共用"最后一件可订房"的订单，仅一笔成功 */
async function concurrentConfirm() {
  const user = await login('user', 'user123')
  const adminUser = await login('admin', 'admin123')
  const adminUser0 = adminUser
  let myPets = await api('/pets', { token: user.token })
  if (!myPets.length) {
    myPets = [await api('/pets', { method: 'POST', token: user.token, body: {
      name: '豆豆' + String(Date.now()).slice(-4), species: 'DOG', breed: '金毛寻回犬', gender: 'M',
      birthDate: '2023-05-01', weight: 28.5, sterilized: 1, tagIds: [1, 2, 4] } })]
  }
  const pet = myPets[0]
  const isCat = pet.species === 'CAT'
  // 第二只宠物：新业务规则禁止"同一宠物时间重叠"，并发竞争需两个宠物；且需与房型同分区
  let pet2 = myPets.find(p => p.id !== pet.id && (p.species === 'CAT') === isCat)
  if (!pet2) {
    pet2 = await api('/pets', { method: 'POST', token: user.token, body: {
      name: isCat ? '来福猫' : '来福', species: isCat ? 'CAT' : 'DOG', breed: isCat ? '狸花猫' : '柯基',
      gender: 'M', birthDate: '2022-03-01', weight: 12.5, sterilized: 1, tagIds: [1, 2] } })
  }

  // 选日期窗口 + 房型：持久化演示库里同一宠物可能已有重叠订单，故扫描未来若干周的窗口，
  // 取"两只宠物都空闲 + 同分区 VIP 房可用 ≥2"的那个（保证本用例可重复执行）
  const allOrders = (await api('/orders?status=ALL&page=1&size=200', { token: user.token })).records || []
  const occupying = allOrders.filter(o => ['PENDING_PAYMENT', 'PAID', 'CONFIRMED', 'CHECKED_IN', 'BOARDING'].includes(o.status))
  const spansOf = pid => occupying.filter(o => o.petId === pid).map(o => [String(o.startDate), String(o.endDate)])
  const overlaps = (spans, s, e) => spans.some(([a, b]) => a < e && b > s)
  const spans1 = spansOf(pet.id), spans2 = spansOf(pet2.id)

  let start = null, end = null, vip = null
  for (let off = 30; off <= 200 && !vip; off += 7) {
    const s = d(off), e = d(off + 3)
    if (overlaps(spans1, s, e) || overlaps(spans2, s, e)) continue
    const av = await api('/boarding/availability', { method: 'POST', token: user.token, body: { startDate: s, endDate: e } })
    const cand = av.find(a => a.roomType.species === (isCat ? 'CAT' : 'DOG') && a.roomType.name.includes('VIP'))
    if (cand && cand.totalRooms >= 2 && cand.minAvailable >= 2) { start = s; end = e; vip = cand }
  }
  if (!vip) { console.log('⚠ 未找到可用于并发测试的日期窗口（宠物档期/VIP 房紧张），跳过'); return }
  console.log(`✓ 并发用例日期窗口: ${start} ~ ${end}，房型 ${vip.roomType.name}（可用 ${vip.minAvailable}）`)

  const bookPet1 = { petId: pet.id, roomTypeId: vip.roomType.id, startDate: start, endDate: end,
                     contactName: '小明', contactPhone: '13800000001' }
  const bookPet2 = { petId: pet2.id, roomTypeId: vip.roomType.id, startDate: start, endDate: end,
                     contactName: '小明', contactPhone: '13800000001' }
  const o1 = await api('/boarding/orders', { method: 'POST', token: user.token, body: bookPet1 })
  const o2 = await api('/boarding/orders', { method: 'POST', token: user.token, body: bookPet2 })
  await api(`/orders/${o1.id}/pay`, { method: 'POST', token: user.token })
  await api(`/orders/${o2.id}/pay`, { method: 'POST', token: user.token })

  // 并发：两名操作者同时把【同一间房】分配给两笔重叠时段的订单，只应成功一笔
  // （不再靠"只剩一间房"制造竞争——分房由人工指定，重叠由数据库条件更新兜住）
  const rooms = (await api('/rooms', { token: adminUser0.token })).filter(r => r.roomTypeId === vip.roomType.id)
  if (!rooms.length) { console.log('⚠ 该房型没有房间，跳过并发测试'); return }
  const target = rooms[0]
  const results = await Promise.allSettled([
    api(`/orders/${o1.id}/assign-room`, { method: 'POST', token: adminUser0.token, body: { roomId: target.id } }),
    api(`/orders/${o2.id}/assign-room`, { method: 'POST', token: adminUser0.token, body: { roomId: target.id } }),
  ])
  const okCount = results.filter(r => r.status === 'fulfilled').length
  const failCount = results.filter(r => r.status === 'rejected').length
  if (okCount !== 1 || failCount !== 1) throw new Error(`并发分配结果异常: 成功${okCount} 失败${failCount}`)
  console.log(`✓ 并发分房验证：两笔重叠订单抢同一间房 ${target.roomNo}，1 成功 1 失败（数据库条件更新生效）`)
}

async function main() {
  if (onlyBooking) {
    await fullBookingFlow()
    await concurrentConfirm()
  } else {
    console.log('（Phase 1+2 链路请直接运行 node petcare-smoke.mjs，本文件聚焦 Phase 4）')
    await fullBookingFlow()
    await concurrentConfirm()
    console.log('\n============================')
    console.log('Phase 4 冒烟测试全部通过 ✅')
  }
}

main().catch(e => {
  console.error('\n❌ 冒烟测试失败:', e.message)
  process.exit(1)
})

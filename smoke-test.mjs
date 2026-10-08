/**
 * PawStay 全流程冒烟测试：node smoke-test.mjs
 * 覆盖：登录 → 首页 → 宠物档案 → 智能排笼 → 下单支付 → 商家接单/入住/日记打卡 → 详情 → 离店 → 评价 → 管理端统计
 */
const BASE = 'http://localhost:8081/api'

async function api(path, { method = 'GET', token, body } = {}) {
  const resp = await fetch(BASE + path, {
    method,
    headers: { 'Content-Type': 'application/json', ...(token ? { Authorization: 'Bearer ' + token } : {}) },
    body: body ? JSON.stringify(body) : undefined,
  })
  const data = await resp.json()
  if (data.code !== 0) throw new Error(`${path} -> [${data.code}] ${data.msg}`)
  return data.data
}

const d = n => {
  const t = new Date(Date.now() + n * 86400000)
  const p = x => String(x).padStart(2, '0')
  return `${t.getFullYear()}-${p(t.getMonth() + 1)}-${p(t.getDate())}`
}

async function main() {
  // 1. 登录三角色
  const owner = await api('/auth/login', { method: 'POST', body: { phone: '13800000001', password: '123456' } })
  const T1 = owner.token
  console.log('✓ 主人登录:', owner.user.nickname)

  const merchant = await api('/auth/login', { method: 'POST', body: { phone: '13900000001', password: '123456' } })
  const admin = await api('/auth/login', { method: 'POST', body: { phone: '10000000000', password: '123456' } })
  console.log('✓ 商家/管理员登录成功')

  // 2. 首页（UTF-8 校验：种子商家应出现在列表中，评分变化会改变排序，故不断言第一位）
  const home = await api('/home?page=1&size=10')
  const names = home.records.map(r => r.shopName)
  const okUtf8 = names.includes('汪汪之家宠物寄养') && names.includes('猫屿·猫咪专属寄养')
  console.log(`✓ 首页商家数 ${home.total}，中文UTF-8校验: ${okUtf8 ? '通过' : '失败!'}`)
  if (!okUtf8) throw new Error('中文数据异常')

  // 3. 宠物档案
  const pets = await api('/pets', { token: T1 })
  console.log('✓ 宠物档案:', pets.map(p => p.name).join('、'))

  // 4. 智能排笼：金毛(28.5kg) vs 犬舍；猫咪 vs 犬舍(应为空)
  const match = await api(`/match/rooms?petId=1&merchantId=1&start=${d(3)}&end=${d(6)}`, { token: T1 })
  console.log('✓ 智能排笼(金毛):', match.map(r => r.room.name + (r.recommend ? ' ★推荐' : '')).join('、'))
  const matchCat = await api(`/match/rooms?petId=2&merchantId=1&start=${d(3)}&end=${d(6)}`, { token: T1 })
  console.log(`✓ 物种约束校验(猫咪搜犬舍,应为0): ${matchCat.length}`)

  // 5. 下单 → 支付
  const order = await api('/orders', {
    method: 'POST', token: T1,
    body: {
      petId: 1, roomId: match[0].room.id, startDate: d(3), endDate: d(6),
      contactName: '小汪', contactPhone: '13800000001', remark: '冒烟测试订单', agreementSigned: true,
    },
  })
  await api(`/orders/${order.id}/pay`, { method: 'POST', token: T1 })
  console.log(`✓ 下单+模拟支付: ${order.orderNo} 金额￥${order.totalAmount}（${order.days}晚）`)

  // 6. 商家看板 → 接单 → 入住 → 日记打卡
  const dash = await api('/merchant/dashboard', { token: merchant.token })
  console.log(`✓ 商家看板: 待接单${dash.pendingConfirm} 寄养中${dash.boarding}`)
  await api(`/orders/${order.id}/confirm`, { method: 'POST', token: merchant.token })
  await api(`/orders/${order.id}/checkin`, { method: 'POST', token: merchant.token, body: { weightIn: 28.3 } })
  await api(`/orders/${order.id}/diary`, {
    method: 'POST', token: merchant.token,
    body: { mealDone: 1, walkDone: 1, mood: '开心', content: '今天吃了两顿，遛弯两次，认识了一个新朋友', weight: 28.2 },
  })
  console.log('✓ 接单/入住/日记打卡完成（含当日体重，供健康曲线使用）')

  // 7. 主人查看详情（日记可见）
  const detail = await api(`/orders/${order.id}`, { token: T1 })
  console.log(`✓ 订单详情: 状态=${detail.order.status} 日记${detail.diaries.length}条 商家=${detail.merchant.shopName}`)

  // 8. 离店 → 评价
  await api(`/orders/${order.id}/checkout`, { method: 'POST', token: merchant.token, body: { weightOut: 28.1 } })
  await api(`/orders/${order.id}/review`, { method: 'POST', token: T1, body: { rating: 5, content: '服务很棒，日记反馈及时' } })
  console.log('✓ 离店结算 + 五星评价')

  // 9. 管理端统计
  const stats = await api('/admin/stats', { token: admin.token })
  console.log('✓ 管理端统计:', JSON.stringify(stats.overview))

  console.log('\n============================')
  console.log('全部冒烟测试通过 ✅')
}

main().catch(e => {
  console.error('\n❌ 冒烟测试失败:', e.message)
  process.exit(1)
})

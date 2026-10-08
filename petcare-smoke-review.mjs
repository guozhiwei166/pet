
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

/** Phase 7 冒烟：五维评价 + 回复 + 成长档案对接 */
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
async function main() {
  const user = (await captchaLogin('user', 'user123')).token
  const admin = (await captchaLogin('admin', 'admin123')).token
  // 找一笔"未评价过的"COMPLETED 订单（Phase 4/6 冒烟产生的；跳过已评价的，保证幂等可重复跑）
  const orders = (await api('/orders?status=COMPLETED', { token: user })).records
  if (!orders.length) throw new Error('没有已完成订单，请先跑 petcare-smoke-booking.mjs')
  let order = null
  for (const o of orders) {
    const existing = await api(`/pets/${o.petId}/reviews`, { token: user }).catch(() => [])
    const already = (existing || []).some(r => r.orderId === o.id)
    if (!already) { order = o; break }
  }
  if (!order) throw new Error('所有已完成订单均已评价，请先产生新订单（跑 petcare-smoke-booking.mjs）')
  await api(`/orders/${order.id}/review`, { method: 'POST', token: user, body: {
    ratingOverall: 5, ratingEnv: 5, ratingService: 4, ratingStaff: 5, ratingPetStatus: 5,
    content: '豆豆回家后状态很好，每天看日报特别安心', tags: '环境干净,反馈及时' } })
  console.log('✓ 五维评价提交成功')
  let again = true
  try { await api(`/orders/${order.id}/review`, { method: 'POST', token: user, body: { ratingOverall: 5 } }) }
  catch (e) { again = String(e.message).includes('已评价') }
  if (!again) throw new Error('重复评价未被拦截')
  console.log('✓ 重复评价被拦截')
  const petId = order.petId
  const reviews = await api(`/pets/${petId}/reviews`, { token: user })
  if (!reviews.length || reviews[0].ratingOverall !== 5) throw new Error('评价未关联到宠物')
  console.log(`✓ 宠物成长档案关联评价 ${reviews.length} 条`)
  const replyTarget = reviews[0].id
  await api(`/reviews/${replyTarget}/reply`, { method: 'POST', token: admin, body: { reply: '谢谢信任，欢迎下次再来' } })
  const replied = (await api(`/pets/${petId}/reviews`, { token: user }))[0]
  if (!replied.reply) throw new Error('回复失败')
  console.log('✓ 管理员回复:', replied.reply)
  console.log('\n============================')
  console.log('Phase 7 冒烟测试全部通过 ✅')
}
main().catch(e => { console.error('\n❌ 冒烟测试失败:', e.message); process.exit(1) })

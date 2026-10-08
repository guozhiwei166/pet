// 针对本次修复的定点验证（临时脚本，不属于项目）
import { execSync } from 'node:child_process'

const BASE = 'http://localhost:8082/api'
const REDIS_CLI = 'C:/tools/redis/redis-cli.exe'
let pass = 0, fail = 0

function api(path, { method = 'GET', token, body } = {}) {
  return fetch(BASE + path, {
    method,
    headers: { 'Content-Type': 'application/json', ...(token ? { Authorization: 'Bearer ' + token } : {}) },
    body: body === undefined ? undefined : JSON.stringify(body),
  }).then(async r => ({ status: r.status, body: await r.json() }))
}
const readCaptcha = id => execSync(`"${REDIS_CLI}" GET captcha:${id}`).toString().trim().replace(/^"|"$/g, '')
async function login(username, password) {
  const cap = (await api('/auth/captcha')).body.data
  const r = await api('/auth/login', { method: 'POST', body: { username, password, captchaId: cap.captchaId, captchaCode: readCaptcha(cap.captchaId) } })
  if (r.body.code !== 200) throw new Error('登录失败: ' + JSON.stringify(r.body))
  return r.body.data
}
function check(name, cond, extra = '') {
  if (cond) { console.log('✓ ' + name); pass++ } else { console.log('✗ ' + name + ' ' + extra); fail++ }
}
const d = n => new Date(Date.now() + n * 86400000).toISOString().slice(0, 10)

const staff = await login('staff', 'staff123')

// 新用户：注册 + 建宠物 + 领券
const uname = 'ver' + String(Date.now()).slice(-8)
const cap = (await api('/auth/captcha')).body.data
await api('/auth/register', { method: 'POST', body: {
  username: uname, phone: '137' + String(Date.now()).slice(-8), password: 'ver123', nickname: '验证家长',
  captchaId: cap.captchaId, captchaCode: readCaptcha(cap.captchaId) } })
const me = await login(uname, 'ver123')
const pet = (await api('/pets', { method: 'POST', token: me.token, body: {
  name: '验证犬', species: 'DOG', breed: '柯基', gender: 'M', birthDate: '2023-01-01', weight: 10.0 } })).body.data

const coupons = (await api('/coupons/available', { token: me.token })).body.data
for (const c of coupons.slice(0, 2)) await api(`/coupons/${c.id}/claim`, { method: 'POST', token: me.token })
const mine = (await api('/coupons/mine', { token: me.token })).body.data.filter(c => c.status === 'UNUSED')
check('领券成功（2 张可用）', mine.length >= 2, JSON.stringify(mine.map(c => c.name)))

const expectDiscount = (c, total) => c.type === 'DISCOUNT' ? Number(c.amount) : Math.round(total * (100 - c.percent) / 100 * 100) / 100

// A. 下单时用券：折扣与总额必须一致
const avail = (await api('/boarding/availability', { method: 'POST', token: me.token, body: { startDate: d(1), endDate: d(4) } })).body.data
const rt = avail.find(a => a.bookable && !a.roomType.name.includes('猫')).roomType
const useCoupon = mine[0]
let o1 = (await api('/boarding/orders', { method: 'POST', token: me.token, body: {
  petId: pet.id, roomTypeId: rt.id, startDate: d(1), endDate: d(4), contactName: '验证', contactPhone: '13700000000',
  userCouponId: useCoupon.id } })).body.data
const exp1 = expectDiscount(useCoupon, Number(o1.roomAmount) + Number(o1.serviceAmount))
check('下单用券：discountAmount 正确', Number(o1.discountAmount) === exp1, `实际 ${o1.discountAmount} 期望 ${exp1}`)
check('下单用券：totalAmount = 房费+服务-优惠',
  Number(o1.totalAmount) === Number(o1.roomAmount) + Number(o1.serviceAmount) - exp1, `实际 ${o1.totalAmount}`)
check('下单用券：券状态转为 USED',
  (await api('/coupons/mine', { token: me.token })).body.data.find(c => c.id === useCoupon.id)?.status === 'USED')

// B. apply-coupon：服务端算金额并回写订单
const pet2 = (await api('/pets', { method: 'POST', token: me.token, body: {
  name: '验证犬2', species: 'DOG', breed: '柴犬', gender: 'F', birthDate: '2023-05-01', weight: 9.0 } })).body.data
let o2 = (await api('/boarding/orders', { method: 'POST', token: me.token, body: {
  petId: pet2.id, roomTypeId: rt.id, startDate: d(5), endDate: d(8), contactName: '验证', contactPhone: '13700000000' } })).body.data
const before = Number(o2.totalAmount)
const applied = (await api(`/orders/${o2.id}/apply-coupon`, { method: 'POST', token: me.token })).body
const after = (await api(`/orders/${o2.id}`, { token: me.token })).body.data.order
check('apply-coupon 返回非零减免', Number(applied.data) > 0, JSON.stringify(applied))
check('apply-coupon 回写订单金额（券不再白扣）',
  Number(after.discountAmount) === Number(applied.data) && Number(after.totalAmount) === before - Number(applied.data),
  `discount=${after.discountAmount} total=${after.totalAmount} before=${before}`)
const again = (await api(`/orders/${o2.id}/apply-coupon`, { method: 'POST', token: me.token })).body
check('重复 apply-coupon 被拦截', again.code !== 200, JSON.stringify(again))
const others = (await api(`/orders/${o1.id}/apply-coupon`, { method: 'POST', token: staff.token })).body
check('apply-coupon 拒绝非本人订单', others.code !== 200, JSON.stringify(others))

// C. 权限缺口
const sb = await api('/service-bookings', { token: me.token })
check('普通用户访问 /service-bookings 被拒', sb.body.code === 403, JSON.stringify(sb.body))
check('员工访问 /service-bookings 正常', (await api('/service-bookings', { token: staff.token })).body.code === 200)
const note = await api(`/orders/${o2.id}/daily-reports/${d(0)}/note`, { method: 'POST', token: me.token, body: { note: 'x' } })
check('普通用户写日报备注被拒', note.body.code === 403, JSON.stringify(note.body))

const userTok = await login('user', 'user123')
const theirPet = (await api('/pets', { token: userTok.token })).body.data[0]
const idor = await api(`/pets/${theirPet.id}/daily-reports`, { token: me.token })
check('越权读他人宠物日报被拒', idor.body.code === 403, JSON.stringify(idor.body))
check('属主读自己宠物日报正常', (await api(`/pets/${theirPet.id}/daily-reports`, { token: userTok.token })).body.code === 200)

// D. 参数健壮性：应返回 400 可读提示而不是 500
const badWeight = (await api(`/orders/${o2.id}/check-out`, { method: 'POST', token: staff.token, body: { weightOut: 'abc' } })).body
check('离店体重非法 → 400 友好提示', badWeight.code === 400, JSON.stringify(badWeight))
const badId = (await api('/orders/abc', { token: userTok.token })).body
check('路径参数非法 → 400 友好提示', badId.code === 400, JSON.stringify(badId))
const badJson = (await api('/boarding/availability', { method: 'POST', token: userTok.token, body: { startDate: 'not-a-date', endDate: 'x' } })).body
check('请求体日期非法 → 400 友好提示', badJson.code === 400, JSON.stringify(badJson))

console.log(`\n${fail === 0 ? '✅ 全部通过' : '❌ 存在失败'}：${pass} 通过 / ${fail} 失败`)
process.exit(fail === 0 ? 0 : 1)

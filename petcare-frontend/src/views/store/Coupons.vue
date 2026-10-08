<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../../api'

const coupons = ref<any[]>([])
const mine = ref<any[]>([])
const claimedIds = ref<Set<number>>(new Set())

async function load() {
  try {
    coupons.value = await api.availableCoupons()
    mine.value = await api.myCoupons()
    claimedIds.value = new Set(mine.value.map((c: any) => c.couponId))
  } catch (e: any) {
    ElMessage.error(e?.message || '优惠券加载失败，请刷新重试')
  }
}
onMounted(load)

async function claim(c: any) {
  try {
    await api.claimCoupon(c.id)
    ElMessage.success('🎉 领取成功，去下单即可使用')
    load()
  } catch { /* 已领等由后端提示 */ }
}

/** 是否已领取过该券 */
const claimed = (c: any) => claimedIds.value.has(c.id)

/** 面额：满减券 ￥X；折扣券 X折（percent 即折数*10，80=8折） */
const couponVal = (c: any) => {
  if (c.type === 'DISCOUNT') return `￥${c.amount}`
  const p = Number(c.percent)
  if (!p || p >= 100) return null // 10折无效券不应出现，防御性返回 null
  return String(p / 10)
}
const couponUnit = (c: any) => (c.type === 'DISCOUNT' ? '' : '折')
const couponCond = (c: any) => (c.type === 'DISCOUNT' ? `满${c.threshold}元可用` : '全场通用')
const couponTypeText = (c: any) => (c.type === 'DISCOUNT' ? '满减券' : '折扣券')
/** 有效期：无则"无有效期"，有则完整日期（斜杠格式） */
const dateText = (d?: string) => (d ? d.replace(/-/g, '/') : '无有效期')
/** 可领取券：只显示真正有效的券（过滤 percent>=100 的异常券） */
const validCoupons = () => coupons.value.filter((c: any) =>
  !(c.type === 'PERCENT' && Number(c.percent) >= 100))
/** 我的券（已由后端过滤 10 折无效券，这里再防御一次）*/
const validMine = () => mine.value.filter((c: any) =>
  !(c.type === 'PERCENT' && Number(c.percent) >= 100))
/** 统计真正可用（未使用）的券数量 */
const unusedCount = () => validMine().filter((c: any) => c.status === 'UNUSED').length
const statusText = (s: string) => ({ UNUSED: '未使用', USED: '已使用', EXPIRED: '已过期' } as Record<string, string>)[s] || s
</script>

<template>
  <div class="coupon-page">
    <!-- 页头 -->
    <div class="page-head-row">
      <div>
        <h2 class="page-title">领券中心</h2>
        <p class="page-desc">下单寄养 / 门店洗护，先领券更划算</p>
      </div>
      <div class="count-badge" v-if="validMine().length">
        我的券 <b class="num">{{ unusedCount() }}</b> 张可用
      </div>
    </div>

    <!-- 券区：浅灰底衬 + 白券卡形成对比 -->
    <div class="coupon-zone">
      <div v-for="c in validCoupons()" :key="c.id" class="ticket">
        <div class="ticket-side">
          <div class="ticket-value">
            <span class="val">{{ couponVal(c) }}</span><span class="unit">{{ couponUnit(c) }}</span>
          </div>
          <div class="ticket-cond">{{ couponCond(c) }}</div>
        </div>
        <div class="ticket-line"></div>
        <div class="ticket-body">
          <div class="ticket-name">{{ c.name }}</div>
          <div class="ticket-date sub">
            {{ c.type === 'DISCOUNT' ? `满${c.threshold}元可用 · ` : '' }}有效期至 {{ dateText(c.endDate) }}
          </div>
          <span class="ticket-tag">{{ couponTypeText(c) }}</span>
        </div>
        <el-button type="primary" round class="claim-btn"
                   :disabled="claimed(c)"
                   :class="{ 'claimed-btn': claimed(c) }"
                   @click="claim(c)">{{ claimed(c) ? '已领取' : '领取' }}</el-button>
      </div>
    </div>
    <el-empty v-if="!validCoupons().length" description="暂时没有可领的优惠券" />

    <!-- 我的券 -->
    <template v-if="validMine().length">
      <div class="sec-title">我的优惠券</div>
      <div class="mine-zone">
        <div v-for="c in validMine()" :key="c.id" class="mine-ticket" :class="'st-' + c.status.toLowerCase()">
          <div class="mine-val">{{ couponVal(c) }}{{ couponUnit(c) }}</div>
          <div class="mine-info">
            <b>{{ c.name }}</b>
            <span class="sub">{{ couponCond(c) }} · 至 {{ dateText(c.endDate) }}</span>
            <span class="ticket-tag">{{ couponTypeText(c) }}</span>
          </div>
          <span class="mine-status" :class="'st-' + c.status.toLowerCase()">{{ statusText(c.status) }}</span>
        </div>
      </div>
    </template>
    <div v-else class="mine-empty">
      还没有优惠券，去上方领一张吧～
    </div>
  </div>
</template>

<style scoped>
.coupon-page { display: flex; flex-direction: column; gap: 16px; }
.page-head-row { display: flex; justify-content: space-between; align-items: center; }
.count-badge { background: #fff; border-radius: 999px; padding: 8px 18px; font-size: 13px;
  color: var(--pc-ink-2); box-shadow: 0 4px 12px -6px rgba(47,70,57,.12); }
.count-badge b { color: var(--pc-primary-deep); font-size: 16px; }

/* 券区用浅灰底，券卡纯白 → 明显区分 */
.coupon-zone {
  background: #F0EDE8; border-radius: 16px; padding: 18px;
  display: grid; grid-template-columns: repeat(auto-fill, minmax(340px, 1fr)); gap: 14px;
}
.ticket {
  display: flex; align-items: stretch; background: #fff; border-radius: 14px; overflow: hidden;
  box-shadow: 0 4px 14px -8px rgba(47,70,57,.18); transition: transform .15s, box-shadow .15s;
}
.ticket:hover { transform: translateY(-2px); box-shadow: 0 10px 24px -12px rgba(47,70,57,.28); }

/* 左侧面额：白底深绿字，和券身同底色但用留白+分隔线区分 */
.ticket-side {
  width: 116px; flex-shrink: 0; padding: 18px 12px;
  display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 4px;
  background: #F5FAF7;
}
.ticket-value { display: flex; align-items: baseline; }
.ticket-value .val { font-size: 32px; font-weight: 900; color: #2E9E6B; line-height: 1; }
.ticket-value .unit { font-size: 14px; font-weight: 800; color: #2E9E6B; margin-left: 2px; }
.ticket-cond { font-size: 11px; color: #6B7F72; }

/* 锯齿分隔 */
.ticket-line { width: 0; border-left: 1.5px dashed #D8E6DE; position: relative; margin: 8px 0; flex-shrink: 0; }

.ticket-body { flex: 1; padding: 16px 14px; display: flex; flex-direction: column; gap: 6px; min-width: 0; }
.ticket-name { font-size: 15px; font-weight: 800; }
.ticket-date { font-size: 12px; }
.ticket-tag { align-self: flex-start; font-size: 11px; background: #EAF2ED; color: #2E7D57;
  padding: 2px 10px; border-radius: 999px; font-weight: 700; }

.claim-btn { margin: auto 14px auto 4px; font-weight: 700; }
.claim-btn.claimed-btn { background: #C9CFCB; border-color: #C9CFCB; color: #fff; }

/* 我的券 */
.sec-title { font-weight: 800; font-size: 16px; margin: 8px 0 12px; }
.mine-zone { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px; }
.mine-ticket {
  display: flex; align-items: center; gap: 14px; background: #fff; border-radius: 12px;
  padding: 14px 16px; border: 1px solid #ECE8E2;
}
.mine-ticket.st-used, .mine-ticket.st-expired { opacity: .6; }
.mine-val { font-size: 20px; font-weight: 900; color: #2E9E6B; width: 70px; flex-shrink: 0; }
.mine-ticket.st-used .mine-val, .mine-ticket.st-expired .mine-val { color: #A8B4AE; text-decoration: line-through; }
.mine-info { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 2px; align-items: flex-start; }
.mine-info b { font-size: 14px; }
.mine-status { font-size: 12px; font-weight: 800; flex-shrink: 0; }
.mine-status.st-unused { color: #2E9E6B; }
.mine-status.st-used { color: #9AA5A0; }
.mine-status.st-expired { color: #C7B9AA; }
.mine-empty { background: #F0EDE8; border-radius: 14px; padding: 34px; text-align: center; color: #8B8378; font-size: 14px; }
</style>

<script setup>
import { ref, computed, nextTick, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast, showDialog } from 'vant'
import * as echarts from 'echarts'
import api, { STATUS_MAP, fmtDate } from '../api'
import { toneStyle } from '../utils/tone'
import Illust from '../components/Illust.vue'

const route = useRoute()
const router = useRouter()
const vo = ref(null)
const showReview = ref(false)
const rating = ref(5)
const reviewText = ref('')
const curveRef = ref(null)
let curveChart = null

const statusText = computed(() => STATUS_MAP[vo.value?.order.status]?.text || '')

const stepIndex = computed(() => {
  const s = vo.value?.order.status
  if (s === 'PENDING_CONFIRM') return vo.value.order.payTime ? 1 : 0
  if (s === 'CONFIRMED') return 2
  if (s === 'BOARDING') return 3
  if (s === 'COMPLETED') return 4
  return 0
})

/** 体重健康曲线数据源：入住称重 + 每日日记体重 + 离店称重，按日期排序去重 */
const weightPoints = computed(() => {
  if (!vo.value) return []
  const o = vo.value.order
  const pts = []
  if (o.weightIn) pts.push([o.startDate, Number(o.weightIn)])
  vo.value.diaries?.forEach(d => { if (d.weight) pts.push([d.recordDate, Number(d.weight)]) })
  if (o.weightOut) pts.push([(o.checkoutTime || '').slice(0, 10) || o.endDate, Number(o.weightOut)])
  const byDate = new Map()
  pts.forEach(p => byDate.set(p[0], p[1]))
  return [...byDate.entries()].sort((a, b) => a[0].localeCompare(b[0]))
})

const photosOf = d => (d.photos || '').split(',').filter(Boolean)

async function renderCurve() {
  // 等待 v-if 的曲线容器完成渲染后再初始化 ECharts
  await nextTick()
  if (weightPoints.value.length < 2 || !curveRef.value) return
  if (curveChart) curveChart.dispose()
  curveChart = echarts.init(curveRef.value)
  curveChart.setOption({
    grid: { left: 42, right: 18, top: 24, bottom: 28 },
    tooltip: { trigger: 'axis', formatter: p => `${p[0].name}<br/>体重：<b>${p[0].value} kg</b>` },
    xAxis: { type: 'category', data: weightPoints.value.map(p => p[0].slice(5)), boundaryGap: false },
    yAxis: { type: 'value', scale: true, name: 'kg', nameTextStyle: { color: '#A69C92' } },
    series: [{
      type: 'line', smooth: true, symbol: 'circle', symbolSize: 7,
      data: weightPoints.value.map(p => p[1]),
      lineStyle: { color: '#FF6B3D', width: 3 },
      itemStyle: { color: '#FF6B3D', borderColor: '#fff', borderWidth: 2 },
      areaStyle: { color: 'rgba(255,107,61,.12)' },
    }],
  })
}

async function load() {
  vo.value = await api.orderDetail(route.params.id)
  renderCurve()
}

async function pay() {
  await api.payOrder(vo.value.order.id)
  showToast({ type: 'success', message: '支付成功' })
  load()
}

async function cancel() {
  try {
    await showDialog({ title: '取消订单', message: '确定要取消这笔寄养订单吗？已支付款项将原路退回。' })
    await api.cancelOrder(vo.value.order.id)
    showToast('订单已取消')
    load()
  } catch (e) { /* 用户取消 */ }
}

async function submitReview() {
  if (!reviewText.value.trim()) return showToast('写点什么吧，一句话也行')
  await api.addReview(vo.value.order.id, { rating: rating.value, content: reviewText.value })
  showToast({ type: 'success', message: '感谢你的评价' })
  showReview.value = false
  load()
}

/** 联系商家：一键拨打 / 复制号码 */
function callMerchant() {
  window.location.href = 'tel:' + vo.value.merchant.phone
}
async function copyPhone() {
  try {
    await navigator.clipboard.writeText(vo.value.merchant.phone)
    showToast('商家电话已复制')
  } catch (e) {
    showToast('复制失败，请手动记录')
  }
}

onMounted(load)
</script>

<template>
  <div class="page" v-if="vo">
    <van-nav-bar left-arrow @click-left="router.back()" title="订单详情" />

    <!-- 状态头部 -->
    <div class="hero hero-gradient">
      <div class="status-line">
        <b class="status-text">{{ statusText }}</b>
        <span v-if="vo.order.status === 'PENDING_CONFIRM' && !vo.order.payTime" class="pay-btn press" @click="pay">去支付</span>
      </div>
      <div class="sub-text" v-if="vo.order.status === 'REJECTED'">拒单原因：{{ vo.order.rejectReason }}</div>
      <van-steps :active="stepIndex" active-color="#fff" inactive-color="rgba(255,255,255,.55)" class="steps">
        <van-step>提交订单</van-step>
        <van-step>完成支付</van-step>
        <van-step>商家接单</van-step>
        <van-step>办理入住</van-step>
        <van-step>寄养结束</van-step>
      </van-steps>
    </div>

    <!-- 订单信息 -->
    <div class="clay info-card">
      <div class="row"><span>寄养商家</span><b>{{ vo.merchant.shopName }}</b></div>
      <div class="row"><span>联系电话</span>
        <span class="contact-ops">
          <b class="num">{{ vo.merchant.phone }}</b>
          <span class="chip chip-warm press" @click="callMerchant"><van-icon name="phone-o" /> 拨打</span>
          <span class="chip chip-gray press" @click="copyPhone"><van-icon name="description-o" /> 复制</span>
        </span>
      </div>
      <div class="row"><span>房型</span><b>{{ vo.room.name }}</b></div>
      <div class="row"><span>寄养日期</span>
        <b class="num">{{ fmtDate(vo.order.startDate) }} ~ {{ fmtDate(vo.order.endDate) }}（{{ vo.order.days }} 晚）</b></div>
      <div class="row"><span>金额</span><b class="price num">￥{{ vo.order.totalAmount }}</b></div>
      <div class="row" v-if="vo.order.weightIn">
        <span>入住/离店体重</span>
        <b class="num">{{ vo.order.weightIn }}kg<template v-if="vo.order.weightOut"> → {{ vo.order.weightOut }}kg</template></b>
      </div>
      <div class="row" v-if="vo.order.remark"><span>备注</span><b>{{ vo.order.remark }}</b></div>
      <div class="row" v-if="vo.order.status === 'PENDING_CONFIRM'">
        <span></span>
        <van-button size="small" plain round type="danger" @click="cancel">取消订单</van-button>
      </div>
    </div>

    <!-- 宠物卡片 -->
    <div class="clay pet-line" v-if="vo.pet">
      <div class="pet-avatar" :style="toneStyle(vo.pet.name)">
        <span>{{ vo.pet.species === 'CAT' ? '🐱' : '🐶' }}</span>
      </div>
      <div class="pet-info">
        <b>{{ vo.pet.name }}</b>
        <div class="sub">{{ vo.pet.breed }} · {{ vo.pet.gender === 'M' ? '弟弟' : '妹妹' }} · {{ vo.pet.temperament }}</div>
      </div>
      <div class="chip chip-mint" v-if="vo.pet.vaccinated">疫苗齐全</div>
    </div>

    <!-- 体重健康曲线 -->
    <div class="clay curve-card" v-if="weightPoints.length >= 2">
      <div class="curve-head">
        <b>体重健康曲线</b>
        <span class="sub">入住 → 离店的体重变化，异常波动早发现</span>
      </div>
      <div ref="curveRef" class="curve"></div>
    </div>

    <!-- 寄养日记时间线 -->
    <div class="sec" v-if="vo.diaries?.length">寄养日记</div>
    <div class="timeline" v-if="vo.diaries?.length">
      <div v-for="(d, i) in vo.diaries" :key="d.id" class="tl-item">
        <div class="tl-rail">
          <span class="tl-dot" :class="{ today: i === 0 }"></span>
          <span class="tl-line" v-if="i < vo.diaries.length - 1"></span>
        </div>
        <div class="clay tl-card">
          <div class="diary-head">
            <b class="num">{{ d.recordDate }}</b>
            <span class="chip chip-rose" v-if="d.mood">{{ d.mood }}</span>
          </div>
          <div class="care-tags">
            <span :class="['care-tag', { done: d.mealDone }]">
              <van-icon :name="d.mealDone ? 'checked' : 'circle'" /> 喂食
            </span>
            <span :class="['care-tag', { done: d.walkDone }]">
              <van-icon :name="d.walkDone ? 'checked' : 'circle'" /> 遛弯
            </span>
            <span v-if="d.weight" class="care-tag done"><van-icon name="balance-o" /> {{ d.weight }}kg</span>
          </div>
          <div class="diary-content sub2">{{ d.content }}</div>
          <div class="photo-grid" v-if="photosOf(d).length">
            <div v-for="(p, i2) in photosOf(d)" :key="i2" class="photo-item"><img :src="p" alt="" /></div>
          </div>
        </div>
      </div>
    </div>
    <div class="empty-wrap" v-if="!vo.diaries?.length && ['CONFIRMED','BOARDING'].includes(vo.order.status)">
      <Illust scene="sleep" style="width: 170px; margin: 0 auto; display: block;" />
      <div class="sub" style="text-align:center">入住后商家会每天在这里更新日记哦</div>
    </div>

    <!-- 评价 -->
    <div class="clay review-card" v-if="vo.review">
      <div class="diary-head"><b>我的评价</b><van-rate :model-value="vo.review.rating" readonly size="12px" color="#FFB637" void-color="#EFE7DE" /></div>
      <div class="diary-content sub2">{{ vo.review.content }}</div>
      <div v-if="vo.review.reply" class="note-block">商家回复：{{ vo.review.reply }}</div>
    </div>
    <van-button v-if="vo.order.status === 'COMPLETED' && !vo.review" type="primary" round block
                class="btn-main press" @click="showReview = true">评价本次寄养</van-button>
    <div style="height: 24px"></div>
  </div>

  <van-popup v-model:show="showReview" round position="bottom" class="review-pop">
    <div class="pop-title">评价本次寄养</div>
    <van-rate v-model="rating" size="26px" color="#FFB637" gutter="8px" class="pop-rate" />
    <van-field v-model="reviewText" type="textarea" rows="3" autosize maxlength="200" show-word-limit
               placeholder="毛孩子住得开心吗？商家服务怎么样？" class="pop-field" />
    <van-button type="primary" round block class="pop-btn press" @click="submitReview">发布评价</van-button>
  </van-popup>
</template>

<style scoped>
.hero { margin-top: -46px; padding: 58px 16px 10px; border-radius: 0 0 28px 28px; }
.status-line { display: flex; justify-content: space-between; align-items: center; }
.status-text { font-size: 20px; font-weight: 900; }
.pay-btn { background: #fff; color: var(--brand-deep); font-weight: 800; border-radius: 999px; padding: 7px 18px; font-size: 13px; }
.sub-text { font-size: 12px; opacity: .92; margin-top: 6px; }
.steps { margin-top: 12px; --van-step-font-size: 11px; background: transparent; }

.info-card { margin: 14px 16px; padding: 6px 16px; }
.contact-ops { display: inline-flex; align-items: center; gap: 8px; }
.contact-ops .chip .van-icon { margin-right: 2px; }
.row { display: flex; justify-content: space-between; font-size: 13px; margin: 10px 0; gap: 12px; align-items: center; }
.row > span:first-child { color: var(--ink-3); flex-shrink: 0; }

.pet-line { margin: 0 16px; padding: 13px 15px; display: flex; align-items: center; }
.pet-avatar { width: 50px; height: 50px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 22px; margin-right: 12px; }
.pet-info { flex: 1; }
.pet-line .chip { margin-left: 8px; }

.timeline { padding: 4px 16px 0; }
.tl-item { display: flex; }
.tl-rail { width: 18px; display: flex; flex-direction: column; align-items: center; flex-shrink: 0; }
.tl-dot { width: 11px; height: 11px; border-radius: 50%; background: var(--brand-soft); border: 3px solid var(--brand); margin-top: 16px; flex-shrink: 0; }
.tl-dot.today { background: var(--brand); box-shadow: 0 0 0 4px var(--brand-soft); }
.tl-line { flex: 1; width: 2.5px; background: #EFE3D7; border-radius: 2px; margin: 3px 0; }
.tl-card { flex: 1; margin: 0 0 14px 10px; padding: 14px; min-width: 0; }
.diary-head { display: flex; justify-content: space-between; align-items: center; }
.care-tags { display: flex; gap: 14px; font-size: 11.5px; color: var(--ink-3); margin-top: 9px; }
.care-tag { display: inline-flex; align-items: center; gap: 3px; font-weight: 600; }
.care-tag.done { color: var(--mint); }
.care-tag .van-icon { font-size: 14px; }
.note-block { background: var(--brand-mist); border-radius: 12px; padding: 10px 12px; font-size: 12.5px;
  margin-top: 10px; line-height: 1.8; color: var(--ink); }
.curve-card { margin: 12px 16px 0; padding: 14px 14px 8px; }
.curve-head { display: flex; justify-content: space-between; align-items: baseline; gap: 10px; }
.curve { height: 170px; }
.diary-content { margin-top: 9px; line-height: 1.75; }
.photo-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 7px; margin-top: 10px; }
.photo-item { aspect-ratio: 1; border-radius: 12px; overflow: hidden; }
.photo-item img { width: 100%; height: 100%; object-fit: cover; }
.review-card { margin: 16px 16px 0; padding: 15px; }

.review-pop { padding: 22px 18px 26px; }
.pop-title { text-align: center; font-weight: 800; margin-bottom: 14px; }
.pop-rate { display: flex; justify-content: center; margin-bottom: 14px; }
.pop-field { background: var(--bg); border-radius: 14px; }
.pop-btn { margin-top: 15px; font-weight: 800; }
</style>

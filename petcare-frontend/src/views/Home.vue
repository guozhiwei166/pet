<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import api, { type Pet, type OrderInfo, type DailyReport, ORDER_STATUS } from '../api'
import { useUserStore } from '../stores/user'
import * as echarts from 'echarts'

const router = useRouter()
const store = useUserStore()

const pets = ref<Pet[]>([])
const boardingOrders = ref<OrderInfo[]>([])
const reports = ref<Record<number, DailyReport[]>>({})
const storeInfo = ref<any>(null)
const announcements = ref<any[]>([])
const hotStories = ref<any[]>([])
const availableCoupons = ref<any[]>([])
const services = ref<any[]>([])
const curveRef = ref<HTMLElement | null>(null)
let chart: echarts.ECharts | null = null

const greeting = computed(() => {
  const h = new Date().getHours()
  return h < 11 ? '早上好' : h < 14 ? '中午好' : h < 18 ? '下午好' : '晚上好'
})

/** 快捷入口宫格 */
const quickLinks = [
  { path: '/boarding', icon: '🛏️', label: '我要寄养', desc: '预约寄养床位', color: '#EAF2ED' },
  { path: '/store', icon: '💈', label: '门店服务', desc: '洗澡美容摄影', color: '#FDF3E7' },
  { path: '/orders', icon: '📋', label: '我的订单', desc: '寄养与消费', color: '#E9F1FE' },
  { path: '/coupons', icon: '🎟️', label: '领券中心', desc: '优惠券领取', color: '#FDEBF1' },
  { path: '/pets', icon: '🐾', label: '我的宠物', desc: '宠物档案', color: '#EAF2ED' },
  { path: '/stories', icon: '💬', label: '故事墙', desc: '主人分享', color: '#FDF3E7' },
]

const hasBoardingData = computed(() =>
  boardingOrders.value.some(o => o.status === 'BOARDING' || o.status === 'CHECKED_IN'))

const featureOf = (s: string) => {
  const map: Record<string, string> = { 洗: '🛁', 美: '💇', 遛: '🦮', 摄: '📷', 接: '🚗', 喂: '💊', 特: '🍲' }
  for (const k of Object.keys(map)) if (s.includes(k)) return map[k]
  return '✨'
}

onMounted(async () => {
  try {
    const [petsData, ordersPage] = await Promise.all([
      api.pets(),
      api.orders('ALL', 1),
    ])
    pets.value = petsData
    boardingOrders.value = (ordersPage.records || []).filter(o => ['BOARDING', 'CHECKED_IN', 'CONFIRMED', 'PAID'].includes(o.status))
  } catch { /* 失败已由全局拦截器 toast；保持页面可交互 */ }
  // 首页附加区块数据
  try {
    storeInfo.value = await api.storeProfile()
    announcements.value = (await api.announcements()).slice(0, 3)
    hotStories.value = (await api.stories()).slice(0, 3)
    availableCoupons.value = (await api.availableCoupons()).slice(0, 3)
    services.value = (await api.services()).slice(0, 6)
  } catch { /* 忽略 */ }
  // 为寄养中的订单拉取最新日报
  for (const o of boardingOrders.value.filter(o => o.status === 'BOARDING' || o.status === 'CHECKED_IN')) {
    try { reports.value[o.id] = (await api.dailyReports(o.id)).slice(0, 1) } catch { /* 忽略 */ }
  }
  // 体重曲线
  const activeOrder = boardingOrders.value.find(o => o.status === 'BOARDING' || o.status === 'CHECKED_IN')
  if (activeOrder) {
    try {
      const growth = await api.growth(activeOrder.petId)
      if (growth.weightCurve.length >= 2) {
        await nextTick()
        if (curveRef.value) {
          chart = echarts.init(curveRef.value)
          chart.setOption({
            grid: { left: 40, right: 14, top: 20, bottom: 26 },
            tooltip: { trigger: 'axis' },
            xAxis: { type: 'category', data: growth.weightCurve.map(p => p.date.slice(5)), boundaryGap: false },
            yAxis: { type: 'value', scale: true },
            series: [{ type: 'line', smooth: true, data: growth.weightCurve.map(p => p.weight),
              lineStyle: { color: '#6FA58B', width: 3 }, itemStyle: { color: '#6FA58B' },
              areaStyle: { color: 'rgba(111,165,139,.14)' } }],
          })
        }
      }
    } catch { /* 忽略 */ }
  }
})

// 图表随窗口自适应，并在离开首页时销毁，避免路由切换反复进出后实例堆积
const resizeChart = () => chart?.resize()
onMounted(() => window.addEventListener('resize', resizeChart))
onUnmounted(() => {
  window.removeEventListener('resize', resizeChart)
  chart?.dispose()
  chart = null
})
</script>

<template>
  <div class="home">
    <!-- ========== 顶部主视觉：问候 + 当前寄养状态 ========== -->
    <div class="hero-card">
      <div class="hero-main">
        <div class="hero-greet">
          <h1>{{ greeting }}，{{ store.nickname }} 👋</h1>
          <p class="hero-sub">{{ storeInfo?.slogan || '让每一次寄养都像在家一样安心' }}</p>
        </div>
        <div v-if="pets.length" class="hero-pets">
          <div v-for="p in pets" :key="p.id" class="hero-pet" @click="router.push(`/pets/${p.id}`)">
            <img v-if="p.photo" :src="p.photo" :alt="p.name" />
            <span v-else class="chip-emoji">{{ p.species === 'CAT' ? '🐱' : '🐶' }}</span>
            <i>{{ p.name }}</i>
          </div>
          <el-button class="add-pet" circle @click="router.push('/pets?add=1')" title="添加宠物">+</el-button>
        </div>
        <div v-else class="hero-nopet">
          <span>还没有宠物档案 →</span>
          <el-button type="primary" round size="small" @click="router.push('/pets?add=1')">立即添加</el-button>
        </div>
      </div>
      <div class="hero-bg">
        <img src="/img/hero/golden.jpg" alt="" />
      </div>
    </div>

    <!-- ========== 快捷功能宫格 ========== -->
    <div class="quick-grid">
      <div v-for="q in quickLinks" :key="q.path" class="quick-item" :style="{ background: q.color }"
           @click="router.push(q.path)">
        <div class="quick-icon">{{ q.icon }}</div>
        <b>{{ q.label }}</b>
        <span class="sub">{{ q.desc }}</span>
      </div>
    </div>

    <!-- ========== 中部：有寄养时展示安排+曲线，否则广告横幅 ========== -->
    <div v-if="boardingOrders.length" class="mid-grid">
      <div class="pc-card" style="grid-column: span 2">
        <div class="block-title">📋 我的寄养安排</div>
        <el-row :gutter="14">
          <el-col v-for="o in boardingOrders" :key="o.id" :span="12">
            <div class="pc-card-md hoverable order-card" @click="router.push(`/orders/${o.id}`)">
              <div class="order-left">
                <div class="pet-thumb">
                  <img v-if="pets.find(p => p.id === o.petId)?.photo" :src="pets.find(p => p.id === o.petId)?.photo" alt="" />
                  <span v-else class="chip-emoji">{{ pets.find(p => p.id === o.petId)?.species === 'CAT' ? '🐱' : '🐶' }}</span>
                </div>
                <div class="order-mid">
                  <div class="cur-line">
                    <b>{{ pets.find(p => p.id === o.petId)?.name || '毛孩子' }}</b>
                    <el-tag :type="(ORDER_STATUS[o.status]?.type as any)" effect="light" size="small">
                      {{ ORDER_STATUS[o.status]?.text }}
                    </el-tag>
                  </div>
                  <div class="sub num">{{ o.startDate }} ~ {{ o.endDate }}（共 {{ o.days }} 晚）· ￥{{ o.totalAmount }}</div>
                  <div v-if="(reports[o.id] || []).length" class="report-brief">
                    <span class="sub">最新日报：</span>
                    <span class="sub2">精神 {{ reports[o.id][0].mentalState || '—' }} · 食欲 {{ reports[o.id][0].appetite || '—' }}
                      · 活动 {{ reports[o.id][0].activityMinutes }} 分钟</span>
                  </div>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>
      <div v-if="hasBoardingData" class="pc-card">
        <div class="block-title">📈 体重变化</div>
        <div ref="curveRef" style="height: 200px; margin-top: 8px"></div>
      </div>
      <div v-else class="pc-card promo-card">
        <img src="/img/hero/cat2.jpg" alt="猫咪" />
        <div class="promo-text">
          <b>毛孩子也需要一次旅行</b>
          <span class="sub">出差旅行时，给它订一个五星寄养小窝</span>
          <el-button type="primary" round size="small" style="margin-top:8px" @click="router.push('/boarding')">去预约寄养</el-button>
        </div>
      </div>
    </div>
    <div v-else class="pc-card promo-card wide" style="margin-top:16px">
      <img src="/img/hero/cat2.jpg" alt="猫咪" />
      <div class="promo-text">
        <b>所有毛孩子都在家里陪着你～</b>
        <span class="sub">需要出差或旅行时，记得提前给毛孩子订好寄养小窝</span>
        <el-button type="primary" round style="margin-top:10px" @click="router.push('/boarding')">去预约寄养</el-button>
      </div>
    </div>

    <!-- ========== 公告 + 门店卡片 ========== -->
    <div v-if="storeInfo" class="store-card" @click="router.push('/store')">
      <div class="store-left">
        <div class="store-icon">🏠</div>
        <div>
          <b class="store-name">{{ storeInfo.storeName }}</b>
          <div class="sub">{{ storeInfo.openHours }}</div>
          <div class="store-tags">
            <span v-for="f in (storeInfo.features || '').split(',').filter(Boolean).slice(0, 4)" :key="f">{{ f }}</span>
          </div>
        </div>
      </div>
      <div class="store-ann">
        <div v-for="a in announcements" :key="a.id" class="ann-line">
          <span class="ann-type" :class="'t-' + (a.type || 'NOTICE')">{{ a.type === 'ACTIVITY' ? '活动' : a.type === 'SAFETY' ? '安全' : '公告' }}</span>
          <b>{{ a.title }}</b>
          <span class="sub ellipsis">{{ a.content }}</span>
        </div>
      </div>
      <el-button type="primary" round class="store-go">进店看看 →</el-button>
    </div>

    <!-- ========== 门店服务横排 ========== -->
    <div v-if="services.length">
      <div class="block-title-row">
        <div class="block-title">💈 热门门店服务</div>
        <el-link type="primary" :underline="false" @click="router.push('/store')">全部服务 →</el-link>
      </div>
      <div class="svc-row">
        <div v-for="s in services" :key="s.id" class="pc-card-md svc-item hoverable" @click="router.push('/store')">
          <div class="svc-icon">{{ featureOf(s.name) }}</div>
          <div class="svc-name">{{ s.name }}</div>
          <div class="price num">￥{{ s.price }}<i class="sub">/{{ s.unit }}</i></div>
        </div>
      </div>
    </div>

    <!-- ========== 优惠券推荐 ========== -->
    <div v-if="availableCoupons.length">
      <div class="block-title-row">
        <div class="block-title">🎟️ 限时优惠</div>
        <el-link type="primary" :underline="false" @click="router.push('/coupons')">全部优惠 →</el-link>
      </div>
      <div class="coupon-row">
        <div v-for="c in availableCoupons" :key="c.id" class="coupon-mini" @click="router.push('/coupons')">
          <div class="coupon-val">
            <span v-if="c.type === 'DISCOUNT'">￥{{ c.amount }}</span>
            <span v-else-if="Number(c.percent) < 100">{{ Number(c.percent) / 10 }}折</span>
          </div>
          <div class="coupon-name">{{ c.name }}</div>
          <span class="sub">去领取 →</span>
        </div>
      </div>
    </div>

    <!-- ========== 故事墙 ========== -->
    <div v-if="hotStories.length">
      <div class="block-title-row">
        <div class="block-title">💬 主人故事</div>
        <el-link type="primary" :underline="false" @click="router.push('/stories')">更多故事 →</el-link>
      </div>
      <div class="stories-row">
        <div v-for="s in hotStories" :key="s.id" class="pc-card-md mini-story hoverable" @click="router.push('/stories')">
          <b>{{ s.title }}</b>
          <p class="sub2 ellipsis2">{{ s.content }}</p>
          <span class="sub">❤️ {{ s.likes }} · {{ s.author }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.home { display: flex; flex-direction: column; gap: 16px; }

/* ---- 顶部主视觉 ---- */
.hero-card {
  display: flex; justify-content: space-between; align-items: center; gap: 24px;
  background: linear-gradient(120deg, #2F4639 0%, #4A6B57 60%, #6FA58B 100%);
  color: #fff; border-radius: 20px; padding: 26px 32px; position: relative; overflow: hidden;
  min-height: 150px;
}
.hero-main { position: relative; z-index: 2; flex: 1; }
.hero-greet h1 { font-size: 26px; font-weight: 900; margin: 0 0 6px; }
.hero-sub { opacity: .85; font-size: 14px; margin: 0; }
.hero-pets { display: flex; align-items: center; gap: 14px; margin-top: 16px; flex-wrap: wrap; }
.hero-pet { display: flex; flex-direction: column; align-items: center; gap: 4px; cursor: pointer;
  transition: transform .15s; }
.hero-pet:hover { transform: translateY(-3px); }
.hero-pet img, .hero-pet .chip-emoji { width: 52px; height: 52px; border-radius: 50%; object-fit: cover;
  border: 2px solid rgba(255,255,255,.8); box-shadow: 0 6px 14px -4px rgba(0,0,0,.3); }
.hero-pet .chip-emoji { display: flex; align-items: center; justify-content: center;
  font-size: 24px; background: rgba(255,255,255,.9); }
.hero-pet i { font-style: normal; font-size: 12px; }
.add-pet { width: 52px; height: 52px; font-size: 20px; border: 2px dashed rgba(255,255,255,.6);
  background: rgba(255,255,255,.15); color: #fff; }
.hero-nopet { margin-top: 16px; display: flex; align-items: center; gap: 12px; font-size: 14px; opacity: .9; }
.hero-bg { position: absolute; right: -30px; top: 0; bottom: 0; width: 34%; opacity: .35; z-index: 1; }
.hero-bg img { width: 100%; height: 100%; object-fit: cover; }

/* ---- 快捷宫格 ---- */
.quick-grid { display: grid; grid-template-columns: repeat(6, 1fr); gap: 12px; }
.quick-item { border-radius: 16px; padding: 16px 10px; text-align: center; cursor: pointer;
  display: flex; flex-direction: column; align-items: center; gap: 4px;
  transition: transform .15s, box-shadow .15s; border: 1px solid transparent; }
.quick-item:hover { transform: translateY(-4px); box-shadow: 0 10px 22px -12px rgba(47,70,57,.35); }
.quick-icon { font-size: 26px; }
.quick-item b { font-size: 14px; }

/* ---- 中部 ---- */
.mid-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; }
.block-title { font-weight: 800; font-size: 16px; margin-bottom: 12px; }
.block-title-row { display: flex; justify-content: space-between; align-items: center; }
.order-card { margin-bottom: 12px; }
.order-left { display: flex; gap: 12px; width: 100%; }
.pet-thumb { width: 54px; height: 54px; border-radius: 14px; overflow: hidden; flex-shrink: 0;
  display: flex; align-items: center; justify-content: center; background: var(--pc-primary-soft); }
.pet-thumb img { width: 100%; height: 100%; object-fit: cover; }
.order-mid { flex: 1; min-width: 0; }
.cur-line { display: flex; justify-content: space-between; align-items: center; gap: 6px; }
.report-brief { background: var(--pc-primary-soft); border-radius: 8px; padding: 6px 10px; margin-top: 6px; }
.promo-card { display: flex; align-items: center; gap: 20px; }
.promo-card.wide { justify-content: center; text-align: left; }
.promo-card img { width: 150px; height: 100px; object-fit: cover; border-radius: 14px; }
.promo-text { display: flex; flex-direction: column; align-items: flex-start; }
.promo-text b { font-size: 18px; margin-bottom: 4px; }

/* ---- 门店卡 ---- */
.store-card { display: flex; align-items: center; gap: 20px; background: linear-gradient(120deg, #FDF6EC, #fff 60%);
  border-radius: 18px; padding: 18px 24px; cursor: pointer; border: 1px solid #F3E5D5;
  transition: transform .15s; }
.store-card:hover { transform: translateY(-2px); }
.store-left { display: flex; align-items: center; gap: 12px; min-width: 260px; }
.store-icon { width: 52px; height: 52px; border-radius: 16px; display: flex; align-items: center;
  justify-content: center; font-size: 26px; background: #FFE9D6; }
.store-name { font-size: 17px; }
.store-tags { display: flex; flex-wrap: wrap; gap: 6px; margin-top: 6px; }
.store-tags span { font-size: 11px; background: #fff; padding: 2px 8px; border-radius: 999px; color: #7A6A58; }
.store-ann { flex: 1; display: flex; flex-direction: column; gap: 8px; min-width: 0; }
.ann-line { display: flex; align-items: center; gap: 8px; font-size: 13px; }
.ann-line b { white-space: nowrap; }
.ellipsis { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.ann-type { font-size: 10px; font-weight: 800; color: #fff; background: #D9822B; border-radius: 4px; padding: 1px 7px; flex-shrink: 0; }
.ann-type.t-SAFETY { background: #D96C61; }
.ann-type.t-NOTICE { background: #7B827D; }
.store-go { flex-shrink: 0; }

/* ---- 服务横排 ---- */
.svc-row { display: grid; grid-template-columns: repeat(6, 1fr); gap: 12px; margin-top: 12px; }
.svc-item { text-align: center; padding: 16px 10px; }
.svc-icon { font-size: 26px; }
.svc-name { font-size: 14px; font-weight: 700; margin: 6px 0 2px; }
.price i { font-style: normal; }

/* ---- 优惠券 ---- */
.coupon-row { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; margin-top: 12px; }
.coupon-mini { background: linear-gradient(135deg, #FDF3E7, #FBDFC2); border-radius: 14px; padding: 14px 18px;
  display: flex; flex-direction: column; gap: 4px; cursor: pointer; position: relative; overflow: hidden;
  transition: transform .15s; }
.coupon-mini:hover { transform: translateY(-3px); }
.coupon-mini::after { content: ''; position: absolute; right: -12px; top: 50%; width: 24px; height: 24px;
  transform: translateY(-50%); border-radius: 50%; background: #F7F5F0; }
.coupon-val { font-size: 26px; font-weight: 900; color: #D9822B; }
.coupon-name { font-size: 14px; font-weight: 700; }

/* ---- 故事 ---- */
.stories-row { display: grid; grid-template-columns: repeat(3, 1fr); gap: 14px; margin-top: 12px; }
.mini-story b { font-size: 14px; }
.ellipsis2 { display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.chip-emoji { font-size: 18px; }
</style>

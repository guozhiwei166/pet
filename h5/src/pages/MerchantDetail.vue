<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '../api'
import { toneStyle } from '../utils/tone'
import Illust from '../components/Illust.vue'

const route = useRoute()
const router = useRouter()
const detail = ref(null)

onMounted(async () => {
  detail.value = await api.merchantDetail(route.params.id)
})

/** 店内环境相册：按可寄养物种生成场景与配色 */
const gallery = computed(() => {
  const m = detail.value?.merchant
  const catOnly = m?.serviceSpecies?.includes('CAT') && !m?.serviceSpecies?.includes('DOG')
  const items = [
    { label: '接待大厅', scene: 'shop' },
    { label: catOnly ? '猫咪公寓' : '独立犬舍', scene: catOnly ? 'tower' : 'house' },
    { label: '标准笼位', scene: 'box' },
    { label: '休息活动区', scene: 'sleep' },
  ]
  const tones = ['#FFE9D6', '#DFF2E8', '#E3EDFD', '#FFF1CE']
  return items.map((it, i) => ({ ...it, bg: `linear-gradient(160deg, ${tones[i]} 0%, #FFFFFF 130%)` }))
})

const roomScene = s => (s === 'CAT' ? 'tower' : 'house')
const roomTone = s => (s === 'CAT' ? '#E3EDFD' : '#FFE9D6')
</script>

<template>
  <div class="page" v-if="detail">
    <van-nav-bar left-arrow @click-left="router.back()" title="商家详情" />

    <!-- 店铺头部 -->
    <div class="hero hero-gradient">
      <div class="shop-line">
        <div class="avatar" :style="toneStyle(detail.merchant.shopName)">
          <Illust scene="shop" />
        </div>
        <div class="shop-main">
          <div class="shop-name">{{ detail.merchant.shopName }}</div>
          <div class="shop-tags">
            <span v-for="t in (detail.merchant.tags || '').split(',').filter(Boolean)" :key="t">{{ t }}</span>
          </div>
        </div>
        <div class="score num">★ {{ detail.merchant.rating }}</div>
      </div>
      <div class="metrics">
        <div><b class="num">{{ detail.merchant.rating }}</b><span>综合评分</span></div>
        <div><b class="num">{{ detail.merchant.replyRate }}%</b><span>回复率</span></div>
        <div><b class="num">{{ detail.merchant.boardingCount }}</b><span>累计寄养</span></div>
      </div>
    </div>

    <div class="body">
      <!-- 信任信息 -->
      <div class="clay trust-card">
        <div class="trust-line"><van-icon name="certificate" class="ok" />{{ detail.merchant.license || '资质信息待补充' }}</div>
        <div class="trust-line"><van-icon name="location-o" />{{ detail.merchant.address }}</div>
        <div class="trust-line"><van-icon name="clock-o" />{{ detail.merchant.serviceSpecies?.includes('CAT') && detail.merchant.serviceSpecies?.includes('DOG') ? '猫犬均可寄养' : (detail.merchant.serviceSpecies?.includes('CAT') ? '猫咪专属寄养' : '狗狗寄养') }}</div>
        <p class="desc sub2">{{ detail.merchant.description }}</p>
      </div>

      <!-- 店内环境相册 -->
      <div class="sec">店内环境</div>
      <div class="gallery">
        <div v-for="g in gallery" :key="g.label" class="gallery-item press">
          <div class="gallery-bg" :style="g.bg"><Illust :scene="g.scene" /></div>
          <span class="gallery-label">{{ g.label }}</span>
        </div>
      </div>

      <!-- 房型 -->
      <div class="sec">可选房型</div>
      <div v-for="r in detail.rooms" :key="r.id" class="clay room-card press">
        <div class="room-photo" :style="{ background: roomTone(r.species) }">
          <Illust :scene="roomScene(r.species)" />
        </div>
        <div class="room-info">
          <div class="room-name">{{ r.name }}</div>
          <div class="room-tags">
            <span v-for="t in (r.tags || '').split(',').filter(Boolean)" :key="t" class="chip chip-warm">{{ t }}</span>
          </div>
          <div class="sub2 room-desc">{{ r.description }}</div>
          <div class="room-bottom">
            <span class="price num">￥{{ r.pricePerDay }}<i>/天</i></span>
            <van-button size="small" type="primary" round class="room-btn press"
                        @click="router.push(`/booking?merchantId=${detail.merchant.id}&roomId=${r.id}`)">预订</van-button>
          </div>
        </div>
      </div>

      <!-- 评价 -->
      <div class="sec">家长评价（{{ detail.reviews.length }}）</div>
      <div v-for="r in detail.reviews" :key="r.id" class="clay review-card">
        <div class="review-top">
          <div class="reviewer">
            <span class="avatar-mini">{{ r.nickname.slice(0, 1) }}</span>
            <b>{{ r.nickname }}</b>
          </div>
          <van-rate :model-value="r.rating" readonly size="12px" color="#FFB637" void-color="#EFE7DE" />
        </div>
        <div class="review-content sub2">{{ r.content }}</div>
        <div v-if="r.reply" class="reply">商家回复：{{ r.reply }}</div>
      </div>
      <div v-if="!detail.reviews.length" class="empty-wrap">
        <Illust scene="sleep" style="width: 170px; display:block; margin: 0 auto;" />
        <div class="sub" style="text-align:center; margin-top: 6px;">还没有评价，等第一位家长来点评～</div>
      </div>
      <div style="height: 20px"></div>
    </div>

    <!-- 底部预订栏 -->
    <div class="book-bar clay" v-if="detail.rooms.length">
      <div class="bar-price num">￥{{ Math.min(...detail.rooms.map(r => Number(r.pricePerDay))) }}<i> 起/天</i></div>
      <van-button type="primary" round class="bar-btn press"
                  @click="router.push(`/booking?merchantId=${detail.merchant.id}`)">立即预订</van-button>
    </div>
  </div>
</template>

<style scoped>
.hero { margin-top: -46px; padding: 56px 16px 18px; border-radius: 0 0 28px 28px; }
.shop-line { display: flex; align-items: center; }
.avatar { width: 68px; height: 68px; border-radius: 20px; overflow: hidden; flex-shrink: 0; box-shadow: 0 6px 14px -6px rgba(0,0,0,.25); }
.shop-main { flex: 1; margin-left: 13px; min-width: 0; }
.shop-name { font-size: 19px; font-weight: 900; }
.shop-tags { margin-top: 7px; display: flex; gap: 6px; flex-wrap: wrap; }
.shop-tags span { background: rgba(255,255,255,.24); border-radius: 999px; font-size: 11px; padding: 3px 10px; }
.score { font-size: 17px; font-weight: 900; background: rgba(255,255,255,.22); padding: 5px 10px; border-radius: 999px; }
.metrics { display: flex; margin-top: 16px; background: rgba(255,255,255,.16); border-radius: 16px; padding: 10px 0; }
.metrics > div { flex: 1; display: flex; flex-direction: column; align-items: center; }
.metrics b { font-size: 17px; }
.metrics span { font-size: 11px; opacity: .9; margin-top: 2px; }
.body { padding: 0 16px; }
.trust-card { margin-top: 14px; padding: 16px; }
.trust-line { display: flex; align-items: center; gap: 8px; font-size: 13px; margin: 5px 0; }
.trust-line .van-icon { color: var(--mint); font-size: 16px; }
.desc { margin-top: 10px; line-height: 1.8; }
.room-card { display: flex; margin-top: 12px; padding: 12px; }
.gallery { display: flex; gap: 10px; overflow-x: auto; padding: 2px 2px 6px; scrollbar-width: none; }
.gallery::-webkit-scrollbar { display: none; }
.gallery-item { flex: 0 0 150px; height: 104px; border-radius: 14px; overflow: hidden; position: relative; box-shadow: var(--clay); }
.gallery-bg { width: 100%; height: 100%; }
.gallery-label { position: absolute; left: 8px; bottom: 8px; background: rgba(255,255,255,.92);
  border-radius: 999px; font-size: 11px; font-weight: 700; padding: 2px 9px; color: var(--ink-2); }
.room-photo { width: 92px; height: 92px; border-radius: 16px; overflow: hidden; flex-shrink: 0; }
.room-info { flex: 1; margin-left: 13px; min-width: 0; display: flex; flex-direction: column; }
.room-name { font-weight: 800; font-size: 15px; }
.room-tags { margin: 6px 0 4px; display: flex; flex-wrap: wrap; gap: 5px; }
.room-desc { white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.room-bottom { display: flex; justify-content: space-between; align-items: center; margin-top: auto; }
.price { font-size: 16px; }
.room-btn { font-weight: 700; box-shadow: var(--clay-brand); }
.review-card { margin-top: 12px; padding: 14px; }
.review-top { display: flex; justify-content: space-between; align-items: center; }
.reviewer { display: flex; align-items: center; gap: 8px; font-size: 13px; }
.avatar-mini { width: 28px; height: 28px; border-radius: 50%; background: var(--brand-soft); color: var(--brand-deep);
  display: inline-flex; align-items: center; justify-content: center; font-weight: 800; font-size: 13px; }
.review-content { margin-top: 9px; line-height: 1.7; }
.reply { background: var(--brand-mist); border-radius: 10px; padding: 9px 11px; font-size: 12px; color: var(--ink-2); margin-top: 9px; line-height: 1.6; }
.book-bar { position: fixed; left: 12px; right: 12px; bottom: calc(12px + env(safe-area-inset-bottom));
  padding: 10px 12px 10px 20px; display: flex; justify-content: space-between; align-items: center;
  border-radius: 999px; z-index: 10; }
.bar-price { color: var(--brand-deep); font-weight: 900; font-size: 20px; }
.bar-price i { font-style: normal; font-size: 11px; color: var(--ink-3); font-weight: 500; }
.bar-btn { width: 138px; font-weight: 800; box-shadow: var(--clay-brand); }
</style>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '../api'
import { useUserStore } from '../store/user'
import { toneStyle } from '../utils/tone'
import Illust from '../components/Illust.vue'

const router = useRouter()
const store = useUserStore()

const keyword = ref('')
const species = ref('')
const list = ref([])
const page = ref(1)
const finished = ref(false)
const loading = ref(false)
const refreshing = ref(false)

async function load(reset = false) {
  if (reset) { page.value = 1; finished.value = false }
  loading.value = true
  try {
    const data = await api.home({ page: page.value, size: 10, keyword: keyword.value, species: species.value })
    const records = data.records || []
    list.value = reset ? records : list.value.concat(records)
    finished.value = list.value.length >= data.total
    page.value++
  } catch (e) { finished.value = true } finally {
    loading.value = false
    refreshing.value = false
  }
}

function pickSpecies(v) { species.value = v; load(true) }
function onSearch() { load(true) }
function onRefresh() { load(true) }

onMounted(() => {
  load(true)
  api.unreadCount().then(n => store.setUnread(n)).catch(() => {})
})
</script>

<template>
  <div class="page">
    <!-- 顶部品牌区 -->
    <div class="hero hero-gradient">
      <div class="hero-row">
        <div class="brand-lock">
          <svg viewBox="0 0 24 24" width="26" height="26" fill="#fff" aria-hidden="true">
            <ellipse cx="7" cy="8.2" rx="2.1" ry="2.7"/><ellipse cx="17" cy="8.2" rx="2.1" ry="2.7"/>
            <ellipse cx="3.6" cy="13" rx="1.9" ry="2.4"/><ellipse cx="20.4" cy="13" rx="1.9" ry="2.4"/>
            <path d="M12 11c3.6 0 6.4 2.7 6.4 5.6 0 2.1-1.7 3.4-3.6 3.4-1.1 0-1.9-.4-2.8-.4-.9 0-1.7.4-2.8.4-1.9 0-3.6-1.3-3.6-3.4C5.6 13.7 8.4 11 12 11z"/>
          </svg>
          <span>毛孩驿站</span>
        </div>
        <span class="date-chip" v-if="list.length">{{ list.length }} 家持证商家</span>
      </div>

      <van-search v-model="keyword" placeholder="搜索商家、地址…" background="transparent" shape="round"
                  @search="onSearch" class="search" />

      <!-- 品牌横幅 -->
      <div class="banner clay">
        <div class="banner-text">
          <b>寄养日记，看得见的安心</b>
          <span>商家每日打卡反馈，毛孩子的每一天都有记录</span>
        </div>
        <div class="banner-ill"><Illust scene="hero" /></div>
      </div>

      <div class="trust">
        <span><van-icon name="certificate" /> 持证商家审核</span>
        <span><van-icon name="chat-square" /> 每日寄养日记</span>
        <span><van-icon name="signed" /> 不满意随时退</span>
      </div>
    </div>

    <!-- 分类筛选 -->
    <div class="cats">
      <span :class="['cat-chip press', { on: species === '' }]" @click="pickSpecies('')">
        全部
      </span>
      <span :class="['cat-chip press', { on: species === 'DOG' }]" @click="pickSpecies('DOG')">
        <svg viewBox="0 0 24 24" width="15" height="15" aria-hidden="true"><g fill="currentColor"><ellipse cx="7.5" cy="7" rx="2" ry="2.6"/><ellipse cx="16.5" cy="7" rx="2" ry="2.6"/><ellipse cx="4" cy="12" rx="1.8" ry="2.3"/><ellipse cx="20" cy="12" rx="1.8" ry="2.3"/><path d="M12 10c3.4 0 6 2.6 6 5.4 0 2-1.6 3.2-3.4 3.2-1 0-1.8-.4-2.6-.4-.8 0-1.6.4-2.6.4C7.6 18.6 6 17.4 6 15.4 6 12.6 8.6 10 12 10z"/></g></svg>
        狗狗寄养
      </span>
      <span :class="['cat-chip press', { on: species === 'CAT' }]" @click="pickSpecies('CAT')">
        <svg viewBox="0 0 24 24" width="15" height="15" aria-hidden="true"><g fill="currentColor"><path d="M6 9.5 4.6 4.4l5 2.2Z"/><path d="M18 9.5 19.4 4.4l-5 2.2Z"/><path d="M12 19.6c-4 0-6.8-2.6-6.8-6 0-3 2.4-5.4 6.8-5.4s6.8 2.4 6.8 5.4c0 3.4-2.8 6-6.8 6Z"/></g></svg>
        猫咪寄养
      </span>
    </div>

    <!-- 商家列表 -->
    <van-pull-refresh v-model="refreshing" success-text="已刷新" @refresh="onRefresh">
      <!-- 首次加载骨架屏 -->
      <template v-if="loading && !list.length">
        <div v-for="i in 3" :key="'sk' + i" class="card shop-card clay">
          <van-skeleton title :row="2" row-width="60%" avatar avatar-shape="square" />
        </div>
      </template>
      <van-list v-else v-model:loading="loading" :finished="finished" finished-text="没有更多啦" @update:loading="l => l && page > 1 && load()">
        <article v-for="m in list" :key="m.id" class="card shop-card clay press" @click="router.push(`/merchants/${m.id}`)">
          <div class="cover" :style="toneStyle(m.shopName)">
            <Illust scene="shop" />
            <span class="rate-badge num">★ {{ m.rating }}</span>
          </div>
          <div class="info">
            <div class="name">{{ m.shopName }}</div>
            <div class="tags">
              <span v-for="t in (m.tags || '').split(',').filter(Boolean).slice(0, 2)" :key="t" class="chip chip-warm">{{ t }}</span>
              <span class="chip chip-gray">已寄养 {{ m.boardingCount }} 单</span>
            </div>
            <div class="addr sub2"><van-icon name="location-o" /> {{ m.address }}</div>
            <div class="bottom-line">
              <span class="price num">￥{{ m.minPrice }}<i>/天起</i></span>
              <span class="book-cta">预订 <van-icon name="arrow" /></span>
            </div>
          </div>
        </article>
      </van-list>
      <div v-if="finished && !list.length" class="empty-wrap">
        <Illust scene="sleep" style="width: 180px; margin: 0 auto; display: block;" />
        <div class="sub" style="text-align:center; margin-top: 6px;">附近还没有寄养商家～</div>
      </div>
    </van-pull-refresh>
  </div>
</template>

<style scoped>
.hero { padding: 14px 16px 18px; border-radius: 0 0 28px 28px; }
.hero-row { display: flex; justify-content: space-between; align-items: center; padding: 4px 4px 10px; }
.brand-lock { display: flex; align-items: center; gap: 8px; font-size: 19px; font-weight: 900; letter-spacing: 1px; }
.date-chip { font-size: 12px; font-weight: 600; background: rgba(255,255,255,.22);
  padding: 7px 12px; border-radius: 999px; }
.search { padding: 0 0 10px; }
:deep(.van-search__content) { box-shadow: var(--clay); }
.banner { display: flex; align-items: center; padding: 14px 16px 14px 18px; margin-bottom: 12px; }
.banner-text { flex: 1; display: flex; flex-direction: column; gap: 5px; }
.banner-text b { font-size: 16px; }
.banner-text span { font-size: 12px; color: var(--ink-2); line-height: 1.6; }
.banner-ill { width: 118px; height: 74px; flex-shrink: 0; }
.trust { display: flex; gap: 8px; padding: 2px 2px 0; }
.trust span { display: inline-flex; align-items: center; background: rgba(255,255,255,.92);
  color: var(--brand-deep); font-size: 11px; font-weight: 700; border-radius: 999px; padding: 4px 11px;
  box-shadow: 0 3px 8px -3px rgba(190, 106, 58, .25); }
.trust .van-icon { margin-right: 3px; }

.cats { display: flex; gap: 10px; padding: 16px 16px 4px; }
.cat-chip { display: inline-flex; align-items: center; gap: 5px; background: #fff; border-radius: 999px;
  padding: 8px 15px; font-size: 13px; font-weight: 600; color: var(--ink-2); box-shadow: var(--clay); }
.cat-chip.on { background: var(--brand); color: #fff; box-shadow: var(--clay-brand); }

.shop-card { display: flex; margin: 14px 16px; padding: 12px; overflow: hidden; }
.cover { width: 104px; height: 104px; border-radius: 16px; overflow: hidden; flex-shrink: 0; position: relative; }
.rate-badge { position: absolute; top: 8px; left: 8px; background: rgba(255,255,255,.92); color: var(--ink);
  font-size: 11px; font-weight: 800; border-radius: 999px; padding: 2px 8px; }
.rate-badge::first-letter { color: var(--sun); }
.info { flex: 1; margin-left: 13px; display: flex; flex-direction: column; min-width: 0; }
.name { font-weight: 800; font-size: 15.5px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.tags { margin: 7px 0 6px; display: flex; gap: 6px; white-space: nowrap; overflow: hidden; }
.addr { white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.addr .van-icon { color: var(--brand); }
.bottom-line { display: flex; justify-content: space-between; align-items: center; margin-top: auto; }
.price { font-size: 17px; }
.book-cta { display: inline-flex; align-items: center; background: var(--brand-soft); color: var(--brand-deep);
  font-size: 12px; font-weight: 700; border-radius: 999px; padding: 6px 12px; }
</style>

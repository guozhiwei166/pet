<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api, { STATUS_MAP, fmtDate } from '../api'
import { toneStyle } from '../utils/tone'
import Illust from '../components/Illust.vue'

const router = useRouter()
const active = ref(0)
const tabs = [
  { label: '全部', value: 'ALL' },
  { label: '待确认', value: 'PENDING_CONFIRM' },
  { label: '寄养中', value: 'BOARDING' },
  { label: '已完成', value: 'COMPLETED' },
]
const list = ref([])
const loading = ref(false)
const refreshing = ref(false)

async function load() {
  loading.value = true
  try {
    const data = await api.orders({ page: 1, size: 50, status: tabs[active.value].value })
    list.value = data.records || []
  } finally { loading.value = false; refreshing.value = false }
}

function onChange() { load() }
function onRefresh() { load() }
onMounted(load)
</script>

<template>
  <div class="page">
    <van-nav-bar title="我的订单" />
    <van-tabs v-model:active="active" @change="onChange" color="#FF6B3D" title-active-color="#FF6B3D"
              line-width="22px" background="transparent">
      <van-tab v-for="t in tabs" :key="t.value" :title="t.label" />
    </van-tabs>

    <van-loading v-if="loading" vertical class="loading">加载中...</van-loading>

    <van-pull-refresh v-else v-model="refreshing" success-text="已刷新" @refresh="onRefresh">
      <template>
        <div v-for="o in list" :key="o.id" class="card order-card clay press"
             @click="router.push(`/orders/${o.id}`)">
          <div class="top-line">
            <span class="shop"><van-icon name="shop-o" /> {{ o.shopName }}</span>
            <span class="status-chip" :style="{ color: STATUS_MAP[o.status]?.color, background: STATUS_MAP[o.status]?.color + '18' }">
              {{ STATUS_MAP[o.status]?.text }}
            </span>
          </div>
          <div class="mid-line">
            <div class="pet-avatar" :style="toneStyle(o.petName || 'pet')">
              <span class="pet-emoji">{{ o.petSpecies === 'CAT' ? '🐱' : '🐶' }}</span>
            </div>
            <div class="mid-info">
              <div class="pet-name">{{ o.petName }} · {{ o.roomName }}</div>
              <div class="sub num">{{ fmtDate(o.startDate) }} ~ {{ fmtDate(o.endDate) }}（{{ o.days }} 晚）</div>
            </div>
            <div class="amount price num">￥{{ o.totalAmount }}</div>
          </div>
        </article>
        <div v-if="!list.length" class="empty-wrap">
          <Illust scene="sleep" style="width: 180px; margin: 0 auto; display: block;" />
          <div class="sub" style="text-align:center; margin-top: 6px;">还没有订单，去给毛孩子找个好归宿吧～</div>
        </div>
      </template>
    </van-pull-refresh>
  </div>
</template>

<style scoped>
.loading { margin: 60px 0; }
.order-card { margin: 14px 16px; padding: 15px 16px; }
.top-line { display: flex; justify-content: space-between; align-items: center; }
.shop { font-weight: 800; font-size: 14px; display: inline-flex; align-items: center; gap: 5px; }
.shop .van-icon { color: var(--brand); }
.status-chip { font-weight: 700; font-size: 11.5px; border-radius: 999px; padding: 3px 10px; }
.mid-line { display: flex; align-items: center; margin-top: 13px; }
.pet-avatar { width: 46px; height: 46px; border-radius: 50%; flex-shrink: 0; display: flex; align-items: center; justify-content: center; font-size: 20px; }
.pet-emoji { filter: drop-shadow(0 2px 2px rgba(0,0,0,.08)); }
.mid-info { flex: 1; margin-left: 11px; min-width: 0; }
.pet-name { font-weight: 700; font-size: 14px; }
.amount { font-size: 17px; }
</style>

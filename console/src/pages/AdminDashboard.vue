<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import api from '../api'

const stats = ref(null)
const lineRef = ref(null)
const pieRef = ref(null)
const barRef = ref(null)
const charts = []

const cards = [
  { key: 'users', label: '宠物主人', icon: '👪', bg: '#E8F3FF' },
  { key: 'merchants', label: '入驻商家', icon: '🏠', bg: '#FFF1E6' },
  { key: 'pets', label: '宠物档案', icon: '🐾', bg: '#FFE8DD' },
  { key: 'orders', label: '寄养订单', icon: '📦', bg: '#E6F7EF' },
  { key: 'revenue', label: '累计营收(元)', icon: '💰', bg: '#FFF7D6', money: true },
]

const resize = () => charts.forEach(c => c.resize())

onMounted(async () => {
  stats.value = await api.adminStats()
  // 等待 v-if 的图表容器完成渲染后再初始化 ECharts
  await nextTick()
  const s = stats.value

  const line = echarts.init(lineRef.value)
  line.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['订单量', '营收(元)'], top: 0 },
    grid: { left: 46, right: 56, top: 40, bottom: 30 },
    xAxis: { type: 'category', data: s.daily.map(d => d.date) },
    yAxis: [
      { type: 'value', name: '订单量', minInterval: 1 },
      { type: 'value', name: '营收(元)' },
    ],
    series: [
      { name: '订单量', type: 'line', smooth: true, data: s.daily.map(d => d.cnt),
        itemStyle: { color: '#FF7D4D' }, areaStyle: { color: 'rgba(255,125,77,.15)' } },
      { name: '营收(元)', type: 'line', smooth: true, yAxisIndex: 1,
        data: s.daily.map(d => d.amount), itemStyle: { color: '#409EFF' } },
    ],
  })
  charts.push(line)

  const pie = echarts.init(pieRef.value)
  pie.setOption({
    tooltip: {},
    legend: { bottom: 0 },
    series: [{
      type: 'pie', radius: ['42%', '68%'], data: s.species,
      color: ['#FF9A62', '#8FB0FF'], itemStyle: { borderRadius: 8 },
      label: { formatter: '{b}\n{c} 只' },
    }],
  })
  charts.push(pie)

  const top = [...s.topMerchants].reverse()
  const bar = echarts.init(barRef.value)
  bar.setOption({
    tooltip: {},
    grid: { left: 130, right: 40, top: 14, bottom: 30 },
    xAxis: { type: 'value', minInterval: 1 },
    yAxis: { type: 'category', data: top.map(t => t.name) },
    series: [{
      type: 'bar', data: top.map(t => t.cnt), barWidth: 18,
      itemStyle: { color: '#FFB38A', borderRadius: 6 },
      label: { show: true, position: 'right' },
    }],
  })
  charts.push(bar)

  window.addEventListener('resize', resize)
})

onUnmounted(() => window.removeEventListener('resize', resize))
</script>

<template>
  <div v-if="stats">
    <div class="stat-grid">
      <div v-for="c in cards" :key="c.key" class="stat-card">
        <div class="stat-icon" :style="{ background: c.bg }">{{ c.icon }}</div>
        <div>
          <div class="stat-value">{{ c.money ? '￥' : '' }}{{ stats.overview[c.key] ?? 0 }}</div>
          <div class="stat-label">{{ c.label }}</div>
        </div>
      </div>
    </div>

    <div class="chart-row">
      <div class="page-card chart-card">
        <b>近 7 日订单与营收趋势</b>
        <div ref="lineRef" class="chart"></div>
      </div>
    </div>

    <div class="chart-row half">
      <div class="page-card chart-card">
        <b>寄养宠物物种分布</b>
        <div ref="pieRef" class="chart"></div>
      </div>
      <div class="page-card chart-card">
        <b>商家接单量 Top5</b>
        <div ref="barRef" class="chart"></div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.stat-grid { display: grid; grid-template-columns: repeat(5, 1fr); gap: 14px; }
.chart-row { margin-top: 16px; }
.chart-row.half { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.chart-card b { font-size: 15px; }
.chart { height: 300px; margin-top: 10px; }
</style>

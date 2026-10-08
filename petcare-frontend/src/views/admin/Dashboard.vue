<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import http from '../../api/http'

const data = ref<any>(null)
const trendDays = ref(7)
const trendRef = ref<HTMLElement | null>(null)
const pieRef = ref<HTMLElement | null>(null)
const svcRef = ref<HTMLElement | null>(null)
const staffRef = ref<HTMLElement | null>(null)
let charts: echarts.ECharts[] = []

const resize = () => charts.forEach(c => c.resize())

async function renderTrend(days: number) {
  const s = await http.get<never, any[]>('/admin/stats/trend', { params: { days } })
  await nextTick()
  // 复用已有实例，避免重复 init
  const line = charts.find(c => c.getDom() === trendRef.value) ?? echarts.init(trendRef.value)
  line.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['订单量', '营业额(元)'], top: 0 },
    grid: { left: 46, right: 56, top: 36, bottom: 28 },
    xAxis: { type: 'category', data: s.map(x => x.date) },
    yAxis: [{ type: 'value', name: '订单', minInterval: 1 }, { type: 'value', name: '元' }],
    series: [
      { name: '订单量', type: 'line', smooth: true, data: s.map(x => x.orders),
        itemStyle: { color: '#6FA58B' }, areaStyle: { color: 'rgba(111,165,139,.15)' } },
      { name: '营业额(元)', type: 'line', smooth: true, yAxisIndex: 1, data: s.map(x => x.revenue),
        itemStyle: { color: '#F3A65A' } },
    ],
  })
  if (!charts.includes(line)) charts.push(line)
}

onMounted(async () => {
  data.value = await http.get<never, any>('/admin/dashboard')
  await new Promise(r => setTimeout(r, 30))

  await renderTrend(trendDays.value)

  const pie = echarts.init(pieRef.value)
  pie.setOption({
    tooltip: {},
    legend: { bottom: 0 },
    series: [{ type: 'pie', radius: ['45%', '68%'],
      data: [
        { name: '状态正常', value: data.value.petNormal },
        { name: '今日异常', value: data.value.petAbnormal },
      ],
      color: ['#6FA58B', '#D96C61'], itemStyle: { borderRadius: 8 },
      label: { formatter: '{b}\n{c}' } }],
  })
  charts.push(pie)

  const svc = await http.get<never, any[]>('/admin/stats/services')
  const svcTop = [...svc].reverse()
  const svcChart = echarts.init(svcRef.value)
  svcChart.setOption({
    tooltip: {},
    grid: { left: 90, right: 40, top: 10, bottom: 26 },
    xAxis: { type: 'value', minInterval: 1 },
    yAxis: { type: 'category', data: svcTop.map(s => s.name) },
    series: [{ type: 'bar', barWidth: 16, data: svcTop.map(s => s.value),
      itemStyle: { color: '#F3A65A', borderRadius: 6 }, label: { show: true, position: 'right' } }],
  })
  charts.push(svcChart)

  const staffStats = await http.get<never, any[]>('/admin/stats/staff')
  const staffChart = echarts.init(staffRef.value)
  staffChart.setOption({
    tooltip: {},
    grid: { left: 90, right: 40, top: 10, bottom: 26 },
    xAxis: { type: 'value', max: 100 },
    yAxis: { type: 'category', data: staffStats.map(s => s.name) },
    series: [{ type: 'bar', barWidth: 16, data: staffStats.map(s => s.rate),
      itemStyle: { color: '#6FA58B', borderRadius: 6 }, label: { show: true, position: 'right', formatter: '{c}%' } }],
  })
  charts.push(staffChart)

  window.addEventListener('resize', resize)
})

onUnmounted(() => {
  window.removeEventListener('resize', resize)
  charts.forEach(c => c.dispose())
  charts = []
})
</script>

<template>
  <div v-if="data">
    <div class="stat-grid">
      <div class="stat-card"><div class="stat-icon st-gold"><el-icon :size="22"><Money /></el-icon></div>
        <div><div class="stat-value num">￥{{ data.todayRevenue }}</div><div class="stat-label">今日营业额</div></div></div>
      <div class="stat-card"><div class="stat-icon st-green"><el-icon :size="22"><House /></el-icon></div>
        <div><div class="stat-value num">{{ data.occupying }}<i class="stat-unit">/{{ data.roomTotal }}</i></div>
        <div class="stat-label">当前入住（{{ data.roomUsage }}%）</div></div></div>
      <div class="stat-card"><div class="stat-icon st-blue"><el-icon :size="22"><Van /></el-icon></div>
        <div><div class="stat-value num">{{ data.todayCheckin }}</div><div class="stat-label">今日入住</div></div></div>
      <div class="stat-card"><div class="stat-icon st-gray"><el-icon :size="22"><HomeFilled /></el-icon></div>
        <div><div class="stat-value num">{{ data.todayCheckout }}</div><div class="stat-label">今日离店</div></div></div>
      <div class="stat-card"><div class="stat-icon st-red"><el-icon :size="22"><WarningFilled /></el-icon></div>
        <div><div class="stat-value num">{{ data.petAbnormal }}</div><div class="stat-label">今日异常</div></div></div>
    </div>

    <div class="pc-card" style="margin-top: 16px">
      <div style="display:flex; justify-content:space-between; align-items:center">
        <b>营业趋势</b>
        <el-radio-group v-model="trendDays" size="small" @change="renderTrend(trendDays)">
          <el-radio-button :value="7">近 7 天</el-radio-button>
          <el-radio-button :value="30">近 30 天</el-radio-button>
        </el-radio-group>
      </div>
      <div ref="trendRef" style="height: 300px; margin-top: 8px"></div>
    </div>

    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="8">
        <div class="pc-card"><b>宠物状态分布（今日日报）</b><div ref="pieRef" style="height: 250px"></div></div>
      </el-col>
      <el-col :span="8">
        <div class="pc-card"><b>热门增值服务</b><div ref="svcRef" style="height: 250px"></div></div>
      </el-col>
      <el-col :span="8">
        <div class="pc-card"><b>员工任务完成率</b><div ref="staffRef" style="height: 250px"></div></div>
      </el-col>
    </el-row>
  </div>
</template>

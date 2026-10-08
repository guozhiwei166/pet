<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../../api/http'
import { useUserStore } from '../../stores/user'

const store = useUserStore()

interface Task {
  id: number; orderId: number; petId: number; taskType: string; taskDate: string
  planTime: string; title: string; content: string; status: string; remark?: string
  petName?: string; petNotes?: string; roomNo?: string
}

const date = ref(new Date().toLocaleDateString('sv-SE'))
const wb = ref<{ total: number; done: number; pending: number } | null>(null)
const tasks = ref<Task[]>([])
const typeFilter = ref('ALL')

const TYPE_META: Record<string, { text: string; icon: string; cls: string }> = {
  FEEDING: { text: '喂食', icon: 'food', cls: 'warm' },
  WATER: { text: '饮水', icon: 'milk-tea', cls: 'sky' },
  WALK: { text: '遛宠', icon: 'Sunny', cls: 'mint' },
  CLEANING: { text: '清洁', icon: 'Brush', cls: 'gray' },
  MEDICATION: { text: '喂药', icon: 'FirstAidKit', cls: 'rose' },
  PHOTO: { text: '拍照', icon: 'Camera', cls: 'sky' },
  HEALTH_CHECK: { text: '健康观察', icon: 'View', cls: 'mint' },
  CHECKIN_CHECK: { text: '入住检查', icon: 'Checked', cls: 'warm' },
  CHECKOUT_CHECK: { text: '离店检查', icon: 'Finished', cls: 'gray' },
}

const doneRate = computed(() => {
  const t = wb.value?.total || 0
  return t === 0 ? 0 : Math.round(((wb.value?.done || 0) * 100) / t)
})

const completeVisible = ref(false)
const current = ref<Task | null>(null)
const completeForm = ref<Record<string, any>>({})

const bookings = ref<any[]>([])

async function load() {
  const wbData = await http.get<never, any>('/care/workbench', { params: { date: date.value } })
  wb.value = wbData
  tasks.value = await http.get<never, Task[]>('/care/tasks', {
    params: { date: date.value, type: typeFilter.value, status: 'ALL' },
  })
  try {
    bookings.value = (await http.get<never, any[]>('/service-bookings', {
      params: { date: date.value },
    })) || []
  } catch { bookings.value = [] }
}

const BOOKING_STATUS: Record<string, { text: string; type: string }> = {
  PENDING: { text: '待确认', type: 'warning' },
  CONFIRMED: { text: '已确认', type: 'primary' },
  DONE: { text: '已完成', type: 'success' },
  CANCELLED: { text: '已取消', type: 'info' },
}

async function confirmBooking(id: number, action: string) {
  await http.post(`/service-bookings/${id}/${action}`)
  load()
}

function openComplete(t: Task) {
  current.value = t
  // 体温只在健康观察（HEALTH_CHECK）有输入框，入住/离店检查不能带默认体温，
  // 否则会把从未测量的 38.5 当成体检数据写入宠物健康档案
  const vitalDefaults = { weight: undefined, mentalState: '正常', appetite: '正常', water: '正常', poop: '正常' }
  completeForm.value = t.taskType === 'FEEDING'
    ? { food: t.content, amount: '150g', appetite: '正常' }
    : t.taskType === 'WALK' ? { durationMinutes: 30, mood: '活跃' }
    : t.taskType === 'HEALTH_CHECK' ? { ...vitalDefaults, temperature: 38.5 }
    : t.taskType.includes('CHECK') ? { ...vitalDefaults }
    : { remark: '' }
  completeVisible.value = true
}

async function submitComplete() {
  if (!current.value) return
  // 健康类任务：任一体征异常即标记 isAbnormal，驱动驾驶舱异常统计
  const f = completeForm.value
  const payload = { ...f }
  if (current.value.taskType.includes('CHECK')) {
    payload.isAbnormal =
      (f.mentalState === '异常' || f.appetite === '未进食' || f.poop === '腹泻' || f.isAbnormal === 1) ? 1 : 0
  }
  await http.post(`/care/tasks/${current.value.id}/complete`, payload)
  ElMessage.success('✓ 任务已完成')
  completeVisible.value = false
  load()
}

async function skip(t: Task) {
  await http.post(`/care/tasks/${t.id}/skip`, { remark: '员工跳过' })
  ElMessage.info('已跳过')
  load()
}

const showRemark = (t: string) => !['FEEDING', 'WALK'].includes(t)
const needWeight = (t: string) => t.includes('CHECK')

onMounted(load)
</script>

<template>
  <div>
    <!-- 顶部问候横幅 -->
    <div class="pc-card wb-head">
      <div class="wb-left">
        <h2 class="page-title">早上好，{{ store.nickname }} 👋</h2>
        <p class="page-desc num">{{ date }} · 今日任务安排</p>
        <div class="wb-ring-wrap">
          <el-progress type="dashboard" :percentage="doneRate" :width="86" :show-text="false"
                       :color="doneRate === 100 ? '#4FA482' : '#6FA58B'" />
          <div class="wb-ring-label">
            <b class="num">{{ doneRate }}%</b>
            <span>今日完成率</span>
          </div>
        </div>
      </div>
      <div class="wb-stats">
        <div class="wb-stat"><b class="num">{{ wb?.total ?? 0 }}</b><span>今日任务</span></div>
        <div class="wb-stat"><b class="num ok">{{ wb?.done ?? 0 }}</b><span>已完成</span></div>
        <div class="wb-stat"><b class="num warn">{{ wb?.pending ?? 0 }}</b><span>待处理</span></div>
      </div>
    </div>

    <!-- 筛选 -->
    <div class="pc-card" style="margin: 14px 0; padding: 12px 16px; display: flex; gap: 12px; align-items: center">
      <span class="sub">日期</span>
      <el-date-picker v-model="date" type="date" value-format="YYYY-MM-DD" style="width: 150px" @change="load" />
      <span class="sub">类型</span>
      <el-select v-model="typeFilter" style="width: 140px" @change="load">
        <el-option label="全部" value="ALL" />
        <el-option v-for="(m, k) in TYPE_META" :key="k" :label="m.text" :value="k" />
      </el-select>
    </div>

    <!-- 今日门店预约 -->
    <div v-if="bookings.length" class="pc-card bookings-card">
      <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom: 10px">
        <b>💈 今日门店预约（{{ bookings.length }}）</b>
        <span class="sub">洗澡 / 美容 / 遛宠 / 摄影</span>
      </div>
      <div class="booking-row">
        <div v-for="b in bookings" :key="b.id" class="booking-chip">
          <b class="num">{{ b.bookTime }}</b>
          <span>{{ b.serviceName }}</span>
          <el-tag :type="(BOOKING_STATUS[b.status]?.type as any)" effect="light" size="small">
            {{ BOOKING_STATUS[b.status]?.text }}
          </el-tag>
          <template v-if="b.status === 'PENDING'">
            <el-button size="small" type="primary" text @click="confirmBooking(b.id, 'CONFIRM')">确认</el-button>
          </template>
          <template v-else-if="b.status === 'CONFIRMED'">
            <el-button size="small" type="success" text @click="confirmBooking(b.id, 'DONE')">完成</el-button>
          </template>
        </div>
      </div>
    </div>

    <!-- 任务卡流 -->
    <div class="task-grid">
      <div v-for="t in tasks" :key="t.id" class="pc-card-md task-card" :class="{ done: t.status === 'DONE' }">
        <div class="task-head">
          <span class="t-icon" :class="TYPE_META[t.taskType]?.cls">
            <el-icon><component :is="TYPE_META[t.taskType]?.icon || 'List'" /></el-icon>
          </span>
          <div style="flex: 1">
            <b>{{ t.title }}</b>
            <div class="sub num">{{ t.planTime }} · {{ t.petName }} · {{ t.roomNo }}</div>
          </div>
          <el-tag :type="t.status === 'DONE' ? 'success' : t.status === 'SKIPPED' ? 'info' : 'warning'"
                  effect="light" size="small">
            {{ t.status === 'DONE' ? '已完成' : t.status === 'SKIPPED' ? '已跳过' : '待处理' }}
          </el-tag>
        </div>
        <div class="task-content sub">{{ t.content }}</div>
        <div v-if="t.petNotes" class="task-notes">⚠ {{ t.petNotes }}</div>
        <div class="task-ops" v-if="t.status === 'PENDING'">
          <el-button type="primary" size="small" round @click="openComplete(t)">完成任务</el-button>
          <el-button size="small" round plain @click="skip(t)">跳过</el-button>
        </div>
        <div v-else class="task-ops sub">✓ 记录已保存</div>
      </div>
    </div>
    <el-empty v-if="!tasks.length" description="今天没有任务，好好休息" />

    <!-- 完成任务弹窗 -->
    <el-dialog v-model="completeVisible" :title="`完成任务：${current?.title}（${current?.petName}）`" width="440px">
      <el-form label-width="90px">
        <template v-if="current?.taskType === 'FEEDING'">
          <el-form-item label="食物"><el-input v-model="completeForm.food" /></el-form-item>
          <el-form-item label="份量"><el-input v-model="completeForm.amount" placeholder="如：150g" /></el-form-item>
          <el-form-item label="食欲">
            <el-radio-group v-model="completeForm.appetite">
              <el-radio value="正常">正常</el-radio><el-radio value="偏少">偏少</el-radio><el-radio value="未进食">未进食</el-radio>
            </el-radio-group>
          </el-form-item>
        </template>
        <template v-else-if="current?.taskType === 'WALK'">
          <el-form-item label="活动时长"><el-input-number v-model="completeForm.durationMinutes" :min="5" :max="120" /> 分钟</el-form-item>
          <el-form-item label="状态">
            <el-radio-group v-model="completeForm.mood">
              <el-radio value="很活跃">很活跃</el-radio><el-radio value="正常">正常</el-radio><el-radio value="疲惫">疲惫</el-radio>
            </el-radio-group>
          </el-form-item>
        </template>
        <template v-if="needWeight(current?.taskType || '')">
          <el-form-item label="体重(kg)"><el-input-number v-model="completeForm.weight" :min="0" :precision="2" /></el-form-item>
          <el-form-item v-if="current?.taskType === 'HEALTH_CHECK'" label="体温(℃)">
            <el-input-number v-model="completeForm.temperature" :min="35" :max="42" :precision="1" />
          </el-form-item>
          <el-form-item label="精神">
            <el-radio-group v-model="completeForm.mentalState">
              <el-radio value="很活跃">很活跃</el-radio><el-radio value="正常">正常</el-radio>
              <el-radio value="一般">一般</el-radio><el-radio value="异常">异常</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="食欲">
            <el-radio-group v-model="completeForm.appetite">
              <el-radio value="正常">正常</el-radio><el-radio value="偏少">偏少</el-radio><el-radio value="未进食">未进食</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="排便">
            <el-radio-group v-model="completeForm.poop">
              <el-radio value="正常">正常</el-radio><el-radio value="偏软">偏软</el-radio>
              <el-radio value="腹泻">腹泻</el-radio><el-radio value="未观察">未观察</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="标记异常">
            <el-switch v-model="completeForm.isAbnormal" :active-value="1" :inactive-value="0" />
            <span class="sub" style="margin-left: 8px">打开后驾驶舱异常统计 +1，并提醒管理员</span>
          </el-form-item>
        </template>
        <el-form-item label="备注"><el-input v-model="completeForm.remark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="completeVisible = false">取消</el-button>
        <el-button type="primary" @click="submitComplete">完成任务</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.wb-head { display: flex; justify-content: space-between; align-items: center;
  background: linear-gradient(120deg, #EAF2ED 0%, #fff 60%); }
.wb-left { display: flex; flex-direction: column; gap: 4px; }
.wb-ring-wrap { position: relative; width: 86px; height: 86px; margin-top: 6px; }
.wb-ring-wrap :deep(.el-progress) { display: block; }
.wb-ring-label { position: absolute; inset: 0; display: flex; flex-direction: column;
  align-items: center; justify-content: center; pointer-events: none; }
.wb-ring-label b { font-size: 16px; font-weight: 900; color: var(--pc-primary-deep); }
.wb-ring-label span { font-size: 10px; color: var(--pc-ink-2); }
.wb-stats { display: flex; gap: 30px; align-items: center; }
.wb-stat { display: flex; flex-direction: column; align-items: center; }
.wb-stat b { font-size: 22px; font-weight: 900; }
.wb-stat b.ok { color: #4FA482; }
.wb-stat b.warn { color: var(--pc-accent); }
.wb-stat span { font-size: 11px; color: var(--pc-ink-2); }
.task-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(330px, 1fr)); gap: 14px; }
.task-card.done { opacity: .6; }
.task-head { display: flex; gap: 10px; align-items: center; }
.t-icon { width: 36px; height: 36px; border-radius: 11px; display: flex; align-items: center; justify-content: center; }
.t-icon.warm { background: #FBEEDD; color: #C97B2C; }
.t-icon.mint { background: #EAF2ED; color: #5A8F76; }
.t-icon.sky { background: #E9F1FE; color: #4A8FE0; }
.t-icon.rose { background: #F9E8E6; color: #D96C61; }
.t-icon.gray { background: #F2F0EA; color: #7B827D; }
.task-content { margin: 8px 0 4px; }
.task-notes { background: var(--pc-danger-soft); color: #B5574D; border-radius: 8px;
  padding: 6px 10px; font-size: 12px; margin: 6px 0; }
.task-ops { display: flex; gap: 8px; margin-top: 10px; align-items: center; }
</style>

.bookings-card { margin-bottom: 16px; }
.booking-row { display: flex; flex-wrap: wrap; gap: 10px; }
.booking-chip { display: flex; align-items: center; gap: 10px; background: var(--pc-bg);
  padding: 8px 14px; border-radius: 12px; font-size: 13px; }

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import api, { ORDER_STATUS } from '../../api'

const route = useRoute()
const router = useRouter()
const vo = ref<any>(null)
const reports = ref<any[]>([])
const showReview = ref(false)
const review = ref({ ratingOverall: 5, ratingEnv: 5, ratingService: 5, ratingStaff: 5, ratingPetStatus: 5, content: '', tags: [] as string[] })
const reviewTags = ['环境干净', '服务很好', '工作人员负责', '反馈及时', '宠物状态好']

const stepIndex = computed(() => {
  const s = vo.value?.order.status
  const flow = ['PENDING_PAYMENT', 'PAID', 'CONFIRMED', 'CHECKED_IN', 'BOARDING', 'COMPLETED']
  if (s === 'PENDING_PAYMENT' && vo.value.order.payTime) return 1
  return flow.indexOf(s) < 0 ? 0 : flow.indexOf(s)
})

/** 取消/退款类终态：流程条不适用（此前会错误地把第 1 步"待支付"高亮为当前进度） */
const terminated = computed(() => ['CANCELLED', 'REFUNDING', 'REFUNDED'].includes(vo.value?.order.status))

async function load() {
  vo.value = await api.orderDetail(route.params.id as string)
  try { reports.value = await api.dailyReports(vo.value.order.id) } catch { reports.value = [] }
}

async function submitReview() {
  await api.reviewOrder(vo.value.order.id, review.value)
  ElMessage.success('感谢你的评价')
  showReview.value = false
  load()
}

async function pay() {
  await ElMessageBox.confirm(`订单金额 ￥${vo.value.order.totalAmount}（演示环境模拟支付）`, '确认支付', {
    confirmButtonText: '立即支付', type: 'info',
  })
  await api.payOrder(vo.value.order.id)
  ElMessage.success('支付成功')
  load()
}

async function cancel() {
  const { value } = await ElMessageBox.prompt('请输入取消原因', '取消订单', {
    inputPlaceholder: '如：行程有变', confirmButtonText: '确认取消', type: 'warning',
  })
  await api.cancelOrder(vo.value.order.id, value || '')
  ElMessage.success('订单已取消')
  load()
}

async function refund() {
  await ElMessageBox.confirm(
    `确定申请退款？\n\n退款去向：原路退回至支付账户（${vo.value.order.payTime ? '模拟支付渠道，预计 1-3 个工作日到账' : ''}）。\n申请后需管理员审批，请留意订单状态与消息通知。`,
    '退款申请', { type: 'warning', confirmButtonText: '确认申请', cancelButtonText: '再想想' })
  await api.refundOrder(vo.value.order.id)
  ElMessage.success('退款申请已提交，待管理员审批')
  load()
}

// ---------- 升级房型（补差价） ----------
const upgradeVisible = ref(false)
const upgradeTargets = ref<any[]>([])
const upgradeTargetId = ref<number | null>(null)
const upgrading = ref(false)

/** 计算升级到某房型的差价 */
const upgradeDiff = (rt: any) => {
  const order = vo.value.order
  const oldTotal = Number(order.roomAmount) || 0
  const newRoom = Number(rt.price) * Number(order.days || 1)
  return Math.max(0, Math.round((newRoom - oldTotal) * 100) / 100)
}

async function openUpgrade() {
  const order = vo.value.order
  try {
    // 取目标日期内可订房型：仅同分区（猫区订单只能升猫房，犬区订单只能升犬房）且比当前更贵
    const av: any[] = await api.availability({ startDate: order.startDate, endDate: order.endDate })
    const curSpecies = vo.value.roomType?.species === 'CAT' ? 'CAT' : 'DOG'
    const curPrice = Number(vo.value.roomType?.price) || 0
    upgradeTargets.value = av
      .filter((a: any) => a.onSale && a.bookable
        && (a.roomType.species === 'CAT' ? 'CAT' : 'DOG') === curSpecies
        && Number(a.roomType.price) > curPrice)
      .map((a: any) => a.roomType)
    upgradeTargetId.value = null
    if (!upgradeTargets.value.length) {
      ElMessage.info('暂无可升级的同分区更高价房型（或均已订满）')
      return
    }
    upgradeVisible.value = true
  } catch { /* 忽略 */ }
}

async function confirmUpgrade() {
  if (!upgradeTargetId.value) return ElMessage.warning('请选择目标房型')
  const rt = upgradeTargets.value.find((t: any) => t.id === upgradeTargetId.value)
  const diff = upgradeDiff(rt)
  upgrading.value = true
  try {
    const res: any = await api.upgradeRoom(vo.value.order.id, upgradeTargetId.value)
    ElMessage.success(`已升级为 ${rt?.name || ''}，补差价 ￥${res.diffAmount ?? diff}（模拟渠道已支付）`)
    upgradeVisible.value = false
    load()
  } finally {
    upgrading.value = false
  }
}

/** 支付流水文字说明：金额 + 去向 */
const payText = (p: any) => {
  const amt = `￥${Number(p.amount ?? 0).toFixed(2)}`
  if (p.status === 'SUCCESS') return `${amt} · 支付成功（模拟渠道）`
  if (p.status === 'REFUNDED') return `${amt} · 已原路退回支付账户`
  if (p.status === 'REFUNDING') return `${amt} · 退款处理中`
  if (p.status === 'FAILED') return `${amt} · 退款失败（原路退回未成功）`
  if (p.status === 'CANCELLED') return `${amt} · 已取消`
  return `${amt} · ${p.status}`
}

onMounted(load)
</script>

<template>
  <div v-if="vo">
    <div class="pc-card">
      <div class="status-line">
        <div>
          <b class="order-no num">{{ vo.order.orderNo }}</b>
          <el-tag :type="(ORDER_STATUS[vo.order.status]?.type as any)" effect="light" style="margin-left: 10px">
            {{ ORDER_STATUS[vo.order.status]?.text }}
          </el-tag>
        </div>
        <div class="ops">
          <el-button v-if="vo.order.status === 'PENDING_PAYMENT'" type="primary" round @click="pay">立即支付</el-button>
          <el-button v-if="['PENDING_PAYMENT', 'PAID'].includes(vo.order.status)" round @click="cancel">取消订单</el-button>
          <el-button v-if="['PENDING_PAYMENT', 'PAID', 'CONFIRMED'].includes(vo.order.status)"
                     round plain type="primary" @click="openUpgrade">升级房型</el-button>
          <el-button v-if="['PAID', 'CONFIRMED'].includes(vo.order.status)" round plain type="warning" @click="refund">申请退款</el-button>
          <el-button v-if="vo.order.status === 'COMPLETED' && !vo.review" type="primary" round plain @click="showReview = true">评价本次寄养</el-button>
        </div>
      </div>
      <el-steps v-if="!terminated" :active="stepIndex" align-center finish-status="success" style="margin-top: 18px">
        <el-step title="待支付" />
        <el-step title="已支付" />
        <el-step title="已预约" />
        <el-step title="已入住" />
        <el-step title="寄养中" />
        <el-step title="已完成" />
      </el-steps>
      <el-alert v-else :closable="false" show-icon style="margin-top: 18px"
                :type="vo.order.status === 'REFUNDING' ? 'warning' : 'info'"
                :title="vo.order.status === 'CANCELLED' ? '订单已取消，服务流程已终止'
                        : vo.order.status === 'REFUNDING' ? '退款处理中，订单流程已暂停'
                        : '订单已退款，服务流程已终止'" />
      <div v-if="vo.order.cancelReason" class="sub" style="margin-top: 10px">取消原因：{{ vo.order.cancelReason }}</div>
    </div>

    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="14">
        <div class="pc-card">
          <h4 style="margin-bottom: 12px">寄养信息</h4>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="毛孩子">{{ vo.pet?.name }}（{{ vo.pet?.breed }}）</el-descriptions-item>
            <el-descriptions-item label="房型">{{ vo.roomType?.name }}<template v-if="vo.room"> · {{ vo.room.roomNo }} 号房</template></el-descriptions-item>
            <el-descriptions-item label="寄养日期">
              {{ vo.order.startDate }} ~ {{ vo.order.endDate }}（{{ vo.order.days }} 晚）
            </el-descriptions-item>
            <el-descriptions-item label="备注">{{ vo.order.remark || '无' }}</el-descriptions-item>
            <el-descriptions-item v-if="vo.order.checkinTime" label="入住时间">{{ vo.order.checkinTime }}</el-descriptions-item>
            <el-descriptions-item v-if="vo.order.checkoutTime" label="离店时间">{{ vo.order.checkoutTime }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </el-col>
      <el-col :span="10">
        <div class="pc-card">
          <h4 style="margin-bottom: 12px">费用明细</h4>
          <div class="pd-line"><span>住宿（{{ vo.order.days }} 晚 × ￥{{ vo.order.roomAmount / vo.order.days }}）</span>
            <span class="num">￥{{ vo.order.roomAmount }}</span></div>
          <div v-for="s in vo.services" :key="s.id" class="pd-line">
            <span>{{ s.serviceName }} ×{{ s.quantity }}</span><span class="num">￥{{ s.amount }}</span>
          </div>
          <div v-if="Number(vo.order.discountAmount) > 0" class="pd-line">
            <span>优惠券抵扣</span><span class="num" style="color:#2E9E6B">-￥{{ vo.order.discountAmount }}</span>
          </div>
          <div class="pd-line total"><span>总计</span><span class="price num">￥{{ vo.order.totalAmount }}</span></div>
          <el-divider>支付 / 退款流水</el-divider>
          <div v-for="p in vo.payments" :key="p.id" class="pd-line">
            <div style="display:flex; flex-direction:column; gap:2px">
              <span class="sub num">{{ p.paymentNo }}</span>
              <span class="sub" style="font-size:12px">{{ payText(p) }}</span>
            </div>
            <el-tag size="small" :type="p.status === 'SUCCESS' ? 'success' : p.status === 'REFUNDED' ? 'warning' : 'info'" effect="light">
              {{ p.status === 'SUCCESS' ? '支付成功' : p.status === 'REFUNDED' ? '已退款' : p.status === 'REFUNDING' ? '退款中' : p.status === 'FAILED' ? '退款失败' : p.status }}
            </el-tag>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 照护日报 -->
    <div class="pc-card" v-if="reports.length" style="margin-top: 16px">
      <h4 style="margin-bottom: 12px">📖 每日照护日报</h4>
      <div v-for="r in reports" :key="r.id" class="report-card">
        <div class="report-head">
          <b class="num">{{ r.reportDate }}</b>
          <el-tag :type="r.isAbnormal ? 'danger' : 'success'" effect="light" size="small">
            {{ r.isAbnormal ? '有异常' : '状态良好' }}
          </el-tag>
        </div>
        <div class="report-grid">
          <div><span class="sub">精神</span><b>{{ r.mentalState || '—' }}</b></div>
          <div><span class="sub">食欲</span><b>{{ r.appetite || '—' }}</b></div>
          <div><span class="sub">饮水</span><b>{{ r.water || '—' }}</b></div>
          <div><span class="sub">排便</span><b>{{ r.poop || '—' }}</b></div>
          <div><span class="sub">活动</span><b class="num">{{ r.activityMinutes }} 分钟</b></div>
        </div>
        <div class="sub" v-if="r.mealsSummary">饮食：{{ r.mealsSummary }}</div>
        <div v-if="r.staffNote" class="sub" style="margin-top: 4px">工作人员备注：{{ r.staffNote }}</div>
      </div>
    </div>

    <!-- 已有评价 -->
    <div class="pc-card" v-if="vo.review" style="margin-top: 16px">
      <h4>我的评价</h4>
      <el-rate :model-value="vo.review.ratingOverall" disabled style="margin: 8px 0" />
      <p style="font-size: 14px; margin: 6px 0">{{ vo.review.content }}</p>
      <div v-if="vo.review.reply" class="sub">商家回复：{{ vo.review.reply }}</div>
    </div>

    <!-- 升级房型弹窗 -->
    <el-dialog v-model="upgradeVisible" title="升级房型（补差价）" width="520px">
      <div class="upgrade-tip">
        当前房型：<b>{{ vo.roomType?.name }}</b> ￥{{ vo.roomType?.price }}/天 × {{ vo.order.days }} 晚
        <br />升级到更高价房型，需补差价（模拟渠道即时支付）。
      </div>
      <div v-for="t in upgradeTargets" :key="t.id"
           :class="['upgrade-item', { on: upgradeTargetId === t.id }]"
           @click="upgradeTargetId = t.id">
        <img v-if="t.photo" :src="t.photo" class="upgrade-img" alt="" />
        <div class="upgrade-main">
          <b>{{ t.name }}</b>
          <span class="sub">￥{{ t.price }}/天 · {{ t.area }}㎡ · {{ t.facilities }}</span>
        </div>
        <div class="upgrade-diff num">补差价 +￥{{ upgradeDiff(t) }}</div>
      </div>
      <template #footer>
        <el-button @click="upgradeVisible = false">取消</el-button>
        <el-button type="primary" :loading="upgrading" @click="confirmUpgrade">确认升级并支付差价</el-button>
      </template>
    </el-dialog>

    <!-- 评价弹窗 -->
    <el-dialog v-model="showReview" title="评价本次寄养" width="480px">
      <el-form label-width="90px">
        <el-form-item v-for="(label, key) in ({ ratingOverall: '整体体验', ratingEnv: '环境', ratingService: '服务', ratingStaff: '工作人员', ratingPetStatus: '宠物状态' })" :key="key" :label="label">
          <el-rate v-model="(review as any)[key]" />
        </el-form-item>
        <el-form-item label="评价内容">
          <el-input v-model="review.content" type="textarea" :rows="3" placeholder="说说这次寄养的体验吧" />
        </el-form-item>
        <el-form-item label="标签">
          <el-select v-model="review.tags" multiple placeholder="选择标签" style="width: 100%">
            <el-option v-for="t in reviewTags" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showReview = false">取消</el-button>
        <el-button type="primary" @click="submitReview">发布评价</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.status-line { display: flex; justify-content: space-between; align-items: center; }
.order-no { font-size: 17px; font-weight: 800; margin-right: 6px; }
.ops { display: flex; gap: 10px; }
.pd-line { display: flex; justify-content: space-between; font-size: 13.5px; margin: 8px 0; }
.report-card { border: 1px solid var(--pc-line); border-radius: 12px; padding: 12px; margin-bottom: 10px; }
.report-head { display: flex; justify-content: space-between; align-items: center; }
.report-grid { display: grid; grid-template-columns: repeat(5, 1fr); gap: 8px; margin: 10px 0 6px; }
.report-grid div { background: var(--pc-bg); border-radius: 8px; padding: 6px 8px;
  display: flex; flex-direction: column; gap: 2px; font-size: 13px; }
.pd-line.total { font-weight: 800; font-size: 16px; border-top: 1px dashed var(--pc-line); padding-top: 10px; }
.upgrade-tip { background: var(--pc-primary-soft); border-radius: 10px; padding: 10px 14px;
  font-size: 13px; line-height: 1.7; margin-bottom: 12px; }
.upgrade-item { display: flex; align-items: center; gap: 12px; border: 1.5px solid var(--pc-line);
  border-radius: 12px; padding: 10px 12px; margin-bottom: 8px; cursor: pointer; transition: all .15s; }
.upgrade-item.on { border-color: var(--pc-primary); background: var(--pc-primary-soft); }
.upgrade-img { width: 64px; height: 46px; border-radius: 8px; object-fit: cover; flex-shrink: 0; }
.upgrade-main { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 3px; }
.upgrade-diff { color: #C97B2C; font-weight: 800; flex-shrink: 0; }
</style>

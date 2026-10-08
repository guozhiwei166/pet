<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast, showConfirmDialog, showDialog } from 'vant'
import api, { fmtDate } from '../api'
import { toneStyle } from '../utils/tone'
import Illust from '../components/Illust.vue'

const route = useRoute()
const router = useRouter()
const merchantId = Number(route.query.merchantId)

const step = ref(1)
const pets = ref([])
const petId = ref(null)
const startDate = ref(null)
const endDate = ref(null)
const showCalendar = ref(false)
const rooms = ref([])
const roomId = ref(null)
const contactName = ref('')
const contactPhone = ref('')
const remark = ref('')
const agreed = ref(false)
const submitting = ref(false)

const pet = computed(() => pets.value.find(p => p.id === petId.value))
const room = computed(() => rooms.value.find(r => r.room.id === roomId.value))
const days = computed(() => {
  if (!startDate.value || !endDate.value) return 0
  return Math.round((endDate.value - startDate.value) / 86400000)
})
const rangeText = computed(() =>
  startDate.value && endDate.value ? `${fmtDate(startDate.value)} 至 ${fmtDate(endDate.value)}` : '请选择寄养日期')
const totalPrice = computed(() => {
  if (!room.value || !days.value) return '0.00'
  return (room.value.room.pricePerDay * days.value).toFixed(2)
})

onMounted(async () => {
  pets.value = await api.pets()
  contactPhone.value = localStorage.getItem('ps_user')
    ? (JSON.parse(localStorage.getItem('ps_user')).phone || '') : ''
  if (!pets.value.length) {
    await showDialog({ title: '先添加毛孩子', message: '寄养前需要先建立宠物档案（品种、体重、疫苗信息），商家会据此安排合适的笼位。' })
    router.replace('/pets/edit')
  }
})

function onDatesConfirm(vals) {
  startDate.value = vals[0]
  endDate.value = vals[1]
  showCalendar.value = false
}

async function goMatch() {
  if (!petId.value) return showToast('请选择要寄养的毛孩子')
  const data = await api.matchRooms({
    petId: petId.value, merchantId,
    start: fmtDate(startDate.value), end: fmtDate(endDate.value),
  })
  rooms.value = data
  const pre = data.find(r => r.room.id === Number(route.query.roomId))
  roomId.value = pre ? pre.room.id : null
  step.value = 2
}

function pickRoom(id) { roomId.value = id }

async function submitOrder() {
  if (!roomId.value) return showToast('请选择房型')
  if (!contactName.value || !contactPhone.value) return showToast('请填写联系人信息')
  if (!agreed.value) return showToast('请先阅读并同意寄养协议')
  submitting.value = true
  try {
    const order = await api.createOrder({
      petId: petId.value, roomId: roomId.value,
      startDate: fmtDate(startDate.value), endDate: fmtDate(endDate.value),
      contactName: contactName.value, contactPhone: contactPhone.value,
      remark: remark.value, agreementSigned: true,
    })
    try {
      await showConfirmDialog({
        title: '确认支付',
        message: `订单金额 ￥${order.totalAmount}\n（演示环境模拟支付，不产生真实扣款）`,
        confirmButtonText: '立即支付',
      })
      await api.payOrder(order.id)
      showToast({ type: 'success', message: '支付成功' })
    } catch (e) {
      showToast('已跳过支付，可在订单中继续支付')
    }
    router.replace(`/orders/${order.id}`)
  } finally {
    submitting.value = false
  }
}

async function onAgreeClick() {
  try {
    await showConfirmDialog({
      title: '宠物寄养服务协议',
      message: '1. 寄养前请确认宠物疫苗齐全且在有效期内；\n2. 如实填写宠物性格与健康信息，隐瞒造成的损失由主人承担；\n3. 商家每日提供喂食、清洁与反馈服务；\n4. 寄养期间突发疾病，商家将第一时间联系主人并送医；\n5. 未消费天数支持按天退款。',
      confirmButtonText: '同意',
    })
    agreed.value = true
  } catch (e) { agreed.value = false }
}
</script>

<template>
  <div class="page">
    <van-nav-bar left-arrow @click-left="router.back()" title="预约寄养" />

    <!-- 步骤指示 -->
    <div class="steps-wrap">
      <div v-for="(s, i) in ['选宠物日期', '智能选房型', '确认支付']" :key="s"
           :class="['step', { on: step >= i + 1, now: step === i + 1 }]">
        <span class="dot num">{{ step > i + 1 ? '✓' : i + 1 }}</span>
        <span class="step-label">{{ s }}</span>
        <span v-if="i < 2" class="bar" :class="{ fill: step > i + 1 }"></span>
      </div>
    </div>

    <!-- 第一步：宠物 + 日期 -->
    <template v-if="step === 1">
      <div class="sec">谁要寄养？</div>
      <div class="pet-grid">
        <div v-for="p in pets" :key="p.id" :class="['pet-card clay press', { on: petId === p.id }]" @click="petId = p.id">
          <div class="pet-avatar" :style="toneStyle(p.name)">
            <Illust :scene="p.species === 'CAT' ? 'tower' : 'house'" />
          </div>
          <div class="pet-name">{{ p.name }}</div>
          <div class="sub">{{ p.breed || '未知品种' }}</div>
          <div class="sub num">{{ p.weight }}kg</div>
        </div>
      </div>

      <div class="sec">什么时候入住？</div>
      <div class="clay date-card press" @click="showCalendar = true">
        <div class="date-main">
          <van-icon name="calendar-o" />
          <b>{{ rangeText }}</b>
        </div>
        <span v-if="days > 0" class="chip chip-warm num">共 {{ days }} 晚</span>
        <span v-else class="chip chip-gray">点我选择</span>
      </div>

      <van-button type="primary" round block class="btn-main press" @click="goMatch">
        查看智能推荐的房型
      </van-button>
      <div class="ai-hint"><van-icon name="magic" /> 已按体型、性格、绝育情况为你筛选</div>
    </template>

    <!-- 第二步：智能排笼结果 -->
    <template v-if="step === 2">
      <div class="clay match-tip">
        <van-icon name="magic" class="tip-icon" />
        <span>已根据 <b>{{ pet?.name }}</b> 的体型（{{ pet?.weight }}kg）、性格与绝育情况，从本店筛选出 <b>{{ rooms.length }}</b> 个可订房型</span>
      </div>
      <div v-for="r in rooms" :key="r.room.id" :class="['clay room-card press', { on: roomId === r.room.id }]"
           @click="pickRoom(r.room.id)">
        <div class="room-photo" :style="{ background: r.room.species === 'CAT' ? '#E3EDFD' : '#FFE9D6' }">
          <Illust :scene="r.room.species === 'CAT' ? 'tower' : 'house'" />
        </div>
        <div class="room-body">
          <div class="room-head">
            <span class="room-name">{{ r.room.name }}</span>
            <span class="price num">￥{{ r.room.pricePerDay }}<i>/天</i></span>
          </div>
          <div class="match-reasons">
            <span v-if="r.recommend" class="chip chip-rose">最优推荐</span>
            <span v-for="t in r.reasons" :key="t" class="chip chip-mint">{{ t }}</span>
          </div>
          <div class="avail sub">当前日期剩余 <b class="price num">{{ r.available }}</b> 间</div>
        </div>
        <span class="check-ring" :class="{ on: roomId === r.room.id }">
          <van-icon name="success" />
        </span>
      </div>
      <div v-if="!rooms.length" class="empty-wrap">
        <Illust scene="sleep" style="width: 180px; display:block; margin: 0 auto;" />
        <div class="sub" style="text-align:center; margin-top: 6px;">这些日期没有符合条件的房型，换个日期试试～</div>
      </div>
      <div class="btn-row">
        <van-button round class="step-btn press" @click="step = 1">上一步</van-button>
        <van-button type="primary" round class="step-btn press" :disabled="!roomId" @click="step = 3">下一步</van-button>
      </div>
    </template>

    <!-- 第三步：确认订单 -->
    <template v-if="step === 3">
      <div class="clay sum-card">
        <div class="sum-line"><span>毛孩子</span><b>{{ pet?.name }}（{{ pet?.breed }}）</b></div>
        <div class="sum-line"><span>寄养日期</span><b class="num">{{ rangeText }}</b></div>
        <div class="sum-line"><span>房型</span><b>{{ room?.room.name }}</b></div>
        <div class="sum-line price-line num">
          <span>￥{{ room?.room.pricePerDay }} × {{ days }} 晚</span>
          <b class="price total">￥{{ totalPrice }}</b>
        </div>
      </div>

      <van-cell-group inset class="form">
        <van-field v-model="contactName" label="联系人" placeholder="姓名" left-icon="contact-o" />
        <van-field v-model="contactPhone" label="联系电话" type="tel" maxlength="11" left-icon="phone-o" />
        <van-field v-model="remark" label="备注" type="textarea" rows="2" autosize
                   placeholder="毛孩子的生活习惯、禁忌等（如：怕打雷需安抚）" />
      </van-cell-group>

      <div class="agree" @click="onAgreeClick">
        <van-checkbox v-model="agreed" shape="round" icon-size="16px" @click.stop />
        <span>我已阅读并同意 <b class="agree-link">《宠物寄养服务协议》</b></span>
      </div>

      <div class="btn-row">
        <van-button round class="step-btn press" @click="step = 2">上一步</van-button>
        <van-button type="primary" round class="step-btn press" :loading="submitting" @click="submitOrder">提交订单</van-button>
      </div>
    </template>

    <van-calendar v-model:show="showCalendar" type="range" allow-same-day
                  :min-date="new Date()" :max-date="new Date(Date.now() + 60 * 86400000)"
                  @confirm="onDatesConfirm" />
  </div>
</template>

<style scoped>
.steps-wrap { display: flex; align-items: center; margin: 14px 24px 4px; }
.step { display: flex; align-items: center; flex: 1; position: relative; }
.dot { width: 26px; height: 26px; border-radius: 50%; background: #EFE7DE; color: var(--ink-3);
  display: inline-flex; align-items: center; justify-content: center; font-size: 12px; font-weight: 800;
  flex-shrink: 0; transition: all .25s; }
.step.on .dot { background: var(--brand); color: #fff; box-shadow: var(--clay-brand); }
.step-label { margin-left: 7px; font-size: 12.5px; font-weight: 600; color: var(--ink-3); white-space: nowrap; }
.step.now .step-label { color: var(--ink); }
.bar { flex: 1; height: 3px; background: #EFE7DE; margin: 0 8px; border-radius: 2px; }
.bar.fill { background: var(--brand); }

.pet-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 11px; padding: 0 18px; }
.pet-card { text-align: center; padding: 13px 8px 11px; border: 2.5px solid transparent; }
.pet-card.on { border-color: var(--brand); box-shadow: var(--clay-lg); }
.pet-avatar { width: 58px; height: 44px; margin: 0 auto 9px; border-radius: 12px; overflow: hidden; }
.pet-name { font-weight: 800; font-size: 14px; }

.date-card { margin: 0 18px; padding: 15px 16px; display: flex; justify-content: space-between; align-items: center; }
.date-main { display: flex; align-items: center; gap: 9px; font-weight: 700; }
.date-main .van-icon { color: var(--brand); font-size: 19px; }
.ai-hint { text-align: center; color: var(--ink-3); font-size: 11.5px; margin-top: 12px; }
.ai-hint .van-icon { color: var(--brand); margin-right: 2px; }

.match-tip { margin: 12px 18px 0; padding: 13px 15px; font-size: 12.5px; line-height: 1.8;
  color: var(--ink-2); background: var(--brand-soft); box-shadow: none; display: flex; gap: 9px; }
.tip-icon { color: var(--brand-deep); font-size: 17px; margin-top: 2px; }
.room-card { display: flex; align-items: center; margin: 12px 18px; padding: 12px; border: 2.5px solid transparent; position: relative; }
.room-card.on { border-color: var(--brand); box-shadow: var(--clay-lg); }
.room-photo { width: 84px; height: 76px; border-radius: 14px; overflow: hidden; flex-shrink: 0; }
.room-body { flex: 1; margin-left: 12px; min-width: 0; }
.room-head { display: flex; justify-content: space-between; align-items: center; }
.room-name { font-weight: 800; font-size: 15px; }
.match-reasons { margin: 7px 0 5px; display: flex; gap: 5px; flex-wrap: wrap; }
.check-ring { width: 22px; height: 22px; border-radius: 50%; border: 2px solid #E3D9CE;
  display: flex; align-items: center; justify-content: center; color: transparent; font-size: 13px; margin-left: 8px; flex-shrink: 0; }
.check-ring.on { background: var(--brand); border-color: var(--brand); color: #fff; }

.btn-row { display: flex; justify-content: center; gap: 16px; margin: 26px 0 10px; }
.step-btn { width: 132px; font-weight: 700; }

.sum-card { margin: 12px 18px; padding: 6px 18px; }
.sum-line { display: flex; justify-content: space-between; font-size: 13.5px; margin: 13px 0; align-items: center; }
.sum-line > span:first-child { color: var(--ink-3); }
.price-line { border-top: 1px dashed var(--line); padding-top: 14px; margin-top: 15px; }
.total { font-size: 20px; }
.form { margin-top: 14px; }
.agree { display: flex; align-items: center; gap: 8px; justify-content: center; margin: 18px 0 0; font-size: 13px; }
.agree-link { color: var(--brand-deep); }
</style>

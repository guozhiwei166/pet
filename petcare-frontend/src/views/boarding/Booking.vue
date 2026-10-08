<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import api, { type Pet, type AvailabilityItem, type ServiceItem } from '../../api'

const router = useRouter()
const step = ref(0)

const pets = ref<Pet[]>([])
const petId = ref<number | null>(null)
const dates = ref<[string, string] | null>(null)
const avail = ref<AvailabilityItem[]>([])
const roomTypeId = ref<number | null>(null)
const services = ref<ServiceItem[]>([])
const chosenServices = ref<Record<number, number>>({})
const contactName = ref('')
const contactPhone = ref('')
const remark = ref('')
const submitting = ref(false)

const pet = computed(() => pets.value.find(p => p.id === petId.value))
const roomType = computed(() => avail.value.find(a => a.roomType.id === roomTypeId.value)?.roomType)
const days = computed(() => {
  if (!dates.value) return 0
  return Math.round((new Date(dates.value[1]).getTime() - new Date(dates.value[0]).getTime()) / 86400000)
})
const serviceAmount = computed(() =>
  Object.entries(chosenServices.value).reduce((sum, [id, qty]) => {
    const s = services.value.find(x => x.id === Number(id))
    return sum + (s ? s.price * qty : 0)
  }, 0))

const totalAmount = computed(() =>
  roomType.value && days.value ? roomType.value.price * days.value + serviceAmount.value : 0)
const serviceSummaryText = computed(() =>
  Object.entries(chosenServices.value)
    .map(([id, q]) => {
      const s = services.value.find(x => x.id === Number(id))
      return s ? `${s.name}×${q}` : ''
    })
    .filter(Boolean)
    .join('、'))

const myCoupons = ref<any[]>([])
const chosenCouponId = ref<number | null>(null)
/** 本单可用的券：未使用 + 达到满减门槛 + 折扣券折数有效（与后端 create 的校验口径一致） */
const usableCoupons = computed(() => myCoupons.value.filter(c => {
  if (c.type === 'DISCOUNT') return totalAmount.value >= Number(c.threshold || 0)
  const p = Number(c.percent)
  return p > 0 && p < 100
}))
// 金额变化后若所选券已不满足条件，自动取消选择，避免提交时被后端拒绝
watch(usableCoupons, () => {
  if (chosenCouponId.value && !usableCoupons.value.some(c => c.id === chosenCouponId.value)) {
    chosenCouponId.value = null
  }
})
const couponDiscount = computed(() => {
  const c = myCoupons.value.find(x => x.id === chosenCouponId.value)
  if (!c) return 0
  // 折扣券：percent 即折数×10（80=8折=付80%），优惠额 = 金额 × (100-percent)/100，保留两位小数与后端一致
  const raw = c.type === 'DISCOUNT'
    ? Number(c.amount)
    : totalAmount.value * (100 - Number(c.percent)) / 100
  return Math.min(totalAmount.value, Math.round(raw * 100) / 100)
})
const payAmount = computed(() => Math.max(0, totalAmount.value - couponDiscount.value))

async function loadMyCoupons() {
  try { myCoupons.value = (await api.myCoupons()).filter((c: any) => c.status === 'UNUSED') } catch { myCoupons.value = [] }
}
onMounted(async () => {
  pets.value = await api.pets()
  const userRaw = localStorage.getItem('pc_user')
  if (userRaw) {
    const u = JSON.parse(userRaw)
    contactPhone.value = u.phone || ''
    contactName.value = u.nickname || ''
  }
  if (!pets.value.length) {
    await ElMessageBox.alert('寄养前需要先建立宠物档案（品种、体重、疫苗信息）。', '先添加毛孩子')
    router.push('/pets')
  }
})

function pickDates() {
  if (!dates.value || !dates.value[0] || !dates.value[1]) return
  ElMessage.success('日期已选择')
}

async function goStep2() {
  if (!petId.value) return ElMessage.warning('请选择要寄养的毛孩子')
  step.value = 1
}

async function loadAvailability() {
  if (!dates.value) return ElMessage.warning('请选择寄养日期')
  avail.value = await api.availability({ startDate: dates.value[0], endDate: dates.value[1] })
  roomTypeId.value = null
  step.value = 2
}

async function goStep3() {
  if (!roomTypeId.value) return ElMessage.warning('请选择房型')
  services.value = await api.services()
  step.value = 3
}

/** 进入确认页：先拉取可用券，券选择框才会渲染（此前在下单时才拉，导致选择框永不出现） */
async function goConfirm() {
  await loadMyCoupons()
  step.value = 4
}

function changeQty(id: number, delta: number) {
  const cur = chosenServices.value[id] || 0
  const next = cur + delta
  if (next <= 0) delete chosenServices.value[id]
  else chosenServices.value[id] = next
}

async function submitOrder() {
  if (!contactName.value || !contactPhone.value) return ElMessage.warning('请填写联系人信息')
  submitting.value = true
  try {
    const order = await api.createOrder({
      petId: petId.value, roomTypeId: roomTypeId.value,
      startDate: dates.value![0], endDate: dates.value![1],
      services: Object.entries(chosenServices.value).map(([id, quantity]) => ({ serviceId: Number(id), quantity })),
      contactName: contactName.value, contactPhone: contactPhone.value, remark: remark.value,
      userCouponId: chosenCouponId.value,
    })
    try {
      await ElMessageBox.confirm(
        `订单金额 ￥${order.totalAmount}（演示环境模拟支付，不产生真实扣款）`, '确认支付',
        { confirmButtonText: '立即支付', cancelButtonText: '稍后支付', type: 'info' })
      await api.payOrder(order.id)
      ElMessage.success('支付成功！')
    } catch (e) {
      // 用户点"稍后支付"取消弹窗 → 正常；接口异常 → 提示并留在确认页
      const cancelled = e === 'cancel' || e === 'close'
      if (cancelled) {
        ElMessage.info('已生成订单，可在「我的订单」中稍后支付')
      } else {
        ElMessage.error('支付失败：' + ((e as Error)?.message || '请稍后重试'))
        return
      }
    }
    router.replace('/orders')
  } finally {
    submitting.value = false
  }
}

const fmt = (s: string) => (s ? s.slice(5).replace('-', '/') : '')
/** 折扣券：percent 即折数*10，80=8折；减免 = 金额 × (10-折数)/10 */
const couponLabel = (c: any) => c.type === 'DISCOUNT'
  ? `${c.name}（满${c.threshold}减${c.amount}）` : `${c.name}（${Number(c.percent) / 10}折）`

/** 是否猫区房型：按房型 species 字段（DOG/CAT）判断，与后端分区口径一致 */
const isCatRoom = (rt: { species?: string }) => rt.species === 'CAT'
/** 房型配图：优先使用房型 photo（seed/管理端均已配置），缺失时按分区回退默认图 */
function roomImg(rt: { name: string; photo?: string; species?: string }): string {
  if (rt.photo) return rt.photo
  return isCatRoom(rt) ? '/img/rooms/cat-standard.svg' : '/img/rooms/standard.svg'
}

/** 当前宠物分区：CAT 看猫区，DOG 看犬区 */
const zoneOfPet = computed(() => (pet.value?.species === 'CAT' ? 'cat' : 'dog'))
/** 分区标题 */
const zoneTitle = computed(() => (zoneOfPet.value === 'cat' ? '猫咪区' : '犬区'))
/** 按宠物物种过滤后的房型列表（猫宠只看猫区房型，狗宠只看犬区房型） */
const filteredAvail = computed(() => {
  const wantCat = zoneOfPet.value === 'cat'
  return avail.value.filter(a => isCatRoom(a.roomType) === wantCat)
})
</script>

<template>
  <div>
    <div class="pc-card" style="margin-bottom: 16px">
      <el-steps :active="step" align-center finish-status="success">
        <el-step title="选择宠物" />
        <el-step title="选择日期" />
        <el-step title="选择房型" />
        <el-step title="增值服务" />
        <el-step title="确认订单" />
      </el-steps>
    </div>

    <!-- 第一步：选宠物 -->
    <div v-if="step === 0" class="pc-card">
      <h3 class="blk-title">谁要寄养？</h3>
      <el-row :gutter="14">
        <el-col v-for="p in pets" :key="p.id" :span="6">
          <div :class="['pc-card-md hoverable pet-pick', { on: petId === p.id }]"
               @click="petId = p.id; roomTypeId = null">
            <div class="pick-avatar">
              <img v-if="p.photo" :src="p.photo" :alt="p.name" />
              <span v-else>{{ p.species === 'CAT' ? '🐱' : '🐶' }}</span>
            </div>
            <b>{{ p.name }}</b>
            <div class="sub">{{ p.breed || '未知品种' }} · {{ p.age ?? '?' }}岁</div>
            <el-tag :type="p.species === 'CAT' ? 'warning' : 'primary'" effect="light" size="small" style="margin-top:6px">
              {{ p.species === 'CAT' ? '🐱 猫咪区' : '🐶 犬区' }}
            </el-tag>
          </div>
        </el-col>
      </el-row>
      <div style="text-align:center; margin-top: 18px">
        <el-button type="primary" size="large" round @click="goStep2">下一步：选择日期</el-button>
      </div>
    </div>

    <!-- 第二步：选日期 -->
    <div v-if="step === 1" class="pc-card">
      <h3 class="blk-title">什么时候入住？</h3>
      <el-date-picker v-model="dates" type="daterange" value-format="YYYY-MM-DD"
                      :disabled-date="(d: Date) => d.getTime() < Date.now() - 86400000"
                      start-placeholder="入住日期" end-placeholder="离店日期" style="width: 100%" @change="pickDates" />
      <div v-if="days > 0" class="pick-summary">共 <b class="num">{{ days }}</b> 晚 ·
        {{ dates![0] }} 至 {{ dates![1] }}</div>
      <div style="text-align:center; margin-top: 18px">
        <el-button round @click="step = 0">上一步</el-button>
        <el-button type="primary" size="large" round :disabled="!dates" @click="loadAvailability">查询可订房型</el-button>
      </div>
    </div>

    <!-- 第三步：选房型（带实时库存；按宠物物种只显示对应分区） -->
    <div v-if="step === 2" class="pc-card">
      <h3 class="blk-title">选择房型（{{ zoneTitle }} · {{ days }} 晚）</h3>
      <div v-if="!filteredAvail.length" class="sub" style="padding: 20px 0; text-align:center">
        {{ zoneTitle }}暂无可用房型，请更换日期或联系门店
      </div>
      <div v-for="a in filteredAvail" :key="a.roomType.id" :class="['pc-card-md hoverable room-pick', { on: roomTypeId === a.roomType.id, off: !a.bookable }]"
           @click="a.bookable && (roomTypeId = a.roomType.id)">
        <div class="room-thumb">
          <img :src="roomImg(a.roomType)" :alt="a.roomType.name" />
          <span class="room-zone" :class="isCatRoom(a.roomType) ? 'zone-cat' : 'zone-dog'">
            {{ isCatRoom(a.roomType) ? '猫咪区' : '犬区' }}
          </span>
        </div>
        <div class="room-info">
          <div class="room-name-line">
            <b>{{ a.roomType.name }}</b>
            <el-tag v-if="!a.onSale" type="info" effect="light" size="small">已下架</el-tag>
            <el-tag v-else-if="a.minAvailable <= 1 && a.bookable" type="warning" effect="light" size="small">仅剩少量</el-tag>
            <el-tag v-else-if="!a.bookable" type="danger" effect="light" size="small">已订满</el-tag>
          </div>
          <div class="sub">{{ a.roomType.area }}㎡ · {{ a.roomType.facilities }}</div>
          <div class="sub">{{ a.roomType.description }}</div>
          <div class="sub num" style="margin-top: 4px">
            每日可用：<b v-for="(v, k) in a.perDay" :key="k" style="margin-right: 8px; color: var(--pc-primary-deep)">{{ fmt(k) }}:{{ v }}</b>
          </div>
        </div>
        <div class="room-price">
          <span class="price num">￥{{ a.roomType.price }}<i>/天</i></span>
          <el-tag v-if="roomTypeId === a.roomType.id" type="success" effect="light">✓ 已选择</el-tag>
        </div>
      </div>
      <div style="text-align:center; margin-top: 18px">
        <el-button round @click="step = 1">上一步</el-button>
        <el-button type="primary" size="large" round :disabled="!roomTypeId" @click="goStep3">下一步：增值服务</el-button>
      </div>
    </div>

    <!-- 第四步：增值服务 -->
    <div v-if="step === 3" class="pc-card">
      <h3 class="blk-title">需要额外的照护服务吗？</h3>
      <p class="sub" style="margin: -6px 0 12px">数量可自由增减：同一项服务在一段寄养期内可以选多次（如住 1 晚也可以洗澡 2 次）</p>
      <div v-for="s in services" :key="s.id" class="pc-card-md svc-row">
        <div style="flex: 1">
          <b>{{ s.name }}</b>
          <div class="sub">{{ s.description }}</div>
        </div>
        <span class="price num">￥{{ s.price }}<i class="sub">/{{ s.unit }}</i></span>
        <el-input-number :model-value="chosenServices[s.id] || 0" :min="0" :max="30" size="small"
                         @change="(v: number | undefined) => { if (!v || v <= 0) delete chosenServices[s.id]; else chosenServices[s.id] = v }" />
      </div>
      <div style="text-align:center; margin-top: 18px">
        <el-button round @click="step = 2">上一步</el-button>
        <el-button type="primary" size="large" round @click="goConfirm">下一步：确认订单</el-button>
      </div>
    </div>

    <!-- 第五步：确认 -->
    <div v-if="step === 4" class="pc-card">
      <h3 class="blk-title">确认订单</h3>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="毛孩子">{{ pet?.name }}（{{ pet?.breed }}）</el-descriptions-item>
        <el-descriptions-item label="寄养日期" class="num">{{ dates?.[0] }} ~ {{ dates?.[1] }}（{{ days }} 晚）</el-descriptions-item>
        <el-descriptions-item label="房型">{{ roomType?.name }}</el-descriptions-item>
        <el-descriptions-item label="增值服务">{{ serviceSummaryText || '无' }}</el-descriptions-item>
      </el-descriptions>
      <div class="coupon-pick" v-if="usableCoupons.length">
        <span class="cp-label">🎟️ 优惠券</span>
        <el-select v-model="chosenCouponId" placeholder="选择优惠券抵扣" clearable style="width: 60%">
          <el-option v-for="c in usableCoupons" :key="c.id" :label="couponLabel(c)" :value="c.id" />
        </el-select>
      </div>
      <div class="sub" v-else-if="myCoupons.length" style="margin-top: 10px">
        已领取的优惠券不满足本单条件（未达满减门槛或折扣券无效）
      </div>
      <div class="price-detail">
        <div class="pd-line"><span>住宿费用</span><span class="num">￥{{ roomType ? roomType.price * days : 0 }}</span></div>
        <div class="pd-line"><span>增值服务</span><span class="num">￥{{ serviceAmount }}</span></div>
        <div class="pd-line" v-if="couponDiscount > 0"><span>优惠券抵扣</span><span class="num" style="color:#2E9E6B">-￥{{ couponDiscount }}</span></div>
        <div class="pd-line total"><span>应付总额</span><span class="price num">￥{{ payAmount }}</span></div>
      </div>
      <el-form style="margin-top: 14px" label-width="90px">
        <el-form-item label="联系人"><el-input v-model="contactName" style="width: 240px" /></el-form-item>
        <el-form-item label="联系电话"><el-input v-model="contactPhone" maxlength="11" style="width: 240px" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="remark" type="textarea" :rows="2" style="width: 60%" placeholder="毛孩子的生活习惯、禁忌等" /></el-form-item>
      </el-form>
      <div style="text-align:center; margin-top: 10px">
        <el-button round @click="step = 3">上一步</el-button>
        <el-button type="primary" size="large" round :loading="submitting" @click="submitOrder">提交订单</el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.blk-title { margin-bottom: 16px; }
.pet-pick { text-align: center; border: 2px solid transparent; }
.pet-pick.on { border-color: var(--pc-primary); background: var(--pc-primary-soft); }
.pick-avatar { width: 60px; height: 60px; margin: 0 auto 8px; border-radius: 50%; overflow: hidden;
  display: flex; align-items: center; justify-content: center;
  background: var(--pc-primary-soft); font-size: 26px; box-shadow: 0 6px 14px -8px rgba(47,70,57,.3); }
.pick-avatar img { width: 100%; height: 100%; object-fit: cover; }
.pick-summary { text-align: center; margin-top: 14px; font-size: 14px; }
.room-pick { display: flex; gap: 14px; align-items: center; margin-bottom: 12px; border: 2px solid transparent; }
.room-pick.on { border-color: var(--pc-primary); background: var(--pc-primary-soft); }
.room-pick.off { opacity: .55; cursor: not-allowed; }
.room-thumb { width: 96px; height: 76px; border-radius: 12px; overflow: hidden; flex-shrink: 0;
  position: relative; }
.room-thumb img { width: 100%; height: 100%; object-fit: cover; }
.room-zone { position: absolute; left: 6px; bottom: 6px; font-size: 10px; font-weight: 700;
  color: #fff; padding: 1px 7px; border-radius: 999px; }
.zone-cat { background: rgba(90,134,184,.85); }
.zone-dog { background: rgba(232,155,60,.9); }
.room-info { flex: 1; }
.room-name-line { display: flex; gap: 8px; align-items: center; margin-bottom: 4px; }
.room-price { text-align: right; display: flex; flex-direction: column; gap: 6px; align-items: flex-end; }
.svc-row { display: flex; align-items: center; gap: 16px; margin-bottom: 10px; }
.price-detail { margin-top: 14px; border-top: 1px dashed var(--pc-line); padding-top: 12px; max-width: 420px; margin-left: auto; }
.pd-line { display: flex; justify-content: space-between; font-size: 14px; margin: 8px 0; }
.pd-line.total { font-size: 17px; font-weight: 800; border-top: 1px dashed var(--pc-line); padding-top: 10px; }
</style>

.coupon-pick { display: flex; align-items: center; gap: 10px; margin-top: 12px;
  background: #FFF9F2; border: 1px dashed #F0D9B8; border-radius: 10px; padding: 10px 14px; }
.cp-label { font-size: 13px; font-weight: 700; color: #A97B46; flex-shrink: 0; }

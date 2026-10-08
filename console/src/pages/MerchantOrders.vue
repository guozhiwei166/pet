<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api, { ORDER_STATUS } from '../api'
import { compress } from '../utils/img'

const tabs = [
  { label: '全部', value: 'ALL' },
  { label: '待接单', value: 'PENDING_CONFIRM' },
  { label: '已接单·待入住', value: 'CONFIRMED' },
  { label: '寄养中', value: 'BOARDING' },
  { label: '已完成', value: 'COMPLETED' },
]
const active = ref('ALL')
const list = ref([])
const total = ref(0)
const page = ref(1)
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const data = await api.orders({ page: page.value, size: 10, status: active.value })
    list.value = data.records
    total.value = data.total
  } finally { loading.value = false }
}
function changeTab() { page.value = 1; load() }

// 接单 / 拒单
async function confirm(row) {
  await api.confirmOrder(row.id)
  ElMessage.success('已接单，主人已收到通知')
  load()
}
const rejectVisible = ref(false)
const rejectId = ref(null)
const rejectReason = ref('')
function openReject(row) { rejectId.value = row.id; rejectReason.value = ''; rejectVisible.value = true }
async function doReject() {
  await api.rejectOrder(rejectId.value, rejectReason.value)
  ElMessage.success('已拒单')
  rejectVisible.value = false
  load()
}

// 入住 / 离店
const checkinVisible = ref(false)
const checkinId = ref(null)
const weightIn = ref(null)
function openCheckin(row) { checkinId.value = row.id; weightIn.value = null; checkinVisible.value = true }
async function doCheckin() {
  await api.checkinOrder(checkinId.value, weightIn.value)
  ElMessage.success('入住办理完成')
  checkinVisible.value = false
  load()
}
const checkoutVisible = ref(false)
const checkoutId = ref(null)
const weightOut = ref(null)
function openCheckout(row) { checkoutId.value = row.id; weightOut.value = null; checkoutVisible.value = true }
async function doCheckout() {
  await api.checkoutOrder(checkoutId.value, weightOut.value)
  ElMessage.success('离店结算完成')
  checkoutVisible.value = false
  load()
}

// 每日打卡（寄养日记）
const diaryVisible = ref(false)
const diaryOrderId = ref(null)
const diaryForm = ref({})
function openDiary(row) {
  diaryOrderId.value = row.id
  diaryForm.value = {
    recordDate: new Date().toISOString().slice(0, 10), mealDone: 1, walkDone: 1,
    mood: '开心', content: '', weight: null, photos: [],
  }
  diaryVisible.value = true
}
async function uploadPhoto({ file }) { diaryForm.value.photos.push(await compress(file)) }
async function doDiary() {
  const f = diaryForm.value
  if (!f.content) return ElMessage.warning('写点今天的记录吧')
  await api.postDiary(diaryOrderId.value, { ...f, photos: f.photos.join(',') })
  ElMessage.success('日记已发布，主人已收到通知')
  diaryVisible.value = false
  load()
}

// 详情抽屉
const drawer = ref(false)
const detail = ref(null)
async function openDetail(row) {
  detail.value = await api.orderDetail(row.id)
  drawer.value = true
}

onMounted(load)
</script>

<template>
  <div class="page-card">
    <el-radio-group v-model="active" @change="changeTab">
      <el-radio-button v-for="t in tabs" :key="t.value" :value="t.value">{{ t.label }}</el-radio-button>
    </el-radio-group>

    <el-table :data="list" stripe v-loading="loading" style="margin-top: 14px">
      <el-table-column prop="orderNo" label="订单号" width="170" />
      <el-table-column label="宠物" width="110">
        <template #default="{ row }">{{ row.petName }}（{{ row.petSpecies === 'CAT' ? '猫' : '狗' }}）</template>
      </el-table-column>
      <el-table-column prop="roomName" label="房型" width="95" />
      <el-table-column label="寄养日期" width="180">
        <template #default="{ row }">{{ row.startDate }} ~ {{ row.endDate }}（{{ row.days }}晚）</template>
      </el-table-column>
      <el-table-column label="联系人" width="120">
        <template #default="{ row }">{{ row.contactName }}<br /><span class="sub">{{ row.contactPhone }}</span></template>
      </el-table-column>
      <el-table-column prop="totalAmount" label="金额" width="76">
        <template #default="{ row }">￥{{ row.totalAmount }}</template>
      </el-table-column>
      <el-table-column label="状态" width="96">
        <template #default="{ row }">
          <el-tag :type="ORDER_STATUS[row.status]?.type" effect="light">{{ ORDER_STATUS[row.status]?.text }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" min-width="200" fixed="right">
        <template #default="{ row }">
          <template v-if="row.status === 'PENDING_CONFIRM'">
            <el-button type="primary" size="small" @click="confirm(row)">接单</el-button>
            <el-button type="danger" plain size="small" @click="openReject(row)">拒单</el-button>
          </template>
          <el-button v-if="row.status === 'CONFIRMED'" type="primary" size="small" @click="openCheckin(row)">办理入住</el-button>
          <template v-if="row.status === 'BOARDING'">
            <el-button type="warning" size="small" @click="openDiary(row)">每日打卡</el-button>
            <el-button type="success" plain size="small" @click="openCheckout(row)">办理离店</el-button>
          </template>
          <el-button text type="primary" size="small" @click="openDetail(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination background layout="prev, pager, next, total" :total="total"
                   :page-size="10" v-model:current-page="page" @current-change="load" />
  </div>

  <!-- 拒单 -->
  <el-dialog v-model="rejectVisible" title="拒单原因" width="420px">
    <el-input v-model="rejectReason" type="textarea" :rows="3" placeholder="如：该日期笼位已被大型犬订满，建议换个日期" />
    <template #footer>
      <el-button @click="rejectVisible = false">取消</el-button>
      <el-button type="danger" @click="doReject">确认拒单</el-button>
    </template>
  </el-dialog>

  <!-- 入住 -->
  <el-dialog v-model="checkinVisible" title="办理入住" width="420px">
    <div class="dialog-tip">给毛孩子称个重吧，离店时对比体重就是一份健康报告 🐾</div>
    <el-input-number v-model="weightIn" :min="0.1" :max="100" :precision="1" placeholder="入住体重 kg" style="width: 100%" />
    <template #footer>
      <el-button @click="checkinVisible = false">取消</el-button>
      <el-button type="primary" @click="doCheckin">确认入住</el-button>
    </template>
  </el-dialog>

  <!-- 离店 -->
  <el-dialog v-model="checkoutVisible" title="办理离店" width="420px">
    <div class="dialog-tip">记录离店体重，主人会看到这次寄养的体重变化。</div>
    <el-input-number v-model="weightOut" :min="0.1" :max="100" :precision="1" placeholder="离店体重 kg" style="width: 100%" />
    <template #footer>
      <el-button @click="checkoutVisible = false">取消</el-button>
      <el-button type="success" @click="doCheckout">确认离店</el-button>
    </template>
  </el-dialog>

  <!-- 每日打卡 -->
  <el-dialog v-model="diaryVisible" title="📖 每日护理打卡（寄养日记）" width="560px">
    <el-form label-width="90px">
      <el-form-item label="日期">
        <el-date-picker v-model="diaryForm.recordDate" type="date" value-format="YYYY-MM-DD" style="width: 180px" />
        <el-switch v-model="diaryForm.mealDone" :active-value="1" :inactive-value="0"
                   active-text="已喂食" inline-prompt style="margin-left: 16px" />
        <el-switch v-model="diaryForm.walkDone" :active-value="1" :inactive-value="0"
                   active-text="已遛弯" inline-prompt style="margin-left: 12px" />
      </el-form-item>
      <el-form-item label="情绪">
        <el-radio-group v-model="diaryForm.mood">
          <el-radio-button value="开心">开心</el-radio-button>
          <el-radio-button value="平静">平静</el-radio-button>
          <el-radio-button value="想家">想家</el-radio-button>
          <el-radio-button value="紧张">紧张</el-radio-button>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="今日体重">
        <el-input-number v-model="diaryForm.weight" :min="0" :precision="1" placeholder="选填 kg" />
      </el-form-item>
      <el-form-item label="今日记录">
        <el-input v-model="diaryForm.content" type="textarea" :rows="3"
                  placeholder="今天吃了什么、玩了什么、状态怎么样…" />
      </el-form-item>
      <el-form-item label="照片">
        <el-upload :show-file-list="false" accept="image/*" :http-request="uploadPhoto" multiple>
          <el-button size="small">+ 上传照片</el-button>
        </el-upload>
        <div class="photo-row">
          <div v-for="(p, i) in diaryForm.photos" :key="i" class="photo-thumb">
            <img :src="p" alt="" />
            <span class="photo-del" @click="diaryForm.photos.splice(i, 1)">×</span>
          </div>
        </div>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="diaryVisible = false">取消</el-button>
      <el-button type="primary" @click="doDiary">发布日记</el-button>
    </template>
  </el-dialog>

  <!-- 详情抽屉 -->
  <el-drawer v-model="drawer" title="订单详情" size="420px">
    <template v-if="detail">
      <div class="d-row"><span>订单号</span><b>{{ detail.order.orderNo }}</b></div>
      <div class="d-row"><span>状态</span>
        <el-tag :type="ORDER_STATUS[detail.order.status]?.type">{{ ORDER_STATUS[detail.order.status]?.text }}</el-tag></div>
      <div class="d-row"><span>宠物</span><b>{{ detail.pet?.name }} · {{ detail.pet?.breed }}</b></div>
      <div class="d-row"><span>房型</span><b>{{ detail.room?.name }}</b></div>
      <div class="d-row"><span>日期</span><b>{{ detail.order.startDate }} ~ {{ detail.order.endDate }}</b></div>
      <div class="d-row"><span>备注</span><b>{{ detail.order.remark || '无' }}</b></div>
      <div class="d-row"><span>体重</span><b>{{ detail.order.weightIn || '-' }}kg → {{ detail.order.weightOut || '-' }}kg</b></div>
      <el-divider>寄养日记（{{ detail.diaries?.length || 0 }}）</el-divider>
      <div v-for="d in detail.diaries" :key="d.id" class="diary-item">
        <div class="diary-date">{{ d.recordDate }} <el-tag size="small" type="warning" effect="plain">{{ d.mood }}</el-tag></div>
        <div class="diary-sub">🍚 {{ d.mealDone ? '已喂食' : '未记录' }} ｜ 🎾 {{ d.walkDone ? '已遛弯' : '未记录' }}<template v-if="d.weight"> ｜ ⚖️ {{ d.weight }}kg</template></div>
        <div class="diary-content">{{ d.content }}</div>
      </div>
      <el-empty v-if="!detail.diaries?.length" description="暂无日记" :image-size="60" />
    </template>
  </el-drawer>
</template>

<style scoped>
.sub { color: #9B938D; font-size: 12px; }
.dialog-tip { font-size: 13px; color: #9B938D; margin-bottom: 12px; }
.photo-row { display: flex; gap: 8px; margin-top: 8px; flex-wrap: wrap; }
.photo-thumb { position: relative; width: 64px; height: 64px; border-radius: 8px; overflow: hidden; }
.photo-thumb img { width: 100%; height: 100%; object-fit: cover; }
.photo-del { position: absolute; top: 0; right: 0; background: rgba(0,0,0,.45); color: #fff;
  width: 18px; height: 18px; font-size: 12px; display: flex; align-items: center; justify-content: center;
  cursor: pointer; border-radius: 0 0 0 8px; }
.d-row { display: flex; justify-content: space-between; font-size: 13px; margin: 10px 0; align-items: center; }
.d-row span { color: #9B938D; }
.diary-item { border: 1px solid #F5EFE8; border-radius: 10px; padding: 12px; margin-bottom: 10px; }
.diary-date { font-weight: 700; display: flex; gap: 8px; align-items: center; }
.diary-sub { font-size: 12px; color: #9B938D; margin: 6px 0; }
.diary-ai { background: #FFE8DD; border-radius: 8px; padding: 8px 10px; font-size: 12px; margin-bottom: 6px; line-height: 1.6; }
.diary-content { font-size: 13px; line-height: 1.6; }
</style>

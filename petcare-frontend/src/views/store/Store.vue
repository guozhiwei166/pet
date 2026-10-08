<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '../../api'

const router = useRouter()
const store = ref<any>(null)
const services = ref<any[]>([])
const announcements = ref<any[]>([])
const pets = ref<any[]>([])
const bookingVisible = ref(false)
const bookingForm = ref({ petId: undefined as number | undefined, serviceId: undefined as number | undefined, bookDate: '', bookTime: '10:00', remark: '' })

const timeSlots = ['09:00', '10:00', '11:00', '14:00', '15:00', '16:00', '17:00']
const featureOf = (s: string) => {
  const map: Record<string, string> = { 洗: '🛁', 美: '💇', 遛: '🦮', 摄: '📷', 接: '🚗', 喂: '💊', 特: '🍲' }
  for (const k of Object.keys(map)) if (s.includes(k)) return map[k]
  return '✨'
}

async function load() {
  store.value = await api.storeProfile()
  services.value = await api.services()
  announcements.value = await api.announcements()
  pets.value = await api.pets()
}
onMounted(load)

async function submitBooking() {
  if (!bookingForm.value.petId || !bookingForm.value.serviceId || !bookingForm.value.bookDate) {
    return ElMessage.warning('请选择宠物、服务与日期')
  }
  await api.createServiceBooking(bookingForm.value)
  ElMessage.success('预约成功，等待门店确认')
  bookingVisible.value = false
  router.push('/service-bookings')
}
</script>

<template>
  <div v-if="store">
    <!-- 门店主视觉 -->
    <div class="store-hero">
      <div class="hero-text">
        <h1>{{ store.storeName }}</h1>
        <p class="slogan">{{ store.slogan }}</p>
        <div class="hero-meta">
          <span><el-icon><Location /></el-icon>{{ store.address }}</span>
          <span><el-icon><Phone /></el-icon>{{ store.phone }}</span>
          <span><el-icon><Clock /></el-icon>{{ store.openHours }}</span>
        </div>
      </div>
      <div class="hero-imgs">
        <img v-for="p in (store.photos || '').split(',').filter(Boolean).slice(0, 3)" :key="p" :src="p" alt="门店" />
      </div>
    </div>

    <!-- 公告 -->
    <div class="pc-card ann-card" v-if="announcements.length">
      <div class="ann-title"><el-icon><BellFilled /></el-icon> 公告</div>
      <div v-for="a in announcements" :key="a.id" class="ann-item">
        <b>{{ a.title }}</b>
        <span class="sub">{{ a.content }}</span>
      </div>
    </div>

    <!-- 门店简介与特色 -->
    <div class="pc-card" style="margin-top: 16px">
      <p class="intro">{{ store.intro }}</p>
      <div class="feature-chips">
        <span v-for="f in (store.features || '').split(',').filter(Boolean)" :key="f" class="feature-chip">{{ f }}</span>
      </div>
    </div>

    <!-- 增值服务（可预约） -->
    <h3 class="sec-title">门店增值服务 · 在线预约</h3>
    <el-row :gutter="14">
      <el-col v-for="s in services" :key="s.id" :span="8">
        <div class="pc-card-md service-card hoverable">
          <div class="svc-icon">{{ featureOf(s.name) }}</div>
          <div class="svc-body">
            <b>{{ s.name }}</b>
            <div class="sub">{{ s.description }}</div>
            <div class="svc-bottom">
              <span class="price num">￥{{ s.price }}<i class="sub">/{{ s.unit }}</i></span>
              <el-button size="small" type="primary" round @click="bookingForm.serviceId = s.id; bookingVisible = true">预约</el-button>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 预约弹窗 -->
    <el-dialog v-model="bookingVisible" title="预约门店服务" width="460px">
      <el-form label-width="90px">
        <el-form-item label="宠物" required>
          <el-select v-model="bookingForm.petId" placeholder="选择宠物" style="width: 100%">
            <el-option v-for="p in pets" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="服务" required>
          <el-select v-model="bookingForm.serviceId" placeholder="选择服务" style="width: 100%">
            <el-option v-for="s in services" :key="s.id" :label="`${s.name}（￥${s.price}/${s.unit}）`" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期" required>
          <el-date-picker v-model="bookingForm.bookDate" type="date" value-format="YYYY-MM-DD"
                          :disabled-date="(d: Date) => d.getTime() < Date.now() - 86400000" style="width: 100%" />
        </el-form-item>
        <el-form-item label="时间">
          <el-select v-model="bookingForm.bookTime" style="width: 120px">
            <el-option v-for="t in timeSlots" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="bookingForm.remark" type="textarea" :rows="2" placeholder="如：最近皮肤有点敏感，洗澡用温和香波" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="bookingVisible = false">取消</el-button>
        <el-button type="primary" @click="submitBooking">提交预约</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.store-hero { display: flex; gap: 24px; align-items: center; justify-content: space-between;
  background: linear-gradient(130deg, #2F4639 0%, #4A6B57 60%, #5A8F76 100%); color: #fff;
  border-radius: 20px; padding: 30px 34px; margin-bottom: 16px; }
.hero-text h1 { font-size: 28px; font-weight: 900; margin-bottom: 8px; }
.hero-text .slogan { font-size: 15px; opacity: .9; margin-bottom: 18px; }
.hero-meta { display: flex; flex-direction: column; gap: 8px; font-size: 13px; opacity: .9; }
.hero-meta span { display: flex; align-items: center; gap: 6px; }
.hero-imgs { display: flex; gap: 10px; flex-shrink: 0; }
.hero-imgs img { width: 120px; height: 90px; object-fit: cover; border-radius: 12px;
  box-shadow: 0 8px 18px -8px rgba(0,0,0,.4); }
.ann-card { display: flex; gap: 20px; align-items: center; }
.ann-title { display: flex; align-items: center; gap: 6px; font-weight: 800; color: var(--pc-accent); white-space: nowrap; }
.ann-item { font-size: 13px; display: flex; flex-direction: column; }
.ann-item b { font-size: 14px; }
.intro { font-size: 14px; line-height: 1.9; color: var(--pc-ink-2); }
.feature-chips { display: flex; flex-wrap: wrap; gap: 8px; margin-top: 12px; }
.feature-chip { background: var(--pc-primary-soft); color: var(--pc-primary-deep); font-size: 12px;
  font-weight: 600; padding: 5px 12px; border-radius: 999px; }
.sec-title { font-weight: 800; margin: 22px 2px 12px; }
.service-card { display: flex; gap: 12px; align-items: center; margin-bottom: 14px; }
.svc-icon { width: 46px; height: 46px; border-radius: 14px; flex-shrink: 0; display: flex; align-items: center;
  justify-content: center; font-size: 22px; background: var(--pc-accent-soft); }
.svc-body { flex: 1; min-width: 0; }
.svc-bottom { display: flex; justify-content: space-between; align-items: center; margin-top: 8px; }
.price i { font-style: normal; }
</style>

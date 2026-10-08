<script setup lang="ts">
import { ref, watch, computed, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { ElMessage, ElMessageBox } from 'element-plus'
import api, { type Pet, type VaccineItem, type HealthItem, type GrowthData, type PetTag } from '../../api'
import { useUserStore } from '../../stores/user'

const route = useRoute()
const router = useRouter()
const store = useUserStore()
/** 员工/管理员浏览他人档案：隐藏仅主人可见的入口 */
const isOwnerView = computed(() => !store.isStaff && !store.isAdmin)
// 用 computed 取路由参数：同一路由记录内 /pets/1 → /pets/2 时组件复用、onMounted 不再触发，
// 取成常量会一直显示上一只宠物的数据
const petId = computed(() => route.params.id as string)

const pet = ref<Pet | null>(null)
const vaccines = ref<VaccineItem[]>([])
const healths = ref<HealthItem[]>([])
const growth = ref<GrowthData | null>(null)
const tags = ref<PetTag[]>([])
const activeTab = ref('basic')
const curveRef = ref<HTMLElement | null>(null)
let curveChart: echarts.ECharts | null = null

const vacVisible = ref(false)
const vacForm = ref<{ id?: number; vaccineName: string; inoculationDate: string; expireDate: string; org: string; remark: string }>({
  vaccineName: '', inoculationDate: '', expireDate: '', org: '', remark: '',
})

async function load() {
  pet.value = await api.petDetail(petId.value)
  vaccines.value = await api.vaccines(petId.value)
  healths.value = await api.health(petId.value, 365)
  growth.value = await api.growth(petId.value)
  tags.value = await api.tagDict()
  // 成长档案 Tab 是懒渲染，切到该页签时再画曲线
  if (activeTab.value === 'growth') renderCurve()
}

// 曲线容器在成长档案页签内，切页签时补渲染
watch(activeTab, tab => {
  if (tab === 'growth') renderCurve()
})

function renderCurve() {
  nextTick(() => {
    if (!curveRef.value || !growth.value) return
    if (curveChart) curveChart.dispose()
    curveChart = echarts.init(curveRef.value)
    curveChart.setOption({
      grid: { left: 44, right: 20, top: 26, bottom: 30 },
      tooltip: { trigger: 'axis', formatter: (params: any) => {
        const p = Array.isArray(params) ? params[0] : params
        return `${p.name}<br/>体重：<b>${p.value} kg</b>`
      } },
      xAxis: { type: 'category', data: growth.value.weightCurve.map(p => p.date.slice(5)), boundaryGap: false },
      yAxis: { type: 'value', scale: true, name: 'kg' },
      series: [{
        type: 'line', smooth: true, symbol: 'circle', symbolSize: 8,
        data: growth.value.weightCurve.map(p => p.weight),
        lineStyle: { color: '#6FA58B', width: 3 },
        itemStyle: { color: '#6FA58B', borderColor: '#fff', borderWidth: 2 },
        areaStyle: { color: 'rgba(111,165,139,.14)' },
      }],
    })
  })
}

function openVac(v?: VaccineItem) {
  vacForm.value = v
    ? { ...v }
    : { vaccineName: '', inoculationDate: '', expireDate: '', org: '', remark: '' }
  vacVisible.value = true
}

async function saveVac() {
  if (!vacForm.value.vaccineName || !vacForm.value.inoculationDate) {
    return ElMessage.warning('请填写疫苗名称与接种日期')
  }
  if (vacForm.value.id) await api.updateVaccine(petId.value, vacForm.value.id, vacForm.value)
  else await api.saveVaccine(petId.value, vacForm.value)
  ElMessage.success('保存成功')
  vacVisible.value = false
  vaccines.value = await api.vaccines(petId.value)
}

async function delVac(v: VaccineItem) {
  await ElMessageBox.confirm(`删除「${v.vaccineName}」记录？`, '删除确认', { type: 'warning' })
  await api.deleteVaccine(petId.value, v.id)
  vaccines.value = await api.vaccines(petId.value)
}

const statusColor = (s: string) => (s === '有效' ? 'success' : s === '即将到期' ? 'warning' : 'danger')
const speciesText = (s: string) => (s === 'CAT' ? '猫咪' : '狗狗')

onMounted(load)
// 同一路由记录内切换宠物（组件被复用）时重新拉取，避免停留在上一只宠物的数据
watch(() => route.params.id, id => {
  if (id) load()
})
</script>

<template>
  <div v-if="pet">
    <!-- 档案头卡 -->
    <div class="pc-card head-card" :class="pet.species === 'CAT' ? 'head-cat' : 'head-dog'">
      <div class="head-avatar">
        <img v-if="pet.photo" :src="pet.photo" alt="" />
        <span v-else>{{ pet.species === 'CAT' ? '🐱' : '🐶' }}</span>
      </div>
      <div class="head-info">
        <h2>{{ pet.name }}</h2>
        <div class="sub2">{{ speciesText(pet.species) }} · {{ pet.breed || '未知品种' }}
          <template v-if="pet.age !== null && pet.age !== undefined">· {{ pet.age }}岁 </template>
          · {{ pet.gender === 'M' ? '公' : '母' }}
          <template v-if="pet.weight">· {{ pet.weight }}kg</template>
        </div>
        <div class="head-tags">
          <el-tag v-for="t in pet.tags" :key="t" effect="light" color="#fff" style="color:#5A8F76"># {{ t }}</el-tag>
          <el-tag v-if="pet.sterilized" effect="light" color="#fff" style="color:#C97B2C">已绝育</el-tag>
        </div>
      </div>
      <div class="head-right">
        <div class="sub">当前体重</div>
        <div class="weight num">{{ pet.weight }}<i>kg</i></div>
      </div>
    </div>

    <el-tabs v-model="activeTab" class="profile-tabs">
      <!-- 基础资料 -->
      <el-tab-pane label="基础资料" name="basic">
        <div class="pc-card">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="昵称">{{ pet.name }}</el-descriptions-item>
            <el-descriptions-item label="物种">{{ speciesText(pet.species) }}</el-descriptions-item>
            <el-descriptions-item label="品种">{{ pet.breed || '—' }}</el-descriptions-item>
            <el-descriptions-item label="性别">{{ pet.gender === 'M' ? '公' : '母' }}</el-descriptions-item>
            <el-descriptions-item label="生日">{{ pet.birthDate || '—' }}</el-descriptions-item>
            <el-descriptions-item label="年龄">{{ pet.age ?? '—' }} 岁</el-descriptions-item>
            <el-descriptions-item label="体重">{{ pet.weight }} kg</el-descriptions-item>
            <el-descriptions-item label="毛色">{{ pet.furColor || '—' }}</el-descriptions-item>
            <el-descriptions-item label="是否绝育">{{ pet.sterilized ? '已绝育' : '未绝育' }}</el-descriptions-item>
          </el-descriptions>
          <div v-if="pet.notes" class="warn-box">⚠ {{ pet.notes }}</div>
        </div>
      </el-tab-pane>

      <!-- 健康记录 -->
      <el-tab-pane label="健康记录" name="health">
        <div class="pc-card">
          <el-table :data="healths" stripe>
            <el-table-column prop="recordDate" label="日期" width="120" />
            <el-table-column prop="weight" label="体重(kg)" width="100" />
            <el-table-column prop="temperature" label="体温(℃)" width="100" />
            <el-table-column prop="mentalState" label="精神" width="90" />
            <el-table-column prop="appetite" label="食欲" width="90" />
            <el-table-column prop="water" label="饮水" width="90" />
            <el-table-column prop="poop" label="排便" width="90" />
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="row.isAbnormal ? 'danger' : 'success'" effect="light">
                  {{ row.isAbnormal ? '异常' : '正常' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="remark" label="备注" min-width="140" />
          </el-table>
          <el-empty v-if="!healths.length" description="暂无健康观察记录，寄养期间工作人员会定期记录" :image-size="70" />
        </div>
      </el-tab-pane>

      <!-- 疫苗记录 -->
      <el-tab-pane label="疫苗记录" name="vaccine">
        <div class="pc-card">
          <div class="vac-head">
            <span class="sub">商家接单前会核对疫苗有效性</span>
            <el-button type="primary" size="small" icon="Plus" @click="openVac()">添加疫苗记录</el-button>
          </div>
          <el-table :data="vaccines" stripe style="margin-top: 10px">
            <el-table-column prop="vaccineName" label="疫苗名称" min-width="120" />
            <el-table-column prop="inoculationDate" label="接种日期" width="120" />
            <el-table-column prop="expireDate" label="有效期至" width="120" />
            <el-table-column prop="org" label="接种机构" min-width="130" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="statusColor(row.status)" effect="light">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button text type="primary" size="small" @click="openVac(row)">编辑</el-button>
                <el-button text type="danger" size="small" @click="delVac(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!vaccines.length" description="还没有疫苗记录" :image-size="70" />
        </div>
      </el-tab-pane>

      <!-- 寄养历史 -->
      <el-tab-pane label="寄养历史" name="history">
        <div class="pc-card">
          <el-table :data="growth?.boardings || []" stripe>
            <el-table-column prop="startDate" label="入住日期" width="120" />
            <el-table-column prop="endDate" label="离店日期" width="120" />
            <el-table-column prop="days" label="住宿天数" width="90" />
            <el-table-column prop="totalAmount" label="消费金额" width="110">
              <template #default="{ row }"><span class="price num">￥{{ row.totalAmount }}</span></template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag effect="light" :type="row.status === 'COMPLETED' ? 'success' : 'warning'">
                  {{ ({ BOARDING: '寄养中', COMPLETED: '已完成' } as Record<string, string>)[row.status] || row.status }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="110">
              <template #default="{ row }">
                <el-button v-if="isOwnerView" text type="primary" size="small" @click="router.push(`/orders/${row.id}`)">查看本次寄养</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!(growth?.boardings || []).length" description="还没有寄养记录，去「我要寄养」看看吧" :image-size="80" />
        </div>
      </el-tab-pane>

      <!-- 成长档案 -->
      <el-tab-pane label="成长档案" name="growth">
        <div class="pc-card">
          <el-row :gutter="14">
            <el-col :span="8"><div class="growth-stat"><b class="num">{{ growth?.totalBoardings ?? 0 }}</b><span class="sub">累计寄养次数</span></div></el-col>
            <el-col :span="8"><div class="growth-stat"><b class="num">{{ growth?.totalDays ?? 0 }}</b><span class="sub">累计住宿天数</span></div></el-col>
            <el-col :span="8"><div class="growth-stat"><b class="num">{{ growth?.currentWeight ?? '—' }}</b><span class="sub">当前体重(kg)</span></div></el-col>
          </el-row>
          <h4 style="margin: 20px 0 4px">体重变化曲线</h4>
          <div ref="curveRef" style="height: 260px"></div>
          <el-empty v-if="(growth?.weightCurve.length ?? 0) < 2"
                    description="体重记录不足两次，寄养期间的健康检查会自动补充数据" :image-size="70" />
        </div>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="vacVisible" :title="vacForm.id ? '编辑疫苗记录' : '添加疫苗记录'" width="480px">
      <el-form label-width="90px">
        <el-form-item label="疫苗名称" required><el-input v-model="vacForm.vaccineName" placeholder="如：犬联苗年度" /></el-form-item>
        <el-form-item label="接种日期" required>
          <el-date-picker v-model="vacForm.inoculationDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="有效期至">
          <el-date-picker v-model="vacForm.expireDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="接种机构"><el-input v-model="vacForm.org" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="vacForm.remark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="vacVisible = false">取消</el-button>
        <el-button type="primary" @click="saveVac">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.head-card { display: flex; align-items: center; gap: 20px; position: relative; overflow: hidden; }
.head-card::after {
  content: ''; position: absolute; right: -60px; top: -60px; width: 200px; height: 200px;
  border-radius: 50%; opacity: .35; pointer-events: none;
}
.head-dog { background: linear-gradient(135deg, #FDF6EC 0%, #fff 55%); }
.head-dog::after { background: #F3A65A; }
.head-cat { background: linear-gradient(135deg, #EFF4FA 0%, #fff 55%); }
.head-cat::after { background: #8FB0D9; }
.head-avatar { width: 92px; height: 92px; border-radius: 24px; overflow: hidden; flex-shrink: 0;
  display: flex; align-items: center; justify-content: center; font-size: 42px;
  background: rgba(255,255,255,.7); box-shadow: 0 10px 24px -10px rgba(47,70,57,.35);
  border: 3px solid #fff; z-index: 1; }
.head-avatar img { width: 100%; height: 100%; object-fit: cover; }
.head-info { flex: 1; z-index: 1; }
.head-info h2 { font-size: 24px; font-weight: 900; }
.head-info .sub2 { margin: 6px 0 10px; color: var(--pc-ink-2); font-size: 13px; }
.head-tags { display: flex; gap: 6px; flex-wrap: wrap; }
.head-right { text-align: right; z-index: 1; }
.weight { font-size: 26px; font-weight: 900; color: var(--pc-accent); }
.weight i { font-style: normal; font-size: 12px; color: var(--pc-ink-2); font-weight: 500; }
.profile-tabs { margin-top: 16px; }
.warn-box { margin-top: 14px; background: var(--pc-danger-soft); color: #B5574D;
  border-radius: 10px; padding: 10px 14px; font-size: 13px; }
.vac-head { display: flex; justify-content: space-between; align-items: center; }
.growth-stat { background: var(--pc-primary-soft); border-radius: 14px; padding: 16px;
  display: flex; flex-direction: column; align-items: center; gap: 4px; }
.growth-stat b { font-size: 24px; font-weight: 900; color: var(--pc-primary-deep); }
</style>

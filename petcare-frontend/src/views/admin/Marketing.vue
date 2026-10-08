<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../../api'

const tab = ref('store')
const store = ref<any>({ storeName: '', slogan: '', intro: '', address: '', phone: '', openHours: '', features: '', photos: '' })
const coupons = ref<any[]>([])
const announcements = ref<any[]>([])

// 券表单
const couponVisible = ref(false)
const couponForm = ref<any>({})

// 公告表单
const annVisible = ref(false)
const annForm = ref<any>({ title: '', content: '', type: 'NOTICE', pinned: 0, status: 1 })

async function load() {
  store.value = (await api.storeProfile()) || store.value
  coupons.value = await api.allCoupons()
  announcements.value = await api.announcements(true)
}
onMounted(load)

async function saveStore() {
  if (!store.value.storeName) return ElMessage.warning('请填写门店名称')
  await api.saveStoreProfile(store.value)
  ElMessage.success('门店信息已保存')
}

// 券
function openCoupon(row?: any) {
  couponForm.value = row ? { ...row } : { name: '', type: 'DISCOUNT', threshold: 100, amount: 30, percent: 90, totalCount: 100, status: 1 }
  couponVisible.value = true
}
async function saveCoupon() {
  await api.saveCoupon(couponForm.value)
  ElMessage.success('优惠券已保存')
  couponVisible.value = false
  coupons.value = await api.allCoupons()
}
async function delCoupon(row: any) {
  await ElMessageBox.confirm(`删除「${row.name}」？`, '删除确认', { type: 'warning' })
  await api.deleteCoupon(row.id)
  ElMessage.success('已删除')
  coupons.value = await api.allCoupons()
}

// 公告
function openAnn(row?: any) {
  annForm.value = row ? { ...row } : { title: '', content: '', type: 'NOTICE', pinned: 0, status: 1 }
  annVisible.value = true
}
async function saveAnn() {
  if (!annForm.value.title) return ElMessage.warning('请填写公告标题')
  await api.saveAnnouncement(annForm.value)
  ElMessage.success('公告已发布')
  annVisible.value = false
  announcements.value = await api.announcements(true)
}
async function delAnn(row: any) {
  await ElMessageBox.confirm(`删除公告「${row.title}」？`, '删除确认', { type: 'warning' })
  await api.deleteAnnouncement(row.id)
  announcements.value = await api.announcements(true)
}

const TYPE_TEXT: Record<string, string> = { NOTICE: '通知', ACTIVITY: '活动', SAFETY: '安全' }
const couponDesc = (c: any) => c.type === 'DISCOUNT'
  ? `满 ${c.threshold} 减 ${c.amount}`
  : `${Number(c.percent) / 10} 折`
</script>

<template>
  <div>
    <el-tabs v-model="tab">
      <!-- 门店信息 -->
      <el-tab-pane label="门店信息" name="store">
        <div class="pc-card">
          <el-form label-width="100px" style="max-width: 720px">
            <el-form-item label="门店名称"><el-input v-model="store.storeName" /></el-form-item>
            <el-form-item label="品牌口号"><el-input v-model="store.slogan" /></el-form-item>
            <el-form-item label="地址"><el-input v-model="store.address" /></el-form-item>
            <el-form-item label="电话"><el-input v-model="store.phone" /></el-form-item>
            <el-form-item label="营业时间"><el-input v-model="store.openHours" /></el-form-item>
            <el-form-item label="设施特色"><el-input v-model="store.features" placeholder="逗号分隔，如：持证护理师,独立猫犬分区" /></el-form-item>
            <el-form-item label="照片URL"><el-input v-model="store.photos" placeholder="逗号分隔多个URL" /></el-form-item>
            <el-form-item label="门店介绍">
              <el-input v-model="store.intro" type="textarea" :rows="4" />
            </el-form-item>
            <el-form-item><el-button type="primary" @click="saveStore">保存门店信息</el-button></el-form-item>
          </el-form>
        </div>
      </el-tab-pane>

      <!-- 优惠券管理 -->
      <el-tab-pane label="优惠券" name="coupons">
        <div class="pc-card">
          <div style="display:flex; justify-content:space-between; margin-bottom: 12px">
            <span class="sub">管理满减 / 折扣券模板</span>
            <el-button type="primary" size="small" @click="openCoupon()">新增优惠券</el-button>
          </div>
          <el-table :data="coupons" stripe>
            <el-table-column prop="name" label="名称" min-width="140" />
            <el-table-column label="类型/面额" width="120">
              <template #default="{ row }">{{ row.type === 'DISCOUNT' ? '满减' : '折扣' }} · {{ couponDesc(row) }}</template>
            </el-table-column>
            <el-table-column prop="totalCount" label="总量" width="80" />
            <el-table-column prop="issuedCount" label="已领" width="80" />
            <el-table-column label="有效期" width="180">
              <template #default="{ row }" class="num">{{ (row.startDate || '').slice(5) }} ~ {{ (row.endDate || '').slice(5) }}</template>
            </el-table-column>
            <el-table-column label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="light">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template #default="{ row }">
                <el-button text type="primary" size="small" @click="openCoupon(row)">编辑</el-button>
                <el-button text type="danger" size="small" @click="delCoupon(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>

      <!-- 公告管理 -->
      <el-tab-pane label="平台公告" name="announcements">
        <div class="pc-card">
          <div style="display:flex; justify-content:space-between; margin-bottom: 12px">
            <span class="sub">向全体用户发布公告 / 活动 / 安全须知</span>
            <el-button type="primary" size="small" @click="openAnn()">发布公告</el-button>
          </div>
          <el-table :data="announcements" stripe>
            <el-table-column prop="title" label="标题" min-width="200" />
            <el-table-column label="类型" width="90">
              <template #default="{ row }">
                <el-tag effect="light" size="small">{{ TYPE_TEXT[row.type] || row.type }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="content" label="内容" min-width="260" show-overflow-tooltip />
            <el-table-column label="置顶" width="70">
              <template #default="{ row }">{{ row.pinned ? '📌' : '' }}</template>
            </el-table-column>
            <el-table-column prop="createTime" label="时间" width="160" />
            <el-table-column label="操作" width="150">
              <template #default="{ row }">
                <el-button text type="primary" size="small" @click="openAnn(row)">编辑</el-button>
                <el-button text type="danger" size="small" @click="delAnn(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 券弹窗 -->
    <el-dialog v-model="couponVisible" :title="couponForm.id ? '编辑优惠券' : '新增优惠券'" width="460px">
      <el-form label-width="90px">
        <el-form-item label="名称" required><el-input v-model="couponForm.name" /></el-form-item>
        <el-form-item label="类型">
          <el-radio-group v-model="couponForm.type">
            <el-radio value="DISCOUNT">满减</el-radio>
            <el-radio value="PERCENT">折扣</el-radio>
          </el-radio-group>
        </el-form-item>
        <template v-if="couponForm.type === 'DISCOUNT'">
          <el-form-item label="满额门槛"><el-input-number v-model="couponForm.threshold" :min="0" /></el-form-item>
          <el-form-item label="减免金额"><el-input-number v-model="couponForm.amount" :min="1" /></el-form-item>
        </template>
        <template v-else>
          <el-form-item label="折扣">
            <el-input-number v-model="couponForm.percent" :min="10" :max="99" /> <span class="sub">（如 90 = 9 折）</span>
          </el-form-item>
        </template>
        <el-form-item label="发放总量"><el-input-number v-model="couponForm.totalCount" :min="1" /></el-form-item>
        <el-form-item label="有效期">
          <el-date-picker v-model="couponForm.startDate" type="date" value-format="YYYY-MM-DD" style="width: 46%" />
          <el-date-picker v-model="couponForm.endDate" type="date" value-format="YYYY-MM-DD" style="width: 46%; margin-left: 8%" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="couponForm.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="couponVisible = false">取消</el-button>
        <el-button type="primary" @click="saveCoupon">保存</el-button>
      </template>
    </el-dialog>

    <!-- 公告弹窗 -->
    <el-dialog v-model="annVisible" :title="annForm.id ? '编辑公告' : '发布公告'" width="500px">
      <el-form label-width="70px">
        <el-form-item label="标题" required><el-input v-model="annForm.title" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="annForm.type" style="width: 150px">
            <el-option label="通知" value="NOTICE" />
            <el-option label="活动" value="ACTIVITY" />
            <el-option label="安全" value="SAFETY" />
          </el-select>
          <span style="margin-left: 16px; font-size: 13px">置顶 <el-switch v-model="annForm.pinned" :active-value="1" :inactive-value="0" /></span>
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="annForm.content" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="annVisible = false">取消</el-button>
        <el-button type="primary" @click="saveAnn">发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

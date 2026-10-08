<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../../api/http'

interface Exc { id: number; petName?: string; type: string; level: string; description: string; status: string; createTime: string }

const pets = ref<{ id: number; name: string }[]>([])
const orders = ref<{ id: number; orderNo: string; petId: number; startDate?: string; endDate?: string }[]>([])
const list = ref<Exc[]>([])
const visible = ref(false)
const form = ref({ petId: undefined as number | undefined, orderId: undefined as number | undefined, type: 'APPETITE', level: 'NORMAL', description: '' })

const TYPE_TEXT: Record<string, string> = { MENTAL: '精神异常', APPETITE: '食欲异常', VOMIT: '呕吐', DIARRHEA: '腹泻', INJURY: '外伤', BEHAVIOR: '行为异常', OTHER: '其他' }
const LEVEL_TEXT: Record<string, string> = { MILD: '轻微', NORMAL: '一般', SEVERE: '严重' }
const STATUS_TEXT: Record<string, string> = { PENDING: '待处理', PROCESSING: '处理中', OBSERVING: '观察中', RESOLVED: '已解决', CLOSED: '已关闭' }

/** 当前选中宠物在寄养中的订单（用于关联异常事件） */
const ordersOfPet = computed(() => {
  if (!form.value.petId) return []
  return orders.value.filter(o => o.petId === form.value.petId)
})

async function load() {
  pets.value = await http.get<never, any[]>('/pets')
  orders.value = (await http.get<never, any>('/orders', { params: { size: 50 } })).records || []
  list.value = await http.get<never, Exc[]>('/exceptions', { params: { status: 'ALL' } })
}

async function submit() {
  if (!form.value.petId || !form.value.description) return ElMessage.warning('请选择宠物并描述情况')
  await http.post('/exceptions', form.value)
  ElMessage.success('已上报，管理员将尽快处理')
  visible.value = false
  load()
}

onMounted(load)
</script>

<template>
  <div>
    <div class="page-head">
      <div>
        <h2 class="page-title">异常上报</h2>
        <p class="page-desc">发现宠物状态异常时第一时间上报，一般/严重级别会通知管理员</p>
      </div>
      <el-button type="primary" icon="WarningFilled" @click="visible = true">上报异常</el-button>
    </div>

    <div class="pc-card">
      <el-table :data="list" stripe>
        <el-table-column prop="petName" label="宠物" width="90" />
        <el-table-column label="类型" width="110">
          <template #default="{ row }">{{ TYPE_TEXT[row.type] || row.type }}</template>
        </el-table-column>
        <el-table-column label="级别" width="80">
          <template #default="{ row }">
            <el-tag :type="row.level === 'SEVERE' ? 'danger' : row.level === 'NORMAL' ? 'warning' : 'info'" effect="light">
              {{ LEVEL_TEXT[row.level] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="情况描述" min-width="200" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag effect="light">{{ STATUS_TEXT[row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="上报时间" width="170" />
      </el-table>
      <el-empty v-if="!list.length" description="暂无异常上报记录——这是好事" :image-size="70" />
    </div>

    <el-dialog v-model="visible" title="上报异常事件" width="480px">
      <el-form label-width="90px">
        <el-form-item label="宠物" required>
          <el-select v-model="form.petId" placeholder="选择宠物" style="width: 100%" @change="form.orderId = undefined">
            <el-option v-for="p in pets" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="关联订单" v-if="ordersOfPet.length">
          <el-select v-model="form.orderId" placeholder="选择本次寄养订单（选填）" clearable style="width: 100%">
            <el-option v-for="o in ordersOfPet" :key="o.id"
                       :label="`${o.orderNo}（${o.startDate} ~ ${o.endDate}）`" :value="o.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="异常类型">
          <el-select v-model="form.type" style="width: 100%">
            <el-option v-for="(t, k) in TYPE_TEXT" :key="k" :label="t" :value="k" />
          </el-select>
        </el-form-item>
        <el-form-item label="严重程度">
          <el-radio-group v-model="form.level">
            <el-radio value="MILD">轻微</el-radio>
            <el-radio value="NORMAL">一般（通知管理员）</el-radio>
            <el-radio value="SEVERE">严重（通知主人）</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="情况描述" required>
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="具体表现、发生时间、已采取的措施…" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="submit">提交上报</el-button>
      </template>
    </el-dialog>
  </div>
</template>

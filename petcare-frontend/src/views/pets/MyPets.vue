<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import api, { type Pet, type PetPayload, type PetTag } from '../../api'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const route = useRoute()
const store = useUserStore()

/** 员工/管理员进入本页为只读浏览（员工端宠物档案用于查看全店宠物，不允许在自己账号下造数据） */
const readonly = computed(() => store.isStaff || store.isAdmin)

/** 档案详情路径：按当前端分流，管理员不再跳到员工端 */
const petDetailPath = (id: number) =>
  store.isAdmin ? `/admin/pets/${id}` : store.isStaff ? `/staff/pets/${id}` : `/pets/${id}`

const pets = ref<Pet[]>([])
const tags = ref<PetTag[]>([])
const loading = ref(false)
const visible = ref(false)
const submitting = ref(false)
const editingId = ref<number | null>(null)
const photoUrl = ref('')

const form = reactive<PetPayload>({
  name: '', species: 'DOG', breed: '', gender: 'M', birthDate: undefined,
  weight: undefined, sterilized: 0, furColor: '', photo: '', temperament: '', notes: '', tagIds: [],
})

async function load() {
  loading.value = true
  try {
    pets.value = await api.pets()
    tags.value = await api.tagDict()
  } finally {
    loading.value = false
  }
}

function openAdd() {
  editingId.value = null
  Object.assign(form, {
    id: undefined, name: '', species: 'DOG', breed: '', gender: 'M', birthDate: undefined,
    weight: undefined, sterilized: 0, furColor: '', photo: '', temperament: '', notes: '', tagIds: [],
  })
  photoUrl.value = ''
  visible.value = true
}

function openEdit(pet: Pet) {
  editingId.value = pet.id
  Object.assign(form, {
    id: pet.id, name: pet.name, species: pet.species, breed: pet.breed, gender: pet.gender,
    birthDate: pet.birthDate, weight: pet.weight, sterilized: pet.sterilized, furColor: pet.furColor,
    photo: pet.photo, temperament: pet.temperament, notes: pet.notes,
    tagIds: (pet.tags || []).map(t => tags.value.find(x => x.name === t)?.id).filter(Boolean) as number[],
  })
  photoUrl.value = pet.photo || ''
  visible.value = true
}

async function uploadPhoto(opt: { file: Blob }) {
  try {
    const fd = new FormData()
    fd.append('file', opt.file)
    fd.append('bizType', 'PET')
    const data = await api.upload(fd)
    photoUrl.value = data.url
    form.photo = data.url
  } catch (e: any) {
    ElMessage.error(e?.message || '图片上传失败')
  }
}

async function save() {
  if (!form.name) return ElMessage.warning('给毛孩子起个名字吧')
  // 同一主人下不允许重名（后端同样会校验，这里先给出即时提示）
  const dup = pets.value.some(p => p.name === form.name!.trim() && p.id !== editingId.value)
  if (dup) return ElMessage.warning(`已经有叫「${form.name.trim()}」的毛孩子了，换个名字吧`)
  submitting.value = true
  try {
    if (editingId.value) await api.updatePet(editingId.value, { ...form })
    else await api.savePet({ ...form })
    ElMessage.success('保存成功')
    visible.value = false
    load()
  } finally {
    submitting.value = false
  }
}

async function del(pet: Pet) {
  await ElMessageBox.confirm(`确定删除「${pet.name}」的档案吗？`, '删除确认', {
    confirmButtonText: '确认删除', cancelButtonText: '取消', type: 'warning',
  })
  await api.deletePet(pet.id)
  ElMessage.success('已删除')
  load()
}

const speciesText = (s: string) => (s === 'CAT' ? '猫咪' : '狗狗')
const genderText = (g: string) => (g === 'M' ? '♂ 公' : '♀ 母')

onMounted(async () => {
  await load()
  // 支持从首页“+ 添加宠物”跳转过来自动弹出添加框（/pets?add=1）
  if (route.query.add === '1') openAdd()
})
</script>

<template>
  <div>
    <div class="page-head">
      <div>
        <h2 class="page-title">我的宠物</h2>
        <p class="page-desc">完善档案有助于我们安排合适的房间与照护方式</p>
      </div>
      <el-button v-if="!readonly" type="primary" icon="Plus" @click="openAdd">添加宠物</el-button>
      <el-tag v-else effect="plain" type="info">员工只读 · 全店宠物档案</el-tag>
    </div>

    <el-row v-if="pets.length" :gutter="16">
      <el-col v-for="p in pets" :key="p.id" :span="8" style="margin-bottom: 16px">
        <div class="pc-card hoverable" style="height: 100%">
          <div class="pet-top">
            <div class="pet-avatar" :class="p.species">
              <img v-if="p.photo" :src="p.photo" alt="" />
              <span v-else>{{ p.species === 'CAT' ? '🐱' : '🐶' }}</span>
            </div>
            <div class="pet-head-info">
              <b style="font-size: 16px">{{ p.name }}</b>
              <div class="sub">{{ p.breed || '未知品种' }} · {{ p.age ?? '?' }}岁 · {{ genderText(p.gender) }}</div>
              <div class="sub" v-if="p.weight">{{ p.weight }}kg{{ p.furColor ? ' · ' + p.furColor : '' }}</div>
            </div>
          </div>
          <div class="tag-row">
            <el-tag v-for="t in p.tags" :key="t" effect="light" color="#EAF2ED" style="color:#5A8F76"># {{ t }}</el-tag>
            <el-tag v-if="p.sterilized" effect="light" color="#FBEEDD" style="color:#C97B2C">已绝育</el-tag>
          </div>
          <p v-if="p.notes" class="sub notes">⚠ {{ p.notes }}</p>
          <div class="ops">
            <el-button type="primary" plain round size="small"
                       @click="router.push(petDetailPath(p.id))">查看档案</el-button>
            <el-button v-if="!readonly" round size="small" @click="openEdit(p)">编辑</el-button>
            <el-button v-if="!readonly" round size="small" type="danger" plain @click="del(p)">删除</el-button>
          </div>
        </div>
      </el-col>
    </el-row>

    <div v-else-if="!loading" class="pc-card" style="text-align: center; padding: 60px">
      <el-icon :size="46" color="#A8C8B5"><Histogram /></el-icon>
      <h3 style="margin: 12px 0 6px">{{ readonly ? '暂没有宠物档案' : '还没有添加宠物' }}</h3>
      <p class="sub">{{ readonly ? '主人创建的宠物档案会展示在这里。' : '添加宠物后，就可以开始寄养预约啦。' }}</p>
      <el-button v-if="!readonly" type="primary" round style="margin-top: 14px" @click="openAdd">添加宠物</el-button>
    </div>

    <el-dialog v-model="visible" :title="editingId ? '编辑宠物档案' : '添加宠物'" width="560px">
      <el-form label-width="90px">
        <el-form-item label="档案照">
          <el-upload :show-file-list="false" accept="image/*" :http-request="uploadPhoto">
            <div class="photo-ring">
              <img v-if="photoUrl" :src="photoUrl" alt="" />
              <el-icon v-else :size="24" color="#A8C8B5"><Camera /></el-icon>
            </div>
          </el-upload>
        </el-form-item>
        <el-form-item label="昵称" required>
          <el-input v-model="form.name" placeholder="毛孩子叫什么" />
        </el-form-item>
        <el-form-item label="物种" required>
          <el-radio-group v-model="form.species">
            <el-radio-button value="DOG">狗狗</el-radio-button>
            <el-radio-button value="CAT">猫咪</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="品种"><el-input v-model="form.breed" placeholder="如：金毛寻回犬" /></el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="form.gender">
            <el-radio value="M">公</el-radio>
            <el-radio value="F">母</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="生日">
          <el-date-picker v-model="form.birthDate" type="date" value-format="YYYY-MM-DD"
                          :disabled-date="(d: Date) => d.getTime() > Date.now()"
                          placeholder="选择出生日期（不能晚于今天）" style="width: 100%" />
        </el-form-item>
        <el-form-item label="体重(kg)"><el-input-number v-model="form.weight" :min="0" :precision="1" /></el-form-item>
        <el-form-item label="毛色"><el-input v-model="form.furColor" placeholder="如：金黄色" /></el-form-item>
        <el-form-item label="已绝育">
          <el-switch v-model="form.sterilized" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="性格标签">
          <el-select v-model="form.tagIds" multiple collapse-tags placeholder="选择性格标签" style="width: 100%">
            <el-option v-for="t in tags" :key="t.id" :label="'# ' + t.name" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="性格描述"><el-input v-model="form.temperament" placeholder="温顺亲人 / 活泼好动…" /></el-form-item>
        <el-form-item label="注意事项">
          <el-input v-model="form.notes" type="textarea" :rows="2"
                    placeholder="寄养期间需要特别注意的事，如：怕打雷、不喜欢陌生大型犬" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.pet-top { display: flex; align-items: center; gap: 14px; }
.pet-avatar { width: 66px; height: 66px; border-radius: 20px; overflow: hidden; flex-shrink: 0;
  display: flex; align-items: center; justify-content: center; font-size: 30px;
  border: 2px solid #fff; box-shadow: 0 8px 18px -8px rgba(47,70,57,.35);
  background: linear-gradient(135deg, #EAF2ED, #D9E8E0); }
.pet-avatar.CAT { background: linear-gradient(135deg, #FDF1E4, #F7E2CF); }
.pet-avatar img { width: 100%; height: 100%; object-fit: cover; }
.pet-head-info { display: flex; flex-direction: column; gap: 2px; min-width: 0; }
.tag-row { margin: 12px 0 8px; display: flex; flex-wrap: wrap; gap: 6px; }
.notes { background: var(--pc-danger-soft); color: #B5574D; border-radius: 8px; padding: 7px 10px; }
.ops { display: flex; gap: 8px; margin-top: 8px; }
.photo-ring { width: 74px; height: 74px; border-radius: 50%; border: 2px dashed var(--pc-primary);
  display: flex; align-items: center; justify-content: center; overflow: hidden; }
.photo-ring img { width: 100%; height: 100%; object-fit: cover; }
</style>

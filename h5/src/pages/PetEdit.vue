<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import api from '../api'

const route = useRoute()
const router = useRouter()
const id = route.query.id ? Number(route.query.id) : null

const form = ref({
  id, name: '', species: 'DOG', breed: '', gender: 'M', birthDate: null,
  weight: null, sterilized: 0, temperament: '温顺', aggressive: 0,
  vaccinated: 0, vaccineNote: '', photo: '', notes: '',
})
const showBirth = ref(false)

onMounted(async () => {
  if (id) {
    const pets = await api.pets()
    const p = pets.find(x => x.id === id)
    if (p) form.value = { ...form.value, ...p }
  }
})

/** 压缩照片为 Base64（演示环境直存，生产建议对象存储） */
function compress(file) {
  return new Promise(resolve => {
    const img = new Image()
    const url = URL.createObjectURL(file)
    img.onload = () => {
      const canvas = document.createElement('canvas')
      const scale = Math.min(1, 480 / Math.max(img.width, img.height))
      canvas.width = Math.round(img.width * scale)
      canvas.height = Math.round(img.height * scale)
      canvas.getContext('2d').drawImage(img, 0, 0, canvas.width, canvas.height)
      URL.revokeObjectURL(url)
      resolve(canvas.toDataURL('image/jpeg', 0.7))
    }
    img.src = url
  })
}

async function afterRead({ file }) {
  form.value.photo = await compress(file)
}

async function save() {
  if (!form.value.name) return showToast('给毛孩子起个名字吧')
  await api.savePet({ ...form.value, birthDate: form.value.birthDate || null })
  showToast({ type: 'success', message: '保存成功' })
  router.back()
}
</script>

<template>
  <div class="page">
    <van-nav-bar left-arrow @click-left="router.back()" :title="id ? '编辑档案' : '添加毛孩子'" />

    <div class="photo-area">
      <van-uploader :after-read="afterRead" :max-count="1" :deletable="false">
        <div class="photo-ring" :class="{ filled: form.photo }">
          <img v-if="form.photo" :src="form.photo" alt="" />
          <template v-else>
            <van-icon name="photograph" size="26" />
            <i>上传照片</i>
          </template>
        </div>
      </van-uploader>
      <div class="photo-tip sub">照片将展示在宠物档案中，帮助商家提前认识它</div>
    </div>

    <van-cell-group inset>
      <van-field v-model="form.name" label="昵称" placeholder="毛孩子叫什么" required />
      <van-field label="物种" required>
        <template #input>
          <van-radio-group v-model="form.species" direction="horizontal">
            <van-radio name="DOG">狗狗</van-radio>
            <van-radio name="CAT">猫咪</van-radio>
          </van-radio-group>
        </template>
      </van-field>
      <van-field v-model="form.breed" label="品种" placeholder="如：金毛寻回犬" />
      <van-field label="性别">
        <template #input>
          <van-radio-group v-model="form.gender" direction="horizontal">
            <van-radio name="M">弟弟</van-radio>
            <van-radio name="F">妹妹</van-radio>
          </van-radio-group>
        </template>
      </van-field>
      <van-field label="生日" readonly :model-value="form.birthDate || '选择日期'" @click="showBirth = true" is-link />
      <van-field v-model="form.weight" label="体重(kg)" type="number" placeholder="如 12.5" />
      <van-field label="性格">
        <template #input>
          <van-radio-group v-model="form.temperament" direction="horizontal">
            <van-radio>温顺</van-radio>
            <van-radio>活泼</van-radio>
            <van-radio>警惕</van-radio>
          </van-radio-group>
        </template>
      </van-field>
      <van-field label="已绝育">
        <template #input>
          <van-switch v-model="form.sterilized" :active-value="1" :inactive-value="0" size="22px" />
        </template>
      </van-field>
      <van-field label="有攻击倾向">
        <template #input>
          <van-switch v-model="form.aggressive" :active-value="1" :inactive-value="0" size="22px" />
          <span class="switch-note">开启后仅推荐可单独照看的房型</span>
        </template>
      </van-field>
      <van-field label="疫苗齐全">
        <template #input>
          <van-switch v-model="form.vaccinated" :active-value="1" :inactive-value="0" size="22px" />
        </template>
      </van-field>
      <van-field v-model="form.vaccineNote" label="疫苗记录" placeholder="如：2026-03 猫三联年度" />
      <van-field v-model="form.notes" label="备注" type="textarea" rows="2" autosize
                 placeholder="生活习惯、禁忌、安抚方式等" />
    </van-cell-group>

    <van-button type="primary" round block class="btn-main press" @click="save">保存档案</van-button>

    <van-calendar v-model:show="showBirth" :min-date="new Date(2010, 0, 1)" :max-date="new Date()"
                  @confirm="d => { form.birthDate = d.toISOString().slice(0, 10); showBirth = false }" />
  </div>
</template>

<style scoped>
.photo-area { display: flex; flex-direction: column; align-items: center; padding: 22px 0 10px; }
.photo-ring { width: 96px; height: 96px; border-radius: 50%; border: 2.5px dashed var(--brand);
  display: flex; flex-direction: column; align-items: center; justify-content: center; overflow: hidden;
  color: var(--brand); background: #fff; gap: 3px; }
.photo-ring i { font-style: normal; font-size: 11px; }
.photo-ring.filled { border-style: solid; box-shadow: var(--clay); }
.photo-ring img { width: 100%; height: 100%; object-fit: cover; }
.photo-tip { margin-top: 12px; }
.switch-note { font-size: 11px; color: var(--ink-3); margin-left: 10px; }
</style>

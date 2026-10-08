<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showConfirmDialog, showToast } from 'vant'
import api from '../api'
import { toneStyle } from '../utils/tone'
import Illust from '../components/Illust.vue'

const router = useRouter()
const pets = ref([])

async function load() { pets.value = await api.pets() }

async function del(p) {
  try {
    await showConfirmDialog({ title: '删除档案', message: `确定删除「${p.name}」的档案吗？` })
    await api.delPet(p.id)
    showToast('已删除')
    load()
  } catch (e) { /* 用户取消 */ }
}

function age(b) {
  if (!b) return ''
  const days = Math.max(0, Math.round((Date.now() - new Date(b)) / 86400000))
  return days >= 365 ? `${Math.floor(days / 365)}岁` : `${Math.floor(days / 30)}个月`
}

onMounted(load)
</script>

<template>
  <div class="page">
    <van-nav-bar left-arrow @click-left="router.back()" title="我的毛孩子" />
    <div class="tip sub2">完善档案有助于商家安排合适的笼位和照看方式</div>

    <div v-for="p in pets" :key="p.id" class="clay pet-card">
      <div class="pet-avatar" :style="toneStyle(p.name)">
        <span>{{ p.species === 'CAT' ? '🐱' : '🐶' }}</span>
      </div>
      <div class="pet-info">
        <div class="name-line">
          <b>{{ p.name }}</b>
          <span class="sub">{{ p.gender === 'M' ? '♂ 弟弟' : '♀ 妹妹' }} · {{ age(p.birthDate) }}</span>
        </div>
        <div class="sub">{{ p.breed || '未知品种' }} · {{ p.weight }}kg · {{ p.temperament }}</div>
        <div class="pet-tags">
          <span v-if="p.vaccinated" class="chip chip-mint">疫苗齐全</span>
          <span v-if="p.sterilized" class="chip chip-sky">已绝育</span>
          <span v-if="p.aggressive" class="chip chip-rose">需单独照看</span>
        </div>
        <div class="sub" v-if="p.vaccineNote">疫苗记录：{{ p.vaccineNote }}</div>
      </div>
      <div class="ops">
        <van-button size="small" round plain type="primary" @click="router.push(`/pets/edit?id=${p.id}`)">编辑</van-button>
        <van-button size="small" round plain @click="del(p)">删除</van-button>
      </div>
    </div>

    <div v-if="!pets.length" class="empty-wrap">
      <Illust scene="sleep" style="width: 180px; margin: 0 auto; display: block;" />
      <div class="sub" style="text-align:center; margin-top: 6px;">还没有毛孩子档案，添加一只吧</div>
    </div>
    <van-button type="primary" round block icon="plus" class="btn-main press" @click="router.push('/pets/edit')">添加毛孩子</van-button>
  </div>
</template>

<style scoped>
.tip { margin: 4px 20px 10px; }
.pet-card { display: flex; margin: 12px 16px; padding: 14px; align-items: center; }
.pet-avatar { width: 62px; height: 62px; border-radius: 50%; margin-right: 13px; flex-shrink: 0;
  display: flex; align-items: center; justify-content: center; font-size: 26px; }
.pet-info { flex: 1; min-width: 0; }
.name-line { display: flex; gap: 10px; align-items: baseline; }
.pet-tags { margin: 7px 0 6px; display: flex; gap: 6px; }
.ops { display: flex; flex-direction: column; gap: 8px; margin-left: 8px; }
</style>

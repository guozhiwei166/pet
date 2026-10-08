<script setup>
defineProps({
  photo: { type: String, default: '' },
  species: { type: String, default: 'DOG' },
  round: { type: Boolean, default: false },
})
const emojiOf = s => (s === 'CAT' ? '🐱' : s === 'SHOP' ? '🏠' : '🐶')
</script>

<template>
  <img v-if="photo" :src="photo" :class="['pb-img', { round }]" alt="" />
  <div v-else :class="['ph', species === 'CAT' ? 'ph-cat' : 'ph-shop', 'pb-img', { round }]">
    {{ emojiOf(species) }}
  </div>
</template>

<style scoped>
.pb-img { width: 100%; height: 100%; object-fit: cover; display: block; }
.round { border-radius: 50%; }
</style>

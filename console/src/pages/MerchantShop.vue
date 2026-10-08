<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../api'

const form = ref({ serviceSpecies: [] })

onMounted(async () => {
  const shop = await api.shop()
  form.value = { ...shop, serviceSpecies: (shop.serviceSpecies || '').split(',').filter(Boolean) }
})

async function save() {
  await api.updateShop({ ...form.value, serviceSpecies: form.value.serviceSpecies.join(',') })
  ElMessage.success('保存成功')
}
</script>

<template>
  <div class="page-card" style="max-width: 640px">
    <el-form label-width="100px">
      <el-form-item label="店铺名称"><el-input v-model="form.shopName" /></el-form-item>
      <el-form-item label="联系电话"><el-input v-model="form.phone" /></el-form-item>
      <el-form-item label="店铺地址"><el-input v-model="form.address" /></el-form-item>
      <el-form-item label="资质说明">
        <el-input v-model="form.license" placeholder="如：动物防疫条件合格证 编号XXX（展示给用户建立信任）" />
      </el-form-item>
      <el-form-item label="可寄养物种">
        <el-checkbox-group v-model="form.serviceSpecies">
          <el-checkbox value="DOG">🐶 狗狗</el-checkbox>
          <el-checkbox value="CAT">🐱 猫咪</el-checkbox>
        </el-checkbox-group>
      </el-form-item>
      <el-form-item label="特色标签">
        <el-input v-model="form.tags" placeholder="逗号分隔，如：持证经营,监控直看" />
      </el-form-item>
      <el-form-item label="店铺介绍">
        <el-input v-model="form.description" type="textarea" :rows="4" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="save">保存</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

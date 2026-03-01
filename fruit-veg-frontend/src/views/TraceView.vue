<template>
  <div class="trace-page">
    <div class="page-header">
      <h2>全流程溯源档案</h2>
      <el-button link @click="router.back()">返回</el-button>
    </div>

    <el-skeleton v-if="loading" :rows="8" animated />

    <el-alert
      v-else-if="errorMessage"
      :title="errorMessage"
      type="error"
      show-icon
      :closable="false"
      class="status-alert"
    />

    <el-empty v-else-if="!trace" description="未找到对应的溯源信息">
      <el-button type="primary" @click="toProducts">去逛商品</el-button>
    </el-empty>

    <el-card v-else class="trace-card">
      <template #header>
        <div class="trace-head">
          <h3>{{ trace.productName }} · 溯源档案</h3>
          <div class="trace-actions">
            <el-button type="primary" @click="toProductDetail">查看商品详情</el-button>
            <el-button @click="toProducts">继续选购</el-button>
          </div>
        </div>
      </template>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="产地">{{ trace.originName }}</el-descriptions-item>
        <el-descriptions-item label="产地地址">{{ trace.originAddress }}</el-descriptions-item>
        <el-descriptions-item label="种植方式">{{ trace.plantMethod }}</el-descriptions-item>
        <el-descriptions-item label="保质期">{{ trace.shelfLife }} 天</el-descriptions-item>
        <el-descriptions-item label="种植时间">{{ trace.plantTime }}</el-descriptions-item>
        <el-descriptions-item label="采收时间">{{ trace.harvestTime }}</el-descriptions-item>
        <el-descriptions-item label="储存建议" :span="2">{{ trace.storageCondition }}</el-descriptions-item>
      </el-descriptions>

      <h4 class="sec-title">流转时间线</h4>
      <el-timeline>
        <el-timeline-item v-for="(item, idx) in trace.timeline || []" :key="idx" :timestamp="item.time" placement="top">
          <b>{{ item.stage }}</b>：{{ item.description }}
        </el-timeline-item>
      </el-timeline>

      <h4 class="sec-title">检测报告</h4>
      <el-image :src="trace.testReport" fit="cover" class="report-img" preview-teleported>
        <template #error>
          <div class="report-fallback">检测报告图片暂不可用</div>
        </template>
      </el-image>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getTraceDetail } from '@/api/trace'
import { getProductDetail } from '@/api/product'

const route = useRoute()
const router = useRouter()
const trace = ref(null)
const loading = ref(false)
const errorMessage = ref('')
const productId = ref(null)

const toProducts = () => router.push('/products')

const toProductDetail = async () => {
  if (!productId.value) {
    ElMessage.warning('未找到关联商品')
    return
  }
  router.push(`/products/${productId.value}`)
}

const fetchTraceDetail = async () => {
  loading.value = true
  errorMessage.value = ''
  try {
    const id = Number(route.params.id)
    const data = await getTraceDetail(id)
    trace.value = data || null
    if (!trace.value) {
      return
    }

    if (data?.productId) {
      productId.value = Number(data.productId)
      return
    }
    try {
      const product = await getProductDetail(id)
      productId.value = Number(product?.id || id)
    } catch (e) {
      productId.value = id
    }
  } catch (error) {
    trace.value = null
    errorMessage.value = '加载溯源信息失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

onMounted(fetchTraceDetail)
watch(() => route.params.id, fetchTraceDetail)
</script>

<style scoped>
.trace-page { padding: 20px; }
.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.trace-card { max-width: 920px; margin: 0 auto; }
.status-alert { max-width: 920px; margin: 0 auto; }
.trace-head { display: flex; justify-content: space-between; gap: 12px; align-items: center; }
.trace-actions { display: flex; gap: 8px; }
.sec-title { margin: 18px 0 10px; }
.report-img { width: 340px; height: 220px; border-radius: 10px; }
.report-fallback {
  width: 340px;
  height: 220px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #8b9a92;
  background: #eef4f0;
  border: 1px dashed #c9d8cf;
}
</style>

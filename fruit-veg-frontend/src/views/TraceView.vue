<template>
  <div class="trace-page">
    <div class="page-header">
      <h2>全流程溯源</h2>
      <el-button link @click="router.back()">返回</el-button>
    </div>

    <el-card v-if="trace" class="trace-card">
      <template #header>
        <h3>{{ trace.productName }} · 溯源档案</h3>
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
      <el-image :src="trace.testReport" fit="cover" class="report-img" preview-teleported />
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getTraceDetail } from '@/api/trace'

const route = useRoute()
const router = useRouter()
const trace = ref(null)

onMounted(async () => {
  trace.value = await getTraceDetail(route.params.id)
})
</script>

<style scoped>
.trace-page { padding: 20px; }
.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.trace-card { max-width: 920px; margin: 0 auto; }
.sec-title { margin: 18px 0 10px; }
.report-img { width: 340px; height: 220px; border-radius: 10px; }
</style>

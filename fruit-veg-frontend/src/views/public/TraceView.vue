<template>
  <div class="trace-page">
    <div class="page-container">
      <div class="page-header">
        <h2 class="title"><el-icon><Monitor /></el-icon> 果蔬全流程溯源</h2>
        <el-button link @click="router.back()">返回</el-button>
      </div>

      <!-- 搜索栏：当没有查询结果或处于查询模式时显示 -->
      <el-card class="search-card" shadow="never">
        <div class="search-input-wrapper">
          <el-input
            v-model="searchId"
            placeholder="输入商品溯源 ID (例如: 1, 2, 3...)"
            class="trace-search-input"
            size="large"
            clearable
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
            <template #append>
              <el-button type="primary" @click="handleSearch">立即查询</el-button>
            </template>
          </el-input>
        </div>
        <p class="search-hint">提示：您可以在商品包装或详情页找到该商品的溯源数字 ID。</p>
      </el-card>

      <el-skeleton v-if="loading" :rows="8" animated class="trace-skeleton" />

      <el-alert
        v-else-if="errorMessage"
        :title="errorMessage"
        type="error"
        show-icon
        :closable="false"
        class="status-alert"
      />

      <!-- 未查询到结果时的展示 -->
      <div v-else-if="!trace && !loading" class="empty-state">
        <el-empty description="暂无溯源信息">
          <p v-if="!route.params.id">请输入溯源 ID 开始查询</p>
          <p v-else>未找到 ID 为 {{ route.params.id }} 的溯源档案</p>
          <el-button type="primary" plain @click="toProducts">去逛商品</el-button>
        </el-empty>
      </div>

      <!-- 溯源详情展示 -->
      <div v-else-if="trace" class="trace-content">
        <el-card class="trace-card" shadow="hover">
          <template #header>
            <div class="trace-head">
              <div class="product-info-title">
                <el-tag type="success" effect="dark" class="trace-tag">官方验证</el-tag>
                <h3>{{ trace.productName }}</h3>
              </div>
              <div class="trace-actions">
                <el-button type="primary" plain @click="toProductDetail">商品详情</el-button>
                <el-button @click="toProducts">继续选购</el-button>
              </div>
            </div>
          </template>

          <div class="trace-grid">
            <div class="trace-left">
              <h4 class="sec-title"><el-icon><Location /></el-icon> 产地档案</h4>
              <el-descriptions :column="1" border class="custom-desc">
                <el-descriptions-item label="产地名称">{{ trace.originName }}</el-descriptions-item>
                <el-descriptions-item label="详细地址">{{ trace.originAddress }}</el-descriptions-item>
                <el-descriptions-item label="种植方式">
                  <el-tag size="small" type="info">{{ trace.plantMethod }}</el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="保质时长">{{ trace.shelfLife }} 天</el-descriptions-item>
                <el-descriptions-item label="种植日期">{{ trace.plantTime }}</el-descriptions-item>
                <el-descriptions-item label="采收日期">{{ trace.harvestTime }}</el-descriptions-item>
                <el-descriptions-item label="储存条件">{{ trace.storageCondition }}</el-descriptions-item>
              </el-descriptions>

              <h4 class="sec-title"><el-icon><Checked /></el-icon> 农残检测报告</h4>
              <div class="report-container">
                <el-image :src="trace.testReport" fit="cover" class="report-img" :preview-src-list="[trace.testReport]">
                  <template #error>
                    <div class="report-fallback">
                      <el-icon><Picture /></el-icon>
                      <span>检测报告暂不可用</span>
                    </div>
                  </template>
                </el-image>
                <div class="report-info">
                  <p><b>检测结果：</b> 合格</p>
                  <p><b>检测机构：</b> 郑州市农产品质量检测中心</p>
                  <p class="report-tip">点击图片查看高清原件</p>
                </div>
              </div>
            </div>

            <div class="trace-right">
              <h4 class="sec-title"><el-icon><Calendar /></el-icon> 流转时间线</h4>
              <el-timeline class="custom-timeline">
                <el-timeline-item
                  v-for="(item, idx) in trace.timeline || []"
                  :key="idx"
                  :timestamp="item.time"
                  placement="top"
                  :type="idx === 0 ? 'primary' : ''"
                  :hollow="idx !== 0"
                >
                  <div class="timeline-content">
                    <b>{{ item.stage }}</b>
                    <p>{{ item.description }}</p>
                  </div>
                </el-timeline-item>
              </el-timeline>
            </div>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Monitor, Search, Location, Checked, Calendar, Picture } from '@element-plus/icons-vue'
import { getTraceDetail } from '@/api/trace'
import { getProductDetail } from '@/api/product'

const route = useRoute()
const router = useRouter()
const trace = ref(null)
const loading = ref(false)
const errorMessage = ref('')
const searchId = ref('')
const productId = ref(null)

const toProducts = () => router.push('/products')

const toProductDetail = () => {
  if (productId.value) {
    router.push(`/products/${productId.value}`)
  } else if (trace.value?.productId) {
    router.push(`/products/${trace.value.productId}`)
  } else {
    ElMessage.warning('未找到关联商品')
  }
}

const handleSearch = () => {
  if (!searchId.value.trim()) {
    ElMessage.warning('请输入溯源 ID')
    return
  }
  router.push(`/trace/${searchId.value.trim()}`)
}

const fetchTraceDetail = async (id) => {
  if (!id) {
    trace.value = null
    return
  }
  loading.value = true
  errorMessage.value = ''
  try {
    const data = await getTraceDetail(id)
    trace.value = data || null
    
    if (data?.productId) {
      productId.value = data.productId
    } else {
      // 尝试获取关联商品ID（兜底方案）
      try {
        const p = await getProductDetail(id)
        productId.value = p?.id || null
      } catch(e) {}
    }
  } catch (error) {
    trace.value = null
    errorMessage.value = '加载溯源信息失败，请检查 ID 是否正确'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  if (route.params.id) {
    searchId.value = route.params.id
    fetchTraceDetail(route.params.id)
  }
})

watch(() => route.params.id, (newId) => {
  if (newId) {
    searchId.value = newId
    fetchTraceDetail(newId)
  } else {
    trace.value = null
    searchId.value = ''
  }
})
</script>

<style scoped>
.trace-page {
  background-color: #f8faf9;
  min-height: 100vh;
  padding: 30px 20px;
}
.page-container {
  max-width: 1100px;
  margin: 0 auto;
}
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}
.title {
  margin: 0;
  display: flex;
  align-items: center;
  gap: 10px;
  color: #2c3e50;
  font-size: 24px;
}
.search-card {
  border-radius: 12px;
  margin-bottom: 30px;
  background: white;
  border: 1px solid #eef2f0;
}
.search-input-wrapper {
  max-width: 600px;
  margin: 0 auto;
}
.trace-search-input :deep(.el-input-group__append) {
  background-color: #2fa36f;
  color: white;
  border-color: #2fa36f;
}
.search-hint {
  text-align: center;
  font-size: 13px;
  color: #95a5a6;
  margin-top: 15px;
}

.trace-card {
  border-radius: 16px;
  overflow: hidden;
  border: none;
}
.trace-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.product-info-title {
  display: flex;
  align-items: center;
  gap: 12px;
}
.product-info-title h3 {
  margin: 0;
  font-size: 20px;
  color: #2c3e50;
}
.trace-tag {
  border-radius: 4px;
  font-weight: 600;
}

.trace-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 40px;
  padding: 10px 0;
}

.sec-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 20px;
  color: #2fa36f;
  font-size: 18px;
  border-bottom: 2px solid #f0f4f2;
  padding-bottom: 10px;
}

.custom-desc :deep(.el-descriptions__label) {
  width: 100px;
  background-color: #f9fbfa;
  color: #7f8c8d;
  font-weight: 600;
}

.report-container {
  display: flex;
  gap: 20px;
  margin-top: 15px;
  background: #fcfdfc;
  padding: 15px;
  border-radius: 12px;
  border: 1px solid #f0f4f2;
}
.report-img {
  width: 180px;
  height: 120px;
  border-radius: 8px;
  cursor: pointer;
  transition: opacity 0.3s;
}
.report-img:hover { opacity: 0.8; }
.report-info { flex: 1; }
.report-info p { margin: 0 0 8px; font-size: 14px; color: #34495e; }
.report-tip { font-size: 12px; color: #95a5a6; }

.report-fallback {
  width: 180px;
  height: 120px;
  background: #f4f7f5;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #95a5a6;
  font-size: 12px;
  border-radius: 8px;
}

.timeline-content b {
  color: #2c3e50;
  font-size: 15px;
}
.timeline-content p {
  margin: 5px 0 0;
  color: #7f8c8d;
  font-size: 14px;
  line-height: 1.6;
}

.empty-state {
  padding: 60px 0;
  background: white;
  border-radius: 16px;
}

@media (max-width: 900px) {
  .trace-grid {
    grid-template-columns: 1fr;
    gap: 30px;
  }
}
</style>

<template>
  <div class="home-page">
    <main class="main">
      <div class="hero-section">
        <div class="hero-content">
          <h1 class="hero-title">郑州果蔬，新鲜直达</h1>
          <p class="hero-subtitle">当季直采 · 透明溯源 · 48小时冷链</p>
          <div class="quick-actions">
            <el-button type="primary" size="large" @click="navigate('/products')">去逛商品</el-button>
            <el-button size="large" @click="navigate('/circle')">看果蔬圈</el-button>
            <el-button type="success" size="large" plain @click="toAi">咨询 AI 专家</el-button>
          </div>
        </div>
      </div>

      <el-carousel :interval="4500" indicator-position="outside" class="banner">
        <el-carousel-item v-for="item in banners" :key="item.id">
          <img :src="item.imageUrl" :alt="item.title" class="banner-image" @click="goBanner(item)" />
          <div class="banner-caption">{{ item.title }}</div>
        </el-carousel-item>
      </el-carousel>

      <el-row :gutter="20" class="info-row">
        <el-col :span="16">
          <el-card shadow="never" class="notice-card">
            <template #header>
              <div class="card-header">
                <span class="card-title">平台公告</span>
                <el-button link>更多</el-button>
              </div>
            </template>
            <div v-for="notice in notices.slice(0, 4)" :key="notice.id" class="notice-item">
              <el-tag size="small" :type="notice.type === '平台公告' ? 'warning' : 'info'" effect="light">{{ notice.type }}</el-tag>
              <div class="notice-body">
                <div class="notice-title">{{ notice.title }}</div>
                <div class="notice-content">{{ notice.content }}</div>
              </div>
              <span class="notice-date">{{ formatDate(notice.publishTime) }}</span>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="never" class="guide-card">
            <template #header>
              <div class="card-title">快速入口</div>
            </template>
            <div class="guide-grid">
              <div class="guide-item" @click="navigate('/products')">
                <el-icon><Shop /></el-icon>
                <span>选购商品</span>
              </div>
              <div class="guide-item" @click="navigate('/circle')">
                <el-icon><ChatDotRound /></el-icon>
                <span>果蔬圈</span>
              </div>
              <div class="guide-item" @click="toAi">
                <el-icon><MagicStick /></el-icon>
                <span>AI 建议</span>
              </div>
              <div class="guide-item" @click="navigate('/trace')">
                <el-icon><Search /></el-icon>
                <span>溯源查询</span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <section class="section">
        <div class="section-head">
          <h2 class="section-title">商品分类</h2>
          <el-button link @click="navigate('/products')">查看全部 <el-icon><ArrowRight /></el-icon></el-button>
        </div>
        <div class="category-grid">
          <div v-for="category in categories" :key="category.id" class="category-item" @click="openCategory(category)">
            <span class="cat-name">{{ category.name }}</span>
          </div>
        </div>
      </section>

      <section class="section">
        <div class="section-head">
          <h2 class="section-title">精选推荐</h2>
          <span class="section-desc">每日清晨采收，确保极致新鲜</span>
        </div>
        <el-row :gutter="20">
          <el-col :xs="24" :sm="12" :md="6" v-for="product in hotProducts" :key="product.id" class="product-col">
            <ProductCard 
              :product="product" 
              @navigate="navigate" 
              @add-to-cart="addToCart" 
            />
          </el-col>
        </el-row>
      </section>
    </main>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Shop, ChatDotRound, MagicStick, Search, ArrowRight } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import { useAppNavigation } from '@/composables/useAppNavigation'
import { getCategoryTree, getRecommendProducts } from '@/api/product'
import { getHomeBanners, getNoticeList } from '@/api/content'
import { formatDate } from '@/utils/format'
import ProductCard from '@/components/ProductCard.vue'

const userStore = useUserStore()
const cartStore = useCartStore()
const { navigate } = useAppNavigation()

const categories = ref([])
const hotProducts = ref([])
const banners = ref([])
const notices = ref([])

const fetchHomeData = async () => {
  try {
    const [catData, productData, bannerData, noticeData] = await Promise.all([
      getCategoryTree(),
      getRecommendProducts(),
      getHomeBanners(),
      getNoticeList()
    ])
    categories.value = catData || []
    hotProducts.value = productData || []
    banners.value = bannerData || []
    notices.value = noticeData || []
  } catch (error) {
    console.error('Fetch home data error:', error)
  }
}

const addToCart = (product) => {
  if (!userStore.getIsLoggedIn) {
    ElMessage.warning('登录后可加入购物车')
    navigate('/login')
    return
  }
  cartStore.addToCart(product, 1)
  ElMessage.success('已加入购物车')
}

const openCategory = (category) => {
  navigate({ path: '/products', query: { category: category.id } })
}

const toAi = () => {
  if (!userStore.getIsLoggedIn) {
    ElMessage.warning('AI 咨询需登录后使用')
    navigate('/login')
    return
  }
  navigate('/ai-expert')
}

const goBanner = (item) => {
  if (item.targetUrl?.startsWith('/')) {
    navigate(item.targetUrl)
  }
}

onMounted(fetchHomeData)
</script>

<style scoped>
.home-page { 
  background-color: #f5f7f6;
  min-height: 100vh; 
}
.main {
  max-width: 1240px;
  margin: 0 auto;
  padding: 20px;
}

/* Hero Section 优化 */
.hero-section {
  background: linear-gradient(135deg, #11845a 0%, #2fa36f 100%);
  border-radius: 16px;
  padding: 30px 20px;
  margin-bottom: 24px;
  color: white;
  text-align: center;
  box-shadow: 0 4px 20px rgba(17, 132, 90, 0.1);
}
.hero-title {
  font-size: 32px;
  margin: 0 0 10px;
  font-weight: 800;
  letter-spacing: 0.5px;
}
.hero-subtitle {
  font-size: 16px;
  opacity: 0.9;
  margin-bottom: 20px;
}
.quick-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.banner { margin-bottom: 30px; }
.banner-image {
  width: 100%;
  height: 400px;
  object-fit: cover;
  border-radius: 20px;
  cursor: pointer;
}
.banner-caption {
  position: absolute;
  left: 30px;
  bottom: 30px;
  color: #fff;
  font-size: 24px;
  font-weight: 700;
  text-shadow: 0 4px 15px rgba(0, 0, 0, 0.4);
}

.info-row { margin-bottom: 30px; }
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.card-title { font-weight: 800; font-size: 18px; color: #2c3e50; }

/* 公告列表优化 */
.notice-card { border-radius: 16px; border: none; }
.notice-item {
  display: flex;
  align-items: flex-start;
  gap: 15px;
  padding: 16px 0;
  border-bottom: 1px solid #edf2f0;
}
.notice-item:last-child { border-bottom: none; }
.notice-body { flex: 1; }
.notice-title { font-weight: 600; color: #2c3e50; margin-bottom: 4px; }
.notice-content { font-size: 13px; color: #7f8c8d; line-height: 1.5; }
.notice-date { font-size: 12px; color: #95a5a6; }

/* 快速入口网格 */
.guide-card { border-radius: 16px; border: none; height: 100%; }
.guide-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 15px;
  padding-top: 10px;
}
.guide-item {
  background: #fcfdfc;
  border: 1px solid #eef2f0;
  border-radius: 12px;
  padding: 20px 10px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
}
.guide-item:hover {
  border-color: #2fa36f;
  background: #f0faf5;
  transform: translateY(-3px);
  box-shadow: 0 5px 15px rgba(47, 163, 111, 0.1);
}
.guide-item .el-icon { font-size: 24px; color: #2fa36f; }
.guide-item span { font-size: 14px; font-weight: 500; color: #34495e; }

.section { margin-top: 40px; }
.section-head {
  display: flex;
  align-items: baseline;
  gap: 15px;
  margin-bottom: 20px;
}
.section-title { margin: 0; font-size: 26px; font-weight: 800; color: #2c3e50; }
.section-desc { color: #95a5a6; font-size: 14px; }

.category-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 15px;
}
.category-item {
  background: white;
  border-radius: 12px;
  padding: 20px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid transparent;
}
.category-item:hover {
  border-color: #2fa36f;
  color: #2fa36f;
  box-shadow: 0 8px 20px rgba(0,0,0,0.05);
}
.cat-name { font-weight: 600; }

.product-col { margin-bottom: 20px; }

@media (max-width: 768px) {
  .hero-title { font-size: 28px; }
  .hero-section { padding: 40px 20px; }
  .quick-actions { flex-direction: column; }
  .banner-image { height: 250px; }
  .category-grid { grid-template-columns: repeat(2, 1fr); }
}
</style>

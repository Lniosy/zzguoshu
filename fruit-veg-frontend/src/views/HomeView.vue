<template>
  <div class="home-page">
    <main class="main">
      <el-card class="top-search-card">
        <div class="top-search-inner">
          <h2>当季新鲜，透明可溯源</h2>
          <el-input
            v-model="searchKeyword"
            placeholder="搜索商品或商家"
            class="search-input"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <div class="quick-actions">
            <el-button @click="navigate('/products')">去逛商品</el-button>
            <el-button @click="navigate('/circle')">看果蔬圈</el-button>
            <el-button type="primary" @click="toAi">咨询 AI</el-button>
          </div>
        </div>
      </el-card>

      <el-carousel :interval="4500" indicator-position="outside" class="banner">
        <el-carousel-item v-for="item in banners" :key="item.id">
          <img :src="item.imageUrl" :alt="item.title" class="banner-image" @click="goBanner(item)" />
          <div class="banner-caption">{{ item.title }}</div>
        </el-carousel-item>
      </el-carousel>

      <el-row :gutter="16" class="notice-row">
        <el-col :span="16">
          <el-card>
            <template #header>
              <div class="card-title">平台公告</div>
            </template>
            <div v-for="notice in notices.slice(0, 4)" :key="notice.id" class="notice-item">
              <el-tag size="small" effect="plain">{{ notice.type }}</el-tag>
              <span class="notice-text">
                <b>{{ notice.title }}</b>
                <small class="notice-content">{{ notice.content }}</small>
              </span>
              <small>{{ notice.publishTime }}</small>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="guide-card">
            <template #header>
              <div class="card-title">快速入口</div>
            </template>
            <el-button class="block-btn" type="primary" @click="navigate('/products')">去选购商品</el-button>
            <el-button class="block-btn" @click="navigate('/circle')">浏览果蔬圈</el-button>
            <el-button class="block-btn" @click="toAi">咨询 AI 专家</el-button>
          </el-card>
        </el-col>
      </el-row>

      <section class="section">
        <div class="section-head">
          <h2>商品分类</h2>
          <el-button link @click="navigate('/products')">查看全部</el-button>
        </div>
        <div class="category-grid">
          <button v-for="category in categories" :key="category.id" class="category-item" @click="openCategory(category)">
            <span>{{ category.name }}</span>
          </button>
        </div>
      </section>

      <section class="section">
        <div class="section-head">
          <h2>推荐商品</h2>
        </div>
        <el-row :gutter="16">
          <el-col :span="6" v-for="product in hotProducts" :key="product.id">
            <el-card class="product-card" shadow="hover">
              <img :src="product.image" :alt="product.name" class="product-image" @click="navigate(`/products/${product.id}`)" />
              <div class="product-content">
                <h3>{{ product.name }}</h3>
                <p>{{ product.description }}</p>
                <div class="price-line">
                  <span class="price">¥{{ Number(product.price).toFixed(2) }}</span>
                  <span class="original" v-if="product.originalPrice">¥{{ Number(product.originalPrice).toFixed(2) }}</span>
                </div>
                <div class="product-actions">
                  <el-button type="primary" @click="addToCart(product)">加入购物车</el-button>
                  <el-button @click="navigate(`/products/${product.id}`)">查看详情</el-button>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </section>
    </main>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import { useAppNavigation } from '@/composables/useAppNavigation'
import { getCategoryTree, getRecommendProducts } from '@/api/product'
import { getHomeBanners, getNoticeList } from '@/api/content'

const userStore = useUserStore()
const cartStore = useCartStore()
const { navigate } = useAppNavigation()

const searchKeyword = ref('')
const categories = ref([])
const hotProducts = ref([])
const banners = ref([])
const notices = ref([])

const fetchHomeData = async () => {
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

const handleSearch = () => {
  if (!searchKeyword.value.trim()) return
  navigate({ path: '/products', query: { keyword: searchKeyword.value.trim() } })
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
.home-page { min-height: 100vh; }
.main {
  max-width: 1220px;
  margin: 0 auto;
  padding: 16px;
}
.top-search-card {
  margin-bottom: 16px;
}
.top-search-inner {
  display: grid;
  grid-template-columns: 1fr 380px auto;
  gap: 10px;
  align-items: center;
}
.top-search-inner h2 {
  margin: 0;
  font-size: 28px;
}
.search-input {
  width: 100%;
}
.quick-actions {
  display: flex;
  gap: 8px;
}
.banner { margin-bottom: 16px; }
.banner-image {
  width: 100%;
  height: 360px;
  object-fit: cover;
  border-radius: 14px;
  cursor: pointer;
}
.banner-caption {
  position: absolute;
  left: 24px;
  bottom: 20px;
  color: #fff;
  font-size: 20px;
  font-weight: 700;
  text-shadow: 0 4px 14px rgba(0, 0, 0, 0.35);
}
.notice-row { margin-bottom: 16px; }
.card-title { font-weight: 700; }
.notice-item {
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: 10px;
  align-items: center;
  padding: 9px 0;
  border-bottom: 1px dashed #e2ece6;
}
.notice-item:last-child { border-bottom: 0; }
.notice-text { color: #253a2f; }
.notice-content {
  display: block;
  color: #628173;
  margin-top: 4px;
}
.guide-card .block-btn { width: 100%; margin-bottom: 8px; }
.section { margin-top: 20px; }
.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}
.section-head h2 { margin: 0; font-size: 24px; }
.category-grid {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 10px;
}
.category-item {
  border: 1px solid #d5e5dc;
  border-radius: 12px;
  padding: 12px;
  background: #f8fcfa;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}
.category-item:hover {
  border-color: #2fa36f;
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(37, 85, 62, 0.12);
}
.product-card { overflow: hidden; }
.product-image {
  width: 100%;
  height: 186px;
  object-fit: cover;
  border-radius: 10px;
  cursor: pointer;
}
.product-content h3 { margin: 10px 0 6px; font-size: 18px; }
.product-content p { margin: 0; color: #5a6f64; min-height: 42px; }
.price-line { margin: 10px 0; display: flex; gap: 8px; align-items: baseline; }
.price { color: #11845a; font-size: 24px; font-weight: 700; }
.original { color: #8b9b92; text-decoration: line-through; }
.product-actions { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; }

@media (max-width: 900px) {
  .top-search-inner {
    grid-template-columns: 1fr;
  }

  .top-search-inner h2 {
    font-size: 22px;
  }

  .quick-actions {
    flex-wrap: wrap;
  }
  .banner-image { height: 220px; }
  .category-grid { grid-template-columns: repeat(3, minmax(0, 1fr)); }
}
</style>

<template>
  <div class="product-list-container">
    <el-container class="product-list">
      <el-header class="product-header">
        <div class="header-content">
          <el-button link @click="navigateBack">
            <el-icon><ArrowLeft /></el-icon>
            返回
          </el-button>
          <h2>商品列表</h2>
        </div>
      </el-header>
      <el-main class="product-main">
        <el-row :gutter="20">
          <!-- 左侧分类导航 (仅大屏显示) -->
          <el-col :xs="0" :sm="6" class="hidden-xs-only">
            <el-card class="category-card">
              <template #header>
                <h3>商品分类</h3>
              </template>
              <el-tree
                :data="categoryTree"
                :props="categoryProps"
                :default-expanded-keys="[1]"
                :default-checked-keys="[1]"
                @node-click="handleCategoryClick"
                class="category-tree"
              />
            </el-card>
          </el-col>
          
          <!-- 右侧商品列表 -->
          <el-col :xs="24" :sm="18">
            <!-- 移动端简易分类选单 -->
            <div class="mobile-category hidden-sm-and-up" style="margin-bottom: 12px">
               <el-cascader
                 v-model="filterParams.categoryId"
                 :options="categoryTree"
                 :props="{ value: 'cateId', label: 'cateName', children: 'children', checkStrictly: true }"
                 placeholder="全部分类"
                 style="width: 100%"
                 @change="handleFilter"
                 clearable>
               </el-cascader>
            </div>

            <!-- 工具栏：合并搜索与高级筛选项，样式紧凑化 -->
            <div class="toolbar-wrapper surface-muted" style="padding: 12px 16px; margin-bottom: 16px; display: flex; flex-wrap: wrap; gap: 12px; align-items: center;">
              <el-input
                v-model="searchKeyword"
                placeholder="搜索商品名称/关键词"
                style="flex: 1; min-width: 240px; max-width: 320px;"
                @keyup.enter="handleSearch"
                clearable
              >
                <template #append>
                  <el-button @click="handleSearch">
                    <el-icon><Search /></el-icon>
                  </el-button>
                </template>
              </el-input>
              
              <el-select v-model="filterParams.sortBy" placeholder="综合排序" style="width: 140px" @change="handleFilter">
                <el-option label="综合排序" value="" />
                <el-option label="价格由低到高" value="price_asc" />
                <el-option label="价格由高到低" value="price_desc" />
                <el-option label="销量热度" value="sales_desc" />
                <el-option label="最新上架" value="create_time_desc" />
              </el-select>

              <div class="price-filter" style="display: flex; align-items: center; gap: 6px;">
                <el-input-number v-model="filterParams.minPrice" :min="0" :controls="false" placeholder="最低价" style="width: 80px" @change="handleFilter"/>
                <span style="color: var(--text-sub)">-</span>
                <el-input-number v-model="filterParams.maxPrice" :min="0" :controls="false" placeholder="最高价" style="width: 80px" @change="handleFilter"/>
              </div>

              <el-button link type="primary" @click="handleReset">
                <el-icon><Refresh /></el-icon> 重置
              </el-button>
            </div>

            <!-- 商品列表，带自研加载效果 -->
            <el-card class="product-card">
              <template #header>
                <h3>商家商品</h3>
              </template>
              <div v-loading="loading">
                <el-empty v-if="merchantGroups.length === 0 && !loading" description="暂无相关商品或商铺~" />
                <div v-else>
                  <div v-for="shop in merchantGroups" :key="shop.id" class="merchant-card">
                    <div class="merchant-head">
                  <div class="merchant-meta" @click="navigateToStore(shop.id)">
                    <el-avatar :size="54" :src="shop.avatar">{{ shop.name?.[0] || '店' }}</el-avatar>
                    <div>
                      <h4>{{ shop.name }}</h4>
                      <p>{{ shop.address || '郑州市' }}</p>
                    </div>
                  </div>
                  <el-button type="primary" plain @click="navigateToStore(shop.id)">进入店铺</el-button>
                </div>
                <div class="merchant-products">
                  <div v-for="product in shop.products" :key="product.id" class="product-item">
                    <el-image
                      :src="product.images?.[0] || product.mainImage"
                      fit="cover"
                      class="product-image"
                      @click="navigateToProductDetail(product.id)"
                    >
                      <template #error>
                        <div class="image-error">
                          <el-icon><Picture /></el-icon>
                        </div>
                      </template>
                    </el-image>
                    <div class="product-info">
                      <h5 class="product-name" @click="navigateToProductDetail(product.id)">{{ product.name }}</h5>
                      <p class="product-price">¥{{ Number(product.price).toFixed(2) }}</p>
                      <p class="product-brief">月售 {{ product.sales }} · 库存 {{ product.stock }}</p>
                      <el-button type="danger" circle size="small" @click="handleAddToCart(product)">+</el-button>
                    </div>
                  </div>
                </div>
                </div>
              </div>
              </div>
              <!-- 分页 -->
              <el-pagination
                v-model:current-page="pageParams.current"
                v-model:page-size="pageParams.pageSize"
                :page-sizes="[12, 24, 48, 96]"
                :total="pageParams.total"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange"
              />
            </el-card>
          </el-col>
        </el-row>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppNavigation } from '@/composables/useAppNavigation'
import { ArrowLeft, Search, Refresh, Picture } from '@element-plus/icons-vue'
import { getCategoryTree, getProductList } from '@/api/product'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import { ElMessage } from 'element-plus'

const router = useRouter()
const { navigate } = useAppNavigation()
const route = useRoute()
const userStore = useUserStore()
const cartStore = useCartStore()

// 响应式数据
const searchKeyword = ref('')
const categoryTree = ref([])
const categoryProps = reactive({
  label: 'name',
  children: 'children'
})
const productList = ref([])
const filterParams = reactive({
  category: undefined,
  sortBy: '',
  minPrice: undefined,
  maxPrice: undefined
})
const pageParams = reactive({
  current: 1,
  pageSize: 12,
  total: 0
})

const merchantGroups = computed(() => {
  const grouped = new Map()
  for (const item of productList.value) {
    const merchantId = Number(item.merchantId || 0)
    if (!grouped.has(merchantId)) {
      grouped.set(merchantId, {
        id: merchantId,
        name: item.merchantName || '郑州果蔬商家',
        avatar: item.merchantAvatar || '',
        address: item.merchantAddress || '',
        products: []
      })
    }
    grouped.get(merchantId).products.push(item)
  }
  return Array.from(grouped.values())
})

// 方法
const navigateBack = () => {
  navigate('/')
}

const handleCategoryClick = (data) => {
  filterParams.category = data.id
  pageParams.current = 1
  fetchProductList()
}

const handleSearch = () => {
  pageParams.current = 1
  fetchProductList()
}

const handleFilter = () => {
  pageParams.current = 1
  fetchProductList()
}

const handleReset = () => {
  searchKeyword.value = ''
  filterParams.category = undefined
  filterParams.sortBy = ''
  filterParams.minPrice = undefined
  filterParams.maxPrice = undefined
  pageParams.current = 1
  fetchProductList()
}

const handleSizeChange = (size) => {
  pageParams.pageSize = size
  fetchProductList()
}

const handleCurrentChange = (current) => {
  pageParams.current = current
  fetchProductList()
}

const navigateToProductDetail = (id) => {
  navigate(`/products/${id}`)
}

const navigateToStore = (merchantId) => {
  navigate(`/store/${merchantId}`)
}

const handleAddToCart = (product) => {
  if (!userStore.getIsLoggedIn) {
    ElMessage.warning('请先登录')
    navigate('/login')
    return
  }
  cartStore.addToCart(product, 1)
  ElMessage.success('已加入购物车')
}

// 获取商品分类树
const fetchCategoryTree = async () => {
  try {
    const data = await getCategoryTree()
    categoryTree.value = data
  } catch (error) {
    console.error('获取商品分类树失败:', error)
  }
}

// 获取商品列表
const fetchProductList = async () => {
  try {
    const params = {
      page: pageParams.current,
      size: pageParams.pageSize,
      ...filterParams
    }
    if (searchKeyword.value) {
      params.keyword = searchKeyword.value
    }
    const data = await getProductList(params)
    productList.value = data.records
    pageParams.total = data.total
  } catch (error) {
    console.error('获取商品列表失败:', error)
  }
}

// 钩子函数
onMounted(() => {
  if (route.query.keyword) {
    searchKeyword.value = String(route.query.keyword)
  }
  if (route.query.category) {
    filterParams.category = Number(route.query.category)
  }
  fetchCategoryTree()
  fetchProductList()
})
</script>

<style scoped>
.product-list-container {
  min-height: 100vh;
  background: transparent;
}

.product-list {
  min-height: 100vh;
}

.product-header {
  background-color: rgba(255, 255, 255, 0.82);
  backdrop-filter: blur(8px);
  border-bottom: 1px solid #dce8df;
  padding: 0;
}

.header-content {
  display: flex;
  align-items: center;
  height: 60px;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.header-content h2 {
  margin: 0 0 0 20px;
  font-size: 24px;
}

.product-main {
  padding: 20px;
}

.category-card {
  height: 100%;
}

.category-tree :deep(.el-tree-node__content) {
  padding: 10px 14px;
  border-radius: 10px;
}

.category-tree :deep(.el-tree-node__content:hover) {
  background: #edf6f1;
}

.product-card {
  margin-bottom: 20px;
}

.merchant-card {
  border: 1px solid #e8f0eb;
  border-radius: 14px;
  padding: 12px;
  margin-bottom: 14px;
  background: #fff;
}

.merchant-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.merchant-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}

.merchant-meta h4 {
  margin: 0 0 4px;
}

.merchant-meta p {
  margin: 0;
  color: #6d8378;
  font-size: 13px;
}

.merchant-products {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
}

.product-item {
  display: grid;
  grid-template-columns: 112px 1fr;
  gap: 10px;
  border: 1px solid #eef4f0;
  border-radius: 12px;
  padding: 8px;
}

.product-image {
  width: 112px;
  height: 112px;
  border-radius: 10px;
  overflow: hidden;
  cursor: pointer;
}

.product-info {
  padding: 2px 0;
}

.product-name {
  margin: 0 0 8px 0;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
}

.product-price {
  margin: 0 0 8px 0;
  font-size: 20px;
  font-weight: 700;
  color: #147e58;
}

.product-brief {
  margin: 0 0 8px;
  color: #6f8479;
  font-size: 12px;
}

@media (max-width: 900px) {
  .product-main {
    padding: 12px;
  }

  .header-content {
    padding: 0 12px;
    height: 54px;
  }

  .header-content h2 {
    margin-left: 12px;
    font-size: 20px;
  }

  .merchant-products {
    grid-template-columns: 1fr;
  }
}
</style>

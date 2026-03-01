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
          <!-- 左侧分类导航 -->
          <el-col :span="6">
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
          <el-col :span="18">
            <!-- 搜索栏 -->
            <el-card class="search-card">
              <el-input
                v-model="searchKeyword"
                placeholder="请输入商品名称或关键词"
                style="width: 300px"
                @keyup.enter="handleSearch"
              >
                <template #append>
                  <el-button @click="handleSearch">
                    <el-icon><Search /></el-icon>
                  </el-button>
                </template>
              </el-input>
            </el-card>
            <!-- 筛选排序 -->
            <el-card class="filter-card">
              <el-row :gutter="10">
                <el-col :span="6">
                  <el-select v-model="filterParams.sortBy" placeholder="排序方式" @change="handleFilter">
                    <el-option label="默认排序" value="" />
                    <el-option label="价格从低到高" value="price_asc" />
                    <el-option label="价格从高到低" value="price_desc" />
                    <el-option label="销量从高到低" value="sales_desc" />
                    <el-option label="上架时间从新到旧" value="create_time_desc" />
                  </el-select>
                </el-col>
                <el-col :span="6">
                  <el-input-number
                    v-model="filterParams.minPrice"
                    :min="0"
                    :max="10000"
                    placeholder="最低价格"
                    @change="handleFilter"
                  />
                </el-col>
                <el-col :span="6">
                  <el-input-number
                    v-model="filterParams.maxPrice"
                    :min="0"
                    :max="10000"
                    placeholder="最高价格"
                    @change="handleFilter"
                  />
                </el-col>
                <el-col :span="6">
                  <el-button type="primary" @click="handleReset">
                    <el-icon><Refresh /></el-icon>
                    重置
                  </el-button>
                </el-col>
              </el-row>
            </el-card>
            <!-- 商品列表 -->
            <el-card class="product-card">
              <template #header>
                <h3>商品列表</h3>
              </template>
              <el-row :gutter="20">
                <el-col :span="6" v-for="product in productList" :key="product.id">
                  <el-card
                    class="product-item"
                    :body-style="{ padding: '10px' }"
                    @click="navigateToProductDetail(product.id)"
                  >
                    <el-image
                      :src="product.images[0]"
                      fit="cover"
                      style="width: 100%; height: 150px"
                      :initial-index="0"
                      preview-teleported
                    >
                      <template #error>
                        <div class="image-error">
                          <el-icon><Picture /></el-icon>
                        </div>
                      </template>
                    </el-image>
                    <div class="product-info">
                      <h4 class="product-name">{{ product.name }}</h4>
                      <p class="product-price">¥{{ product.price.toFixed(2) }}</p>
                      <div class="product-sales">
                        <el-icon><Goods /></el-icon>
                        <span>{{ product.sales }}人已购买</span>
                      </div>
                      <div class="product-stock">
                        <el-icon><Location /></el-icon>
                        <span>{{ product.stock }}件库存</span>
                      </div>
                    </div>
                    <div class="product-actions">
                      <el-button
                        type="primary"
                        size="small"
                        @click.stop="handleAddToCart(product)"
                      >
                        <el-icon><ShoppingCart /></el-icon>
                        加入购物车
                      </el-button>
                      <el-button
                        size="small"
                        @click.stop="navigateToProductDetail(product.id)"
                      >
                        <el-icon><View /></el-icon>
                        查看详情
                      </el-button>
                    </div>
                  </el-card>
                </el-col>
              </el-row>
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
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppNavigation } from '@/composables/useAppNavigation'
import { ArrowLeft, Search, Refresh, Picture, Goods, Location, ShoppingCart, View } from '@element-plus/icons-vue'
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

.product-item {
  height: 100%;
  border-radius: 12px;
  transition: transform 0.24s ease, box-shadow 0.24s ease;
}

.product-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 14px 26px rgba(27, 65, 47, 0.14);
}

.product-info {
  padding: 10px 0;
}

.product-name {
  margin: 0 0 10px 0;
  font-size: 15px;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-price {
  margin: 0 0 10px 0;
  font-size: 20px;
  font-weight: 700;
  color: #147e58;
}

.product-sales,
.product-stock {
  display: flex;
  align-items: center;
  font-size: 12px;
  color: #6f8479;
  margin-bottom: 5px;
}

.product-sales span,
.product-stock span {
  margin-left: 5px;
}

.product-actions {
  display: flex;
  justify-content: space-between;
  margin-top: 10px;
}

.product-actions .el-button {
  flex: 1;
  margin: 0 5px;
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
}
</style>

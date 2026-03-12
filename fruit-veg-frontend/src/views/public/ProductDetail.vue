<template>
  <div class="product-detail-container">
    <el-container class="product-detail">
      <el-header class="product-header">
        <div class="header-content">
          <el-button link @click="navigateBack">
            <el-icon><ArrowLeft /></el-icon>
            返回
          </el-button>
          <h2>商品详情</h2>
        </div>
      </el-header>
      <el-main class="product-main">
        <el-row :gutter="20">
          <!-- 商品图片轮播 -->
          <el-col :span="10">
            <el-card class="product-image-card">
              <el-carousel
                v-if="product.images.length > 0"
                :interval="3000"
                type="card"
                height="300px"
              >
                <el-carousel-item v-for="(image, index) in product.images" :key="index">
                  <el-image
                    :src="image"
                    fit="cover"
                    style="width: 100%; height: 100%"
                    :initial-index="0"
                    preview-teleported
                  >
                    <template #error>
                      <div class="image-error">
                        <el-icon><Picture /></el-icon>
                      </div>
                    </template>
                  </el-image>
                </el-carousel-item>
              </el-carousel>
            </el-card>
          </el-col>
          <!-- 商品信息 -->
          <el-col :span="14">
            <el-card class="product-info-card">
              <template #header>
                <h3>{{ product.name }}</h3>
              </template>
              <div class="product-price">
                <span class="price">¥{{ product.price.toFixed(2) }}</span>
                <span class="original-price" v-if="product.originalPrice">
                  ¥{{ product.originalPrice.toFixed(2) }}
                </span>
              </div>
              <div class="product-sales">
                <el-icon><Goods /></el-icon>
                <span>{{ product.sales }}人已购买</span>
              </div>
              <div class="product-stock">
                <el-icon><Location /></el-icon>
                <span>{{ product.stock }}件库存</span>
              </div>
              <div v-if="!isAdmin" class="product-specs">
                <h4>规格选择</h4>
                <el-radio-group v-model="selectedSpec" class="spec-radio-group">
                  <el-radio
                    v-for="spec in product.specs"
                    :key="spec.id"
                    :value="spec.id"
                    class="spec-radio"
                  >
                    {{ spec.name }} - ¥{{ spec.price.toFixed(2) }}
                  </el-radio>
                </el-radio-group>
              </div>
              <div v-if="!isAdmin" class="product-quantity">
                <h4>购买数量</h4>
                <el-input-number
                  v-model="purchaseQuantity"
                  :min="1"
                  :max="Math.max(1, Number(product.stock || 1))"
                  :step="1"
                  class="quantity-input"
                />
              </div>
              <div class="product-actions">
                <template v-if="!isAdmin">
                  <el-button
                    type="primary"
                    size="large"
                    @click="handleAddToCart"
                    :disabled="purchaseQuantity <= 0"
                  >
                    <el-icon><ShoppingCart /></el-icon>
                    加入购物车
                  </el-button>
                  <el-button
                    type="success"
                    size="large"
                    @click="handleBuyNow"
                    :disabled="purchaseQuantity <= 0"
                  >
                    <el-icon><CreditCard /></el-icon>
                    立即购买
                  </el-button>
                  <el-button size="large" @click="handleFavorite">
                    收藏商品
                  </el-button>
                </template>
                <template v-else>
                  <el-button type="primary" size="large" @click="handleAdminManageProduct">
                    编辑商品
                  </el-button>
                  <el-button size="large" @click="navigateToTrace">
                    查看溯源信息
                  </el-button>
                </template>
              </div>
            </el-card>
          </el-col>
        </el-row>
        <!-- 商品详情描述 -->
        <el-card class="product-description-card" style="margin-top: 20px">
          <template #header>
            <h3>商品详情</h3>
          </template>
          <div class="product-description" v-html="product.description" />
        </el-card>
        <!-- 溯源信息 -->
        <el-card class="product-trace-card" style="margin-top: 20px">
          <template #header>
            <h3>溯源信息</h3>
          </template>
          <el-button type="primary" @click="navigateToTrace">
            <el-icon><Location /></el-icon>
            查看溯源信息
          </el-button>
          <el-button style="margin-left: 8px" @click="navigateToAiExpert">
            AI 咨询
          </el-button>
        </el-card>
        <!-- 商家店铺 -->
        <el-card class="product-shop-card" style="margin-top: 20px">
          <template #header>
            <h3>商家店铺</h3>
          </template>
          <div class="shop-info">
            <el-avatar :size="60" :src="product.shop.logo" class="shop-clickable" @click="navigateToShop">
              {{ product.shop.name.charAt(0).toUpperCase() }}
            </el-avatar>
            <div class="shop-details">
              <h4 class="shop-clickable" @click="navigateToShop">{{ product.shop.name }}</h4>
              <p>{{ product.shop.description }}</p>
              <p>店铺位置：{{ product.shop.address || '郑州市' }}</p>
              <div class="shop-rating">
                <el-rate
                  :model-value="product.shop.rating"
                  :max="5"
                  disabled
                  show-score
                  text-color="#ff9900"
                />
              </div>
            </div>
            <el-button type="primary" @click="navigateToShop">
              <el-icon><Shop /></el-icon>
              进入店铺
            </el-button>
          </div>
        </el-card>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAppNavigation } from '@/composables/useAppNavigation'
import { ArrowLeft, Picture, Goods, Location, ShoppingCart, CreditCard, Shop } from '@element-plus/icons-vue'
import { getProductDetail } from '@/api/product'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import { addFavorite } from '@/api/favorite'
import { ElMessage } from 'element-plus'

const router = useRouter()
const { navigate } = useAppNavigation()
const route = useRoute()
const userStore = useUserStore()
const cartStore = useCartStore()
const isAdmin = computed(() => userStore.isAdmin)

// 响应式数据
const product = reactive({
  id: 0,
  name: '',
  description: '',
  price: 0,
  originalPrice: 0,
  images: [],
  specs: [],
  sales: 0,
  stock: 0,
  shop: {
    id: 0,
    name: '',
    logo: '',
    description: '',
    rating: 0
  }
})
const selectedSpec = ref('')
const purchaseQuantity = ref(1)

// 方法
const navigateBack = () => {
  navigate('/products')
}

const handleAddToCart = () => {
  if (isAdmin.value) {
    ElMessage.info('管理员请在管理后台管理商品')
    navigate('/admin/products')
    return
  }
  if (!userStore.getIsLoggedIn) {
    ElMessage.warning('请先登录')
    navigate('/login')
    return
  }
  const selectedSpecObj = product.specs.find(spec => spec.id === selectedSpec.value)
  cartStore.addToCart({
    ...product,
    price: selectedSpecObj ? selectedSpecObj.price : product.price,
    spec: selectedSpecObj ? selectedSpecObj.name : ''
  }, purchaseQuantity.value)
  ElMessage.success('已加入购物车')
}

const handleBuyNow = () => {
  if (isAdmin.value) {
    ElMessage.info('管理员不参与购买流程')
    navigate('/admin/products')
    return
  }
  if (!userStore.getIsLoggedIn) {
    ElMessage.warning('请先登录')
    navigate('/login')
    return
  }
  const selectedSpecObj = product.specs.find(spec => spec.id === selectedSpec.value)
  cartStore.clearCart()
  cartStore.addToCart({
    ...product,
    price: selectedSpecObj ? selectedSpecObj.price : product.price,
    spec: selectedSpecObj ? selectedSpecObj.name : ''
  }, purchaseQuantity.value)
  navigate('/checkout')
}

const handleFavorite = async () => {
  if (isAdmin.value) {
    ElMessage.info('管理员请在管理后台管理商品')
    return
  }
  if (!userStore.getIsLoggedIn) {
    ElMessage.warning('请先登录')
    navigate('/login')
    return
  }
  await addFavorite(product.id)
  ElMessage.success('已加入收藏')
}

const navigateToTrace = () => {
  navigate(`/products/${product.id}/trace`)
}

const navigateToShop = () => {
  navigate(`/store/${product.shop.id}`)
}

const navigateToAiExpert = () => {
  if (!userStore.getIsLoggedIn) {
    ElMessage.warning('请先登录后使用 AI 专家')
    navigate('/login')
    return
  }
  navigate('/ai-expert')
}

const handleAdminManageProduct = () => {
  navigate('/admin/products')
}

// 获取商品详情
const fetchProductDetail = async () => {
  const productId = route.params.id
  if (!productId) {
    ElMessage.error('商品ID无效')
    navigate('/products')
    return
  }
  try {
    const data = await getProductDetail(productId)
    Object.assign(product, data)
    product.shop = {
      id: data.shop?.id || 0,
      name: data.shop?.shopName || data.shop?.name || '商家店铺',
      logo: data.shop?.logo || data.shop?.logoUrl || '',
      description: data.shop?.description || data.shop?.shopDescription || '优质果蔬源头供应',
      address: data.shop?.address || '',
      rating: Number(data.shop?.rating || 4.8)
    }
    // 设置默认规格
    if (product.specs.length > 0) {
      selectedSpec.value = product.specs[0].id
    }
  } catch (error) {
    console.error('获取商品详情失败:', error)
    ElMessage.error('获取商品详情失败')
  }
}

// 钩子函数
onMounted(() => {
  fetchProductDetail()
})
</script>

<style scoped>
.product-detail-container {
  min-height: 100vh;
  background: transparent;
}

.product-detail {
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

.product-image-card {
  height: 300px;
}

.product-info-card {
  height: 100%;
}

.product-price {
  display: flex;
  align-items: baseline;
  margin-bottom: 10px;
}

.price {
  font-size: 28px;
  font-weight: 700;
  color: #147e58;
  margin-right: 10px;
}

.original-price {
  font-size: 16px;
  color: #8b9f93;
  text-decoration: line-through;
}

.product-sales,
.product-stock {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  color: #5e7669;
}

.product-sales span,
.product-stock span {
  margin-left: 5px;
}

.product-specs {
  margin: 20px 0;
}

.product-specs h4 {
  margin-bottom: 10px;
}

.spec-radio-group {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.spec-radio {
  border: 1px solid #dcdfe6;
  border-radius: 10px;
  padding: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.spec-radio:hover {
  border-color: #2f8e67;
  background: #f5fbf7;
}

.product-quantity {
  margin: 20px 0;
}

.product-quantity h4 {
  margin-bottom: 10px;
}

.quantity-input {
  width: 100px;
}

.product-actions {
  display: flex;
  gap: 10px;
  margin: 20px 0;
}

.product-actions .el-button {
  flex: 1;
}

.product-description-card {
  margin-top: 20px;
}

.product-trace-card {
  margin-top: 20px;
}

.product-shop-card {
  margin-top: 20px;
}

.shop-info {
  display: flex;
  align-items: center;
  gap: 20px;
}

.shop-details {
  flex: 1;
}

.shop-details h4 {
  margin-bottom: 5px;
}

.shop-clickable {
  cursor: pointer;
}

.shop-details p {
  margin-bottom: 10px;
  color: #606266;
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

  .shop-info {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .product-actions {
    flex-wrap: wrap;
  }

  .product-actions .el-button {
    min-width: 120px;
    flex: 1 1 auto;
  }
}
</style>

<template>
  <div class="cart-container">
    <el-container class="cart">
      <el-header class="cart-header">
        <div class="header-content">
          <el-button link @click="navigateBack">
            <el-icon><ArrowLeft /></el-icon>
            返回
          </el-button>
          <h2>购物车</h2>
        </div>
      </el-header>
      <el-main class="cart-main">
        <el-card class="cart-card" v-if="cartItems.length > 0">
          <template #header>
            <h3>购物车商品</h3>
          </template>
          <el-table
            :data="cartItems"
            style="width: 100%"
            @selection-change="handleSelectionChange"
          >
            <el-table-column type="selection" width="55" />
            <el-table-column prop="name" label="商品名称" min-width="200">
              <template #default="scope">
                <div class="product-info">
                  <el-image
                    :src="scope.row.images[0]"
                    fit="cover"
                    style="width: 60px; height: 60px"
                    preview-teleported
                  >
                    <template #error>
                      <div class="image-error">
                        <el-icon><Picture /></el-icon>
                      </div>
                    </template>
                  </el-image>
                  <div class="product-details">
                    <h4>{{ scope.row.name }}</h4>
                    <p v-if="scope.row.spec">{{ scope.row.spec }}</p>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="price" label="单价" width="100">
              <template #default="scope">
                ¥{{ scope.row.price.toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column label="数量" width="150">
              <template #default="scope">
                <el-input-number
                  v-model="scope.row.quantity"
                  :min="1"
                  :max="scope.row.stock"
                  @change="(value) => handleQuantityChange(value, scope)"
                />
              </template>
            </el-table-column>
            <el-table-column label="小计" width="100">
              <template #default="scope">
                ¥{{ (scope.row.price * scope.row.quantity).toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template #default="scope">
                <el-button link @click="handleRemove(scope.row)">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="cart-footer">
            <div class="selected-info" v-if="selectedItems.length > 0">
              <span>已选 {{ selectedItems.length }} 件商品</span>
              <span class="total-price">
                合计: ¥{{ selectedTotal.toFixed(2) }}
              </span>
            </div>
            <div class="cart-actions">
              <el-button type="default" @click="handleClear">
                <el-icon><Delete /></el-icon>
                清空购物车
              </el-button>
              <el-button type="primary" size="large" @click="handleCheckout" :disabled="selectedItems.length === 0">
                <el-icon><CreditCard /></el-icon>
                结算
              </el-button>
            </div>
          </div>
        </el-card>
        <div class="empty-cart" v-else>
          <el-empty description="购物车是空的" />
          <el-button type="primary" @click="navigateToProducts">
            <el-icon><Goods /></el-icon>
            去购物
          </el-button>
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAppNavigation } from '@/composables/useAppNavigation'
import { ArrowLeft, Picture, Delete, CreditCard, Goods } from '@element-plus/icons-vue'
import { useCartStore } from '@/stores/cart'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const { navigate } = useAppNavigation()
const cartStore = useCartStore()
const userStore = useUserStore()

// 响应式数据
const selectedItems = ref([])

// 计算属性
const cartItems = computed(() => cartStore.getCartItems)
const selectedTotal = computed(() => {
  return selectedItems.value.reduce((total, item) => total + (item.price * item.quantity), 0)
})

// 方法
const navigateBack = () => {
  navigate('/')
}

const navigateToProducts = () => {
  navigate('/products')
}

const handleSelectionChange = (selection) => {
  selectedItems.value = selection
}

const handleQuantityChange = (value, scope) => {
  cartStore.updateQuantity(scope.row.id, value)
}

const handleRemove = (item) => {
  cartStore.removeFromCart(item.id)
  ElMessage.success('已从购物车移除')
}

const handleClear = () => {
  cartStore.clearCart()
  ElMessage.success('购物车已清空')
}

const handleCheckout = () => {
  if (!userStore.getIsLoggedIn) {
    ElMessage.warning('请先登录')
    navigate('/login')
    return
  }
  navigate('/checkout')
}

// 钩子函数
onMounted(() => {
  // 可以在这里获取购物车数据
})
</script>

<style scoped>
.cart-container {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.cart {
  min-height: 100vh;
}

.cart-header {
  background-color: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
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
}

.cart-main {
  padding: 20px;
}

.cart-card {
  margin-bottom: 20px;
}

.product-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.product-details h4 {
  margin: 0 0 5px 0;
  font-size: 14px;
  font-weight: 600;
}

.product-details p {
  margin: 0;
  font-size: 12px;
  color: #909399;
}

.cart-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
  padding-top: 10px;
  border-top: 1px solid #ebeef5;
}

.selected-info {
  display: flex;
  align-items: baseline;
  gap: 20px;
}

.total-price {
  font-size: 18px;
  font-weight: bold;
  color: #f56c6c;
}

.cart-actions {
  display: flex;
  gap: 10px;
}

.empty-cart {
  text-align: center;
  padding: 60px 20px;
}

.empty-cart :deep(.el-empty) {
  margin-bottom: 20px;
}
</style>

<template>
  <div class="store-page">
    <div class="store-header">
      <el-button link @click="navigate('/products')">返回商品列表</el-button>
    </div>

    <el-card class="store-card" v-loading="loading">
      <div class="store-top">
        <el-avatar :size="64" :src="merchant.shopLogo || merchant.logoUrl">{{ merchant.shopName?.[0] || '店' }}</el-avatar>
        <div class="store-meta">
          <h2>{{ merchant.shopName || merchant.name }}</h2>
          <p>{{ merchant.shopDesc || merchant.shopDescription || '郑州本地果蔬商家' }}</p>
          <div class="store-tags">
            <el-tag type="success">郑州商家</el-tag>
            <el-tag type="info">{{ merchant.address || '郑州市' }}</el-tag>
          </div>
        </div>
      </div>
    </el-card>

    <el-card class="section-card">
      <template #header><h3>店铺商品</h3></template>
      <el-row :gutter="12">
        <el-col :span="6" v-for="item in products" :key="item.id">
          <el-card class="product-item" shadow="hover">
            <img :src="item.mainImage || item.image" :alt="item.name" class="product-image" @click="navigate(`/products/${item.id}`)" />
            <div class="product-title" @click="navigate(`/products/${item.id}`)">{{ item.name }}</div>
            <div class="price-line">
              <span>¥{{ Number(item.price).toFixed(2) }}</span>
              <el-button
                v-if="!isAdmin"
                circle
                type="primary"
                size="small"
                @click="addToCart(item)"
              >
                +
              </el-button>
              <el-button
                v-else
                type="primary"
                plain
                size="small"
                @click="navigate('/admin/products')"
              >
                管理
              </el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>

    <el-card class="section-card">
      <template #header><h3>店铺动态</h3></template>
      <el-empty v-if="posts.length === 0" description="暂无动态" />
      <div v-for="post in posts" :key="post.id" class="post-item">
        <h4>{{ post.title }}</h4>
        <p>{{ post.content }}</p>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAppNavigation } from '@/composables/useAppNavigation'
import { useCartStore } from '@/stores/cart'
import { useUserStore } from '@/stores/user'
import { getPublicMerchantDetail } from '@/api/merchant'

const route = useRoute()
const { navigate } = useAppNavigation()
const cartStore = useCartStore()
const userStore = useUserStore()
const isAdmin = computed(() => userStore.isAdmin)

const loading = ref(false)
const merchant = ref({})
const products = ref([])
const posts = ref([])

const fetchData = async () => {
  loading.value = true
  try {
    const data = await getPublicMerchantDetail(route.params.id)
    merchant.value = data.merchant || {}
    products.value = data.products?.records || []
    posts.value = data.posts?.records || []
  } finally {
    loading.value = false
  }
}

const addToCart = (product) => {
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
  cartStore.addToCart(product, 1)
  ElMessage.success('已加入购物车')
}

onMounted(fetchData)
</script>

<style scoped>
.store-page { max-width: 1180px; margin: 0 auto; padding: 16px; }
.store-header { margin-bottom: 10px; }
.store-card { margin-bottom: 14px; }
.store-top { display: flex; gap: 14px; align-items: center; }
.store-meta h2 { margin: 0 0 4px; }
.store-meta p { margin: 0; color: #587063; }
.store-tags { margin-top: 8px; display: flex; gap: 8px; }
.section-card { margin-bottom: 14px; }
.product-item { border-radius: 12px; }
.product-image { width: 100%; height: 120px; object-fit: cover; border-radius: 8px; cursor: pointer; }
.product-title { margin: 8px 0; font-weight: 600; cursor: pointer; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.price-line { display: flex; align-items: center; justify-content: space-between; color: #12855d; font-weight: 700; }
.post-item { padding: 10px 0; border-bottom: 1px dashed #e6efe9; }
.post-item:last-child { border-bottom: 0; }
.post-item h4 { margin: 0 0 4px; }
.post-item p { margin: 0; color: #54695f; }
</style>

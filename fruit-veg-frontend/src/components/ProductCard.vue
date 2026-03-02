<template>
  <el-card class="product-card" shadow="hover">
    <img :src="product.image" :alt="product.name" class="product-image" @click="emit('navigate', `/products/${product.id}`)" />
    <div class="product-content">
      <h3 @click="emit('navigate', `/products/${product.id}`)" class="clickable-title">{{ product.name }}</h3>
      <div class="merchant-row" @click="emit('navigate', `/store/${product.merchantId}`)">
        <el-avatar :size="24" :src="product.merchantAvatar">{{ product.merchantName?.[0] || '店' }}</el-avatar>
        <span class="merchant-name">{{ product.merchantName }}</span>
        <small class="merchant-address">{{ product.merchantAddress || '郑州市' }}</small>
      </div>
      <p class="description">{{ product.description }}</p>
      <div class="price-line">
        <span class="price">¥{{ Number(product.price).toFixed(2) }}</span>
        <span class="original" v-if="product.originalPrice">¥{{ Number(product.originalPrice).toFixed(2) }}</span>
      </div>
      <div class="product-actions">
        <el-button type="primary" @click="emit('add-to-cart', product)">加入购物车</el-button>
        <el-button @click="emit('navigate', `/products/${product.id}`)">查看详情</el-button>
      </div>
    </div>
  </el-card>
</template>

<script setup>
const props = defineProps({
  product: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['navigate', 'add-to-cart'])
</script>

<style scoped>
.product-card { 
  overflow: hidden; 
  height: 100%;
  display: flex;
  flex-direction: column;
}
.product-image {
  width: 100%;
  height: 186px;
  object-fit: cover;
  border-radius: 10px;
  cursor: pointer;
  transition: transform 0.3s ease;
}
.product-image:hover {
  transform: scale(1.05);
}
.product-content {
  padding: 12px 0 0;
  display: flex;
  flex-direction: column;
  flex: 1;
}
.clickable-title {
  margin: 10px 0 6px;
  font-size: 18px;
  cursor: pointer;
}
.clickable-title:hover {
  color: #2fa36f;
}
.merchant-row { 
  display: flex; 
  align-items: center; 
  gap: 6px; 
  color: #4f695d; 
  font-size: 12px; 
  cursor: pointer; 
  margin-bottom: 8px; 
}
.merchant-name {
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.merchant-row small { 
  margin-left: auto; 
  color: #85a094; 
}
.description { 
  margin: 0; 
  color: #5a6f64; 
  font-size: 14px;
  line-height: 1.4;
  min-height: 40px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.price-line { 
  margin: 12px 0; 
  display: flex; 
  gap: 8px; 
  align-items: baseline; 
}
.price { 
  color: #11845a; 
  font-size: 24px; 
  font-weight: 700; 
}
.original { 
  color: #8b9b92; 
  text-decoration: line-through; 
  font-size: 14px;
}
.product-actions { 
  margin-top: auto;
  display: grid; 
  grid-template-columns: 1fr 1fr; 
  gap: 8px; 
}
</style>

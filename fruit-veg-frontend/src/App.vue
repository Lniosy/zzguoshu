<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import AppNav from '@/components/AppNav.vue'
import { useCartStore } from '@/stores/cart'
import { useAppNavigation } from '@/composables/useAppNavigation'

const route = useRoute()
const cartStore = useCartStore()
const { navigate } = useAppNavigation()
const showAppNav = computed(() => {
  if (route.meta?.hideGlobalNav) return false
  return !route.path.startsWith('/admin')
})
const showCartFloat = computed(() => showAppNav.value && !route.path.startsWith('/cart'))
</script>

<template>
  <div id="app" class="app-shell">
    <AppNav v-if="showAppNav" />
    <div class="route-root" :class="{ 'with-nav': showAppNav }">
      <router-view v-slot="{ Component }">
        <transition name="route-fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </div>
    <el-badge v-if="showCartFloat" :value="cartStore.getCartCount" class="cart-float-badge">
      <button class="cart-float" @click="navigate('/cart')">
        <el-icon><ShoppingCart /></el-icon>
      </button>
    </el-badge>
  </div>
</template>

<style scoped>
.app-shell {
  min-height: 100vh;
}

.route-root.with-nav {
  min-height: calc(100vh - 62px);
}

.cart-float-badge {
  position: fixed;
  right: 20px;
  bottom: 24px;
  z-index: 2000;
}

.cart-float {
  width: 52px;
  height: 52px;
  border: none;
  border-radius: 50%;
  background: linear-gradient(135deg, #ff5c3f, #f83f3f);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  box-shadow: 0 10px 20px rgba(248, 63, 63, 0.35);
  cursor: pointer;
}
</style>

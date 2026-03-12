<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import AppNav from '@/components/AppNav.vue'
import { useCartStore } from '@/stores/cart'
import { useUserStore } from '@/stores/user'
import { useAppNavigation } from '@/composables/useAppNavigation'

const route = useRoute()
const cartStore = useCartStore()
const userStore = useUserStore()
const { navigate } = useAppNavigation()
const showAppNav = computed(() => {
  if (route.meta?.hideGlobalNav) return false
  return !route.path.startsWith('/admin')
})
const showCartFloat = computed(() => showAppNav.value && !route.path.startsWith('/cart') && !userStore.isAdmin)
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
    <transition name="fade">
      <el-badge v-if="showCartFloat" :value="cartStore.getCartCount" class="cart-float-badge">
        <button class="cart-float" @click="navigate('/cart')">
          <el-icon><ShoppingCart /></el-icon>
        </button>
      </el-badge>
    </transition>
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
  background: linear-gradient(135deg, var(--brand), var(--brand-strong));
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  box-shadow: 0 10px 20px rgba(32, 127, 95, 0.35);
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.cart-float:hover {
  transform: scale(1.05);
  box-shadow: 0 14px 24px rgba(32, 127, 95, 0.45);
}

.cart-float:active {
  transform: scale(0.95);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(20px);
}
</style>

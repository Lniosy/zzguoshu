<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import AppNav from '@/components/AppNav.vue'

const route = useRoute()
const showAppNav = computed(() => {
  if (route.meta?.hideGlobalNav) return false
  return !route.path.startsWith('/admin')
})
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
  </div>
</template>

<style scoped>
.app-shell {
  min-height: 100vh;
}

.route-root.with-nav {
  min-height: calc(100vh - 62px);
}
</style>

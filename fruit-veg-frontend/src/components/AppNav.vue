<template>
  <header class="app-nav">
    <div class="inner">
      <button class="brand" @click="handleNav('home')">果蔬销售系统</button>

      <nav class="menu">
        <el-button
          v-for="item in navItems"
          :key="item.key"
          text
          :class="{ active: isActive(item.path) }"
          @click="handleNav(item.key)"
        >
          {{ item.label }}
        </el-button>
      </nav>

      <div class="actions">
        <el-input
          v-model="keyword"
          placeholder="搜索商品或商家"
          class="search"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button v-if="!isLoggedIn" type="primary" @click="handleNav('login')">登录</el-button>
        <el-button v-if="!isLoggedIn" @click="handleNav('register')">注册</el-button>

        <el-dropdown v-else @command="handleNav">
          <span class="user-chip">
            <el-avatar :size="30" :src="userInfo?.avatar">{{ userInfo?.username?.[0]?.toUpperCase() }}</el-avatar>
            <span>{{ userInfo?.nickname || userInfo?.username }}</span>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="user">个人中心</el-dropdown-item>
              <el-dropdown-item command="orders">我的订单</el-dropdown-item>
              <el-dropdown-item command="after-sales">我的售后</el-dropdown-item>
              <el-dropdown-item command="favorites">我的收藏</el-dropdown-item>
              <el-dropdown-item command="cart">购物车</el-dropdown-item>
              <el-dropdown-item command="merchant-apply">商家入驻</el-dropdown-item>
              <el-dropdown-item command="merchant-shop">店铺管理</el-dropdown-item>
              <el-dropdown-item v-if="role === 'ADMIN'" command="admin">管理后台</el-dropdown-item>
              <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
  </header>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { useAppNavigation } from '@/composables/useAppNavigation'

const route = useRoute()
const userStore = useUserStore()
const { navigate } = useAppNavigation()
const keyword = ref('')

const navItems = [
  { key: 'home', label: '首页', path: '/' },
  { key: 'products', label: '商品分类', path: '/products' },
  { key: 'circle', label: '果蔬圈', path: '/circle' },
  { key: 'ai-expert', label: 'AI 专家', path: '/ai-expert' }
]

const isLoggedIn = computed(() => userStore.getIsLoggedIn)
const userInfo = computed(() => userStore.getUserInfo)
const role = computed(() => userStore.getRole || 'USER')

const routeMap = {
  home: { path: '/' },
  products: { path: '/products' },
  circle: { path: '/circle' },
  'ai-expert': { path: '/ai-expert', requireAuth: true },
  login: { path: '/login' },
  register: { path: '/register' },
  user: { path: '/user?tab=profile', requireAuth: true },
  orders: { path: '/user?tab=orders', requireAuth: true },
  'after-sales': { path: '/user?tab=afterSales', requireAuth: true },
  favorites: { path: '/user?tab=favorites', requireAuth: true },
  cart: { path: '/cart', requireAuth: true },
  'merchant-apply': { path: '/merchant/apply', requireAuth: true },
  'merchant-shop': { path: '/merchant/shop', requireAuth: true },
  admin: { path: '/admin', requireAuth: true }
}

const isActive = (path) => {
  const normalized = String(path || '').split('?')[0]
  if (!normalized) return false
  if (normalized === '/') return route.path === '/'
  return route.path === normalized || route.path.startsWith(`${normalized}/`)
}

const handleSearch = () => {
  const value = keyword.value.trim()
  if (!value) return
  navigate({ path: '/products', query: { keyword: value } })
}

const handleNav = (key) => {
  if (key === 'logout') {
    userStore.logout()
    navigate('/')
    return
  }
  const target = routeMap[key]
  if (!target) return
  if (key === 'admin' && role.value !== 'ADMIN') {
    ElMessage.warning('当前账号无管理后台权限')
    return
  }
  navigate(target.path, { requireAuth: target.requireAuth })
}
</script>

<style scoped>
.app-nav {
  position: sticky;
  top: 0;
  z-index: 50;
  backdrop-filter: blur(10px);
  background: linear-gradient(112deg, rgba(22, 83, 58, 0.92), rgba(34, 122, 83, 0.82));
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 10px 26px rgba(10, 41, 28, 0.16);
}

.inner {
  max-width: 1220px;
  margin: 0 auto;
  padding: 10px 16px;
  display: grid;
  grid-template-columns: 190px 1fr auto;
  align-items: center;
  gap: 12px;
}

.brand {
  border: none;
  background: transparent;
  color: #fff;
  font-size: 20px;
  font-weight: 700;
  cursor: pointer;
  text-align: left;
}

.menu {
  display: flex;
  gap: 6px;
}

.menu .el-button {
  color: rgba(255, 255, 255, 0.94);
  min-height: 38px;
  border-radius: 10px;
  padding: 0 12px;
}

.menu .el-button.active {
  color: #fff;
  background: rgba(255, 255, 255, 0.2);
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.2);
}

.actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.search {
  width: 244px;
}

.user-chip {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #fff;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 999px;
  transition: background-color 0.2s ease;
}

.user-chip:hover {
  background: rgba(255, 255, 255, 0.14);
}

@media (max-width: 980px) {
  .inner {
    grid-template-columns: 1fr;
    gap: 8px;
  }

  .menu {
    flex-wrap: wrap;
  }

  .search {
    width: 100%;
  }

  .actions {
    width: 100%;
    flex-wrap: wrap;
  }

  .actions :deep(.el-button) {
    min-height: 40px;
  }
}
</style>

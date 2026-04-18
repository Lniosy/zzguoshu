<template>
  <div class="admin-dashboard">
    <el-container>
      <el-aside width="200px" class="admin-sidebar">
        <div class="logo">
          <h3>果蔬销售系统</h3>
          <p>后台管理</p>
        </div>
        <el-menu
          :default-active="$route.path"
          class="admin-menu"
          router
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
        >
          <template v-if="isSubAdmin">
            <el-menu-item index="/admin/products">
              <el-icon><Goods /></el-icon>
              <span>商品审核</span>
            </el-menu-item>
            <el-menu-item index="/admin/merchant-applications">
              <el-icon><Shop /></el-icon>
              <span>商家审核</span>
            </el-menu-item>
            <el-menu-item index="/admin/complaints">
              <el-icon><Ticket /></el-icon>
              <span>投诉处理</span>
            </el-menu-item>
          </template>
          <template v-else>
            <el-menu-item index="/admin">
              <el-icon><House /></el-icon>
              <span>首页</span>
            </el-menu-item>
            <el-sub-menu index="users">
              <template #title>
                <el-icon><User /></el-icon>
                <span>用户管理</span>
              </template>
              <el-menu-item index="/admin/users">用户列表</el-menu-item>
            </el-sub-menu>
            <el-sub-menu index="products">
              <template #title>
                <el-icon><Goods /></el-icon>
                <span>商品管理</span>
              </template>
              <el-menu-item index="/admin/products">商品列表与审核</el-menu-item>
            </el-sub-menu>
            <el-sub-menu index="orders">
              <template #title>
                <el-icon><Ticket /></el-icon>
                <span>订单管理</span>
              </template>
              <el-menu-item index="/admin/orders">订单列表</el-menu-item>
              <el-menu-item index="/admin/complaints">投诉处理</el-menu-item>
            </el-sub-menu>
            <el-sub-menu index="merchants">
              <template #title>
                <el-icon><Shop /></el-icon>
                <span>商家管理</span>
              </template>
              <el-menu-item index="/admin/merchants">商家列表</el-menu-item>
              <el-menu-item index="/admin/merchant-applications">商家申请</el-menu-item>
            </el-sub-menu>
            <el-menu-item index="/admin/stats">
              <el-icon><DataAnalysis /></el-icon>
              <span>数据统计</span>
            </el-menu-item>
            <el-sub-menu index="traces">
              <template #title>
                <el-icon><Document /></el-icon>
                <span>溯源管理</span>
              </template>
              <el-menu-item index="/admin/traces">数据审核</el-menu-item>
            </el-sub-menu>
            <el-menu-item index="/admin/content">
              <el-icon><Document /></el-icon>
              <span>公告与评论审核</span>
            </el-menu-item>
            <el-menu-item index="/admin/settings">
              <el-icon><Setting /></el-icon>
              <span>系统参数</span>
            </el-menu-item>
          </template>
        </el-menu>
      </el-aside>
      <el-container class="admin-main">
        <el-header class="admin-header">
          <div class="header-left">
            <h2>{{ pageTitle }}</h2>
          </div>
          <div class="header-right">
            <el-button 
              type="success" 
              plain 
              size="default" 
              class="back-btn" 
              @click="navigate('/')"
            >
              <el-icon><HomeFilled /></el-icon>
              <span>返回前台</span>
            </el-button>
            <el-dropdown @command="handleCommand">
              <span class="user-info">
                <el-avatar :size="40" :src="userInfo?.avatar">
                  {{ userInfo?.username?.charAt(0).toUpperCase() }}
                </el-avatar>
                <span class="username">{{ userInfo?.username || '管理员' }}</span>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item v-if="!isSubAdmin" command="settings">
                    <el-icon><Setting /></el-icon>
                    <span>个人设置</span>
                  </el-dropdown-item>
                  <el-dropdown-item command="logout" divided>
                    <el-icon><SwitchButton /></el-icon>
                    <span>退出登录</span>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>
        <el-main class="admin-content">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAppNavigation } from '@/composables/useAppNavigation'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import { HomeFilled, House, User, Goods, Ticket, Shop, DataAnalysis, Document, Setting, SwitchButton } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const { navigate } = useAppNavigation()
const userStore = useUserStore()
const userInfo = computed(() => userStore.userInfo)
const isSubAdmin = computed(() => userStore.getRole === 'SUB_ADMIN')

// 页面标题
const pageTitle = computed(() => {
  const subAdminTitles = {
    '/admin/products': '商品审核',
    '/admin/merchant-applications': '商家审核',
    '/admin/complaints': '投诉处理'
  }
  const adminTitles = {
    '/admin': '首页',
    '/admin/users': '用户管理',
    '/admin/products': '商品管理',
    '/admin/orders': '订单管理',
    '/admin/complaints': '投诉处理',
    '/admin/merchants': '商家列表',
    '/admin/merchant-applications': '商家申请',
    '/admin/stats': '数据统计',
    '/admin/traces': '溯源管理',
    '/admin/content': '公告与评论审核',
    '/admin/settings': '系统参数'
  }
  const titles = isSubAdmin.value ? subAdminTitles : adminTitles
  return titles[route.path] || (isSubAdmin.value ? '商品审核' : '首页')
})

const handleCommand = (command) => {
  if (command === 'settings') {
    navigate('/admin/settings')
    return
  }
  if (command === 'logout') {
    userStore.logout()
    navigate('/login')
  }
}
</script>

<style scoped>
.admin-dashboard {
  height: 100vh;
  width: 100%;
  display: flex;
  flex-direction: column;
}

.admin-dashboard > .el-container {
  height: 100%;
  overflow: hidden;
}

.admin-sidebar {
  background-color: #304156;
  color: #bfcbd9;
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow-y: auto;
  box-sizing: border-box;
}

/* 隐藏侧边栏滚动条但保留功能 */
.admin-sidebar::-webkit-scrollbar {
  width: 0;
}

.logo {
  flex-shrink: 0;
  padding: 20px;
  text-align: center;
  border-bottom: 1px solid #263445;
  background-color: #304156;
}

.logo h3 {
  margin: 0;
  font-size: 18px;
  color: #409EFF;
}

.logo p {
  margin: 5px 0 0;
  font-size: 12px;
  color: #bfcbd9;
}

.admin-menu {
  border-right: none;
  flex: 1;
}

.admin-main {
  background-color: #f0f2f5;
  display: flex;
  flex-direction: column; 
  height: 100%;
  overflow: hidden;
}

.admin-header {
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  height: 60px;
  flex-shrink: 0;
  z-index: 10;
}

.header-left h2 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 15px;
}

.back-btn {
  margin-right: 5px;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 5px 10px;
  border-radius: 5px;
  transition: background-color 0.3s;
}

.user-info:hover {
  background-color: #f5f7fa;
}

.username {
  margin-left: 8px;
  font-size: 14px;
  color: #303133;
}

.admin-content {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  box-sizing: border-box;
}
</style>

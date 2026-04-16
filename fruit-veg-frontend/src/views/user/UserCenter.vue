<template>
  <div class="user-center-container">
    <el-container class="user-center">
      <el-main class="user-main">
        <div class="page-head">
          <h2>用户中心</h2>
          <p>左侧切换模块，右侧统一展示，不再跳转二级页面</p>
        </div>

        <el-row :gutter="20">
          <el-col :span="6">
            <el-card class="menu-shell">
              <el-menu
                :default-active="activeMenu"
                class="user-menu"
                @select="handleMenuSelect"
              >
                <el-menu-item index="profile">
                  <el-icon><User /></el-icon>
                  <span>个人信息</span>
                </el-menu-item>
                <el-menu-item index="address">
                  <el-icon><Location /></el-icon>
                  <span>收货地址</span>
                </el-menu-item>
                <el-menu-item index="orders">
                  <el-icon><Tickets /></el-icon>
                  <span>我的订单</span>
                </el-menu-item>
                <el-menu-item index="afterSales">
                  <el-icon><Tickets /></el-icon>
                  <span>我的售后</span>
                </el-menu-item>
                <el-menu-item index="favorites">
                  <el-icon><Star /></el-icon>
                  <span>我的收藏</span>
                </el-menu-item>
                <el-menu-item index="settings">
                  <el-icon><Setting /></el-icon>
                  <span>设置</span>
                </el-menu-item>
              </el-menu>
            </el-card>
          </el-col>

          <el-col :span="18">
            <el-card class="user-content-card" v-loading="pageLoading">
              <template #header>
                <h3>{{ pageTitle }}</h3>
              </template>

              <div v-if="activeMenu === 'profile'" class="tab-panel">
                <el-descriptions :column="2" border>
                  <el-descriptions-item label="昵称">{{ userInfo?.nickname || '-' }}</el-descriptions-item>
                  <el-descriptions-item label="头像">
                    <el-avatar :size="50" :src="userInfo?.avatar">{{ userInfo?.nickname?.charAt(0)?.toUpperCase() }}</el-avatar>
                  </el-descriptions-item>
                  <el-descriptions-item label="手机号">{{ userInfo?.phone || '-' }}</el-descriptions-item>
                  <el-descriptions-item label="邮箱">{{ userInfo?.email || '-' }}</el-descriptions-item>
                  <el-descriptions-item label="性别">{{ userInfo?.gender === 'MALE' ? '男' : userInfo?.gender === 'FEMALE' ? '女' : '-' }}</el-descriptions-item>
                  <el-descriptions-item label="生日">{{ userInfo?.birthdate || '-' }}</el-descriptions-item>
                </el-descriptions>

                <div class="panel-actions">
                  <el-button type="primary" @click="openProfileEditor = !openProfileEditor">
                    {{ openProfileEditor ? '收起编辑' : '编辑资料' }}
                  </el-button>
                </div>

                <el-form v-if="openProfileEditor" :model="profileForm" label-width="86px" class="profile-form">
                  <el-form-item label="昵称"><el-input v-model="profileForm.nickname" /></el-form-item>
                  <el-form-item label="性别">
                    <el-radio-group v-model="profileForm.gender">
                      <el-radio value="MALE">男</el-radio>
                      <el-radio value="FEMALE">女</el-radio>
                      <el-radio value="UNKNOWN">未知</el-radio>
                    </el-radio-group>
                  </el-form-item>
                  <el-form-item label="生日">
                    <el-date-picker v-model="profileForm.birthdate" value-format="YYYY-MM-DD" type="date" style="width: 100%" />
                  </el-form-item>
                  <el-form-item label="邮箱"><el-input v-model="profileForm.email" /></el-form-item>
                  <el-form-item label="头像URL"><el-input v-model="profileForm.avatar" placeholder="可选" /></el-form-item>
                  <el-form-item>
                    <el-button type="primary" @click="saveProfile">保存资料</el-button>
                  </el-form-item>
                </el-form>
              </div>

              <div v-if="activeMenu === 'address'" class="tab-panel">
                <div class="panel-actions">
                  <el-button type="primary" @click="openAddressDialog()">
                    <el-icon><Plus /></el-icon>
                    添加新地址
                  </el-button>
                </div>
                <el-table :data="addressList" style="width: 100%">
                  <el-table-column prop="name" label="收货人" width="110" />
                  <el-table-column prop="phone" label="手机号" width="130" />
                  <el-table-column prop="fullAddress" label="地址" />
                  <el-table-column label="操作" width="220">
                    <template #default="scope">
                      <el-button link @click="openAddressDialog(scope.row)">编辑</el-button>
                      <el-button link @click="handleDeleteAddress(scope.row.id)">删除</el-button>
                      <el-button v-if="!scope.row.isDefault" link @click="handleSetDefaultAddress(scope.row.id)">设为默认</el-button>
                    </template>
                  </el-table-column>
                </el-table>
                <el-empty v-if="!addressList.length" description="暂无地址" />
              </div>

              <div v-if="activeMenu === 'orders'" class="tab-panel">
                <el-table :data="orderList" v-loading="orderLoading">
                  <el-table-column prop="orderNumber" label="订单号" min-width="180" />
                  <el-table-column prop="totalAmount" label="金额" width="120">
                    <template #default="{ row }">¥{{ Number(row.totalAmount || 0).toFixed(2) }}</template>
                  </el-table-column>
                  <el-table-column prop="status" label="状态" width="120" />
                  <el-table-column prop="createTime" label="创建时间" width="180" />
                  <el-table-column label="操作" width="100">
                    <template #default="{ row }">
                      <el-button link type="primary" @click="navigate(`/orders/${row.id}`)">详情</el-button>
                    </template>
                  </el-table-column>
                </el-table>
                <el-empty v-if="!orderLoading && !orderList.length" description="暂无订单" />
              </div>

              <div v-if="activeMenu === 'afterSales'" class="tab-panel">
                <el-table :data="afterSaleList" v-loading="afterSaleLoading">
                  <el-table-column prop="orderNo" label="订单号" min-width="180" />
                  <el-table-column prop="type" label="类型" width="110" />
                  <el-table-column prop="reason" label="原因" min-width="220" show-overflow-tooltip />
                  <el-table-column prop="statusText" label="状态" width="110" />
                  <el-table-column prop="createTime" label="申请时间" width="170" />
                </el-table>
                <el-empty v-if="!afterSaleLoading && !afterSaleList.length" description="暂无售后记录" />
              </div>

              <div v-if="activeMenu === 'favorites'" class="tab-panel">
                <el-table :data="favoriteList" v-loading="favoriteLoading">
                  <el-table-column label="商品" min-width="240">
                    <template #default="{ row }">
                      <div class="fav-item">
                        <el-image :src="row.image" style="width: 60px; height: 60px; border-radius: 8px" fit="cover" />
                        <div>
                          <div class="name">{{ row.name }}</div>
                          <div class="desc">{{ row.description }}</div>
                        </div>
                      </div>
                    </template>
                  </el-table-column>
                  <el-table-column label="价格" width="120">
                    <template #default="{ row }">¥{{ Number(row.price || 0).toFixed(2) }}</template>
                  </el-table-column>
                  <el-table-column label="操作" width="160">
                    <template #default="{ row }">
                      <el-button link type="primary" @click="navigate(`/products/${row.id}`)">查看</el-button>
                      <el-button link type="danger" @click="removeFavoriteItem(row)">取消收藏</el-button>
                    </template>
                  </el-table-column>
                </el-table>
                <el-empty v-if="!favoriteLoading && !favoriteList.length" description="暂无收藏" />
              </div>

              <div v-if="activeMenu === 'settings'" class="tab-panel">
                <el-form :model="passwordForm" label-width="90px" style="max-width:420px">
                  <el-form-item label="旧密码"><el-input v-model="passwordForm.oldPassword" type="password" show-password /></el-form-item>
                  <el-form-item label="新密码"><el-input v-model="passwordForm.newPassword" type="password" show-password /></el-form-item>
                  <el-form-item label="确认密码"><el-input v-model="passwordForm.confirmPassword" type="password" show-password /></el-form-item>
                  <el-form-item><el-button type="primary" @click="submitPassword">修改密码</el-button></el-form-item>
                </el-form>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </el-main>
    </el-container>

    <el-dialog v-model="addressDialogVisible" :title="addressForm.id ? '编辑地址' : '新增地址'" width="540px">
      <el-form :model="addressForm" label-width="90px">
        <el-form-item label="收货人"><el-input v-model="addressForm.name" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="addressForm.phone" maxlength="11" /></el-form-item>
        <el-form-item label="详细地址"><el-input v-model="addressForm.detail" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="默认地址"><el-switch v-model="addressForm.isDefault" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addressDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveAddress">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useAppNavigation } from '@/composables/useAppNavigation'
import { User, Location, Tickets, Star, Setting, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { updateUserPassword } from '@/api/user'
import { getOrderList, getAfterSaleList } from '@/api/order'
import { getFavoriteList, removeFavorite } from '@/api/favorite'

const TAB_KEYS = ['profile', 'address', 'orders', 'afterSales', 'favorites', 'settings']

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const { navigate } = useAppNavigation()

const pageLoading = ref(false)
const activeMenu = ref('profile')

const passwordForm = ref({ oldPassword: '', newPassword: '', confirmPassword: '' })

const openProfileEditor = ref(false)
const profileForm = reactive({ avatar: '', nickname: '', gender: 'UNKNOWN', birthdate: '', email: '' })

const addressDialogVisible = ref(false)
const addressForm = reactive({ id: null, name: '', phone: '', detail: '', isDefault: false })

const orderLoading = ref(false)
const afterSaleLoading = ref(false)
const favoriteLoading = ref(false)
const orderList = ref([])
const afterSaleList = ref([])
const favoriteList = ref([])

const userInfo = computed(() => userStore.getUserInfo)
const addressList = computed(() => userStore.getAddressList)

const pageTitle = computed(() => {
  const map = {
    profile: '个人信息',
    address: '收货地址',
    orders: '我的订单',
    afterSales: '我的售后',
    favorites: '我的收藏',
    settings: '设置'
  }
  return map[activeMenu.value] || '个人信息'
})

const normalizeTab = (tab) => (TAB_KEYS.includes(tab) ? tab : 'profile')

const syncTabByRoute = () => {
  activeMenu.value = normalizeTab(String(route.query.tab || 'profile'))
}

const syncRouteByTab = async () => {
  const nextQuery = { ...route.query, tab: activeMenu.value }
  await router.replace({ path: '/user', query: nextQuery })
}

const hydrateProfileForm = () => {
  const genderMap = {
    1: 'MALE',
    2: 'FEMALE',
    0: 'UNKNOWN'
  }

  Object.assign(profileForm, {
    avatar: userInfo.value?.avatar || '',
    nickname: userInfo.value?.nickname || '',
    gender: genderMap[userInfo.value?.gender] || 'UNKNOWN',
    birthdate: userInfo.value?.birthdate || '',
    email: userInfo.value?.email || ''
  })
}

const loadOrders = async () => {
  orderLoading.value = true
  try {
    const data = await getOrderList({ page: 1, pageSize: 20 })
    orderList.value = data.records || []
  } finally {
    orderLoading.value = false
  }
}

const loadAfterSales = async () => {
  afterSaleLoading.value = true
  try {
    const data = await getAfterSaleList({ page: 1, pageSize: 20 })
    afterSaleList.value = data.records || []
  } finally {
    afterSaleLoading.value = false
  }
}

const loadFavorites = async () => {
  favoriteLoading.value = true
  try {
    favoriteList.value = await getFavoriteList()
  } finally {
    favoriteLoading.value = false
  }
}

const loadTabData = async (tab) => {
  if (tab === 'orders') await loadOrders()
  if (tab === 'afterSales') await loadAfterSales()
  if (tab === 'favorites') await loadFavorites()
}

const fetchBaseData = async () => {
  pageLoading.value = true
  try {
    await Promise.all([userStore.fetchUserInfo(), userStore.fetchAddressList()])
    hydrateProfileForm()
    await loadTabData(activeMenu.value)
  } catch (error) {
    console.error('获取用户中心数据失败:', error)
  } finally {
    pageLoading.value = false
  }
}

const handleMenuSelect = async (index) => {
  activeMenu.value = index
  await syncRouteByTab()
}

const saveProfile = async () => {
  const genderMap = {
    'MALE': 1,
    'FEMALE': 2,
    'UNKNOWN': 0
  }

  await userStore.updateUserInfo({
    ...profileForm,
    gender: genderMap[profileForm.gender] || 0
  })
  await userStore.fetchUserInfo()
  hydrateProfileForm()
  openProfileEditor.value = false
  ElMessage.success('资料更新成功')
}

const openAddressDialog = (address = null) => {
  if (address) {
    Object.assign(addressForm, {
      id: address.id,
      name: address.name,
      phone: address.phone,
      detail: address.detail || address.fullAddress || '',
      isDefault: !!address.isDefault
    })
  } else {
    Object.assign(addressForm, { id: null, name: '', phone: '', detail: '', isDefault: false })
  }
  addressDialogVisible.value = true
}

const saveAddress = async () => {
  if (!addressForm.name || !addressForm.phone || !addressForm.detail) {
    ElMessage.warning('请完整填写地址信息')
    return
  }
  const payload = {
    name: addressForm.name,
    phone: addressForm.phone,
    detail: addressForm.detail,
    fullAddress: addressForm.detail,
    isDefault: addressForm.isDefault
  }
  if (addressForm.id) {
    await userStore.updateAddress(addressForm.id, payload)
    ElMessage.success('地址更新成功')
  } else {
    await userStore.addAddress(payload)
    ElMessage.success('地址添加成功')
  }
  addressDialogVisible.value = false
}

const handleDeleteAddress = async (id) => {
  try {
    await ElMessageBox.confirm('将删除该地址，请确认', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await userStore.deleteAddress(id)
    ElMessage.success('地址删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

const handleSetDefaultAddress = async (id) => {
  try {
    await userStore.setDefaultAddress(id)
    ElMessage.success('默认地址设置成功')
  } catch (error) {
    ElMessage.error(error.message || '设置失败')
  }
}

const removeFavoriteItem = async (row) => {
  await removeFavorite(row.id)
  ElMessage.success('已取消收藏')
  await loadFavorites()
}

const submitPassword = async () => {
  if (!passwordForm.value.oldPassword || !passwordForm.value.newPassword) {
    ElMessage.warning('请填写完整密码信息')
    return
  }
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    ElMessage.warning('两次新密码不一致')
    return
  }
  await updateUserPassword({
    oldPassword: passwordForm.value.oldPassword,
    newPassword: passwordForm.value.newPassword
  })
  ElMessage.success('密码修改成功，请重新登录')
  userStore.logout()
  navigate('/login')
}

watch(
  () => route.query.tab,
  () => {
    syncTabByRoute()
    loadTabData(activeMenu.value)
  }
)

onMounted(async () => {
  syncTabByRoute()
  await syncRouteByTab()
  await fetchBaseData()
})
</script>

<style scoped>
.user-center-container {
  min-height: 100vh;
  background: transparent;
}

.user-center {
  min-height: 100vh;
}

.user-main {
  padding: 20px;
}

.page-head h2 {
  margin: 0;
  font-size: 28px;
}

.page-head p {
  margin: 6px 0 14px;
  color: #60756a;
}

.menu-shell {
  position: sticky;
  top: 84px;
}

.user-menu {
  border: none;
  background-color: transparent;
}

.user-menu :deep(.el-menu-item) {
  border-radius: 10px;
  margin-bottom: 10px;
  min-height: 44px;
  font-weight: 600;
}

.user-menu :deep(.el-menu-item:hover) {
  background-color: #eaf5ef;
}

.user-menu :deep(.el-menu-item.is-active) {
  background-color: #2f8a61;
  color: #fff;
}

.user-content-card {
  border-radius: 14px;
}

.tab-panel {
  min-height: 360px;
}

.panel-actions {
  margin-top: 16px;
  margin-bottom: 12px;
}

.profile-form {
  max-width: 560px;
  margin-top: 8px;
  padding: 12px;
  border: 1px solid #e7efe9;
  border-radius: 12px;
  background: #fbfefc;
}

.fav-item {
  display: flex;
  gap: 10px;
  align-items: center;
}

.fav-item .name {
  font-weight: 600;
  color: #1f3c2e;
}

.fav-item .desc {
  color: #6a7f73;
  font-size: 13px;
}

@media (max-width: 900px) {
  .menu-shell {
    position: static;
  }

  .user-main {
    padding: 14px 12px;
  }
}
</style>

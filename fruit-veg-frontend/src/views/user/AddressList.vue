<template>
  <div class="address-list-container">
    <el-container class="address-list">
      <el-header class="address-header">
        <div class="header-content">
          <el-button link @click="navigateBack">
            <el-icon><ArrowLeft /></el-icon>
            返回
          </el-button>
          <h2>收货地址</h2>
          <el-button type="primary" @click="navigateToAddressEdit">
            <el-icon><Plus /></el-icon>
            添加新地址
          </el-button>
        </div>
      </el-header>
      <el-main class="address-main">
        <el-card class="address-list-card">
          <el-empty v-if="addressList.length === 0" description="暂无收货地址">
            <el-button type="primary" @click="navigateToAddressEdit">
              <el-icon><Plus /></el-icon>
              添加收货地址
            </el-button>
          </el-empty>
          <div v-else class="address-list-wrapper">
            <div
              v-for="address in addressList"
              :key="address.id"
              class="address-item"
              :class="{ 'default-address': address.isDefault }"
            >
              <div class="address-info">
                <div class="address-header">
                  <h4>{{ address.name }}</h4>
                  <el-tag v-if="address.isDefault" type="success">默认地址</el-tag>
                </div>
                <p class="address-phone">{{ address.phone }}</p>
                <p class="address-detail">{{ address.fullAddress }}</p>
              </div>
              <div class="address-actions">
                <el-button
                  link
                  @click="navigateToAddressEdit(address)"
                  class="edit-button"
                >
                  <el-icon><Edit /></el-icon>
                  编辑
                </el-button>
                <el-button
                  v-if="!address.isDefault"
                  link
                  @click="handleSetDefault(address.id)"
                  class="default-button"
                >
                  <el-icon><Star /></el-icon>
                  设为默认
                </el-button>
                <el-button
                  link
                  @click="handleDelete(address.id)"
                  class="delete-button"
                >
                  <el-icon><Delete /></el-icon>
                  删除
                </el-button>
              </div>
            </div>
          </div>
        </el-card>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAppNavigation } from '@/composables/useAppNavigation'
import { useUserStore } from '@/stores/user'
import { ArrowLeft, Plus, Edit, Star, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const { navigate } = useAppNavigation()
const userStore = useUserStore()

// 响应式数据
const addressList = ref([])

// 计算属性
const filteredAddressList = computed(() => {
  return addressList.value
})

// 方法
const navigateBack = () => {
  navigate('/user')
}

const navigateToAddressEdit = (address = null) => {
  if (address) {
    navigate(`/user/address/edit/${address.id}`)
  } else {
    navigate('/user/address/edit')
  }
}

const handleSetDefault = async (id) => {
  try {
    await userStore.setDefaultAddress(id)
    ElMessage.success('默认地址设置成功')
    fetchAddressList()
  } catch (error) {
    console.error('设置默认地址失败:', error)
  }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个地址吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await userStore.deleteAddress(id)
    ElMessage.success('地址删除成功')
    fetchAddressList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 页面加载时获取地址列表
const fetchAddressList = async () => {
  try {
    const addresses = await userStore.fetchAddressList()
    addressList.value = addresses
  } catch (error) {
    console.error('获取地址列表失败:', error)
  }
}

// 钩子函数
onMounted(() => {
  fetchAddressList()
})
</script>

<style scoped>
.address-list-container {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.address-list {
  min-height: 100vh;
}

.address-header {
  background-color: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  padding: 0;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 60px;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.header-content h2 {
  margin: 0;
}

.address-main {
  padding: 20px;
}

.address-list-card {
  max-width: 800px;
  margin: 0 auto;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.address-list-wrapper {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.address-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 20px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  background-color: #fff;
  transition: all 0.3s;
}

.address-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 12px 0 rgba(64, 158, 255, 0.1);
}

.address-item.default-address {
  border-color: #67c23a;
  background-color: #f0f9eb;
}

.address-info {
  flex: 1;
}

.address-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.address-header h4 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.address-phone {
  margin: 0 0 10px 0;
  color: #606266;
  font-size: 14px;
}

.address-detail {
  margin: 0;
  color: #606266;
  font-size: 14px;
  line-height: 1.5;
}

.address-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.address-actions .el-button {
  padding: 5px 10px;
  font-size: 14px;
}

.edit-button:hover {
  color: #409eff;
}

.default-button:hover {
  color: #67c23a;
}

.delete-button:hover {
  color: #f56c6c;
}
</style>

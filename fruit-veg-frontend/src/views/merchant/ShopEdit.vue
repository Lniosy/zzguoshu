<template>
  <div class="shop-edit-container">
    <el-container class="shop-edit">
      <el-header class="shop-header">
        <div class="header-content">
          <el-button link @click="navigateBack">
            <el-icon><ArrowLeft /></el-icon>
            返回
          </el-button>
          <h2>编辑店铺信息</h2>
        </div>
      </el-header>
      <el-main class="shop-main">
        <el-card class="shop-edit-card">
          <el-form
            ref="editFormRef"
            :model="editForm"
            :rules="editRules"
            label-width="120px"
            class="edit-form"
          >
            <!-- 店铺名称 -->
            <el-form-item label="店铺名称">
              <el-input
                v-model="editForm.shopName"
                placeholder="请输入店铺名称"
                :clearable="true"
              />
            </el-form-item>

            <!-- 经营类型 -->
            <el-form-item label="经营类型">
              <el-select
                v-model="editForm.businessType"
                placeholder="请选择经营类型"
                :clearable="true"
                style="width: 100%"
                disabled
              >
                <el-option label="水果批发" value="FRUIT_WHOLESALE" />
                <el-option label="蔬菜批发" value="VEGETABLE_WHOLESALE" />
                <el-option label="生鲜零售" value="FRESH_RETAIL" />
                <el-option label="综合超市" value="GENERAL_STORE" />
              </el-select>
            </el-form-item>

            <!-- 店铺简介 -->
            <el-form-item label="店铺简介">
              <el-input
                v-model="editForm.shopDescription"
                type="textarea"
                :rows="3"
                placeholder="请输入店铺简介"
                :clearable="true"
              />
            </el-form-item>

            <!-- 联系电话 -->
            <el-form-item label="联系电话">
              <el-input
                v-model="editForm.contactPhone"
                placeholder="请输入联系电话"
                :clearable="true"
                maxlength="11"
              />
            </el-form-item>

            <!-- 经营地址 -->
            <el-form-item label="经营地址">
              <el-input
                v-model="editForm.address"
                type="textarea"
                :rows="3"
                placeholder="请输入详细地址"
                :clearable="true"
              />
            </el-form-item>

            <!-- 店铺Logo -->
            <el-form-item label="店铺Logo">
              <el-avatar v-if="editForm.logoUrl" :size="80" :src="editForm.logoUrl">
                {{ editForm?.shopName?.charAt(0)?.toUpperCase() }}
              </el-avatar>
              <el-upload
                class="upload-demo"
                :action="uploadUrl"
                :headers="uploadHeaders"
                :on-success="handleUploadSuccess"
                :on-error="handleUploadError"
                :before-upload="beforeUpload"
                accept=".jpg,.jpeg,.png"
                :auto-upload="false"
                style="margin-top: 10px"
              >
                <el-button size="small" type="primary">更换Logo</el-button>
                <template #tip>
                  <div class="el-upload__tip">
                    只能上传 jpg/png/jpeg 格式文件，且不超过 2MB
                  </div>
                </template>
              </el-upload>
            </el-form-item>

            <!-- 提交按钮 -->
            <el-form-item>
              <el-button
                type="primary"
                @click="handleSubmit"
                :loading="loading"
              >
                {{ loading ? '保存中...' : '保存修改' }}
              </el-button>
              <el-button @click="handleReset">
                <el-icon><Refresh /></el-icon>
                重置
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAppNavigation } from '@/composables/useAppNavigation'
import { useMerchantStore } from '@/stores/merchant'
import { ArrowLeft, Refresh } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const { navigate } = useAppNavigation()
const merchantStore = useMerchantStore()

// 响应式数据
const editFormRef = ref()
const loading = ref(false)
const editForm = reactive({
  shopName: '',
  businessType: '',
  shopDescription: '',
  contactPhone: '',
  address: '',
  logoUrl: ''
})

// 表单验证规则
const editRules = reactive({
  shopName: [
    { required: true, message: '请输入店铺名称', trigger: 'blur' },
    { min: 2, max: 50, message: '店铺名称长度在 2 到 50 个字符之间', trigger: 'blur' }
  ],
  businessType: [
    { required: true, message: '请选择经营类型', trigger: 'change' }
  ],
  contactPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    {
      pattern: /^1[3-9]\d{9}$/,
      message: '请输入有效的手机号码',
      trigger: 'blur'
    }
  ],
  address: [
    { required: true, message: '请输入详细地址', trigger: 'blur' },
    { min: 5, max: 200, message: '详细地址长度在 5 到 200 个字符之间', trigger: 'blur' }
  ]
})

// 文件上传配置
const uploadUrl = '/api/upload'
const uploadHeaders = reactive({
  'Content-Type': 'multipart/form-data'
})

// 方法
const navigateBack = () => {
  navigate('/merchant/shop')
}

const handleUploadSuccess = (response, file, fileList) => {
  ElMessage.success(`${file.name} 上传成功`)
}

const handleUploadError = (error, file, fileList) => {
  ElMessage.error(`${file.name} 上传失败`)
}

const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片格式的文件')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('文件大小不能超过 2MB')
    return false
  }
  return true
}

const handleSubmit = async () => {
  try {
    const valid = await editFormRef.value.validate()
    if (!valid) {
      return
    }

    loading.value = true

    // 调用 API 更新店铺信息
    await merchantStore.updateShopInfo({
      shopName: editForm.shopName,
      businessType: editForm.businessType,
      shopDescription: editForm.shopDescription,
      contactPhone: editForm.contactPhone,
      address: editForm.address,
      logoUrl: editForm.logoUrl
    })

    ElMessage.success('店铺信息更新成功')
    navigate('/merchant/shop')
  } catch (error) {
    console.error('更新店铺信息失败:', error)
  } finally {
    loading.value = false
  }
}

const handleReset = async () => {
  try {
    await ElMessageBox.confirm('确定要重置为原始信息吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    fetchShopInfo()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('重置失败')
    }
  }
}

// 获取店铺信息
const fetchShopInfo = async () => {
  try {
    const shopInfo = await merchantStore.fetchShopInfo()
    Object.assign(editForm, {
      shopName: shopInfo.shopName,
      businessType: shopInfo.businessType,
      shopDescription: shopInfo.shopDescription,
      contactPhone: shopInfo.contactPhone,
      address: shopInfo.address,
      logoUrl: shopInfo.logoUrl
    })
  } catch (error) {
    console.error('获取店铺信息失败:', error)
  }
}

// 钩子函数
onMounted(() => {
  fetchShopInfo()
})
</script>

<style scoped>
.shop-edit-container {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.shop-edit {
  min-height: 100vh;
}

.shop-header {
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

.shop-main {
  padding: 20px;
}

.shop-edit-card {
  max-width: 800px;
  margin: 0 auto;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.edit-form {
  margin-top: 20px;
}
</style>

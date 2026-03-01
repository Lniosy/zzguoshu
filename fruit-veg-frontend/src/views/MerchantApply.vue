<template>
  <div class="merchant-apply-container">
    <el-container class="merchant-apply">
      <el-header class="merchant-header">
        <div class="header-content">
          <el-button link @click="navigateBack">
            <el-icon><ArrowLeft /></el-icon>
            返回
          </el-button>
          <h2>商家入驻申请</h2>
        </div>
      </el-header>
      <el-main class="merchant-main">
        <el-card class="merchant-apply-card">
          <el-form
            ref="applyFormRef"
            :model="applyForm"
            :rules="applyRules"
            label-width="120px"
            class="apply-form"
          >
            <!-- 店铺基本信息 -->
            <el-form-item label="店铺名称">
              <el-input
                v-model="applyForm.shopName"
                placeholder="请输入店铺名称"
                :clearable="true"
              />
            </el-form-item>

            <el-form-item label="经营类型">
              <el-select
                v-model="applyForm.businessType"
                placeholder="请选择经营类型"
                :clearable="true"
                style="width: 100%"
              >
                <el-option label="水果批发" value="FRUIT_WHOLESALE" />
                <el-option label="蔬菜批发" value="VEGETABLE_WHOLESALE" />
                <el-option label="生鲜零售" value="FRESH_RETAIL" />
                <el-option label="综合超市" value="GENERAL_STORE" />
              </el-select>
            </el-form-item>

            <el-form-item label="联系人">
              <el-input
                v-model="applyForm.contactName"
                placeholder="请输入联系人姓名"
                :clearable="true"
              />
            </el-form-item>

            <el-form-item label="联系电话">
              <el-input
                v-model="applyForm.contactPhone"
                placeholder="请输入联系电话"
                :clearable="true"
                maxlength="11"
              />
            </el-form-item>

            <el-form-item label="经营地址">
              <el-input
                v-model="applyForm.address"
                type="textarea"
                :rows="3"
                placeholder="请输入详细地址"
                :clearable="true"
              />
            </el-form-item>

            <!-- 资质文件上传 -->
            <el-form-item label="营业执照">
              <el-upload
                class="upload-demo"
                :action="uploadUrl"
                :headers="uploadHeaders"
                :on-success="handleUploadSuccess"
                :on-error="handleUploadError"
                :before-upload="beforeUpload"
                accept=".pdf,.jpg,.jpeg,.png"
                :file-list="applyForm.licenseFileList"
                :auto-upload="false"
              >
                <el-button size="small" type="primary">点击上传</el-button>
                <template #tip>
                  <div class="el-upload__tip">
                    只能上传 jpg/png/jpeg/pdf 格式文件，且不超过 10MB
                  </div>
                </template>
              </el-upload>
            </el-form-item>

            <el-form-item label="食品经营许可证">
              <el-upload
                class="upload-demo"
                :action="uploadUrl"
                :headers="uploadHeaders"
                :on-success="handleUploadSuccess"
                :on-error="handleUploadError"
                :before-upload="beforeUpload"
                accept=".pdf,.jpg,.jpeg,.png"
                :file-list="applyForm.foodLicenseFileList"
                :auto-upload="false"
              >
                <el-button size="small" type="primary">点击上传</el-button>
                <template #tip>
                  <div class="el-upload__tip">
                    只能上传 jpg/png/jpeg/pdf 格式文件，且不超过 10MB
                  </div>
                </template>
              </el-upload>
            </el-form-item>

            <el-form-item label="法人身份证">
              <el-upload
                class="upload-demo"
                :action="uploadUrl"
                :headers="uploadHeaders"
                :on-success="handleUploadSuccess"
                :on-error="handleUploadError"
                :before-upload="beforeUpload"
                accept=".pdf,.jpg,.jpeg,.png"
                :file-list="applyForm.idCardFileList"
                :auto-upload="false"
              >
                <el-button size="small" type="primary">点击上传</el-button>
                <template #tip>
                  <div class="el-upload__tip">
                    只能上传 jpg/png/jpeg/pdf 格式文件，且不超过 10MB
                  </div>
                </template>
              </el-upload>
            </el-form-item>

            <!-- 入驻协议 -->
            <el-form-item>
              <el-checkbox v-model="applyForm.agreement">
                我已阅读并同意
                <el-link type="primary" @click="showAgreement">《平台入驻协议》</el-link>
              </el-checkbox>
            </el-form-item>

            <!-- 提交按钮 -->
            <el-form-item>
              <el-button
                type="primary"
                @click="handleSubmit"
                :loading="loading"
                :disabled="!applyForm.agreement"
              >
                {{ loading ? '提交中...' : '提交申请' }}
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAppNavigation } from '@/composables/useAppNavigation'
import { useMerchantStore } from '@/stores/merchant'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const { navigate } = useAppNavigation()
const merchantStore = useMerchantStore()

// 响应式数据
const applyFormRef = ref()
const loading = ref(false)
const applyForm = reactive({
  shopName: '',
  businessType: '',
  contactName: '',
  contactPhone: '',
  address: '',
  licenseFileList: [],
  foodLicenseFileList: [],
  idCardFileList: [],
  agreement: false
})

// 表单验证规则
const applyRules = reactive({
  shopName: [
    { required: true, message: '请输入店铺名称', trigger: 'blur' },
    { min: 2, max: 50, message: '店铺名称长度在 2 到 50 个字符之间', trigger: 'blur' }
  ],
  businessType: [
    { required: true, message: '请选择经营类型', trigger: 'change' }
  ],
  contactName: [
    { required: true, message: '请输入联系人姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '联系人姓名长度在 2 到 20 个字符之间', trigger: 'blur' }
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
  ],
  agreement: [
    { required: true, message: '请阅读并同意平台入驻协议', trigger: 'change' }
  ]
})

// 文件上传配置
const uploadUrl = '/api/upload'
const uploadHeaders = reactive({
  'Content-Type': 'multipart/form-data'
})

// 方法
const navigateBack = () => {
  navigate('/')
}

const handleUploadSuccess = (response, file, fileList) => {
  ElMessage.success(`${file.name} 上传成功`)
}

const handleUploadError = (error, file, fileList) => {
  ElMessage.error(`${file.name} 上传失败`)
}

const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isPdf = file.type === 'application/pdf'
  const isLt10M = file.size / 1024 / 1024 < 10

  if (!isImage && !isPdf) {
    ElMessage.error('只能上传图片或PDF格式的文件')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过 10MB')
    return false
  }
  return true
}

const showAgreement = () => {
  ElMessageBox.alert(
    `平台入驻协议\n\n1. 商家必须遵守平台的各项规定\n2. 商家必须保证所售商品的质量和真实性\n3. 商家必须及时处理订单和售后服务\n4. 平台有权对违规商家进行处罚\n\n请仔细阅读协议内容`,
    '平台入驻协议',
    {
      confirmButtonText: '我已阅读并同意',
      type: 'warning'
    }
  ).then(() => {
    applyForm.agreement = true
  })
}

const handleSubmit = async () => {
  try {
    const valid = await applyFormRef.value.validate()
    if (!valid) {
      return
    }

    loading.value = true

    // 调用 API 提交入驻申请
    await merchantStore.merchantApply({
      shopName: applyForm.shopName,
      businessType: applyForm.businessType,
      contactName: applyForm.contactName,
      contactPhone: applyForm.contactPhone,
      address: applyForm.address
    })

    ElMessage.success('入驻申请提交成功')
    navigate('/merchant/shop')
  } catch (error) {
    console.error('入驻申请失败:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.merchant-apply-container {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.merchant-apply {
  min-height: 100vh;
}

.merchant-header {
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

.merchant-main {
  padding: 20px;
}

.merchant-apply-card {
  max-width: 800px;
  margin: 0 auto;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.apply-form {
  margin-top: 20px;
}
</style>

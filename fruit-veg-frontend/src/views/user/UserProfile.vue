<template>
  <div class="user-profile-container">
    <el-container class="user-profile">
      <el-header class="user-header">
        <div class="header-content">
          <el-button link @click="navigateBack">
            <el-icon><ArrowLeft /></el-icon>
            返回
          </el-button>
          <h2>个人信息</h2>
        </div>
      </el-header>
      <el-main class="user-main">
        <el-card class="user-profile-card">
          <el-form
            ref="profileFormRef"
            :model="profileForm"
            :rules="profileRules"
            label-width="100px"
            class="profile-form"
          >
            <!-- 头像 -->
            <el-form-item label="头像">
              <el-avatar :size="100" :src="profileForm.avatar">
                {{ profileForm.nickname?.charAt(0)?.toUpperCase() }}
              </el-avatar>
              <el-button
                link
                @click="handleAvatarUpload"
                style="margin-top: 10px"
              >
                <el-icon><Edit /></el-icon>
                更换头像
              </el-button>
              <input
                ref="avatarInputRef"
                type="file"
                accept="image/*"
                hidden
                @change="handleAvatarChange"
              />
            </el-form-item>

            <!-- 昵称 -->
            <el-form-item prop="nickname" label="昵称">
              <el-input
                v-model="profileForm.nickname"
                placeholder="请输入昵称"
                :clearable="true"
              />
            </el-form-item>

            <!-- 性别 -->
            <el-form-item prop="gender" label="性别">
              <el-radio-group v-model="profileForm.gender">
                <el-radio :value="'MALE'">男</el-radio>
                <el-radio :value="'FEMALE'">女</el-radio>
                <el-radio :value="'UNKNOWN'">未知</el-radio>
              </el-radio-group>
            </el-form-item>

            <!-- 生日 -->
            <el-form-item prop="birthdate" label="生日">
              <el-date-picker
                v-model="profileForm.birthdate"
                type="date"
                placeholder="请选择生日"
                style="width: 100%"
                value-format="YYYY-MM-DD"
                :clearable="true"
              />
            </el-form-item>

            <!-- 手机号 -->
            <el-form-item label="手机号">
              <el-input
                v-model="profileForm.phone"
                placeholder="请输入手机号"
                :disabled="true"
              />
            </el-form-item>

            <!-- 邮箱 -->
            <el-form-item prop="email" label="邮箱">
              <el-input
                v-model="profileForm.email"
                type="email"
                placeholder="请输入邮箱"
                :clearable="true"
              />
            </el-form-item>

            <!-- 操作按钮 -->
            <el-form-item>
              <el-button type="primary" @click="handleSave">
                <el-icon><DocumentAdd /></el-icon>
                保存
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
import { useUserStore } from '@/stores/user'
import { ArrowLeft, Edit, DocumentAdd, Refresh } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const { navigate } = useAppNavigation()
const userStore = useUserStore()

// 响应式数据
const profileFormRef = ref()
const avatarInputRef = ref()
const profileForm = reactive({
  avatar: '',
  nickname: '',
  gender: 'UNKNOWN',
  birthdate: '',
  phone: '',
  email: ''
})

// 表单验证规则
const profileRules = reactive({
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '昵称长度在 2 到 20 个字符之间', trigger: 'blur' }
  ],
  gender: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ],
  email: [
    { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' }
  ]
})

// 方法
const navigateBack = () => {
  navigate('/user')
}

const handleAvatarUpload = () => {
  avatarInputRef.value.click()
}

const handleAvatarChange = (event) => {
  const file = event.target.files[0]
  if (file) {
    // 简单的图片预览
    const reader = new FileReader()
    reader.onload = (e) => {
      profileForm.avatar = e.target.result
    }
    reader.readAsDataURL(file)
  }
}

const handleSave = async () => {
  try {
    const valid = await profileFormRef.value.validate()
    if (!valid) {
      return
    }

    // 调用更新用户信息 API
    const genderMap = {
      'MALE': 1,
      'FEMALE': 2,
      'UNKNOWN': 0
    }

    await userStore.updateUserInfo({
      nickname: profileForm.nickname,
      gender: genderMap[profileForm.gender] || 0,
      birthdate: profileForm.birthdate,
      email: profileForm.email,
      avatar: profileForm.avatar
    })

    ElMessage.success('个人信息更新成功')
    navigate('/user')
  } catch (error) {
    console.error('更新个人信息失败:', error)
  }
}

const handleReset = async () => {
  try {
    await ElMessageBox.confirm('将重置为原始信息，请确认', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    // 从用户存储中获取原始数据
    const userInfo = userStore.getUserInfo
    Object.assign(profileForm, {
      avatar: userInfo?.avatar || '',
      nickname: userInfo?.nickname || '',
      gender: userInfo?.gender || 'UNKNOWN',
      birthdate: userInfo?.birthdate || '',
      phone: userInfo?.phone || '',
      email: userInfo?.email || ''
    })
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('重置失败')
    }
  }
}

// 页面加载时获取用户信息
const fetchUserInfo = async () => {
  try {
    const userInfo = await userStore.fetchUserInfo()
    const genderMap = {
      1: 'MALE',
      2: 'FEMALE',
      0: 'UNKNOWN'
    }

    Object.assign(profileForm, {
      avatar: userInfo?.avatar || '',
      nickname: userInfo?.nickname || '',
      gender: genderMap[userInfo?.gender] || 'UNKNOWN',
      birthdate: userInfo?.birthdate || '',
      phone: userInfo?.phone || '',
      email: userInfo?.email || ''
    })
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
}

// 钩子函数
onMounted(() => {
  fetchUserInfo()
})
</script>

<style scoped>
.user-profile-container {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.user-profile {
  min-height: 100vh;
}

.user-header {
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

.user-main {
  padding: 20px;
}

.user-profile-card {
  max-width: 800px;
  margin: 0 auto;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.profile-form {
  margin-top: 20px;
}
</style>

<template>
  <div class="register-container">
    <el-card class="register-card" shadow="hover">
      <div class="register-header">
        <h2>注册</h2>
        <p>创建您的果蔬销售系统账号</p>
      </div>
      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        class="register-form"
      >
        <el-form-item prop="phone">
          <el-input
            v-model="registerForm.phone"
            placeholder="请输入手机号"
            prefix-icon="Phone"
            :clearable="true"
          />
        </el-form-item>
        <el-form-item prop="nickname">
          <el-input
            v-model="registerForm.nickname"
            placeholder="请输入昵称"
            prefix-icon="User"
            :clearable="true"
          />
        </el-form-item>
        <el-form-item prop="code">
          <el-input
            v-model="registerForm.code"
            placeholder="请输入验证码"
            prefix-icon="Message"
            :clearable="true"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            :clearable="true"
            show-password
          />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请确认密码"
            prefix-icon="Lock"
            :clearable="true"
            show-password
          />
        </el-form-item>
        <el-form-item prop="agreement">
          <el-checkbox v-model="registerForm.agreement">
            我已阅读并同意
            <el-link type="primary">《用户协议》</el-link>
            和
            <el-link type="primary">《隐私政策》</el-link>
          </el-checkbox>
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            @click="handleRegister"
            class="register-button"
            :disabled="loading"
          >
            {{ loading ? '注册中...' : '立即注册' }}
          </el-button>
        </el-form-item>
      </el-form>
      <div class="register-footer">
        <p>
          已有账号？
          <el-link type="primary" @click="handleLogin">立即登录</el-link>
        </p>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { register } from '@/api/user'
import { ElMessage } from 'element-plus'

const router = useRouter()

// 响应式数据
const registerFormRef = ref()
const loading = ref(false)
const registerForm = reactive({
  phone: '',
  nickname: '',
  code: '',
  password: '',
  confirmPassword: '',
  agreement: false
})

// 表单验证规则
const registerRules = reactive({
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    {
      pattern: /^1[3-9]\d{9}$/,
      message: '请输入有效的手机号',
      trigger: 'blur'
    }
  ],
  nickname: [
    { min: 2, max: 20, message: '昵称长度在 2 到 20 个字符之间', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { length: 6, message: '验证码长度为 6 位', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符之间', trigger: 'blur' },
    {
      pattern: /^(?=.*[a-zA-Z])(?=.*\d)[a-zA-Z\d]{6,20}$/,
      message: '密码必须包含字母和数字',
      trigger: 'blur'
    }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  agreement: [
    { required: true, message: '请阅读并同意用户协议和隐私政策', trigger: 'change' }
  ]
})

// 方法
const handleRegister = async () => {
  try {
    // 表单验证
    const valid = await registerFormRef.value.validate()
    if (!valid) {
      return
    }

    loading.value = true

    // 调用注册接口
    await register({
      phone: registerForm.phone,
      nickname: registerForm.nickname,
      code: registerForm.code,
      password: registerForm.password
    })

    ElMessage.success('注册成功')

    // 跳转到登录页面
    router.push({
      path: '/login',
      query: { username: registerForm.username }
    })
  } catch (error) {
    console.error('注册失败:', error)
  } finally {
    loading.value = false
  }
}

const handleLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
  background-image: url('/api/images/VCG41N1553604624.jpg');
  background-size: cover;
  background-position: center;
}

.register-card {
  width: 450px;
  background-color: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
}

.register-header {
  text-align: center;
  margin-bottom: 30px;
}

.register-header h2 {
  margin: 0;
  font-size: 28px;
  color: #303133;
}

.register-header p {
  margin: 10px 0 0 0;
  color: #606266;
  font-size: 14px;
}

.register-form {
  margin-bottom: 30px;
}

.register-button {
  width: 100%;
}

.register-footer {
  text-align: center;
}

.register-footer p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}
</style>

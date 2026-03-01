<template>
  <div class="login-container">
    <el-card class="login-card" shadow="hover">
      <div class="login-header">
        <h2>登录</h2>
        <p>欢迎使用果蔬销售系统</p>
      </div>
      <el-segmented
        v-model="loginMode"
        :options="[
          { label: '账号密码', value: 'password' },
          { label: '短信验证码', value: 'code' }
        ]"
        style="width: 100%; margin-bottom: 14px"
      />
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
      >
        <el-form-item prop="account" v-if="loginMode === 'password'">
          <el-input
            v-model="loginForm.account"
            placeholder="请输入账号"
            prefix-icon="User"
            :clearable="true"
          />
        </el-form-item>
        <el-form-item prop="password" v-if="loginMode === 'password'">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            :clearable="true"
            show-password
          />
        </el-form-item>
        <el-form-item prop="phone" v-if="loginMode === 'code'">
          <el-input v-model="loginForm.phone" placeholder="请输入手机号" prefix-icon="Iphone" :clearable="true" />
        </el-form-item>
        <el-form-item prop="code" v-if="loginMode === 'code'">
          <el-input v-model="loginForm.code" placeholder="请输入验证码">
            <template #append>
              <el-button @click="sendCode" :disabled="countdown > 0">
                {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
              </el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="rememberMe">
          <el-checkbox v-model="loginForm.rememberMe">记住我</el-checkbox>
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            @click="handleLogin"
            class="login-button"
            :disabled="loading"
          >
            {{ loading ? '登录中...' : '登录' }}
          </el-button>
        </el-form-item>
      </el-form>
      <div class="login-footer">
        <p>
          还没有账号？
          <el-link type="primary" @click="handleRegister">立即注册</el-link>
        </p>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { getLoginCode, loginByCode } from '@/api/user'

const router = useRouter()
const userStore = useUserStore()

// 响应式数据
const loginFormRef = ref()
const loading = ref(false)
const loginMode = ref('password')
const countdown = ref(0)
const loginForm = reactive({
  account: '',
  password: '',
  phone: '',
  code: '',
  rememberMe: false
})

// 表单验证规则
const loginRules = reactive({
  account: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 3, max: 20, message: '账号长度在 3 到 20 个字符之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符之间', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式错误', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' }
  ]
})

// 方法
const sendCode = async () => {
  if (!/^1[3-9]\d{9}$/.test(loginForm.phone)) {
    ElMessage.warning('请先输入正确手机号')
    return
  }
  const code = await getLoginCode(loginForm.phone)
  ElMessage.success(`验证码已发送（演示码：${code}）`)
  countdown.value = 60
  const timer = setInterval(() => {
    countdown.value -= 1
    if (countdown.value <= 0) clearInterval(timer)
  }, 1000)
}

const handleLogin = async () => {
  try {
    // 表单验证
    const valid = await loginFormRef.value.validate()
    if (!valid) {
      return
    }

    loading.value = true

    if (loginMode.value === 'password') {
      await userStore.login({
        account: loginForm.account,
        password: loginForm.password
      })
    } else {
      const data = await loginByCode({
        phone: loginForm.phone,
        code: loginForm.code
      })
      userStore.token = data.token
      userStore.isLoggedIn = true
      userStore.userInfo = data.userInfo
      userStore.role = data.userInfo.role || 'USER'
      localStorage.setItem('token', data.token)
      localStorage.setItem('role', userStore.role)
      localStorage.setItem('userInfo', JSON.stringify(userStore.userInfo))
    }
    ElMessage.success('登录成功')

    // 跳转到首页或之前的页面
    const redirect = router.currentRoute.value.query.redirect
    if (redirect) {
      router.push(decodeURIComponent(redirect))
    } else {
      router.push('/')
    }
  } catch (error) {
    console.error('登录失败:', error)
  } finally {
    loading.value = false
  }
}

const handleRegister = () => {
  router.push('/register')
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
  background-image: url('/api/images/VCG41N1553604624.jpg');
  background-size: cover;
  background-position: center;
}

.login-card {
  width: 400px;
  background-color: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h2 {
  margin: 0;
  font-size: 28px;
  color: #303133;
}

.login-header p {
  margin: 10px 0 0 0;
  color: #606266;
  font-size: 14px;
}

.login-form {
  margin-bottom: 30px;
}

.login-button {
  width: 100%;
}

.login-footer {
  text-align: center;
}

.login-footer p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}
</style>

import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'

// 创建 axios 实例
const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    const userStore = useUserStore()
    if (userStore.getToken) {
      config.headers.Authorization = `Bearer ${userStore.getToken}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const { code, message, data } = response.data

    if (code === 200 || code === 0) {
      return data
    } else {
      ElMessage({
        message: message || '请求失败',
        type: 'error'
      })
      return Promise.reject(new Error(message || '请求失败'))
    }
  },
  error => {
    if (error.response) {
      const { status, data } = error.response

      if (status === 401) {
        // 未授权，清除用户信息并跳转登录页
        const userStore = useUserStore()
        userStore.logout()
        ElMessageBox.alert('登录已过期，请重新登录', '提示', {
          confirmButtonText: '确定'
        }).then(() => {
          const currentPath = router.currentRoute.value.fullPath
          router.push({
            path: '/login',
            query: { redirect: encodeURIComponent(currentPath) }
          })
        })
      } else if (status === 403) {
        ElMessage({
          message: '您没有权限访问该资源',
          type: 'error'
        })
      } else if (status === 404) {
        ElMessage({
          message: '请求的资源不存在',
          type: 'error'
        })
      } else if (status >= 500) {
        ElMessage({
          message: '服务器内部错误',
          type: 'error'
        })
      } else {
        ElMessage({
          message: data?.message || '请求失败',
          type: 'error'
        })
      }
    } else if (error.request) {
      // 请求已发出但没有收到响应
      ElMessage({
        message: '网络错误，请检查网络连接',
        type: 'error'
      })
    } else {
      // 请求配置出错
      ElMessage({
        message: '请求配置错误',
        type: 'error'
      })
    }

    return Promise.reject(error)
  }
)

export default request

import { NavigationFailureType, isNavigationFailure, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import pinia from '@/stores'

export function useAppNavigation() {
  const router = useRouter()
  const userStore = useUserStore(pinia)

  const navigate = (to, options = {}) => {
    const resolved = router.resolve(to)
    const requireAuth = options.requireAuth ?? !!resolved.meta?.requireAuth
    if (requireAuth && !userStore.getIsLoggedIn) {
      ElMessage.warning('请先登录')
      router.push({
        path: '/login',
        query: { redirect: encodeURIComponent(resolved.fullPath) }
      })
      return false
    }
    if (resolved.fullPath === router.currentRoute.value.fullPath) {
      return true
    }

    router.push(to).catch((error) => {
      if (!isNavigationFailure(error, NavigationFailureType.duplicated)) {
        ElMessage.error('页面跳转失败，请重试')
        console.error('导航异常:', error)
      }
    })
    return true
  }

  return {
    navigate
  }
}


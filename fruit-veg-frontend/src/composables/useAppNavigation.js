import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

export function useAppNavigation() {
  const router = useRouter()
  const userStore = useUserStore()

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
    router.push(to)
    return true
  }

  return {
    navigate
  }
}


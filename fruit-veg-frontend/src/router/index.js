import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'
import HomeView from '../views/public/HomeView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView
    },
    {
      path: '/about',
      name: 'about',
      component: () => import('../views/public/AboutView.vue')
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/public/LoginView.vue'),
      meta: { hideGlobalNav: true }
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/public/RegisterView.vue'),
      meta: { hideGlobalNav: true }
    },
    {
      path: '/user',
      name: 'user',
      component: () => import('../views/user/UserCenter.vue'),
      meta: { requireAuth: true }
    },
    {
      path: '/user/profile',
      name: 'user-profile',
      redirect: '/user?tab=profile',
      meta: { requireAuth: true }
    },
    {
      path: '/user/address',
      name: 'user-address',
      redirect: '/user?tab=address',
      meta: { requireAuth: true }
    },
    {
      path: '/user/address/edit',
      name: 'user-address-add',
      component: () => import('../views/user/AddressEdit.vue'),
      meta: { requireAuth: true }
    },
    {
      path: '/user/address/edit/:id',
      name: 'user-address-edit',
      component: () => import('../views/user/AddressEdit.vue'),
      meta: { requireAuth: true }
    },
    {
      path: '/user/orders',
      name: 'user-orders',
      redirect: '/user?tab=orders',
      meta: { requireAuth: true }
    },
    {
      path: '/user/favorites',
      name: 'user-favorites',
      redirect: '/user?tab=favorites',
      meta: { requireAuth: true }
    },
    {
      path: '/user/settings',
      name: 'user-settings',
      redirect: '/user?tab=settings',
      meta: { requireAuth: true }
    },
    {
      path: '/merchant/apply',
      name: 'merchant-apply',
      component: () => import('../views/merchant/MerchantApply.vue'),
      meta: { requireAuth: true }
    },
    {
      path: '/merchant/shop',
      name: 'merchant-shop',
      component: () => import('../views/merchant/ShopManage.vue'),
      meta: { requireAuth: true }
    },
    {
      path: '/merchant/shop/edit',
      name: 'merchant-shop-edit',
      component: () => import('../views/merchant/ShopEdit.vue'),
      meta: { requireAuth: true }
    },
    {
      path: '/products',
      name: 'products',
      component: () => import('../views/public/ProductList.vue')
    },
    {
      path: '/products/:id',
      name: 'product-detail',
      component: () => import('../views/public/ProductDetail.vue')
    },
    {
      path: '/products/:id/trace',
      name: 'product-trace',
      component: () => import('../views/public/TraceView.vue'),
      meta: { requireAuth: true }
    },
    {
      path: '/store/:id',
      name: 'merchant-store',
      component: () => import('../views/merchant/MerchantStoreView.vue')
    },
    {
      path: '/trace/detail/:id',
      name: 'trace-detail',
      component: () => import('../views/public/TraceView.vue')
    },
    {
      path: '/cart',
      name: 'cart',
      component: () => import('../views/public/CartView.vue'),
      meta: { requireAuth: true }
    },
    {
      path: '/orders',
      name: 'orders',
      component: () => import('../views/user/OrderList.vue'),
      meta: { requireAuth: true }
    },
    {
      path: '/orders/:id',
      name: 'order-detail',
      component: () => import('../views/user/OrderDetail.vue'),
      meta: { requireAuth: true }
    },
    {
      path: '/after-sales',
      name: 'after-sales',
      redirect: '/user?tab=afterSales',
      meta: { requireAuth: true }
    },
    {
      path: '/checkout',
      name: 'checkout',
      component: () => import('../views/public/CheckoutView.vue'),
      meta: { requireAuth: true }
    },
    {
      path: '/circle',
      name: 'circle',
      component: () => import('../views/public/CircleView.vue')
    },
    {
      path: '/ai-expert',
      name: 'ai-expert',
      component: () => import('../views/public/AIExpertView.vue'),
      meta: { requireAuth: true }
    },
    {
      path: '/admin',
      name: 'admin',
      component: () => import('../layouts/AdminLayout.vue'),
      meta: {
        requireAuth: true,
        requireAdmin: true
      },
      children: [
        {
          path: '',
          name: 'admin-dashboard',
          component: () => import('../views/admin/AdminStats.vue')
        },
        {
          path: 'users',
          name: 'admin-users',
          component: () => import('../views/admin/AdminUserList.vue')
        },
        {
          path: 'roles',
          name: 'admin-roles',
          component: () => import('../views/admin/AdminRolePermission.vue')
        },
        {
          path: 'traces',
          name: 'admin-traces',
          component: () => import('../views/admin/AdminTraceManage.vue')
        },
        {
          path: 'products',
          name: 'admin-products',
          component: () => import('../views/admin/AdminProductList.vue')
        },
        {
          path: 'orders',
          name: 'admin-orders',
          component: () => import('../views/admin/AdminOrderList.vue')
        },
        {
          path: 'merchants',
          name: 'admin-merchants',
          component: () => import('../views/admin/AdminMerchantList.vue')
        },
        {
          path: 'merchant-applications',
          name: 'admin-merchant-applications',
          component: () => import('../views/admin/AdminMerchantList.vue')
        },
        {
          path: 'stats',
          name: 'admin-stats',
          component: () => import('../views/admin/AdminStats.vue')
        },
        {
          path: 'complaints',
          name: 'admin-complaints',
          component: () => import('../views/admin/AdminComplaintList.vue')
        },
        {
          path: 'content',
          name: 'admin-content',
          component: () => import('../views/admin/AdminContentManage.vue')
        },
        {
          path: 'settings',
          name: 'admin-settings',
          component: () => import('../views/admin/AdminSystemSettings.vue')
        }
      ]
    }
  ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  // 检查是否需要登录
  if (to.meta.requireAuth) {
    if (userStore.getIsLoggedIn && userStore.getToken) {
      // 检查是否需要管理员权限
      if (to.meta.requireAdmin) {
        if (userStore.isAdmin) {
          next()
        } else {
          next('/') // 无权限跳转到首页
        }
      } else {
        next()
      }
    } else {
      next({
        path: '/login',
        query: { redirect: encodeURIComponent(to.fullPath) }
      }) // 未登录跳转到登录页
    }
  } else {
    next()
  }
})

export default router

import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'

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
      component: () => import('../views/AboutView.vue')
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue'),
      meta: { hideGlobalNav: true }
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/RegisterView.vue'),
      meta: { hideGlobalNav: true }
    },
    {
      path: '/user',
      name: 'user',
      component: () => import('../views/UserCenter.vue'),
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
      component: () => import('../views/AddressEdit.vue'),
      meta: { requireAuth: true }
    },
    {
      path: '/user/address/edit/:id',
      name: 'user-address-edit',
      component: () => import('../views/AddressEdit.vue'),
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
      component: () => import('../views/MerchantApply.vue'),
      meta: { requireAuth: true }
    },
    {
      path: '/merchant/shop',
      name: 'merchant-shop',
      component: () => import('../views/ShopManage.vue'),
      meta: { requireAuth: true }
    },
    {
      path: '/merchant/shop/edit',
      name: 'merchant-shop-edit',
      component: () => import('../views/ShopEdit.vue'),
      meta: { requireAuth: true }
    },
    {
      path: '/products',
      name: 'products',
      component: () => import('../views/ProductList.vue')
    },
    {
      path: '/products/:id',
      name: 'product-detail',
      component: () => import('../views/ProductDetail.vue')
    },
    {
      path: '/products/:id/trace',
      name: 'product-trace',
      component: () => import('../views/TraceView.vue'),
      meta: { requireAuth: true }
    },
    {
      path: '/trace/detail/:id',
      name: 'trace-detail',
      component: () => import('../views/TraceView.vue')
    },
    {
      path: '/cart',
      name: 'cart',
      component: () => import('../views/CartView.vue'),
      meta: { requireAuth: true }
    },
    {
      path: '/orders',
      name: 'orders',
      component: () => import('../views/OrderList.vue'),
      meta: { requireAuth: true }
    },
    {
      path: '/orders/:id',
      name: 'order-detail',
      component: () => import('../views/OrderDetail.vue'),
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
      component: () => import('../views/CheckoutView.vue'),
      meta: { requireAuth: true }
    },
    {
      path: '/circle',
      name: 'circle',
      component: () => import('../views/CircleView.vue')
    },
    {
      path: '/ai-expert',
      name: 'ai-expert',
      component: () => import('../views/AIExpertView.vue'),
      meta: { requireAuth: true }
    },
    {
      path: '/admin',
      name: 'admin',
      component: () => import('../views/AdminDashboard.vue'),
      meta: {
        requireAuth: true,
        requireAdmin: true
      },
      children: [
        {
          path: '',
          name: 'admin-dashboard',
          component: () => import('../views/AdminStats.vue')
        },
        {
          path: 'users',
          name: 'admin-users',
          component: () => import('../views/AdminUserList.vue')
        },
        {
          path: 'roles',
          name: 'admin-roles',
          component: () => import('../views/AdminRolePermission.vue')
        },
        {
          path: 'traces',
          name: 'admin-traces',
          component: () => import('../views/AdminTraceManage.vue')
        },
        {
          path: 'products',
          name: 'admin-products',
          component: () => import('../views/AdminProductList.vue')
        },
        {
          path: 'orders',
          name: 'admin-orders',
          component: () => import('../views/AdminOrderList.vue')
        },
        {
          path: 'merchants',
          name: 'admin-merchants',
          component: () => import('../views/AdminMerchantList.vue')
        },
        {
          path: 'merchant-applications',
          name: 'admin-merchant-applications',
          component: () => import('../views/AdminMerchantList.vue')
        },
        {
          path: 'stats',
          name: 'admin-stats',
          component: () => import('../views/AdminStats.vue')
        },
        {
          path: 'complaints',
          name: 'admin-complaints',
          component: () => import('../views/AdminComplaintList.vue')
        },
        {
          path: 'content',
          name: 'admin-content',
          component: () => import('../views/AdminContentManage.vue')
        },
        {
          path: 'settings',
          name: 'admin-settings',
          component: () => import('../views/AdminSystemSettings.vue')
        }
      ]
    }
  ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 检查是否需要登录
  if (to.meta.requireAuth) {
    const token = localStorage.getItem('token')
    if (token) {
      // 检查是否需要管理员权限
      if (to.meta.requireAdmin) {
        const role = localStorage.getItem('role') || 'USER'
        if (role === 'ADMIN') {
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

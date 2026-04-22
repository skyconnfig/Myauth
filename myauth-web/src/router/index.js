import { createRouter, createWebHistory } from 'vue-router'
import Layout from '../layout/index.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: '/dashboard',
        name: 'Dashboard',
        component: () => import('../views/Dashboard.vue'),
        meta: { title: '仪表盘' }
      },
      {
        path: '/licenses',
        name: 'Licenses',
        component: () => import('../views/LicenseList.vue'),
        meta: { title: '授权管理' }
      },
      {
        path: '/generate',
        name: 'Generate',
        component: () => import('../views/GenerateLicense.vue'),
        meta: { title: '生成授权' }
      },
      {
        path: '/keys',
        name: 'Keys',
        component: () => import('../views/KeyManagement.vue'),
        meta: { title: '密钥管理' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  
  if (to.meta.requiresAuth !== false && !token) {
    // 需要登录但未登录，跳转到登录页
    next('/login')
  } else if (to.path === '/login' && token) {
    // 已登录访问登录页，跳转到首页
    next('/dashboard')
  } else {
    next()
  }
})

export default router

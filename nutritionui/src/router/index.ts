import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue')
      },
      {
        path: 'airecommend',
        name: 'AiRecommend',
        component: () => import('@/views/AiRecommend.vue'),
        meta: { title: '饮食推荐（AI小李）' }
      },
      // 将AI聊天路由移动到MainLayout的子路由中
      {
        path: 'ai-chat',
        name: 'AiChat',
        component: () => import('@/views/AiChat.vue'),
        meta: { title: 'AI聊天 - 小李' }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue')
      },
      {
        path: 'plan',
        name: 'Plan',
        component: () => import('@/views/Plan.vue')
      },
      {
        path: 'record',
        name: 'Record',
        component: () => import('@/views/Record.vue')
      },
      {
        path: 'analysis',
        name: 'Analysis',
        component: () => import('@/views/Analysis.vue')
      },
      {
        path: 'recipes',
        name: 'Recipes',
        component: () => import('@/views/Recipes.vue')
      },
      {
        path: 'recipes/:id',
        name: 'RecipeDetail',
        component: () => import('@/views/RecipeDetail.vue')
      },
      {
        path: 'statistics',
        name: 'Statistics',
        component: () => import('@/views/Statistics.vue')
      },
      {
        path: 'messages',
        name: 'Messages',
        component: () => import('@/views/Messages.vue')
      },
      {
        path: 'reminders',
        name: 'Reminders',
        component: () => import('@/views/Reminders.vue')
      },
      {
        path: 'announcements',
        name: 'Announcements',
        component: () => import('@/views/AnnouncementsList.vue'),
        meta: { public: true }
      },
      {
        path: 'announcements/:id',
        name: 'AnnouncementDetail',
        component: () => import('@/views/AnnouncementDetail.vue'),
        meta: { public: true }
      },
      {
        path: 'admin/foods',
        name: 'FoodAdmin',
        component: () => import('@/views/FoodAdmin.vue'),
        meta: { requiresAdmin: true }
      },
      {
        path: 'admin/recipes',
        name: 'RecipeAdmin',
        component: () => import('@/views/RecipeAdmin.vue'),
        meta: { requiresAdmin: true }
      },
      {
        path: 'admin/users',
        name: 'UserAdmin',
        component: () => import('@/views/UserAdmin.vue'),
        meta: { requiresAdmin: true }
      },
      {
        path: 'admin/announcements',
        name: 'AnnouncementAdmin',
        component: () => import('@/views/AnnouncementAdmin.vue'),
        meta: { requiresAdmin: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

router.beforeEach(async (to, _from, next) => {
  const userStore = useUserStore()
  const token = localStorage.getItem('token')
  const isLoggedIn = !!token

  console.log('路由守卫 - 目标路径:', to.path)
  console.log('路由守卫 - 登录状态:', isLoggedIn)

  // 公共白名单（即使父级要求登录也放行）
  const isPublic = to.matched.some(r => r.meta.public) || to.path.startsWith('/announcements')

  // 检查是否需要登录
  if (!isPublic && to.meta.requiresAuth && !isLoggedIn) {
    console.log('路由守卫 - 需要登录，跳转到登录页')
    next('/login')
    return
  }

  // 如果已登录，获取用户信息（包括管理员状态）
  if (isLoggedIn && !userStore.userInfo) {
    await userStore.fetchUserInfo()
  }

  // 检查管理员权限
  if (to.meta.requiresAdmin) {
    const isAdmin = localStorage.getItem('isAdmin') === 'true' || userStore.isAdmin
    if (!isAdmin) {
      console.log('路由守卫 - 需要管理员权限，跳转到首页')
      next('/dashboard')
      return
    }
  }

  if (to.path === '/login' && isLoggedIn) {
    console.log('路由守卫 - 已登录，跳转到首页')
    next('/')
    return
  }

  next()
})

export default router


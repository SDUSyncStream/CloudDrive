// import path = require('path')
import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/HomeView.vue')
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/LoginView.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/RegisterView.vue')
  },
  {
    path: '/forget-password',
    name: 'ForgetPassword',
    component: () => import('../views/ForgetPwdView.vue')
  },
  {
    path: '/admin-login',
    name: 'AdminLogin',
    component: () => import('../views/AdminLoginView.vue')
  },
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('../views/AdminView.vue'),
    children: [
      {
        path: '/admin/dashboard',
        name: 'Dashboard',
        component: () => import('../views/AdminDashboardView.vue')
      },
      {
        path: '/admin/users',
        name: 'Users',
        component: () => import('../views/AdminUserManageView.vue')
      },
      {
        path: '/admin/files',
        name: 'AdminFiles',
        component: () => import('../views/AdminFilesView.vue')
      },
      {
        path: '/admin/orders',
        name: 'AdminOrders',
        component: () => import('../views/AdminOrderView.vue')
      },
      {
        path: '/admin/dish',
        name: 'AdminDish',
        component: () => import('../views/AdminDishView.vue')
      }
    ]
  },
  {
    path: '/main',
    name: 'Main',
    component: () => import('../views/MainView.vue'),
    redirect: '/main/files',
    children: [
      {
        path: '/main/files',
        name: 'Files',
        component: () => import('../views/FilesView.vue'),
      },
      {
        path: '/main/shared',
        name: 'SharedFiles',
        component: () => import('../views/SharedView.vue')
      },
      {
        path: '/main/deleted',
        name: 'Recycle',
        component: () => import('../views/RecycleView.vue')
      },

    ]
  },
  {
    path: '/profile',
    name: 'UserProfile',
    component: () => import('../views/UserProfileView.vue')
  },
  {
    path: '/vip',
    name: 'VIP',
    component: () => import('../views/MainView.vue'),
    redirect: '/vip/membership',
    children: [
      {
        path: '/vip/membership',
        name: 'Membership',
        component: () => import('../views/MembershipView.vue')
      },
      {
        path: '/vip/subscription-history',
        name: 'SubscriptionHistory',
        component: () => import('../views/SubscriptionHistoryView.vue')
      },
    ]
  },
  {
    path: '/orders',
    name: 'Orders',
    component: () => import('../views/OrdersView.vue')
  },
  {
    path: '/orders/:id',
    name: 'OrderDetail',
    component: () => import('../views/OrderDetailView.vue')
  },
  {
    path: '/shareCheck/:shareId',
    name: 'ShareCheck',
    component: () => import('../views/ShareCheckView.vue')
  },
  {
    path: '/share/:shareId',
    name: 'ShareView',
    component: () => import('../views/ShareView.vue')
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../views/NotFoundView.vue')
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

// 路由守卫
router.beforeEach((to, _from, next) => {
  const isAuthenticated = localStorage.getItem('token')

  if (to.meta.requiresAuth && !isAuthenticated) {
    next('/login')
  } else {
    next()
  }
})

export default router
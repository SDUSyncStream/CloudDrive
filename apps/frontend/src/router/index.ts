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
router.beforeEach((to, from, next) => {
  const isAuthenticated = localStorage.getItem('token')
  
  if (to.meta.requiresAuth && !isAuthenticated) {
    next('/login')
  } else {
    next()
  }
})

export default router
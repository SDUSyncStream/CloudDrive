import axios from 'axios'
import type { ApiResponse } from '../types'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  (response) => {
    const data: ApiResponse = response.data
    if (data.code !== 200) {
      throw new Error(data.message || '请求失败')
    }
    return response
  },
  async (error) => {
    const originalRequest = error.config
    
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true
      
      // 尝试刷新Token
      const refreshToken = localStorage.getItem('refreshToken')
      if (refreshToken) {
        try {
          const response = await axios.post('/api/auth/refresh', {
            refreshToken: refreshToken
          })
          
          const { accessToken } = response.data.data
          localStorage.setItem('accessToken', accessToken)
          
          // 重新发送原始请求
          originalRequest.headers.Authorization = `Bearer ${accessToken}`
          return api(originalRequest)
          
        } catch (refreshError) {
          // 刷新失败，清除所有Token并跳转到登录页
          localStorage.removeItem('accessToken')
          localStorage.removeItem('refreshToken')
          window.location.href = '/login'
          return Promise.reject(refreshError)
        }
      } else {
        // 没有refreshToken，直接跳转登录页
        window.location.href = '/login'
      }
    }
    
    return Promise.reject(error)
  }
)

export default api
export { api as apiClient }
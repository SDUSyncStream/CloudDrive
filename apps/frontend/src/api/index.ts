import axios from 'axios'
import type { ApiResponse } from '../types'

const api = axios.create({
  baseURL: '/api',  // 使用相对路径，通过Vite代理转发
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

// import axios from 'axios';
// const baseURL = '/api';  // 使用代理，避免CORS问题
// const instance = axios.create({
//     timeout: 5000,
//     baseURL
// });


// //http request 拦截器
// instance.interceptors.request.use(
//     config => {
//       // 判断是否存在token，如果存在的话，则每个http header都加上token
//       const token = window.localStorage.getItem('token');
//       if (token) {
//         config.headers.Authorization = "Bearer " + token;
//       }
//       config.headers['Content-Type'] = 'application/json';
      
//       return config;
//     },
//     error => {
//       return Promise.reject(error);
//     }
// );


// //http response 拦截器
// // instance.interceptors.response.use(
// //     response => {
// //       return response;
// //     },
// //     error => {
// //       const {response} = error;
// //       if (response) {
// //         // 请求已发出，但是不在2xx的范围
// //         // showMessage(response.status);           // 传入响应码，匹配响应码对应信息
// //         return Promise.reject(response.data);
// //       } else {
// //         ElMessage.warning('网络连接异常,请稍后再试!');
// //       }
// //     }
// // );

// export default instance;
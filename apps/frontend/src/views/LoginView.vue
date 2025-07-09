<template>
  <div class="login-page">
    <el-card class="login-card">
      <template #header>
        <div class="login-header">
          <el-icon size="32" color="#409eff"><User /></el-icon>
          <span>登录 CloudDrive</span>

        </div>
      </template>
      
      <el-form :model="loginForm" :rules="rules" ref="formRef" @submit.prevent="handleLogin">
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="用户名或邮箱"
            prefix-icon="User"
            size="large"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="密码"
            prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>
        
        <el-form-item>
          <el-button 
            type="primary" 
            size="large" 
            style="width: 100%"
            :loading="loading"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="login-footer">
        <el-link type="primary" href="/register">注册账号</el-link>
        <el-link type="info" href="/forget-password">忘记密码？</el-link>
        <el-link type="primary" href="/admin-login">管理员入口</el-link>

      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance } from 'element-plus'
import { useUserStore } from '../stores/user'
import { login } from '../api/auth'
import axios from 'axios'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref<FormInstance>()
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名或邮箱', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  // axios.request({
  //   method: 'post',
  //   url: '/api/auth/login',
  //   data: {
  //     username: loginForm.username,
  //     passwordHash: loginForm.password
  //   }
  // }).then(response => {
  //   if (response.data.code === 200) {
  //     // 保存Token到localStorage
  //     console.log('登录成功:', response.data.data)
  //     localStorage.setItem('accessToken', response.data.data.token)
  //     if (response.data.data.refreshToken) {
  //       localStorage.setItem('refreshToken', response.data.data.refreshToken)
  //     }

  //     // 更新用户状态
  //     userStore.setUser(response.data.data.userId)
  //     userStore.setToken(response.data.data.token)

  //     ElMessage.success('登录成功')
  //     router.push('/main')
  //   } else {
  //     ElMessage.error(response.data.message || '登录失败')
  //   }
  // }).catch(error => {
  //   console.error('登录错误:', error)
  //   ElMessage.error(error.response?.data?.message || '登录失败，请稍后重试')
  // })
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    loading.value = true
    
    console.log('发送登录请求:', { username: loginForm.username, password: '***' })
    
    // 调用登录API
    const response = await login({ username: loginForm.username, passwordHash: loginForm.password })

    console.log('登录响应:', response)
    
    if (response.data.code === 200) {
      // 保存Token到localStorage
      localStorage.setItem('accessToken', response.data.data.token)
      if (response.data.data.refreshToken) {
        localStorage.setItem('refreshToken', response.data.data.refreshToken)
      }
      localStorage.setItem('UserId', response.data.data.userId || 'user-01')

      // 更新用户状态
      userStore.setUser(response.data.data.userId)
      userStore.setToken(response.data.data.token)

      ElMessage.success('登录成功')
      router.push('/main')
    } else {
      ElMessage.error(response.data.message || '登录失败')
    }
    
  } catch (error: any) {
    console.error('登录错误详情:', error)
    console.error('错误响应:', error.response)
    
    // 处理不同类型的错误
    let errorMessage = '登录失败'
    
    if (error.response) {
      // 服务器响应了错误状态码
      const status = error.response.status
      const data = error.response.data
      
      console.log('错误状态码:', status)
      console.log('错误数据:', data)
      
      switch (status) {
        case 400:
          errorMessage = data.message || '请求参数错误'
          break
        case 401:
          errorMessage = data.message || '用户名或密码错误'
          break
        case 403:
          errorMessage = data.message || '访问被拒绝，请检查凭据'
          break
        case 500:
          errorMessage = '服务器内部错误，请稍后重试'
          break
        default:
          errorMessage = data.message || `请求失败 (${status})`
      }
    } else if (error.request) {
      // 网络错误
      errorMessage = '网络连接失败，请检查网络设置'
    } else if (error.message) {
      // 其他错误
      errorMessage = error.message
    }
    
    ElMessage.error(errorMessage)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 400px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.login-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  font-size: 20px;
  font-weight: 600;
  color: #2c3e50;
}

.login-footer {
  display: flex;
  justify-content: space-between;
  margin-top: 20px;
}
</style>
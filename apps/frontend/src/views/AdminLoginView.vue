<template>
  <div class="admin-login-page">
    <el-card class="admin-login-card">
      <template #header>
        <div class="admin-login-header">
          <el-icon size="32" color="#67C23A"><Setting /></el-icon>
          <span>管理员登录</span>
          <el-link type="info" class="back-to-user-login" @click="goToUserLogin">返回普通登录</el-link>
        </div>
      </template>

      <el-form :model="adminLoginForm" :rules="rules" ref="formRef" @submit.prevent="handleAdminLogin">
        <el-form-item prop="username">
          <el-input
              v-model="adminLoginForm.username"
              placeholder="管理员用户名"
              prefix-icon="User"
              size="large"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
              v-model="adminLoginForm.password"
              type="password"
              placeholder="管理员密码"
              prefix-icon="Lock"
              size="large"
              show-password
          />
        </el-form-item>

        <el-form-item>
          <el-button
              type="success"
              size="large"
              style="width: 100%"
              :loading="loading"
              @click="handleAdminLogin"
          >
            登录管理后台
          </el-button>
        </el-form-item>
      </el-form>

      <div class="admin-login-footer">
        <el-link type="warning" href="#">忘记管理员密码？</el-link>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance } from 'element-plus'
// 如果管理员有单独的 store 来管理状态，可以在这里导入
// import { useAdminStore } from '../stores/admin'
import { User, Lock, Setting } from '@element-plus/icons-vue' // 导入所需的图标

const router = useRouter()
// const adminStore = useAdminStore() // 如果有管理员专用的 store，启用这行

const formRef = ref<FormInstance>()
const loading = ref(false)

const adminLoginForm = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入管理员用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入管理员密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' } // 管理员密码通常要求更复杂，这里仅作示例
  ]
}

const handleAdminLogin = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    loading.value = true

    // **重点：管理员登录的API地址与普通用户不同**
    const response = await fetch('/api/admin/login', { // 假设管理员登录API是 /api/admin/login
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        username: adminLoginForm.username,
        password: adminLoginForm.password
      })
    })

    const result = await response.json()

    if (result.code === 200) {
      // **重要：管理员的Token和用户Token可能需要分开存储或管理**
      localStorage.setItem('adminAccessToken', result.data.accessToken)
      localStorage.setItem('adminRefreshToken', result.data.refreshToken)

      // 更新管理员状态（如果存在 adminStore）
      // adminStore.setAdminUser(result.data.user)
      // adminStore.setAdminToken(result.data.accessToken)

      ElMessage.success('管理员登录成功！')
      // **重要：导航到管理后台的主页，例如 /admin/dashboard**
      router.push('/admin/dashboard')
    } else {
      ElMessage.error(result.message || '管理员登录失败，请检查用户名或密码')
    }

  } catch (error) {
    console.error('管理员登录错误:', error)
    ElMessage.error('管理员登录失败，请检查网络连接或服务器')
  } finally {
    loading.value = false
  }
}

// 返回普通用户登录页面的方法
const goToUserLogin = () => {
  router.push('/login') // 假设普通用户登录路由是 /login
}
</script>

<style scoped>
.admin-login-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  /* 为管理员登录页设置一个不同的背景，以示区别 */
  background: linear-gradient(135deg, #4CAF50 0%, #388E3C 100%); /* 绿色系背景 */
}

.admin-login-card {
  width: 400px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.admin-login-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  font-size: 20px;
  font-weight: 600;
  color: #2c3e50;
  position: relative; /* 允许绝对定位 */
}

/* 返回普通登录的链接样式 */
.back-to-user-login {
  font-size: 14px;
  position: absolute;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
  white-space: nowrap;
}

.admin-login-footer {
  display: flex;
  justify-content: flex-end; /* 通常管理员登录页的底部链接会少一些 */
  margin-top: 20px;
}
</style>
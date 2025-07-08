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

// 导入 Element Plus Icons
// 确保这些图标已经通过 `npm install @element-plus/icons-vue` 安装
import { User, Lock, Setting } from '@element-plus/icons-vue'

// 获取 Vue Router 实例
const router = useRouter()

// 表单引用，用于触发表单校验
const formRef = ref<FormInstance>()
// 登录按钮的加载状态
const loading = ref(false)

// 响应式表单数据
const adminLoginForm = reactive({
  username: '',
  password: ''
})

// 表单校验规则
const rules = {
  username: [
    { required: true, message: '请输入管理员用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入管理员密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' } // 可根据实际需求调整复杂度
  ]
}

/**
 * 处理管理员登录逻辑
 */
const handleAdminLogin = async () => {
  // 检查表单引用是否存在
  if (!formRef.value) return

  try {
    // 触发表单校验，如果校验失败会抛出异常
    await formRef.value.validate()
    loading.value = true // 校验通过，显示加载状态

    // ** 核心：发送登录请求到后端 API **
    // 假设后端管理员登录API是 `/api/admin/login`
    const response = await fetch('/api/admin/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        // 如果需要，可以在这里添加其他Header，例如客户端ID等
      },
      body: JSON.stringify({
        username: adminLoginForm.username,
        // 注意：实际项目中，密码应该在前端进行哈希或加密后再发送，
        // 或使用HTTPS确保传输安全，并在后端进行哈希比较。
        password: adminLoginForm.password
      })
    })

    // 解析后端返回的JSON结果
    const result = await response.json()

    // 根据后端返回的 code 进行判断
    if (result.code === 200) {
      // 登录成功
      ElMessage.success('管理员登录成功！正在跳转...')

      // ** 重要：存储管理员相关的认证信息 (例如 Token)**
      // 后端ServerResult通常会返回一个包含token的数据对象
      if (result.data && result.data.accessToken) {
        localStorage.setItem('adminAccessToken', result.data.accessToken)
      }
      if (result.data && result.data.refreshToken) {
        localStorage.setItem('adminRefreshToken', result.data.refreshToken)
      }
      // 如果后端返回了其他管理员用户信息，也可以存储
      // localStorage.setItem('adminUser', JSON.stringify(result.data.user))

      // ** 核心：登录成功后，导航到管理后台的主页 **
      // 请确保你的 Vue Router 已经配置了 '/admin/dashboard' 路由
      router.push('/admin/dashboard')

    } else {
      // 登录失败，显示后端返回的错误信息或默认错误信息
      ElMessage.error(result.message || '管理员登录失败，请检查用户名或密码。')
    }

  } catch (error: any) { // 捕获表单校验失败或网络请求错误
    // 如果是表单校验失败，validate() 会抛出包含错误信息的 Promise rejection
    // 如果是网络请求错误，fetch 会抛出 TypeError
    if (error instanceof Error) {
      console.error('管理员登录错误:', error.message)
      ElMessage.error('管理员登录失败：' + error.message)
    } else if (Array.isArray(error) && error.length > 0) { // Element Plus validate 失败的错误结构
      console.error('表单校验失败:', error);
      // ElMessage.error('表单填写不完整或有误。'); // validate 失败 ElMessage 默认不弹出，这里手动添加也可
    } else {
      console.error('管理员登录未知错误:', error);
      ElMessage.error('管理员登录发生未知错误，请稍后再试。');
    }
  } finally {
    loading.value = false // 无论成功或失败，都解除加载状态
  }
}

/**
 * 返回普通用户登录页面的方法
 */
const goToUserLogin = () => {
  // 假设普通用户登录路由是 /login
  router.push('/login')
}
</script>

<style scoped>
.admin-login-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #4CAF50 0%, #388E3C 100%); /* 绿色系背景 */
}

.admin-login-card {
  width: 400px;
  max-width: 90%; /* 适应小屏幕 */
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border-radius: 8px; /* 增加圆角 */
}

.admin-login-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  font-size: 24px; /* 稍微调大标题字号 */
  font-weight: 600;
  color: #2c3e50;
  position: relative;
  padding-bottom: 10px; /* 增加内边距 */
  border-bottom: 1px solid #eee; /* 增加分割线 */
}

.admin-login-header span {
  /* 确保文本也居中对齐 */
  flex-grow: 1; /* 让span占据更多空间以更好地居中 */
  text-align: center;
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

/* 调整表单项的间距 */
.el-form-item {
  margin-bottom: 22px;
}

.admin-login-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 15px; /* 调整间距 */
}
</style>
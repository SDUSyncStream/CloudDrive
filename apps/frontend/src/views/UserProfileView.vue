<template>
  <div class="user-profile-page">
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <h2>个人资料</h2>
          <!--返回主页面按钮 - 居右-->
          <el-button 
            type="primary" 
            plain 
            @click="goBack"
            class="back-button"
          >
            <el-icon><ArrowLeft /></el-icon>
            返回主页
          </el-button>
        </div>
      </template>

      <div class="profile-content">
        <!-- 头像部分 -->
        <div class="avatar-section">
          <div class="avatar-container">
            <el-avatar
              :size="120"
              :src="userInfo.avatar"
              class="user-avatar"
            >
              <el-icon><User /></el-icon>
            </el-avatar>
            <div class="avatar-upload">
              <el-upload
                :show-file-list="false"
                :before-upload="handleAvatarUpload"
                accept="image/*"
                class="avatar-uploader"
              >
                <el-button size="small" type="text">
                  <el-icon><Camera /></el-icon>
                  更换头像
                </el-button>
              </el-upload>
            </div>
          </div>
        </div>

        <!-- 用户信息表单 -->
        <div class="info-section">
          <el-form
            label-width="100px"
            class="user-form"
          >
            <el-form-item label="用户名">
              <el-input
                :value="userInfo.username"
                disabled
                placeholder="用户名"
              />
            </el-form-item>

            <el-form-item label="邮箱">
              <el-input
                :value="userInfo.email"
                disabled
                placeholder="邮箱"
                type="email"
              />
            </el-form-item>

            <el-form-item label="会员等级">
              <el-tag
                :type="getMembershipTagType(userInfo.membershipLevelId)"
                size="large"
              >
                {{ getMembershipLevelText(userInfo.membershipLevelId) }}
              </el-tag>
            </el-form-item>

            <el-form-item label="存储容量">
              <div class="storage-info">
                <div class="storage-text">
                  <span class="used">{{ formatSize(userInfo.storageUsed || 0) }}</span>
                  /
                  <span class="total">{{ formatSize(userInfo.storageQuota || 0) }}</span>
                </div>
                <el-progress
                  :percentage="storagePercentage"
                  :color="getProgressColor(storagePercentage)"
                  class="storage-progress"
                />
              </div>
            </el-form-item>

            <el-form-item label="注册时间">
              <el-input
                :value="formatDate(userInfo.createdAt)"
                disabled
                placeholder="注册时间"
              />
            </el-form-item>
          </el-form>
        </div>
      </div>
    </el-card>

    <!-- 修改密码对话框 -->
    <el-dialog
      v-model="passwordDialogVisible"
      title="修改密码"
      width="400px"
      @close="handlePasswordDialogClose"
    >
      <el-form
        ref="passwordFormRef"
        :model="passwordForm"
        :rules="passwordRules"
        label-width="100px"
      >
        <el-form-item label="原密码" prop="oldPassword">
          <el-input
            v-model="passwordForm.oldPassword"
            type="password"
            show-password
            placeholder="请输入原密码"
          />
        </el-form-item>

        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            show-password
            placeholder="请输入新密码"
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            show-password
            placeholder="请再次输入新密码"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handlePasswordChange" :loading="changingPassword">
          确认修改
        </el-button>
      </template>
    </el-dialog>

    <!-- 额外操作按钮 -->
    <div class="action-buttons">
      <el-button type="warning" @click="passwordDialogVisible = true">
        <el-icon><Lock /></el-icon>
        修改密码
      </el-button>
      <el-button type="info" @click="$router.push('/vip/membership')">
        <el-icon><Star /></el-icon>
        升级会员
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type UploadProps } from 'element-plus'
import { User, Camera, Lock, Star } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'
import { getUserInfo, uploadAvatar, newPwd } from '../api/auth'
import { encryptPassword } from '../utils/crypto'
import type { User as UserType } from '../types'
import { useRouter } from 'vue-router'

const userStore = useUserStore()
const router = useRouter()

// 响应式数据
const passwordDialogVisible = ref(false)
const changingPassword = ref(false)
const passwordFormRef = ref<FormInstance>()

// 用户信息
const userInfo = ref<UserType>({
  id: '',
  username: '',
  email: '',
  avatar: '',
  createdAt: new Date(),
  updatedAt: new Date(),
  storageUsed: 0,
  storageQuota: 1024 * 1024 * 1024 * 5, // 5GB 默认
  membershipLevelId: '普通用户'
})

// 密码修改表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 密码修改验证规则
const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule: any, value: string, callback: Function) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 计算属性
const storagePercentage = computed(() => {
  if (!userInfo.value.storageQuota || userInfo.value.storageQuota === 0) return 0
  return Math.round((userInfo.value.storageUsed || 0) / userInfo.value.storageQuota * 100)
})

const goBack = () => {
  router.push('/main')
}

// 方法
const loadUserInfo = async () => {
  try {
    const response = await getUserInfo(localStorage.getItem("UserId"))
    if (response.data.code === 200) {
      userInfo.value = response.data.data
    } else {
      ElMessage.error('获取用户信息失败')
    }
  } catch (error) {
    console.error('获取用户信息错误:', error)
    ElMessage.error('获取用户信息失败，请刷新重试')
  }
}

const handleAvatarUpload: UploadProps['beforeUpload'] = async (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }

  try {
    const response = await uploadAvatar(file)
    if (response.data.code === 200) {
      userInfo.value.avatar = response.data.data.avatarUrl
      ElMessage.success('头像上传成功')
    } else {
      ElMessage.error('头像上传失败')
    }
  } catch (error) {
    console.error('头像上传错误:', error)
    ElMessage.error('头像上传失败')
  }

  return false // 阻止默认上传行为
}

const handlePasswordChange = async () => {
  if (!passwordFormRef.value) return

  try {
    await passwordFormRef.value.validate()
    changingPassword.value = true

    console.log('开始修改密码...')

    // 加密原密码和新密码
    const encryptedOldPassword = await encryptPassword(passwordForm.oldPassword)
    const encryptedNewPassword = await encryptPassword(passwordForm.newPassword)

    console.log('密码加密完成')

    // 调用修改密码API
    const response = await newPwd({
      userId: localStorage.getItem("UserId"),
      oldPassword: encryptedOldPassword,
      newPassword: encryptedNewPassword
    })

    console.log('密码修改响应:', response)

    if (response.data.code === 200) {
      ElMessage.success('密码修改成功')
      passwordDialogVisible.value = false
      
      // 重置表单
      passwordForm.oldPassword = ''
      passwordForm.newPassword = ''
      passwordForm.confirmPassword = ''
      
      // 清除表单验证状态
      if (passwordFormRef.value) {
        passwordFormRef.value.resetFields()
      }
    } else {
      ElMessage.error(response.data.message || '密码修改失败')
    }
  } catch (error: any) {
    console.error('密码修改错误:', error)
    
    let errorMessage = '密码修改失败'
    if (error.response?.data?.message) {
      errorMessage = error.response.data.message
    } else if (error.message) {
      errorMessage = error.message
    }
    
    ElMessage.error(errorMessage)
  } finally {
    changingPassword.value = false
  }
}

const handlePasswordDialogClose = () => {
  // 重置表单
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  
  // 清除表单验证状态
  if (passwordFormRef.value) {
    passwordFormRef.value.resetFields()
  }
}

const formatSize = (bytes: number): string => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const formatDate = (date: Date | string): string => {
  const d = new Date(date)
  return d.toLocaleDateString('zh-CN') + ' ' + d.toLocaleTimeString('zh-CN')
}

const getMembershipTagType = (level?: string) => {
  switch (level) {
    case 'level-free': return ''
    case 'level-basic': return 'success'
    case 'level-premium': return 'warning'
    case 'level-enterprise': return 'danger'
    default: return ''
  }
}

const getMembershipLevelText = (level?: string) => {
  switch (level) {
    case 'level-free': return '免费版'
    case 'level-basic': return '基础版'
    case 'level-premium': return '高级版'
    case 'level-enterprise': return '企业版'
    default: return '普通用户'
  }
}

const getProgressColor = (percentage: number) => {
  if (percentage < 50) return '#67C23A'
  if (percentage < 80) return '#E6A23C'
  return '#F56C6C'
}

// 生命周期
onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
.user-profile-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.profile-card {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-radius: 12px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  color: #2c3e50;
  font-weight: 600;
}

.back-button {
  margin-left: auto;
  white-space: nowrap;
}

.profile-content {
  display: flex;
  gap: 40px;
  margin-top: 20px;
}

.avatar-section {
  flex-shrink: 0;
}

.avatar-container {
  text-align: center;
}

.user-avatar {
  border: 4px solid #f0f0f0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.avatar-upload {
  margin-top: 12px;
}

.avatar-uploader :deep(.el-upload) {
  border: none;
  cursor: pointer;
  border-radius: 6px;
  overflow: hidden;
}

.info-section {
  flex: 1;
}

.user-form {
  max-width: 500px;
}

.storage-info {
  width: 100%;
}

.storage-text {
  margin-bottom: 8px;
  font-size: 14px;
}

.storage-text .used {
  color: #409eff;
  font-weight: 600;
}

.storage-text .total {
  color: #909399;
}

.storage-progress {
  width: 100%;
}

.action-buttons {
  margin-top: 20px;
  text-align: center;
  display: flex;
  gap: 12px;
  justify-content: center;
}

@media (max-width: 768px) {
  .profile-content {
    flex-direction: column;
    gap: 20px;
  }
  
  .user-profile-page {
    padding: 10px;
  }
  
  .action-buttons {
    flex-direction: column;
    align-items: center;
  }
}
</style>

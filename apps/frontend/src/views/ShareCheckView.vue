<template>
  <div class="share-view">
    <div class="header">
      <div class="header-content">
        <div class="logo" @click="router.push('/')">
          <el-icon class="logo-icon"><FolderOpened /></el-icon>
          <span class="name">CloudDrive</span>
        </div>
      </div>
    </div>
    <div class="share-body">
      <el-skeleton v-if="loading" :rows="4" animated />
      <el-card v-else class="share-panel">
        <div class="share-user-info">
          <div class="avatar">
            <el-avatar :src="shareInfo.avatar" :size="50">
              <el-icon><User /></el-icon>
            </el-avatar>
          </div>
          <div class="share-info">
            <div class="user-info">
              <span class="nick-name">{{ shareInfo.nickName || shareInfo.username || '用户' }}</span>
              <span class="share-time">分享于 {{ formatDate(shareInfo.shareTime) }}</span>
            </div>
            <div class="file-name">分享文件：{{ shareInfo.fileName }}</div>
          </div>
        </div>
        <div class="check-code-area">
          <el-form :model="formData" :rules="rules" ref="formDataRef" @submit.prevent>
            <el-form-item prop="code">
              <el-input v-model="formData.code" maxlength="5" placeholder="请输入提取码" style="width: 200px; margin-right: 16px;" @keyup.enter="checkShare" />
              <el-button type="primary" @click="checkShare">校验提取码</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { FolderOpened, User } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const shareId = route.params.shareId as string

const shareInfo = ref<any>({})
const loading = ref(true)
const formData = ref({ code: '' })
const formDataRef = ref()
const rules = {
  code: [
    { required: true, message: '请输入提取码' },
    { min: 5, message: '提取码为5位' },
    { max: 5, message: '提取码为5位' }
  ]
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  // 支持 yyyy-MM-dd HH:mm:ss 或时间戳
  if (/^\d+$/.test(dateStr)) {
    const d = new Date(Number(dateStr))
    return d.toLocaleString()
  }
  return dateStr.replace('T', ' ').slice(0, 19)
}

const getShareInfo = async () => {
  loading.value = true
  try {
    const res = await fetch(`/api/showShare/getShareInfo?shareId=${shareId}`).then(r => r.json())
    if (res && res.data) {
      shareInfo.value = res.data
    }
  } finally {
    loading.value = false
  }
}
onMounted(getShareInfo)

const checkShare = async () => {
  await formDataRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    const res = await fetch(`/api/showShare/checkShareCode?shareId=${shareId}&code=${formData.value.code}`).then(r => r.json())
    if (res && (res.code === 0 || res.code === 200)) {
      router.push(`/share/${shareId}`)
    } else {
      ElMessage.error('提取码错误')
    }
  })
}
</script>

<style scoped>
.share-view {
  min-height: 100vh;
  background: #f5f7fa;
}
.header {
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  padding: 0 20px;
}
.header-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  height: 60px;
}
.logo {
  display: flex;
  align-items: center;
  cursor: pointer;
}
.logo-icon {
  font-size: 32px;
  color: #409eff;
  margin-right: 8px;
}
.name {
  font-size: 20px;
  font-weight: bold;
  color: #409eff;
}
.share-body {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px 0 20px;
  min-height: calc(100vh - 60px);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}
.share-panel {
  background: #fff;
  border-radius: 18px;
  padding: 64px 64px 40px 64px;
  margin: 0 auto;
  max-width: 750px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.14);
}
.share-user-info {
  display: flex;
  align-items: center;
  margin-bottom: 44px;
}
.avatar {
  margin-right: 36px;
}
.share-info {
  flex: 1;
}
.user-info {
  display: flex;
  align-items: center;
  margin-bottom: 18px;
}
.nick-name {
  font-size: 32px;
  font-weight: 800;
  color: #303133;
}
.share-time {
  margin-left: 32px;
  font-size: 22px;
  color: #909399;
}
.file-name {
  font-size: 26px;
  color: #606266;
  margin-top: 8px;
}
.check-code-area {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  margin-top: 28px;
}
/deep/ .el-avatar {
  width: 110px !important;
  height: 110px !important;
  font-size: 54px !important;
}
/deep/ .el-input__inner {
  font-size: 28px;
  height: 60px;
}
.el-button {
  font-size: 28px;
  height: 60px;
  padding: 0 44px;
}
</style> 
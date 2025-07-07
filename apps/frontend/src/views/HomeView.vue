<template>
  <div class="home">
    <el-container>
      <el-header>
        <div class="header">
          <h1>
            <el-icon><CloudDownload /></el-icon>
            CloudDrive
          </h1>
          <div class="header-actions">
            <el-button v-if="!isAuthenticated" type="primary" @click="$router.push('/login')">
              登录
            </el-button>
            <el-button v-else type="primary" @click="$router.push('/main')">
              进入云盘
            </el-button>
          </div>
        </div>
      </el-header>
      
      <el-main>
        <div class="hero">
          <h2>您的私人云存储空间</h2>
          <p>安全、便捷、高效的文件管理服务</p>
          
          <div class="features">
            <el-row :gutter="20">
              <el-col :span="8">
                <el-card class="feature-card">
                  <el-icon size="48" color="#409eff"><Upload /></el-icon>
                  <h3>快速上传</h3>
                  <p>支持拖拽上传，批量文件处理</p>
                </el-card>
              </el-col>
              <el-col :span="8">
                <el-card class="feature-card">
                  <el-icon size="48" color="#67c23a"><Share /></el-icon>
                  <h3>安全分享</h3>
                  <p>生成分享链接，控制访问权限</p>
                </el-card>
              </el-col>
              <el-col :span="8">
                <el-card class="feature-card">
                  <el-icon size="48" color="#e6a23c"><Search /></el-icon>
                  <h3>智能搜索</h3>
                  <p>快速定位文件，支持多种筛选</p>
                </el-card>
              </el-col>
            </el-row>
          </div>
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useUserStore } from '../stores/user'

const userStore = useUserStore()
const isAuthenticated = computed(() => userStore.isAuthenticated)
</script>

<style scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
}

.header h1 {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #409eff;
  margin: 0;
}

.hero {
  text-align: center;
  padding: 60px 0;
}

.hero h2 {
  font-size: 2.5rem;
  margin-bottom: 16px;
  color: #2c3e50;
}

.hero p {
  font-size: 1.2rem;
  color: #7f8c8d;
  margin-bottom: 60px;
}

.features {
  max-width: 1200px;
  margin: 0 auto;
}

.feature-card {
  text-align: center;
  padding: 40px 20px;
}

.feature-card h3 {
  margin: 16px 0 8px;
  color: #2c3e50;
}

.feature-card p {
  color: #7f8c8d;
  margin: 0;
}
</style>
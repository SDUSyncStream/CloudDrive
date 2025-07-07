<template>
  <div class="files-page">
    <el-container>
      <el-header>
        <div class="header">
          <h2>我的文件</h2>
          <div class="header-actions">
            <el-button type="primary" :icon="Upload">上传文件</el-button>
            <el-button :icon="FolderAdd">新建文件夹</el-button>
            <el-dropdown @command="handleUserAction">
              <el-avatar :src="userStore.user?.avatar || undefined">
                {{ userStore.user?.username?.charAt(0).toUpperCase() }}
              </el-avatar>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人资料</el-dropdown-item>
                  <el-dropdown-item command="settings">设置</el-dropdown-item>
                  <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </el-header>
      
      <el-main>
        <div class="toolbar">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item>根目录</el-breadcrumb-item>
            <el-breadcrumb-item>文档</el-breadcrumb-item>
          </el-breadcrumb>
          
          <div class="view-controls">
            <el-button-group>
              <el-button :type="viewMode === 'grid' ? 'primary' : 'default'" @click="viewMode = 'grid'">
                <el-icon><Grid /></el-icon>
              </el-button>
              <el-button :type="viewMode === 'list' ? 'primary' : 'default'" @click="viewMode = 'list'">
                <el-icon><List /></el-icon>
              </el-button>
            </el-button-group>
          </div>
        </div>
        
        <div class="file-list" v-if="viewMode === 'list'">
          <el-table :data="mockFiles" style="width: 100%">
            <el-table-column type="selection" width="55" />
            <el-table-column prop="name" label="名称">
              <template #default="{ row }">
                <div class="file-name">
                  <el-icon v-if="row.isDirectory"><Folder /></el-icon>
                  <el-icon v-else><Document /></el-icon>
                  <span>{{ row.name }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="size" label="大小" width="120">
              <template #default="{ row }">
                {{ row.isDirectory ? '-' : formatFileSize(row.size) }}
              </template>
            </el-table-column>
            <el-table-column prop="updatedAt" label="修改时间" width="180">
              <template #default="{ row }">
                {{ formatDate(row.updatedAt) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template #default="{ row }">
                <el-button link type="primary" size="small">下载</el-button>
                <el-button link type="primary" size="small">分享</el-button>
                <el-button link type="danger" size="small">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
        
        <div class="file-grid" v-else>
          <div class="file-item" v-for="file in mockFiles" :key="file.id">
            <div class="file-icon">
              <el-icon v-if="file.isDirectory" size="48"><Folder /></el-icon>
              <el-icon v-else size="48"><Document /></el-icon>
            </div>
            <div class="file-name">{{ file.name }}</div>
          </div>
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'
import { formatFileSize, formatDate } from '../utils'
import type { CloudFile } from '../types'

const router = useRouter()
const userStore = useUserStore()

const viewMode = ref<'grid' | 'list'>('list')

// 模拟文件数据
const mockFiles: CloudFile[] = [
  {
    id: '1',
    name: '文档',
    path: '/documents',
    size: 0,
    type: 'folder',
    mimeType: '',
    isDirectory: true,
    ownerId: 'user-1',
    createdAt: new Date('2024-01-01'),
    updatedAt: new Date('2024-01-01')
  },
  {
    id: '2',
    name: '项目计划.docx',
    path: '/project-plan.docx',
    size: 1024000,
    type: 'document',
    mimeType: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
    isDirectory: false,
    ownerId: 'user-1',
    createdAt: new Date('2024-01-02'),
    updatedAt: new Date('2024-01-02')
  },
  {
    id: '3',
    name: '照片.jpg',
    path: '/photo.jpg',
    size: 2048000,
    type: 'image',
    mimeType: 'image/jpeg',
    isDirectory: false,
    ownerId: 'user-1',
    createdAt: new Date('2024-01-03'),
    updatedAt: new Date('2024-01-03')
  }
]

const handleUserAction = (command: string) => {
  switch (command) {
    case 'logout':
      userStore.logout()
      ElMessage.success('已退出登录')
      router.push('/')
      break
    case 'profile':
      ElMessage.info('个人资料功能开发中')
      break
    case 'settings':
      ElMessage.info('设置功能开发中')
      break
  }
}
</script>

<style scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
  padding: 0 20px;
  border-bottom: 1px solid #e4e7ed;
}

.header h2 {
  margin: 0;
  color: #2c3e50;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid #e4e7ed;
  margin-bottom: 16px;
}

.file-name {
  display: flex;
  align-items: center;
  gap: 8px;
}

.file-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 16px;
}

.file-item {
  text-align: center;
  padding: 16px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.file-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 12px rgba(64, 158, 255, 0.2);
}

.file-icon {
  margin-bottom: 8px;
  color: #909399;
}

.file-item .file-name {
  font-size: 12px;
  color: #606266;
  word-break: break-all;
}
</style>
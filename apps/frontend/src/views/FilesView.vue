<template>
  <div class="files-view">
    <div class="header">
      <div class="main-header">
        <el-button type="primary"><el-icon>
            <Upload />
          </el-icon>上传文件</el-button>
        <el-button><el-icon>
            <FolderAdd />
          </el-icon>新建文件夹</el-button>
      </div>
      <div class="header-actions">
        <el-button-group>
          <el-button :type="viewMode === 'list' ? 'primary' : 'default'" @click="viewMode = 'list'">
            <el-icon>
              <List />
            </el-icon>
          </el-button>
          <el-button :type="viewMode === 'grid' ? 'primary' : 'default'" @click="viewMode = 'grid'">
            <el-icon>
              <Grid />
            </el-icon>
          </el-button>
        </el-button-group>
      </div>
    </div>

    <!-- 路径导航 -->
    <div class="breadcrumb-container">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/main/files' }">我的文件</el-breadcrumb-item>
        <el-breadcrumb-item v-for="(path, index) in currentPath" :key="index" 
          :to="index < currentPath.length - 1 ? { path: generatePath(index) } : undefined">
          {{ path }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 内容区域 -->
    <div class="content-area">
      <!-- List模式 -->
      <el-table v-if="viewMode === 'list'" :data="tableData" stripe style="width: 100%" table-layout="fixed">
        <el-table-column type="selection"/>
        <el-table-column prop="name" label="文件名">
          <template #default="{ row }">
            <div class="file-item">
              <el-icon class="file-icon">
                <Document />
              </el-icon>
              <span>{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="size" label="大小">
          <template #default="{ row }">
            {{ row.size || '--' }}
          </template>
        </el-table-column>
        <el-table-column prop="date" label="修改时间">
          <template #default="{ row }">
            {{ formatDate(row.date) }}
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button link type="primary" size="small" title="分享文件">
                <el-icon><Share /></el-icon>
              </el-button>
              <el-button link type="primary" size="small" title="下载文件">
                <el-icon><Download /></el-icon>
              </el-button>
              <el-dropdown trigger="click">
                <el-button link type="primary" size="small" title="更多操作">
                  <el-icon><MoreFilled /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item>
                      <el-icon><FolderOpened /></el-icon>
                      移动到
                    </el-dropdown-item>
                    <el-dropdown-item>
                      <el-icon><CopyDocument /></el-icon>
                      复制
                    </el-dropdown-item>
                    <el-dropdown-item>
                      <el-icon><Edit /></el-icon>
                      重命名
                    </el-dropdown-item>
                    <el-dropdown-item divided>
                      <el-icon><Delete /></el-icon>
                      删除
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- Grid模式 -->
      <div v-else class="grid-container">
        <div class="file-grid">
          <div v-for="item in tableData" :key="item.name" class="file-card" @click="handleFileClick(item)">
            <div class="file-card-header">
              <el-icon class="file-icon-large">
                <Document />
              </el-icon>
            </div>
            <div class="file-card-content">
              <div class="file-name" :title="item.name">{{ item.name }}</div>
              <div class="file-info">
                <span class="file-size">{{ item.size || '--' }}</span>
                <span class="file-date">{{ formatDate(item.date) }}</span>
              </div>
            </div>
            <div class="file-card-actions">
              <div class="primary-actions">
                <el-button link type="primary" size="small" title="分享文件">
                  <el-icon><Share /></el-icon>
                </el-button>
                <el-button link type="primary" size="small" title="下载文件">
                  <el-icon><Download /></el-icon>
                </el-button>
              </div>
              <el-dropdown trigger="click">
                <el-button text title="更多操作">
                  <el-icon>
                    <MoreFilled />
                  </el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item>
                      <el-icon><FolderOpened /></el-icon>
                      移动到
                    </el-dropdown-item>
                    <el-dropdown-item>
                      <el-icon><CopyDocument /></el-icon>
                      复制
                    </el-dropdown-item>
                    <el-dropdown-item>
                      <el-icon><Edit /></el-icon>
                      重命名
                    </el-dropdown-item>
                    <el-dropdown-item divided>
                      <el-icon><Delete /></el-icon>
                      删除
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </div>
      </div>
    </div>
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

// 当前路径导航
const currentPath = ref<string[]>(['文档', '项目文件', '2024年度'])

// 生成路径的函数
const generatePath = (index: number) => {
  const pathSegments = currentPath.value.slice(0, index + 1)
  return `/main/files/${pathSegments.join('/')}`
}

const tableData = [
  {
    date: '2016-05-03',
    name: 'document1.pdf',
    address: 'No. 189, Grove St, Los Angeles',
    size: '2.3MB'
  },
  {
    date: '2016-05-02',
    name: 'image.jpg',
    address: 'No. 189, Grove St, Los Angeles',
    size: '1.5MB'
  },
  {
    date: '2016-05-04',
    name: 'video.mp4',
    address: 'No. 189, Grove St, Los Angeles',
    size: '15.2MB'
  },
  {
    date: '2016-05-01',
    name: 'spreadsheet.xlsx',
    address: 'No. 189, Grove St, Los Angeles',
    size: '856KB'
  },
]

const handleFileClick = (item: any) => {
  console.log('File clicked:', item)
  // 这里可以添加文件点击的处理逻辑
}
</script>

<style scoped>
.files-view {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #e4e7ed;
  background: #fff;
  flex-shrink: 0;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.breadcrumb-container {
  padding: 12px 16px;
  background: #f8f9fa;
  border-bottom: 1px solid #e4e7ed;
  flex-shrink: 0;
}

.content-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.main-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.file-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.file-icon {
  color: #409eff;
  font-size: 16px;
}

/* List模式样式 */
.el-table {
  flex: 1;
}

.table-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.table-actions .el-button .el-icon {
  color: #677AFA !important;
}

/* Grid模式样式 */
.grid-container {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
}

.file-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}

.file-card {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s;
  background: #fff;
  position: relative;
}

.file-card:hover {
  border-color: #409eff;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.1);
}

.file-card-header {
  text-align: center;
  margin-bottom: 12px;
}

.file-icon-large {
  font-size: 48px;
  color: #409eff;
}

.file-card-content {
  text-align: center;
}

.file-name {
  font-weight: 500;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 12px;
  color: #909399;
}

.file-card-actions {
  position: absolute;
  top: 8px;
  right: 8px;
  opacity: 0;
  transition: opacity 0.3s;
  display: flex;
  align-items: center;
  gap: 2px;
}

.file-card:hover .file-card-actions {
  opacity: 1;
}

.primary-actions {
  display: flex;
  align-items: center;
  gap: 2px;
}

.file-card-actions .el-button {
  width: 24px;
  height: 24px;
  padding: 0;
  min-width: 24px;
}

.file-card-actions .el-button .el-icon {
  color: #677AFA !important;
  font-size: 14px;
}
</style>
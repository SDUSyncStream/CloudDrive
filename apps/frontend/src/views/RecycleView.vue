<template>
  <div class="recycle-view">
    <div class="header">
      <div class="header-right">
        <!-- 无选中文件时显示清空回收站按钮 -->
        <el-button 
          v-if="selectedFiles.length === 0" 
          type="danger" 
          @click="clearRecycleBin"
          :disabled="recycleData.length === 0"
        >
          <el-icon><Delete /></el-icon>
          清空回收站
        </el-button>
        
        <!-- 有选中文件时显示还原和彻底删除按钮 -->
        <template v-else>
          <el-button type="primary" @click="restoreFiles">
            <el-icon><RefreshRight /></el-icon>
            还原 ({{ selectedFiles.length }})
          </el-button>
          <el-button type="danger" @click="permanentlyDelete">
            <el-icon><DeleteFilled /></el-icon>
            彻底删除 ({{ selectedFiles.length }})
          </el-button>
        </template>
      </div>
    </div>

    <!-- 内容区域 -->
    <div class="content-area">
      <el-table 
        :data="recycleData" 
        stripe 
        style="width: 100%" 
        table-layout="fixed"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        
        <el-table-column prop="fileName" label="文件名" min-width="200">
          <template #default="{ row }">
            <div class="file-item">
              <el-icon class="file-icon">
                <Document v-if="getFileType(row.fileName) === 'document'" />
                <Picture v-else-if="getFileType(row.fileName) === 'image'" />
                <VideoPlay v-else-if="getFileType(row.fileName) === 'video'" />
                <Headphone v-else-if="getFileType(row.fileName) === 'audio'" />
                <Document v-else />
              </el-icon>
              <span>{{ row.fileName }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="deleteTime" label="删除时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.deleteTime) }}
          </template>
        </el-table-column>
        
        <el-table-column prop="remainingDays" label="剩余保留天数" width="150">
          <template #default="{ row }">
            <el-tag 
              :type="getRemainingDaysType(row.remainingDays)" 
              size="small"
            >
              {{ row.remainingDays }} 天
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="size" label="大小" width="120">
          <template #default="{ row }">
            {{ formatFileSize(row.size) }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button link type="primary" size="small" title="还原" @click="restoreFile(row)">
                <el-icon><RefreshRight /></el-icon>
              </el-button>
              <el-button link type="danger" size="small" title="彻底删除" @click="permanentlyDeleteFile(row)">
                <el-icon><DeleteFilled /></el-icon>
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 空状态 -->
      <div v-if="recycleData.length === 0" class="empty-state">
        <el-icon class="empty-icon"><Delete /></el-icon>
        <p>回收站为空</p>
        <span>删除的文件将在此处保留30天</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formatDate } from '../utils'

// 响应式数据
const selectedFiles = ref<any[]>([])

// 回收站数据
const recycleData = ref([
  {
    id: 1,
    fileName: 'old-project.zip',
    deleteTime: '2024-01-20 15:30:00',
    remainingDays: 25,
    size: 15728640, // 15MB
    originalPath: '/documents/projects/'
  },
  {
    id: 2,
    fileName: 'temp-notes.txt',
    deleteTime: '2024-01-18 09:15:00',
    remainingDays: 23,
    size: 2048, // 2KB
    originalPath: '/documents/'
  },
  {
    id: 3,
    fileName: 'unused-image.png',
    deleteTime: '2024-01-15 14:45:00',
    remainingDays: 20,
    size: 5242880, // 5MB
    originalPath: '/images/'
  },
  {
    id: 4,
    fileName: 'backup-2023.sql',
    deleteTime: '2024-01-10 11:20:00',
    remainingDays: 15,
    size: 104857600, // 100MB
    originalPath: '/backups/'
  },
  {
    id: 5,
    fileName: 'deprecated-code.js',
    deleteTime: '2024-01-05 16:30:00',
    remainingDays: 10,
    size: 8192, // 8KB
    originalPath: '/projects/src/'
  },
  {
    id: 6,
    fileName: 'old-presentation.pptx',
    deleteTime: '2024-01-02 10:15:00',
    remainingDays: 7,
    size: 25165824, // 24MB
    originalPath: '/documents/presentations/'
  },
  {
    id: 7,
    fileName: 'expired-certificate.pdf',
    deleteTime: '2023-12-28 14:20:00',
    remainingDays: 3,
    size: 1048576, // 1MB
    originalPath: '/documents/certificates/'
  }
])

// 获取文件类型
const getFileType = (fileName: string) => {
  const ext = fileName.split('.').pop()?.toLowerCase()
  
  const imageExts = ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'svg', 'webp']
  const videoExts = ['mp4', 'avi', 'mov', 'wmv', 'flv', 'mkv']
  const audioExts = ['mp3', 'wav', 'flac', 'aac', 'ogg']
  
  if (imageExts.includes(ext || '')) return 'image'
  if (videoExts.includes(ext || '')) return 'video'
  if (audioExts.includes(ext || '')) return 'audio'
  return 'document'
}

// 获取剩余天数标签类型
const getRemainingDaysType = (days: number) => {
  if (days <= 3) return 'danger'
  if (days <= 7) return 'warning'
  return 'success'
}

// 格式化文件大小
const formatFileSize = (bytes: number) => {
  if (bytes === 0) return '0 B'
  
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// 处理选择变化
const handleSelectionChange = (selection: any[]) => {
  selectedFiles.value = selection
}

// 还原单个文件
const restoreFile = async (file: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要还原文件 "${file.fileName}" 到原位置吗？`,
      '确认还原',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info',
      }
    )
    
    // 从回收站移除
    const index = recycleData.value.findIndex(f => f.id === file.id)
    if (index !== -1) {
      recycleData.value.splice(index, 1)
    }
    
    ElMessage.success(`文件 "${file.fileName}" 已还原到 ${file.originalPath}`)
  } catch {
    ElMessage.info('已取消操作')
  }
}

// 批量还原文件
const restoreFiles = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要还原选中的 ${selectedFiles.value.length} 个文件吗？`,
      '确认还原',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info',
      }
    )
    
    const fileNames = selectedFiles.value.map(f => f.fileName)
    
    // 从回收站移除选中的文件
    selectedFiles.value.forEach(file => {
      const index = recycleData.value.findIndex(f => f.id === file.id)
      if (index !== -1) {
        recycleData.value.splice(index, 1)
      }
    })
    
    selectedFiles.value = []
    ElMessage.success(`已还原 ${fileNames.length} 个文件`)
  } catch {
    ElMessage.info('已取消操作')
  }
}

// 彻底删除单个文件
const permanentlyDeleteFile = async (file: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要彻底删除文件 "${file.fileName}" 吗？此操作不可撤销！`,
      '确认彻底删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    // 从回收站移除
    const index = recycleData.value.findIndex(f => f.id === file.id)
    if (index !== -1) {
      recycleData.value.splice(index, 1)
    }
    
    ElMessage.success(`文件 "${file.fileName}" 已彻底删除`)
  } catch {
    ElMessage.info('已取消操作')
  }
}

// 批量彻底删除文件
const permanentlyDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要彻底删除选中的 ${selectedFiles.value.length} 个文件吗？此操作不可撤销！`,
      '确认彻底删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    const fileNames = selectedFiles.value.map(f => f.fileName)
    
    // 从回收站移除选中的文件
    selectedFiles.value.forEach(file => {
      const index = recycleData.value.findIndex(f => f.id === file.id)
      if (index !== -1) {
        recycleData.value.splice(index, 1)
      }
    })
    
    selectedFiles.value = []
    ElMessage.success(`已彻底删除 ${fileNames.length} 个文件`)
  } catch {
    ElMessage.info('已取消操作')
  }
}

// 清空回收站
const clearRecycleBin = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要清空回收站吗？这将彻底删除所有文件，此操作不可撤销！`,
      '确认清空回收站',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    const fileCount = recycleData.value.length
    recycleData.value = []
    selectedFiles.value = []
    
    ElMessage.success(`已清空回收站，彻底删除了 ${fileCount} 个文件`)
  } catch {
    ElMessage.info('已取消操作')
  }
}
</script>

<style scoped>
.recycle-view {
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

.header-left h2 {
  margin: 0;
  color: #303133;
  font-size: 20px;
  font-weight: 600;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.content-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 表格样式 */
.el-table {
  flex: 1;
  margin: 16px;
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

.table-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.table-actions .el-button .el-icon {
  color: #677AFA !important;
}

/* 空状态样式 */
.empty-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
  text-align: center;
  padding: 40px;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
  color: #c0c4cc;
}

.empty-state p {
  font-size: 16px;
  font-weight: 500;
  margin: 0 0 8px 0;
  color: #606266;
}

.empty-state span {
  font-size: 14px;
  color: #909399;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }
  
  .header-right {
    justify-content: center;
  }
  
  .el-table {
    margin: 8px;
  }
}
</style>

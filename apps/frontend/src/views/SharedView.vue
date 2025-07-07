<template>
  <div class="shared-files-view">
    <div class="header">
      <div class="header-left">
        <el-button type="danger" @click="cleanInvalidLinks">
          <el-icon><Delete /></el-icon>
          清理失效链接
        </el-button>
      </div>
      <div class="header-right">
        <el-button-group>
          <el-button 
            :type="viewMode === 'list' ? 'primary' : 'default'"
            @click="viewMode = 'list'"
            title="列表视图"
          >
            <el-icon><List /></el-icon>
          </el-button>
          <el-button 
            :type="viewMode === 'grid' ? 'primary' : 'default'"
            @click="viewMode = 'grid'"
            title="网格视图"
          >
            <el-icon><Grid /></el-icon>
          </el-button>
        </el-button-group>
      </div>
    </div>

    <!-- 内容区域 -->
    <div class="content-area">
      <!-- List模式 -->
      <el-table v-if="viewMode === 'list'" :data="sharedFilesData" stripe style="width: 100%" table-layout="fixed">
        <el-table-column prop="fileName" label="文件名">
          <template #default="{ row }">
            <div class="file-item">
              <el-icon class="file-icon">
                <Document />
              </el-icon>
              <span>{{ row.fileName }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="shareCode" label="提取码" >
          <template #default="{ row }">
            <div class="share-code">
              <span>{{ row.shareCode }}</span>
              <el-button 
                link 
                type="primary" 
                size="small" 
                @click="copyShareCode(row.shareCode)"
                title="复制提取码"
              >
                <el-icon><CopyDocument /></el-icon>
              </el-button>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="shareTime" label="分享时间">
          <template #default="{ row }">
            {{ formatDate(row.shareTime) }}
          </template>
        </el-table-column>
        
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag 
              :type="getStatusType(row.status)" 
              size="small"
            >
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="操作">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button link type="primary" size="small" title="复制链接" @click="copyShareLink(row)">
                <el-icon><Link /></el-icon>
              </el-button>
              <el-button link type="primary" size="small" title="查看详情" @click="viewDetails(row)">
                <el-icon><View /></el-icon>
              </el-button>
              <el-button link type="danger" size="small" title="取消分享" @click="cancelShare(row)">
                <el-icon><Close /></el-icon>
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- Grid模式 -->
      <div v-else class="grid-container">
        <div class="file-grid">
          <div v-for="item in sharedFilesData" :key="item.id" class="file-card" @click="viewDetails(item)">
            <div class="file-card-header">
              <el-icon class="file-icon-large">
                <Document />
              </el-icon>
            </div>
            <div class="file-card-content">
              <div class="file-name" :title="item.fileName">{{ item.fileName }}</div>
              <div class="status-row">
                <el-tag :type="getStatusType(item.status)" size="small">
                  {{ getStatusText(item.status) }}
                </el-tag>
              </div>
              <div class="file-info">
                <div class="info-row">
                  <span class="info-label">提取码:</span>
                  <span class="share-code-text">{{ item.shareCode }}</span>
                </div>
                <div class="info-row">
                  <span class="info-label">分享时间:</span>
                  <span class="share-time">{{ formatDate(item.shareTime) }}</span>
                </div>
                <div class="info-row">
                  <span class="info-label">下载次数:</span>
                  <span class="download-count">{{ item.downloadCount }}</span>
                </div>
              </div>
            </div>
            <div class="file-card-actions">
              <el-button link type="primary" size="small" title="复制链接" @click.stop="copyShareLink(item)">
                <el-icon><Link /></el-icon>
              </el-button>
              <el-button link type="primary" size="small" title="复制提取码" @click.stop="copyShareCode(item.shareCode)">
                <el-icon><CopyDocument /></el-icon>
              </el-button>
              <el-button link type="primary" size="small" title="查看详情" @click.stop="viewDetails(item)">
                <el-icon><View /></el-icon>
              </el-button>
              <el-button link type="danger" size="small" title="取消分享" @click.stop="cancelShare(item)">
                <el-icon><Close /></el-icon>
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 详情对话框 -->
    <el-dialog 
      v-model="detailDialogVisible" 
      title="分享详情" 
      width="500px"
    >
      <div v-if="selectedFile" class="share-details">
        <div class="detail-item">
          <label>文件名：</label>
          <span>{{ selectedFile.fileName }}</span>
        </div>
        <div class="detail-item">
          <label>分享链接：</label>
          <div class="link-container">
            <span class="share-link">{{ generateShareLink(selectedFile) }}</span>
            <el-button size="small" @click="copyShareLink(selectedFile)">复制</el-button>
          </div>
        </div>
        <div class="detail-item">
          <label>提取码：</label>
          <span>{{ selectedFile.shareCode }}</span>
        </div>
        <div class="detail-item">
          <label>分享时间：</label>
          <span>{{ formatDate(selectedFile.shareTime) }}</span>
        </div>
        <div class="detail-item">
          <label>过期时间：</label>
          <span>{{ formatDate(selectedFile.expireTime) }}</span>
        </div>
        <div class="detail-item">
          <label>下载次数：</label>
          <span>{{ selectedFile.downloadCount }}</span>
        </div>
        <div class="detail-item">
          <label>状态：</label>
          <el-tag :type="getStatusType(selectedFile.status)" size="small">
            {{ getStatusText(selectedFile.status) }}
          </el-tag>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formatDate } from '../utils'

// 响应式数据
const detailDialogVisible = ref(false)
const selectedFile = ref<any>(null)
const viewMode = ref<'grid' | 'list'>('list')

// 共享文件数据
const sharedFilesData = ref([
  {
    id: 1,
    fileName: 'project-report.pdf',
    shareCode: 'abc123',
    shareTime: '2024-01-15 14:30:00',
    expireTime: '2024-02-15 14:30:00',
    status: 'active',
    downloadCount: 5,
    shareLink: 'https://clouddrive.com/share/abc123'
  },
  {
    id: 2,
    fileName: 'meeting-notes.docx',
    shareCode: 'def456',
    shareTime: '2024-01-10 09:15:00',
    expireTime: '2024-01-20 09:15:00',
    status: 'expired',
    downloadCount: 2,
    shareLink: 'https://clouddrive.com/share/def456'
  },
  {
    id: 3,
    fileName: 'design-mockup.sketch',
    shareCode: 'ghi789',
    shareTime: '2024-01-20 16:45:00',
    expireTime: '2024-02-20 16:45:00',
    status: 'active',
    downloadCount: 8,
    shareLink: 'https://clouddrive.com/share/ghi789'
  },
  {
    id: 4,
    fileName: 'database-backup.sql',
    shareCode: 'jkl012',
    shareTime: '2024-01-05 11:20:00',
    expireTime: '2024-01-15 11:20:00',
    status: 'cancelled',
    downloadCount: 0,
    shareLink: 'https://clouddrive.com/share/jkl012'
  }
])

// 获取状态类型
const getStatusType = (status: string) => {
  switch (status) {
    case 'active':
      return 'success'
    case 'expired':
      return 'warning'
    case 'cancelled':
      return 'info'
    default:
      return 'info'
  }
}

// 获取状态文本
const getStatusText = (status: string) => {
  switch (status) {
    case 'active':
      return '有效'
    case 'expired':
      return '已过期'
    case 'cancelled':
      return '已取消'
    default:
      return '未知'
  }
}

// 复制提取码
const copyShareCode = async (shareCode: string) => {
  try {
    await navigator.clipboard.writeText(shareCode)
    ElMessage.success('提取码已复制到剪贴板')
  } catch (err) {
    ElMessage.error('复制失败')
  }
}

// 生成分享链接
const generateShareLink = (file: any) => {
  return `https://clouddrive.com/share/${file.shareCode}`
}

// 复制分享链接
const copyShareLink = async (file: any) => {
  try {
    const link = generateShareLink(file)
    await navigator.clipboard.writeText(link)
    ElMessage.success('分享链接已复制到剪贴板')
  } catch (err) {
    ElMessage.error('复制失败')
  }
}

// 查看详情
const viewDetails = (file: any) => {
  selectedFile.value = file
  detailDialogVisible.value = true
}

// 取消分享
const cancelShare = async (file: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要取消分享文件 "${file.fileName}" 吗？`,
      '确认取消',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    // 更新文件状态
    const fileIndex = sharedFilesData.value.findIndex(f => f.id === file.id)
    if (fileIndex !== -1) {
      sharedFilesData.value[fileIndex].status = 'cancelled'
    }
    
    ElMessage.success('已取消分享')
  } catch {
    ElMessage.info('已取消操作')
  }
}

// 清理失效链接
const cleanInvalidLinks = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要清理所有失效的分享链接吗？此操作不可撤销。',
      '确认清理',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    const beforeCount = sharedFilesData.value.length
    sharedFilesData.value = sharedFilesData.value.filter(file => 
      file.status !== 'expired' && file.status !== 'cancelled'
    )
    const afterCount = sharedFilesData.value.length
    const removedCount = beforeCount - afterCount
    
    ElMessage.success(`已清理 ${removedCount} 个失效链接`)
  } catch {
    ElMessage.info('已取消操作')
  }
}
</script>

<style scoped>
.shared-files-view {
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

.header-left {
  display: flex;
  align-items: center;
}

.header-right {
  display: flex;
  align-items: center;
}

.main-header h2 {
  margin: 0;
  color: #303133;
  font-size: 20px;
  font-weight: 600;
}

.content-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* List模式样式 */
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

.share-code {
  display: flex;
  align-items: center;
  gap: 8px;
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
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
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
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12px;
}

.file-icon-large {
  font-size: 32px;
  color: #409eff;
}

.file-card-content {
  margin-bottom: 12px;
}

.file-name {
  font-weight: 500;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 14px;
}

.status-row {
  margin-bottom: 8px;
  display: flex;
  justify-content: center;
}

.file-info {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 12px;
  color: #909399;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.info-label {
  font-weight: 500;
  min-width: 60px;
}

.share-code-text {
  font-family: monospace;
  background: #f5f7fa;
  padding: 2px 6px;
  border-radius: 3px;
  font-size: 11px;
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

/* 详情对话框样式 */
.share-details {
  padding: 20px 0;
}

.detail-item {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
  min-height: 32px;
}

.detail-item label {
  width: 100px;
  font-weight: 500;
  color: #606266;
  flex-shrink: 0;
}

.detail-item span {
  color: #303133;
}

.link-container {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.share-link {
  flex: 1;
  padding: 8px 12px;
  background: #f5f7fa;
  border-radius: 4px;
  font-family: monospace;
  font-size: 12px;
  color: #606266;
  word-break: break-all;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
</style>

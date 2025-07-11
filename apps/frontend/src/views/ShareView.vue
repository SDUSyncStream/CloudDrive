<template>
  <div class="share">
    <div class="header">
      <div class="header-content">
        <div class="logo" @click="jumpHome">
          <el-icon class="logo-icon"><FolderOpened /></el-icon>
          <span class="name">CloudDrive</span>
        </div>
      </div>
    </div>
    <div class="share-body">
      <el-card class="share-panel">
        <div class="share-user-info">
          <el-avatar :src="shareInfo.avatar" :size="50" class="avatar" />
          <div class="share-info">
            <div class="user-info">
              <span class="nick-name">{{ shareInfo.nickName || shareInfo.username }}</span>
              <span class="share-time">分享于 {{ formatDate(shareInfo.shareTime) }}</span>
            </div>
            <div class="file-name">分享文件：{{ shareInfo.fileName }}</div>
          </div>
        </div>
        <div class="share-op-btn">
          <el-button v-if="shareInfo.currentUser" type="primary" @click="cancelShare">取消分享</el-button>
        </div>
      </el-card>
      <div class="table-action-bar">
        <el-button
          v-if="filePidStack.length > 1"
          type="default"
          @click="goBack"
          class="back-btn"
        >
          <el-icon><ArrowLeft /></el-icon>返回上一级
        </el-button>
        <el-button
          v-if="userId !== String(shareInfo.userId)"
          type="primary"
          :disabled="selected.length===0"
          @click="saveToDriveBatch"
        >
          <el-icon><Folder /></el-icon>保存到我的网盘
        </el-button>
      </div>
      <el-table
        :data="fileList"
        style="width: 100%; margin-top: 8px;"
        @selection-change="handleSelectionChange"
        row-key="fileId"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="fileName" label="文件名">
          <template #default="{ row }">
            <span v-if="row.folderType === 1" class="folder-link" @click="enterFolder(row)">
              <el-icon><Folder /></el-icon>
              <span class="file-link">{{ row.fileName }}</span>
            </span>
            <span v-else class="file-link" @click="previewFile(row)">
              <el-icon><Document /></el-icon>
              {{ row.fileName }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="lastUpdateTime" label="上传时间" />
        <el-table-column prop="fileSize" label="大小" :formatter="formatSize" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="downloadFile(row)">
              <el-icon><Download /></el-icon>下载
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <!-- 目录选择弹窗 -->
      <el-dialog v-model="showFolderSelect" title="选择保存目录" width="400px" @close="onFolderDialogClose">
        <el-tree
          :data="folderTree"
          :props="{ label: 'name', children: 'children' }"
          node-key="id"
          highlight-current
          @node-click="onFolderNodeClick"
          :expand-on-click-node="false"
          :default-expanded-keys="[folderTree[0]?.id]"
        />
        <template #footer>
          <el-button @click="showFolderSelect = false">取消</el-button>
          <el-button type="primary" :disabled="!selectedFolderId" @click="onFolderSelect">确定</el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { FolderOpened, User, Download, Folder, Document, ArrowLeft } from '@element-plus/icons-vue'

const downloadingFiles = ref<Set<string>>(new Set());

const router = useRouter()
const route = useRoute()
const shareId = route.params.shareId as string

const userId = localStorage.getItem('UserId') || localStorage.getItem('userId')

const shareInfo = ref<any>({})
const fileList = ref<any[]>([])
const selected = ref<any[]>([])
const loading = ref(true)
const filePidStack = ref(['0'])

// 目录选择弹窗相关
const showFolderSelect = ref(false)
const folderTree = ref<any[]>([])
const selectedFolderId = ref('')
const saveFileIds = ref<string[]>([])

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  if (/^\d+$/.test(dateStr)) {
    const d = new Date(Number(dateStr))
    return d.toLocaleString()
  }
  return dateStr.replace('T', ' ').slice(0, 19)
}

const getShareInfo = async () => {
  loading.value = true
  try {
    const res = await fetch(`/api/showShare/getShareLoginInfo?shareId=${shareId}`).then(r => r.json())
    if (res && res.data) {
      shareInfo.value = res.data
      await loadFileList()
    } else {
      router.replace(`/shareCheck/${shareId}`)
    }
  } finally {
    loading.value = false
  }
}

const loadFileList = async () => {
  const curPid = filePidStack.value[filePidStack.value.length - 1]
  const res = await fetch(`/api/showShare/loadFileList?shareId=${shareId}&filePid=${curPid}`).then(r => r.json())
  if (res && res.data && res.data.list) {
    fileList.value = res.data.list.map((item: any) => ({ ...item, showOp: false }))
  } else {
    fileList.value = []
  }
}

onMounted(getShareInfo)

const handleSelectionChange = (val: any[]) => {
  selected.value = val
}

const enterFolder = (row: any) => {
  filePidStack.value.push(row.fileId)
  loadFileList()
}

const goBack = () => {
  if (filePidStack.value.length > 1) {
    filePidStack.value.pop()
    loadFileList()
  }
}

const previewFile = (row: any) => {
  ElMessage.info(`预览：${row.fileName}`)
  // 可扩展为弹窗预览
}

const downloadFile = async (row: any) => {
  // 如果是文件夹，提示不能下载
  if (row.folderType === 1) {
    ElMessage.warning('文件夹不支持下载');
    return;
  }

  // 检查是否正在下载
  if (downloadingFiles?.value?.has && downloadingFiles.value.has(row.fileId)) {
    ElMessage.info('文件正在下载中，请稍候');
    return;
  }

  try {
    // 标记为下载中
    if (downloadingFiles?.value?.add) downloadingFiles.value.add(row.fileId);
    ElMessage.info(`开始下载: ${row.fileName}`);

    // 1. 创建下载链接获取code
    const userId = shareInfo.value.userId; // 用分享者 userId
    const createUrlRes = await fetch(`/fileup/createDownloadUrl/${row.fileId}?userId=${userId}`)
      .then(r => r.json());
    if (createUrlRes.code !== 200) {
      throw new Error(createUrlRes.message || '获取下载链接失败');
    }
    const downloadcode = createUrlRes.data;
    if (downloadcode == null || downloadcode === undefined) {
      throw new Error('获取下载链接失败');
    }

    // 2. 使用code下载文件
    const downloadRes = await fetch(`/fileup/download/${downloadcode}`);
    const blob = await downloadRes.blob();
    // 创建下载链接
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', row.fileName);
    document.body.appendChild(link);
    link.click();
    // 清理
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
    ElMessage.success(`下载完成: ${row.fileName}`);
  } catch (error: any) {
    console.error('下载失败:', error);
    ElMessage.error(`下载失败: ${error.message || '未知错误'}`);
  } finally {
    if (downloadingFiles?.value?.delete) downloadingFiles.value.delete(row.fileId);
  }
}

// 目录选择弹窗相关逻辑
const saveToDriveBatch = async () => {
  const token = localStorage.getItem('accessToken')
  const userId = localStorage.getItem('UserId') || localStorage.getItem('userId')
  
  console.log('Token:', token)
  console.log('UserId:', userId)
  
  if (!token) {
    ElMessage.warning('请先登录后再保存文件到网盘')
    router.push('/login?redirectUrl=' + route.fullPath)
    return
  }
  
  if (!userId) {
    ElMessage.warning('用户ID不存在，请重新登录')
    router.push('/login?redirectUrl=' + route.fullPath)
    return
  }
  
  if (selected.value.length === 0) return
  saveFileIds.value = selected.value.map(f => f.fileId)
  await loadFolderTree()
  showFolderSelect.value = true
}

const loadFolderTree = async () => {
  const userId = localStorage.getItem('UserId') || localStorage.getItem('userId')
  try {
    // 使用与 FilesView 相同的接口获取根目录文件列表
    const res = await fetch(`/files/get/0?userId=${userId}&delFlag=2`, {
      headers: { Authorization: localStorage.getItem('accessToken') || '' }
    }).then(r => r.json())
    
    if (res && res.data) {
      // 过滤出文件夹类型的数据
      const folders = res.data.filter((item: any) => item.folderType === 1)
      // 转换为树形结构，添加根目录
      folderTree.value = [
        {
          id: '0',
          name: '我的文件',
          children: folders.map((folder: any) => ({
            id: folder.fileId,
            name: folder.fileName,
            children: []
          }))
        }
      ]
      selectedFolderId.value = '0'
    } else {
      folderTree.value = [{ id: '0', name: '我的文件', children: [] }]
      selectedFolderId.value = '0'
    }
  } catch (error) {
    console.error('获取文件夹列表失败:', error)
    ElMessage.error('获取文件夹列表失败')
    folderTree.value = [{ id: '0', name: '我的文件', children: [] }]
    selectedFolderId.value = '0'
  }
}

const onFolderNodeClick = (data: any) => {
  selectedFolderId.value = data.id
}

const onFolderSelect = async () => {
  showFolderSelect.value = false
  if (!selectedFolderId.value) return
  const userId = localStorage.getItem('UserId') || localStorage.getItem('userId')
  const token = localStorage.getItem('accessToken')

  // 新增：禁止保存到自己的网盘根目录
  if (userId === String(shareInfo.value.userId) && selectedFolderId.value === '0') {
    ElMessage.error('不能保存自己的文件')
    return
  }

  const params = new URLSearchParams({
    shareId,
    shareFileIds: saveFileIds.value.join(','),
    myFolderId: selectedFolderId.value,
    userId
  })
  try {
    const res = await fetch(`/api/showShare/saveShare?${params.toString()}`, {
      method: 'GET',
      headers: { Authorization: token || '' }
    }).then(r => r.json())
    if (res && (res.code === 0 || res.code === 200)) {
      ElMessage.success('保存成功')
    } else if (res && res.code === 401) {
      ElMessage.error('登录已过期，请重新登录')
      router.push('/login?redirectUrl=' + route.fullPath)
    } else {
      ElMessage.error(res?.info || res?.message || '保存失败')
    }
  } catch (error) {
    console.error('保存文件失败:', error)
    ElMessage.error('保存失败，请重试')
  }
}
const onFolderDialogClose = () => {
  selectedFolderId.value = ''
}

const cancelShare = () => {
  ElMessage.success('取消分享成功')
  router.push('/')
}

const jumpHome = () => {
  router.push('/')
}

const formatSize = (row: any, column: any, cellValue: any) => {
  if (!cellValue) return '-'
  const size = Number(cellValue)
  if (size < 1024) return size + ' B'
  if (size < 1024 * 1024) return (size / 1024).toFixed(2) + ' KB'
  if (size < 1024 * 1024 * 1024) return (size / 1024 / 1024).toFixed(2) + ' MB'
  return (size / 1024 / 1024 / 1024).toFixed(2) + ' GB'
}
</script>

<style scoped>
.share {
  min-height: 100vh;
  background: #f5f7fa;
}
.header {
  width: 100%;
  background: #0c95f7;
  height: 50px;
  .header-content {
    width: 70%;
    margin: 0px auto;
    color: #fff;
    line-height: 50px;
    .logo {
      display: flex;
      align-items: center;
      cursor: pointer;
      .logo-icon {
        font-size: 32px;
        color: #fff;
        margin-right: 8px;
      }
      .name {
        font-weight: bold;
        margin-left: 5px;
        font-size: 25px;
        color: #fff;
      }
    }
  }
}
.share-body {
  width: 70%;
  margin: 0px auto;
  padding-top: 50px;
}
.share-panel {
  margin-top: 20px;
  display: flex;
  justify-content: space-between;
  border-bottom: 1px solid #ddd;
  padding-bottom: 10px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}
.share-user-info {
  display: flex;
  align-items: center;
}
.avatar {
  margin-right: 12px;
}
.share-info .user-info {
  display: flex;
  align-items: center;
}
.nick-name {
  font-size: 15px;
  font-weight: 700;
}
.share-time {
  margin-left: 20px;
  font-size: 12px;
  color: #909399;
}
.file-name {
  margin-top: 10px;
  font-size: 12px;
  color: #606266;
}
.share-op-btn {
  display: flex;
  align-items: center;
  gap: 10px;
}
.table-action-bar {
  margin: 18px 0 0 0;
  display: flex;
  align-items: center;
  gap: 10px;
}
.back-btn {
  margin-right: 10px;
}
.folder-link, .file-link {
  cursor: pointer;
  color: #409eff;
  margin-left: 4px;
}
.folder-link:hover, .file-link:hover {
  text-decoration: underline;
}
.el-table .el-button {
  font-size: 14px;
  padding: 0 6px;
}
</style>
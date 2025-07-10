<template>
  <div class="admin-files-view">
    <h2>文件管理</h2>

    <el-card shadow="hover" class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="文件名">
          <el-input v-model="searchForm.fileName" placeholder="输入文件名" clearable></el-input>
        </el-form-item>
        <el-form-item label="用户ID/用户名">
          <el-input v-model="searchForm.userIdOrName" placeholder="输入用户ID或用户名" clearable></el-input>
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="searchForm.folderType" placeholder="文件/目录" clearable>
            <el-option label="所有" value=""></el-option>
            <el-option label="文件" :value="0"></el-option>
            <el-option label="目录" :value="1"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.delFlag" placeholder="选择文件状态" clearable>
            <el-option label="所有（不含已删除）" value=""></el-option>
            <el-option label="正常" :value="2"></el-option>
            <el-option label="回收站" :value="1"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchFiles">
            <el-icon><Search /></el-icon> 搜索
          </el-button>
          <el-button @click="resetSearch">
            <el-icon><Refresh /></el-icon> 重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="hover" class="table-card">
      <el-table
          :data="paginatedFiles"
          v-loading="loading"
          style="width: 100%"
          stripe
          border
          height="calc(100vh - 300px)" empty-text="暂无文件数据"
      >
        <el-table-column prop="file_name" label="文件名" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="file-info-cell">
              <el-icon :class="getFileIcon(row.folder_type)">
                <Folder v-if="row.folder_type === 1" />
                <Document v-else />
              </el-icon>
              <span>{{ row.file_name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="user_id" label="所属用户" width="120" show-overflow-tooltip>
          <template #default="{ row }">
            {{ getUserNameById(row.user_id) || row.user_id }}
          </template>
        </el-table-column>
        <el-table-column prop="file_size" label="大小" width="100">
          <template #default="{ row }">
            {{ row.folder_type === 1 ? '--' : formatFileSize(row.file_size) }}
          </template>
        </el-table-column>
        <el-table-column prop="folder_type" label="类型" width="100" :formatter="formatFolderType"></el-table-column>
        <el-table-column prop="file_category" label="分类" width="100" :formatter="formatFileCategory"></el-table-column>
        <el-table-column prop="del_flag" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getFileStatusTagType(row.del_flag)">{{ formatDelFlag(row.del_flag) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="create_time" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.create_time) }}
          </template>
        </el-table-column>
        <el-table-column prop="last_update_time" label="最后更新" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.last_update_time) }}
          </template>
        </el-table-column>
        <el-table-column prop="recovery_time" label="回收站时间" width="180">
          <template #default="{ row }">
            {{ row.del_flag === 1 ? formatDateTime(row.recovery_time) : '--' }}
          </template>
        </el-table-column>

        <el-table-column label="操作" fixed="right" width="180">
          <template #default="{ row }">
            <el-button
                link
                type="primary"
                size="small"
                v-if="row.del_flag === 1" @click="recoverFile(row)"
            >恢复</el-button>
            <el-button
                link
                type="warning"
                size="small"
                v-if="row.del_flag === 2" @click="deleteFile(row, 1)"
            >删除到回收站</el-button>
            <el-button
                link
                type="danger"
                size="small"
                v-if="row.del_flag === 1" @click="deleteFile(row, 0)"
            >永久删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="filteredFiles.length"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          background
          class="pagination-container"
      ></el-pagination>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus' // 移除 FormInstance 等不再需要的类型
import {
  Search,
  Refresh,
  Folder,       // 文件夹图标
  Document,     // 文档图标
} from '@element-plus/icons-vue' // 移除 Delete, DeleteFilled, RefreshRight 等不再需要的图标

// 假设你的 utils 文件中有这些格式化函数
import { formatFileSize } from '../utils'

// 用于格式化日期时间
const formatDateTime = (datetime: string | Date | null) => {
  if (!datetime) return '--'
  const date = new Date(datetime)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// --- 数据定义 ---

// 文件接口，匹配数据库结构
interface FileInfo {
  file_id: string;
  user_id: string;
  file_md5: string;
  file_pid: string | null;
  file_size: number;
  file_name: string;
  file_cover: string | null;
  file_path: string | null;
  create_time: string;
  last_update_time: string;
  folder_type: 0 | 1; // 0文件1目录
  file_category: 1 | 2 | 3 | 4 | 5 | null; // 1视频2音频3图片4文档5其他
  file_type: number | null; // 1视频2音频3图片4pdf5doc6excel7txt8code9zip10其他
  status: 0 | 1 | 2 | null; // 0转码中1转码失败2转码成功
  recovery_time: string | null; // 进入回收站时间
  del_flag: 0 | 1 | 2; // 0删除1回收站2正常
}

// 搜索表单
const searchForm = reactive({
  fileName: '',
  userIdOrName: '',
  folderType: '', // 0: 文件, 1: 目录, '': 所有
  delFlag: '',    // 1: 回收站, 2: 正常, '': 所有 (不含已删除)
})

const fileList = ref<FileInfo[]>([]) // 所有文件数据
const loading = ref(false) // 表格加载状态
// const selectedFiles = ref<FileInfo[]>([]) // 批量操作移除，故此变量不再需要

// 分页
const currentPage = ref(1)
const pageSize = ref(10)

// 模拟的用户ID到用户名的映射 (实际项目中可能从用户管理获取或独立API获取)
const userMap = ref<{ [key: string]: string }>({
  'user001': 'Ono Kasumi',
  'user002': 'Eleanor Henders',
  'user003': 'Saito Ikki',
  'admin-user-id': 'admin',
  '1000000001': 'Wang Li',
  '1000000002': 'Zhang San',
  '1000000003': 'super_admin',
});

// --- 计算属性 ---

// 根据搜索条件过滤文件 (不再包含 del_flag=0 的文件)
const filteredFiles = computed(() => {
  return fileList.value.filter(file => {
    // 排除 del_flag = 0 (已删除) 的文件
    if (file.del_flag === 0) return false;

    const matchesFileName = file.file_name?.includes(searchForm.fileName)

    const matchesUserIdOrName = !searchForm.userIdOrName ||
        file.user_id?.includes(searchForm.userIdOrName) ||
        (userMap.value[file.user_id]?.includes(searchForm.userIdOrName));

    const matchesFolderType = searchForm.folderType === '' || file.folder_type === searchForm.folderType

    // 如果 searchForm.delFlag 为空，则显示所有（正常和回收站）。否则按指定状态筛选。
    const matchesDelFlag = searchForm.delFlag === '' || file.del_flag === searchForm.delFlag

    return matchesFileName && matchesUserIdOrName && matchesFolderType && matchesDelFlag
  })
})

// 分页后的文件数据
const paginatedFiles = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredFiles.value.slice(start, end)
})

// --- 方法 ---

// 获取文件图标 (简单判断文件/目录)
const getFileIcon = (folderType: number) => {
  return folderType === 1 ? 'folder-icon' : 'document-icon';
}

// 格式化文件夹类型
const formatFolderType = (row: FileInfo) => {
  return row.folder_type === 1 ? '目录' : '文件'
}

// 格式化文件分类
const formatFileCategory = (row: FileInfo) => {
  const categories: { [key: number]: string } = {
    1: '视频', 2: '音频', 3: '图片', 4: '文档', 5: '其他'
  }
  return categories[row.file_category || 5] || '其他'
}

// 格式化删除标志 (这里只显示 1 和 2 的状态)
const formatDelFlag = (delFlag: number) => {
  switch (delFlag) {
    case 1: return '回收站'
    case 2: return '正常'
    default: return '未知' // del_flag=0 的数据不再显示
  }
}

// 获取文件状态标签类型
const getFileStatusTagType = (delFlag: number) => {
  switch (delFlag) {
    case 1: return 'warning' // 回收站
    case 2: return 'success' // 正常
    default: return 'info'
  }
}

// 根据用户ID获取用户名
const getUserNameById = (userId: string) => {
  return userMap.value[userId] || '未知用户';
}

// 搜索文件
const searchFiles = () => {
  currentPage.value = 1 // 搜索时重置回第一页
  fetchFiles() // 实际项目中会调用 API 重新获取数据
}

// 重置搜索条件
const resetSearch = () => {
  searchForm.fileName = ''
  searchForm.userIdOrName = ''
  searchForm.folderType = ''
  searchForm.delFlag = ''
  searchFiles() // 重置后重新搜索
}

// 恢复文件 (从回收站恢复到正常)
const recoverFile = async (file: FileInfo) => {
  if (file.del_flag === 2) {
    ElMessage.info('文件已是正常状态，无需恢复。')
    return;
  }
  ElMessageBox.confirm(
      `确定要恢复文件 "${file.file_name}" 吗？`,
      '提示',
      {
        confirmButtonText: '确定恢复',
        cancelButtonText: '取消',
        type: 'info',
      }
  )
      .then(async () => {
        try {
          // 模拟 API 请求：将 del_flag 设置为 2 (正常)
          console.log(`Sending recovery request for file: ${file.file_id}`);
          // 实际 API: await fetch(`/api/admin/files/recover/${file.file_id}`, { method: 'PUT', headers: { Authorization: 'Bearer ...' } });

          await new Promise(resolve => setTimeout(resolve, 500)); // 模拟延迟

          ElMessage.success(`文件 "${file.file_name}" 已恢复。`)
          fetchFiles() // 刷新列表
        } catch (error) {
          console.error('恢复文件错误:', error)
          ElMessage.error('文件恢复失败。')
        }
      })
      .catch(() => {})
}

// 删除文件 (delFlag: 0永久删除, 1移入回收站)
const deleteFile = async (file: FileInfo, delFlag: 0 | 1) => {
  let confirmMsg = '';
  let successMsg = '';
  let apiPath = '';

  if (delFlag === 1) { // 移入回收站
    if (file.del_flag === 1) {
      ElMessage.info('文件已在回收站中。');
      return;
    }
    confirmMsg = `确定要将文件 "${file.file_name}" 移入回收站吗？`;
    successMsg = '文件已移入回收站。';
    apiPath = `/api/admin/files/toRecycleBin/${file.file_id}`; // 假设 API 路径
  } else { // 永久删除
    if (file.del_flag !== 1) { // 只有回收站的文件才能永久删除
      ElMessage.error('只有回收站中的文件才能永久删除！');
      return;
    }
    confirmMsg = `确定要永久删除文件 "${file.file_name}" 吗？此操作不可逆！`;
    successMsg = '文件已永久删除。';
    apiPath = `/api/admin/files/deletePermanently/${file.file_id}`; // 假设 API 路径
  }

  ElMessageBox.confirm(confirmMsg, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  })
      .then(async () => {
        try {
          // 模拟 API 请求：将 del_flag 设置为 0 (删除) 或 1 (回收站)
          console.log(`Sending delete request (${delFlag === 0 ? 'permanently' : 'to recycle bin'}) for file: ${file.file_id}`);
          // 实际 API: await fetch(apiPath, { method: 'DELETE'/'PUT', headers: { Authorization: 'Bearer ...' } });

          await new Promise(resolve => setTimeout(resolve, 500)); // 模拟延迟

          ElMessage.success(successMsg)
          fetchFiles() // 刷新列表
        } catch (error) {
          console.error('删除文件错误:', error)
          ElMessage.error('文件操作失败。')
        }
      })
      .catch(() => {})
}

// 处理每页显示数量变化
const handleSizeChange = (val: number) => {
  pageSize.value = val
  currentPage.value = 1 // 切换每页大小后回到第一页
}

// 处理当前页码变化
const handleCurrentChange = (val: number) => {
  currentPage.value = val
}

// 获取文件列表数据（模拟）
const fetchFiles = async () => {
  loading.value = true
  try {
    // 模拟从后端获取数据
    // 实际的API请求可能是：
    // const response = await fetch(`/api/admin/files?fileName=${searchForm.fileName}&userId=${searchForm.userIdOrName}&folderType=${searchForm.folderType}&delFlag=${searchForm.delFlag}`);
    // const data = await response.json();

    // 模拟你的数据库截图和一些额外的文件数据
    const mockFiles: FileInfo[] = [
      { file_id: 'f001', user_id: 'user001', file_md5: 'md5_abc1', file_pid: null, file_size: 12345678, file_name: 'document.pdf', file_cover: null, file_path: '/path/doc.pdf', create_time: '2023-01-01 10:00:00', last_update_time: '2023-01-01 10:00:00', folder_type: 0, file_category: 4, file_type: 4, status: 2, recovery_time: null, del_flag: 2 },
      { file_id: 'f002', user_id: 'user002', file_md5: 'md5_def2', file_pid: null, file_size: 23456789, file_name: 'image.jpg', file_cover: null, file_path: '/path/img.jpg', create_time: '2023-02-05 11:30:00', last_update_time: '2023-02-05 11:30:00', folder_type: 0, file_category: 3, file_type: 3, status: 2, recovery_time: null, del_flag: 2 },
      { file_id: 'f003', user_id: 'user001', file_md5: 'md5_ghi3', file_pid: null, file_size: 0, file_name: 'Project_Folder', file_cover: null, file_path: '/path/proj/', create_time: '2023-03-10 14:00:00', last_update_time: '2023-03-10 14:00:00', folder_type: 1, file_category: null, file_type: null, status: null, recovery_time: null, del_flag: 2 },
      { file_id: 'f004', user_id: 'user003', file_md5: 'md5_jkl4', file_pid: null, file_size: 345678901, file_name: 'video.mp4', file_cover: null, file_path: '/path/video.mp4', create_time: '2023-04-15 16:30:00', last_update_time: '2023-04-15 16:30:00', folder_type: 0, file_category: 1, file_type: 1, status: 0, recovery_time: null, del_flag: 2 }, // 转码中
      { file_id: 'f005', user_id: 'user001', file_md5: 'md5_mno5', file_pid: null, file_size: 56789012, file_name: 'old_report.doc', file_cover: null, file_path: '/path/old_report.doc', create_time: '2022-01-20 09:00:00', last_update_time: '2022-01-20 09:00:00', folder_type: 0, file_category: 4, file_type: 5, status: 2, recovery_time: '2025-07-01 10:00:00', del_flag: 1 }, // 回收站
      { file_id: 'f006', user_id: 'user002', file_md5: 'md5_pqr6', file_pid: null, file_size: 67890123, file_name: 'deleted_image.png', file_cover: null, file_path: '/path/deleted_image.png', create_time: '2021-11-11 11:11:11', last_update_time: '2021-11-11 11:11:11', folder_type: 0, file_category: 3, file_type: 3, status: 2, recovery_time: null, del_flag: 0 }, // 已删除 (此文件将不会被显示)
      { file_id: 'f007', user_id: 'user001', file_md5: 'md5_stu7', file_pid: null, file_size: 0, file_name: 'Empty Folder', file_cover: null, file_path: '/path/empty/', create_time: '2024-05-01 08:00:00', last_update_time: '2024-05-01 08:00:00', folder_type: 1, file_category: null, file_type: null, status: null, recovery_time: '2025-07-05 12:00:00', del_flag: 1 }, // 回收站目录
      { file_id: 'f008', user_id: '1000000001', file_md5: 'md5_vwx8', file_pid: null, file_size: 5000000, file_name: 'Spreadsheet.xls', file_cover: null, file_path: '/path/sheet.xls', create_time: '2024-06-20 09:30:00', last_update_time: '2024-06-20 09:30:00', folder_type: 0, file_category: 4, file_type: 6, status: 2, recovery_time: null, del_flag: 2 },
      { file_id: 'f009', user_id: '1000000002', file_md5: 'md5_yza9', file_pid: null, file_size: 15000000, file_name: 'Presentation.ppt', file_cover: null, file_path: '/path/ppt.ppt', create_time: '2024-07-01 10:00:00', last_update_time: '2024-07-01 10:00:00', folder_type: 0, file_category: 4, file_type: 5, status: 2, recovery_time: null, del_flag: 2 },
      { file_id: 'f010', user_id: 'user001', file_md5: 'md5_bcd10', file_pid: null, file_size: 80000000, file_name: 'Music.mp3', file_cover: null, file_path: '/path/music.mp3', create_time: '2024-07-08 11:00:00', last_update_time: '2024-07-08 11:00:00', folder_type: 0, file_category: 2, file_type: 2, status: 2, recovery_time: null, del_flag: 2 },
      { file_id: 'f011', user_id: 'user003', file_md5: 'md5_efg11', file_pid: null, file_size: 1024, file_name: 'test.txt', file_cover: null, file_path: '/path/test.txt', create_time: '2025-07-07 09:00:00', last_update_time: '2025-07-07 09:00:00', folder_type: 0, file_category: 4, file_type: 7, status: 2, recovery_time: '2025-07-08 10:00:00', del_flag: 1 },
      { file_id: 'f012', user_id: '1000000001', file_md5: 'md5_hij12', file_pid: null, file_size: 20480, file_name: 'archive.zip', file_cover: null, file_path: '/path/archive.zip', create_time: '2024-09-10 14:00:00', last_update_time: '2024-09-10 14:00:00', folder_type: 0, file_category: 5, file_type: 9, status: 2, recovery_time: null, del_flag: 2 },
    ]

    await new Promise(resolve => setTimeout(resolve, 500)) // 模拟网络延迟
    fileList.value = mockFiles

  } catch (error) {
    console.error('获取文件列表失败:', error)
    ElMessage.error('获取文件数据失败，请检查网络连接。')
  } finally {
    loading.value = false
  }
}

// 组件挂载时获取文件列表
onMounted(() => {
  fetchFiles()
})
</script>

<style scoped>
.admin-files-view {
  padding: 20px;
  background-color: #f0f2f5;
  min-height: calc(100vh - 100px); /* 留出头部和底部空间 */
  display: flex;
  flex-direction: column;
}

h2 {
  font-size: 24px;
  color: #333;
  margin-bottom: 20px;
}

.search-card {
  margin-bottom: 20px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 15px; /* 搜索表单和操作按钮之间的间距 */
}

.search-form .el-form-item {
  margin-bottom: 0; /* 移除表单项默认的下边距 */
  margin-right: 20px; /* 控制表单项右边距 */
}

/* .action-buttons 区域已移除 */

.table-card {
  flex: 1; /* 使表格卡片填充剩余空间 */
  display: flex;
  flex-direction: column;
}

/* 调整表格头部颜色和字体 */
.el-table :deep(.el-table__header-wrapper th) {
  background-color: #f5f7fa;
  color: #606266;
  font-weight: 600;
}

.file-info-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.file-info-cell .el-icon {
  font-size: 18px;
}

.file-info-cell .folder-icon {
  color: #e6a23c; /* 文件夹图标颜色 */
}

.file-info-cell .document-icon {
  color: #409eff; /* 文件图标颜色 */
}

.pagination-container {
  margin-top: 20px;
  justify-content: flex-end; /* 分页组件靠右显示 */
  display: flex;
}
</style>
<template>
  <div class="admin-files-view">
    <h2>文件管理</h2>

    <el-card shadow="hover" class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="文件名">
          <el-input v-model="searchForm.fileName" placeholder="输入文件名" clearable style="width: 100px;"></el-input>
        </el-form-item>
        <el-form-item label="用户ID/用户名">
          <el-input v-model="searchForm.userIdOrName" placeholder="输入用户ID或用户名" clearable></el-input>
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="searchForm.folderType" placeholder="文件/目录" clearable style="width: 100px;">
            <el-option label="所有" value=""></el-option>
            <el-option label="文件" :value="0"></el-option>
            <el-option label="目录" :value="1"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.delFlag" placeholder="选择文件状态" clearable style="width: 150px;">
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
        <el-table-column prop="fileName" label="文件名" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="file-info-cell">
              <el-icon :class="getFileIcon(row.folderType)">
                <Folder v-if="row.folderType === 1" />
                <Document v-else />
              </el-icon>
              <span>{{ row.fileName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="userId" label="所属用户" width="120" show-overflow-tooltip>
          <template #default="{ row }">
            {{ getUserNameById(row.userId) || row.userId }}
          </template>
        </el-table-column>
        <el-table-column prop="fileSize" label="大小" width="100">
          <template #default="{ row }">
            {{ row.folderType === 1 ? '--' : formatFileSize(row.fileSize) }}
          </template>
        </el-table-column>
        <el-table-column prop="folderType" label="类型" width="100" :formatter="formatFolderType"></el-table-column>
        <el-table-column prop="fileCategory" label="分类" width="100" :formatter="formatFileCategory"></el-table-column>
        <el-table-column prop="delFlag" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getFileStatusTagType(row.delFlag)">{{ formatDelFlag(row.delFlag) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="lastUpdateTime" label="最后更新" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.lastUpdateTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="recoveryTime" label="回收站时间" width="180">
          <template #default="{ row }">
            {{ row.delFlag === 1 ? formatDateTime(row.recoveryTime) : '--' }}
          </template>
        </el-table-column>

        <el-table-column label="操作" fixed="right" width="180">
          <template #default="{ row }">
            <el-button
                link
                type="primary"
                size="small"
                v-if="row.delFlag === 1" @click="recoverFile(row)"
            >恢复</el-button>
            <el-button
                link
                type="warning"
                size="small"
                v-if="row.delFlag === 2" @click="deleteFile(row, 1)"
            >删除到回收站</el-button>
            <el-button
                link
                type="danger"
                size="small"
                v-if="row.delFlag === 1" @click="deleteFile(row, 0)"
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
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  Refresh,
  Folder,
  Document,
} from '@element-plus/icons-vue'

// 假设你的 utils 文件中有这些格式化函数
import { formatFileSize } from '../utils' // 确保这个路径是正确的

// 用于格式化日期时间 (适配后端 LocalDateTime 返回的 ISO 8601 字符串)
const formatDateTime = (datetime: string | null) => {
  if (!datetime) return '--'
  try {
    const date = new Date(datetime);
    if (isNaN(date.getTime())) {
      throw new Error('Invalid date string');
    }
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    });
  } catch (e) {
    console.error("Error formatting date:", datetime, e);
    return datetime; // 返回原始字符串或错误提示
  }
}

// --- 数据定义 ---

// 文件接口，匹配后端 FileInfo 实体类 (驼峰命名)
interface FileInfo {
  fileId: string;
  userId: string;
  fileMd5: string;
  filePid: string | null;
  fileSize: number; // bigint 对应 number
  fileName: string;
  fileCover: string | null;
  filePath: string | null;
  createTime: string; // datetime 对应 string (LocalDateTime 的 ISO 8601 格式)
  lastUpdateTime: string; // datetime 对应 string
  folderType: 0 | 1; // tinyint(1) 对应 number
  fileCategory: 1 | 2 | 3 | 4 | 5 | null; // tinyint(1) 对应 number
  fileType: number | null; // tinyint 对应 number
  status: 0 | 1 | 2 | null; // tinyint 对应 number
  recoveryTime: string | null; // datetime 对应 string
  delFlag: 0 | 1 | 2; // tinyint(1) 对应 number
}

// 搜索表单
const searchForm = reactive({
  fileName: '',
  userIdOrName: '', // 前端组合查询用户ID或用户名，实际后端只根据userId查询
  folderType: '', // 0: 文件, 1: 目录, '': 所有
  delFlag: '',    // 1: 回收站, 2: 正常, '': 所有 (不含已删除)
})

const fileList = ref<FileInfo[]>([]) // 所有文件数据
const loading = ref(false) // 表格加载状态

// 分页
const currentPage = ref(1)
const pageSize = ref(10)

// 模拟的用户ID到用户名的映射 (实际项目中通常会从用户管理模块获取，或者在后端Join查询后返回)
// 这里为了演示，继续使用模拟数据
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
    // 排除 del_flag = 0 (已删除) 的文件，因为后端getAllFileInfos不会返回del_flag=0的
    // if (file.delFlag === 0) return false; // 理论上后端 getAllFileInfos 不会返回，但前端可以加层防御

    const matchesFileName = file.fileName?.includes(searchForm.fileName)

    // 用户ID或用户名匹配
    const matchesUserIdOrName = !searchForm.userIdOrName ||
        file.userId?.includes(searchForm.userIdOrName) ||
        (userMap.value[file.userId]?.includes(searchForm.userIdOrName));

    const matchesFolderType = searchForm.folderType === '' || file.folderType === searchForm.folderType

    // 如果 searchForm.delFlag 为空，则显示所有（正常和回收站）。否则按指定状态筛选。
    const matchesDelFlag = searchForm.delFlag === '' || file.delFlag === searchForm.delFlag

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
  return row.folderType === 1 ? '目录' : '文件'
}

// 格式化文件分类
const formatFileCategory = (row: FileInfo) => {
  const categories: { [key: number]: string } = {
    1: '视频', 2: '音频', 3: '图片', 4: '文档', 5: '其他'
  }
  // 如果 fileCategory 为 null 或不在映射中，则默认为 '其他'
  return categories[row.fileCategory || 5] || '其他'
}

// 格式化删除标志 (这里只显示 1 和 2 的状态)
const formatDelFlag = (delFlag: number) => {
  switch (delFlag) {
    case 1: return '回收站'
    case 2: return '正常'
    case 0: return '已删除' // 尽管不显示，但仍给出完整映射
    default: return '未知'
  }
}

// 获取文件状态标签类型
const getFileStatusTagType = (delFlag: number) => {
  switch (delFlag) {
    case 1: return 'warning' // 回收站
    case 2: return 'success' // 正常
    case 0: return 'danger' // 已删除 (虽然不展示)
    default: return 'info'
  }
}

// 根据用户ID获取用户名 (模拟)
const getUserNameById = (userId: string) => {
  return  userId;
}

// 搜索文件
const searchFiles = () => {
  currentPage.value = 1 // 搜索时重置回第一页
  // fetchFiles(); // 这里的搜索是前端过滤，不需要重新从后端获取
}

// 重置搜索条件
const resetSearch = () => {
  searchForm.fileName = ''
  searchForm.userIdOrName = ''
  searchForm.folderType = ''
  searchForm.delFlag = ''
  currentPage.value = 1 // 重置搜索也重置到第一页
  // fetchFiles() // 重置后不需要重新获取，因为过滤是前端进行的
}

// 恢复文件 (从回收站恢复到正常)
const recoverFile = async (file: FileInfo) => {
  if (file.delFlag === 2) {
    ElMessage.info('文件已是正常状态，无需恢复。')
    return;
  }
  ElMessageBox.confirm(
      `确定要恢复文件 "${file.fileName}" 吗？`,
      '提示',
      {
        confirmButtonText: '确定恢复',
        cancelButtonText: '取消',
        type: 'info',
      }
  )
      .then(async () => {
        try {
          const url = `/admin-api/file/updateFileInfo`; // 使用PUT方法更新
          const payload = {
            fileId: file.fileId,
            userId: file.userId,
            delFlag: 2, // 将 del_flag 设置为 2 (正常)
            recoveryTime: null // 清除回收站时间
            // 其他字段无需发送，后端只会更新delFlag和recoveryTime
          };

          const response = await fetch(url, {
            method: 'PUT',
            headers: {
              'Content-Type': 'application/json',
            },
            body: JSON.stringify(payload)
          });
          const result = await response.json();

          if (result.code === 200) {
            ElMessage.success(`文件 "${file.fileName}" 已恢复。`);
            fetchFiles(); // 刷新列表
          } else {
            ElMessage.error(result.message || '文件恢复失败。');
          }
        } catch (error) {
          console.error('恢复文件错误:', error);
          ElMessage.error('文件恢复失败，请检查网络或服务器。');
        }
      })
      .catch(() => {
        ElMessage.info('已取消文件恢复操作。');
      })
}

// 删除文件 (delFlag: 0永久删除, 1移入回收站)
const deleteFile = async (file: FileInfo, targetDelFlag: 0 | 1) => {
  let confirmMsg = '';
  let successMsg = '';
  let url = '';
  let method = 'PUT'; // 默认是PUT用于更新delFlag

  if (targetDelFlag === 1) { // 移入回收站
    if (file.delFlag === 1) {
      ElMessage.info('文件已在回收站中。');
      return;
    }
    confirmMsg = `确定要将文件 "${file.fileName}" 移入回收站吗？`;
    successMsg = '文件已移入回收站。';
    url = `/admin-api/file/updateFileInfo`; // 更新 delFlag
  } else { // 永久删除
    if (file.delFlag !== 1) { // 只有回收站的文件才能永久删除
      ElMessage.error('只有回收站中的文件才能永久删除！');
      return;
    }
    confirmMsg = `确定要永久删除文件 "${file.fileName}" 吗？此操作不可逆！`;
    successMsg = '文件已永久删除。';
    url = `/admin-api/file/deleteFileInfo/${file.fileId}/${file.userId}`; // 使用DELETE方法
    method = 'DELETE';
  }

  ElMessageBox.confirm(confirmMsg, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  })
      .then(async () => {
        try {
          const headers: HeadersInit = { 'Content-Type': 'application/json' };
          let body: string | undefined;

          if (method === 'PUT') {
            const payload = {
              fileId: file.fileId,
              userId: file.userId,
              delFlag: targetDelFlag,
              recoveryTime: targetDelFlag === 1 ? new Date().toISOString() : null // 移入回收站则设置时间
            };
            body = JSON.stringify(payload);
          }

          const response = await fetch(url, {
            method: method,
            headers: headers,
            body: body
          });
          const result = await response.json();

          if (result.code === 200) {
            ElMessage.success(successMsg);
            fetchFiles(); // 刷新列表
          } else {
            ElMessage.error(result.message || '文件操作失败。');
          }
        } catch (error) {
          console.error('文件操作错误:', error);
          ElMessage.error('文件操作失败，请检查网络或服务器。');
        }
      })
      .catch(() => {
        ElMessage.info('已取消文件操作。');
      })
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

// 获取文件列表数据（从后端API获取）
const fetchFiles = async () => {
  loading.value = true
  try {
    // 假设 getAllFileInfos 会返回所有文件信息（正常和回收站），不包含 del_flag=0 的文件
    const response = await fetch('/admin-api/file/getAllFileInfos', {
      headers: {
        // 如果需要认证，添加 Authorization 头
      }
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const result = await response.json();

    if (result.code === 200) {
      // 将后端返回的List<FileInfo>数据赋值给 fileList
      // 确保后端返回的字段名与前端 FileInfo 接口的驼峰命名一致
      fileList.value = result.data.map((file: any) => ({
        ...file,
        fileId: file.fileId,
        userId: file.userId,
        fileMd5: file.fileMd5,
        filePid: file.filePid,
        fileSize: Number(file.fileSize), // 确保 bigint 转换为 number
        fileName: file.fileName,
        fileCover: file.fileCover,
        filePath: file.filePath,
        createTime: file.createTime,
        lastUpdateTime: file.lastUpdateTime,
        folderType: Number(file.folderType),
        fileCategory: file.fileCategory !== null ? Number(file.fileCategory) : null,
        fileType: file.fileType !== null ? Number(file.fileType) : null,
        status: file.status !== null ? Number(file.status) : null,
        recoveryTime: file.recoveryTime,
        delFlag: Number(file.delFlag),
      }));
    } else {
      ElMessage.error(result.message || '获取文件列表失败！');
    }
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
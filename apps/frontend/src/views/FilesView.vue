<template>
  <div class="files-view">
    <!-- 上传任务面板 -->
    <div class="uploader-panel" v-if="showUploader">
      <div class="uploader-title">
        <span>上传任务</span>
        <span class="tips">（仅展示本次上传任务）</span>
      </div>
      <div class="file-list">
        <div v-for="(item, index) in fileList" class="file-item" :key="item.uid">
          <div class="upload-panel">
            <div class="file-name">
              {{ item.fileName }}
            </div>
            <div class="progress">
              <el-progress
                :percentage="item.uploadProgress"
                v-if="
                  item.status == STATUS.uploading.value ||
                  item.status == STATUS.upload_seconds.value ||
                  item.status == STATUS.upload_finish.value
                "
              />
            </div>
            <div class="upload-status">
              <span
                :class="['iconfont', 'icon-' + STATUS[item.status].icon]"
                :style="{ color: STATUS[item.status].color }"
              ></span>
              <span
                class="status"
                :style="{ color: STATUS[item.status].color }"
                >{{
                  item.status == "fail" ? item.errorMsg : STATUS[item.status].desc
                }}</span
              >
              <span
                class="upload-info"
                v-if="item.status == STATUS.uploading.value"
              >
                {{ size2Str(item.uploadSize) }}/{{
                  size2Str(item.totalSize)
                }}
              </span>
            </div>
          </div>
          <div class="op">
            <el-progress
              type="circle"
              :width="50"
              :percentage="item.md5Progress"
              v-if="item.status == STATUS.init.value"
            />
            <div class="op-btn">
              <span v-if="item.status === STATUS.uploading.value">
                <el-button
                  link
                  type="primary"
                  size="small"
                  v-if="item.pause"
                  title="上传"
                  @click.stop="startUpload(item.uid)"
                >
                  <el-icon><VideoPlay /></el-icon>
                </el-button>
                <el-button
                  link
                  type="primary"
                  size="small"
                  title="暂停"
                  @click.stop="pauseUpload(item.uid)"
                  v-else
                >
                  <el-icon><VideoPause /></el-icon>
                </el-button>
              </span>
              <el-button
                link
                type="danger"
                size="small"
                title="删除"
                v-if="
                  item.status != STATUS.init.value &&
                  item.status != STATUS.upload_finish.value &&
                  item.status != STATUS.upload_seconds.value
                "
                @click.stop="delUpload(item.uid, index)"
              >
                <el-icon><Delete /></el-icon>
              </el-button>
              <el-button
                link
                type="primary"
                size="small"
                title="清除"
                v-if="
                  item.status == STATUS.upload_finish.value ||
                  item.status == STATUS.upload_seconds.value
                "
                @click.stop="delUpload(item.uid, index)"
              >
                <el-icon><Close /></el-icon>
              </el-button>
            </div>
          </div>
        </div>
        <div v-if="fileList.length == 0">
          <div class="no-data">暂无上传任务</div>
        </div>
      </div>
    </div>

    <div class="header">
      <div class="main-header">
        <el-button type="primary" @click="showFileInput">
          <el-icon>
            <Upload />
          </el-icon>上传文件
        </el-button>
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

    <!-- 隐藏的文件选择器 -->
    <input
      type="file"
      ref="fileInput"
      @change="handleFileChange"
      style="display: none"
      multiple
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import SparkMD5 from 'spark-md5'
import { formatDate } from '../utils'

const router = useRouter()

const viewMode = ref<'grid' | 'list'>('list')
const showUploader = ref(false)
const fileInput = ref<HTMLInputElement | null>(null)
const currentPath = ref<string[]>(['文档', '项目文件', '2024年度'])

// 文件上传相关状态和变量
const STATUS = {
  emptyfile: {
    value: "emptyfile",
    desc: "文件为空",
    color: "#F75000",
    icon: "close",
  },
  fail: {
    value: "fail",
    desc: "上传失败",
    color: "#F75000",
    icon: "close",
  },
  init: {
    value: "init",
    desc: "解析中",
    color: "#e6a23c",
    icon: "clock",
  },
  uploading: {
    value: "uploading",
    desc: "上传中",
    color: "#409eff",
    icon: "upload",
  },
  upload_finish: {
    value: "upload_finish",
    desc: "上传完成",
    color: "#67c23a",
    icon: "ok",
  },
  upload_seconds: {
    value: "upload_seconds",
    desc: "秒传",
    color: "#67c23a",
    icon: "ok",
  },
};

const chunkSize = 1024 * 512; // 512KB 分片大小
const fileList = ref<any[]>([]);
const delList = ref<any[]>([]);

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

// 显示文件选择器
const showFileInput = () => {
  if (fileInput.value) {
    fileInput.value.value = ''; // 重置以允许选择相同文件
    fileInput.value.click();
  }
}

// 处理文件选择
const handleFileChange = (event: Event) => {
  const target = event.target as HTMLInputElement;
  if (target.files && target.files.length > 0) {
    for (let i = 0; i < target.files.length; i++) {
      const file = target.files[i];
      addFile(file, '0'); // '0' 表示根目录
    }
    showUploader.value = true;
  }
}

// 添加文件到上传列表
const addFile = async (file: File, filePid: string) => {
  const fileItem = {
    file: file,
    uid: Date.now() + Math.random().toString(36).substring(2), // 生成唯一ID
    md5Progress: 0,
    md5: null,
    fileName: file.name,
    status: STATUS.init.value,
    uploadSize: 0,
    totalSize: file.size,
    uploadProgress: 0,
    pause: false,
    chunkIndex: 0,
    filePid: filePid,
    errorMsg: null,
  };

  fileList.value.unshift(fileItem);

  if (fileItem.totalSize === 0) {
    fileItem.status = STATUS.emptyfile.value;
    return;
  }

  const md5FileUid = await computeMD5(fileItem);
  if (md5FileUid) {
    uploadFile(md5FileUid);
  }
};

// 开始上传
const startUpload = (uid: string) => {
  const currentFile = getFileByUid(uid);
  if (currentFile) {
    currentFile.pause = false;
    uploadFile(uid, currentFile.chunkIndex);
  }
};

// 暂停上传
const pauseUpload = (uid: string) => {
  const currentFile = getFileByUid(uid);
  if (currentFile) {
    currentFile.pause = true;
  }
};

// 删除上传
const delUpload = (uid: string, index: number) => {
  delList.value.push(uid);
  fileList.value.splice(index, 1);
  if (fileList.value.length === 0) {
    showUploader.value = false;
  }
};

// 计算MD5
const computeMD5 = (fileItem: any) => {
  return new Promise((resolve) => {
    const file = fileItem.file;
    const blobSlice = File.prototype.slice;
    const chunks = Math.ceil(file.size / chunkSize);
    let currentChunk = 0;
    const spark = new SparkMD5.ArrayBuffer();
    const fileReader = new FileReader();

    const loadNext = () => {
      const start = currentChunk * chunkSize;
      const end = Math.min(start + chunkSize, file.size);
      fileReader.readAsArrayBuffer(blobSlice.call(file, start, end));
    };

    fileReader.onload = (e) => {
      if (!e.target?.result) return;

      spark.append(e.target.result as ArrayBuffer);
      currentChunk++;

      if (currentChunk < chunks) {
        const percent = Math.floor((currentChunk / chunks) * 100);
        fileItem.md5Progress = percent;
        loadNext();
      } else {
        const md5 = spark.end();
        fileItem.md5Progress = 100;
        fileItem.status = STATUS.uploading.value;
        fileItem.md5 = md5;
        resolve(fileItem.uid);
        spark.destroy();
      }
    };

    fileReader.onerror = () => {
      fileItem.md5Progress = -1;
      fileItem.status = STATUS.fail.value;
      resolve(null);
    };

    loadNext();
  });
};

// 上传文件
const uploadFile = async (uid: string, chunkIndex = 0) => {
  const currentFile = getFileByUid(uid);
  if (!currentFile) return;

  const file = currentFile.file;
  const fileSize = currentFile.totalSize;
  const chunks = Math.ceil(fileSize / chunkSize);

  for (let i = chunkIndex; i < chunks; i++) {
    // 检查是否被删除
    if (delList.value.includes(uid)) {
      const index = delList.value.indexOf(uid);
      if (index !== -1) delList.value.splice(index, 1);
      break;
    }

    // 检查是否暂停
    if (currentFile.pause) break;

    const start = i * chunkSize;
    const end = Math.min(start + chunkSize, fileSize);
    const chunkFile = file.slice(start, end);
    let  userId=localStorage.getItem('UserId');
    userId='1000000001';
    const formData = new FormData();
    formData.append('file', chunkFile);
    formData.append('fileName', file.name);
    formData.append('fileMd5', currentFile.md5 || '');
    formData.append('chunkIndex', i.toString());
    formData.append('chunks', chunks.toString());
    formData.append('filePid', currentFile.filePid);
    formData.append('userId',userId);

    try {
      const response = await fetch('/file/uploadFile', {
        method: 'POST',
        body: formData,

      });

      if (!response.ok) {
        throw new Error(`上传失败: ${response.statusText}`);
      }

      const result = await response.json();
      currentFile.status = STATUS[result.data.status].value;
      currentFile.chunkIndex = i;

      if (result.data.fileId) {
        currentFile.fileId = result.data.fileId;
      }

      // 更新上传进度
      currentFile.uploadSize = (i + 1) * chunkSize;
      if (currentFile.uploadSize > fileSize) {
        currentFile.uploadSize = fileSize;
      }
      currentFile.uploadProgress = Math.floor((currentFile.uploadSize / fileSize) * 100);

      // 上传完成
      if (
        result.data.status === STATUS.upload_seconds.value ||
        result.data.status === STATUS.upload_finish.value
      ) {
        currentFile.uploadProgress = 100;
        ElMessage.success(`文件 ${file.name} 上传成功`);
        break;
      }
    } catch (error: any) {
      currentFile.status = STATUS.fail.value;
      currentFile.errorMsg = error.message || '上传出错';
      break;
    }
  }
};

// 根据UID获取文件
const getFileByUid = (uid: string) => {
  return fileList.value.find(item => item.uid === uid);
};

// 文件大小格式化
const size2Str = (size: number) => {
  if (size < 1024) {
    return size + "B";
  } else if (size < 1024 * 1024) {
    return (size / 1024).toFixed(2) + "KB";
  } else if (size < 1024 * 1024 * 1024) {
    return (size / (1024 * 1024)).toFixed(2) + "MB";
  } else {
    return (size / (1024 * 1024 * 1024)).toFixed(2) + "GB";
  }
};

// 生成路径
const generatePath = (index: number) => {
  const pathSegments = currentPath.value.slice(0, index + 1)
  return `/main/files/${pathSegments.join('/')}`
}

const handleFileClick = (item: any) => {
  console.log('File clicked:', item)
}
</script>

<style scoped>
.files-view {
  height: 100%;
  display: flex;
  flex-direction: column;
}

/* 上传面板样式 */
.uploader-panel {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  margin: 0 16px 16px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.uploader-title {
  border-bottom: 1px solid #ddd;
  line-height: 40px;
  padding: 0 10px;
  font-size: 15px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.uploader-title .tips {
  font-size: 13px;
  color: rgb(169, 169, 169);
}

.file-list {
  max-height: 300px;
  overflow-y: auto;
  padding: 10px 0;
}

.file-item {
  position: relative;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 15px;
  border-bottom: 1px solid #eee;
}

.file-item:last-child {
  border-bottom: none;
}

.upload-panel {
  flex: 1;
  min-width: 0;
}

.file-name {
  color: rgb(64, 62, 62);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-size: 14px;
}

.upload-status {
  display: flex;
  align-items: center;
  margin-top: 5px;
  font-size: 12px;
}

.upload-status .status {
  margin-left: 5px;
}

.upload-info {
  margin-left: 10px;
  color: #888;
}

.progress {
  margin-top: 5px;
}

.op {
  display: flex;
  align-items: center;
  gap: 10px;
}

.op-btn {
  display: flex;
  gap: 5px;
}

.no-data {
  text-align: center;
  padding: 20px;
  color: #999;
}

/* 原有样式 */
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
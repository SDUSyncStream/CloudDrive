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
        <el-button  @click="handleNewFolder()">
          <el-icon>
            <FolderAdd />
          </el-icon>新建文件夹
        </el-button>
        <el-button @click="handlePaste()" v-if="copied_fileId != -1">
          粘贴
        </el-button>
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
<!--        <el-breadcrumb-item :to="{ path: '/main/files' }">我的文件</el-breadcrumb-item>-->
<!--        <el-breadcrumb-item v-for="(path, index) in currentPath" :key="index"-->
<!--          :to="index < currentPath.length - 1 ? { path: generatePath(index) } : undefined">-->
<!--          {{ path }}-->
<!--        </el-breadcrumb-item>-->
          <el-breadcrumb-item
              v-for="item in breadcrumbList"
              :key="index"
              @click="handleBreadcrumbClick(item)"
          >
            {{ item.fileName }}
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
              <el-icon v-if="row.folderType == 1"><Folder /></el-icon>
              <el-icon v-else-if="row.folderType == 0"><Document /></el-icon>
              <span>{{ row.fileName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="size" label="大小">
          <template #default="{ row }">
            {{ formatFileSize(row.fileSize) || '--' }}
          </template>
        </el-table-column>
        <el-table-column prop="date" label="修改时间">
          <template #default="{ row }">
            {{ formatDate(row.lastUpdateTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button link type="primary" size="small" title="查看文件" @click="handleViewClick(row)">
                <el-icon><View /></el-icon>
              </el-button>
              <el-button link type="primary" size="small" title="分享文件">
                <el-icon><Share /></el-icon>
              </el-button>
              <el-button link type="primary" size="small" title="下载文件" @click="handleDownload(row)">
                <el-icon><Download /></el-icon>
              </el-button>
              <el-dropdown trigger="click">
                <el-button link type="primary" size="small" title="更多操作">
                  <el-icon><MoreFilled /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="handleCopy(row)">
                      <el-icon><CopyDocument /></el-icon>
                      复制
                    </el-dropdown-item >
                    <el-dropdown-item @click="handleRename(row)">
                      <el-icon><Edit /></el-icon>
                      重命名
                    </el-dropdown-item >
                    <el-dropdown-item divided @click="handleRecycle(row)">
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
          <div v-for="item in tableData" :key="item.file_id" class="file-card">
            <div class="file-card-header">
              <el-icon v-if="item.folderType == 1"><Folder /></el-icon>
              <el-icon v-else-if="item.folderType == 0"><Document /></el-icon>
            </div>
            <div class="file-card-content">
              <div class="file-name" :title="item.fileName">{{ item.fileName }}</div>
              <div class="file-info">
                <span class="file-size">{{ item.fileSize || '--' }}</span>
                <span class="file-date">{{ formatDate(item.lastUpdateTime) }}</span>
              </div>
            </div>
            <div class="file-card-actions">
              <div class="primary-actions">
                <el-button link type="primary" size="small" title="查看文件" @click="handleViewClick(item)">
                  <el-icon><View /></el-icon>
                </el-button>
                <el-button link type="primary" size="small" title="分享文件">
                  <el-icon><Share /></el-icon>
                </el-button>
                <el-button link type="primary" size="small" title="下载文件" @click="handleDownload(item)">
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
                    <el-dropdown-item @click="handleCopy(item)">
                      <el-icon><CopyDocument /></el-icon>
                      复制
                    </el-dropdown-item>
                    <el-dropdown-item @click="handleRename(item)">
                      <el-icon><Edit /></el-icon>
                      重命名
                    </el-dropdown-item>
                    <el-dropdown-item divided @click="handleRecycle(item)">
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
    <!-- 预览对话框 -->
    <el-dialog
        v-model="previewDialogVisible"
        title="文件预览"
        width="80%"
        height="80%"
        :before-close="handleClosePreview"
    >
      <template #content>
        <div v-if="previewType == 'image'" class="preview-image-container">
          <img :src="previewUrl" :alt="previewFileName" class="preview-image" />
        </div>
        <div v-else-if="previewType == 'text'" class="preview-text-container">
          <pre class="preview-text">{{ previewContent }}</pre>
        </div>
        <div v-else class="preview-unsupported">
          <el-icon><Document /></el-icon>
          <p>不支持预览此文件类型</p>
          <p>文件类型: {{ previewFileExtension }}</p>
        </div>
      </template>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="previewDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleDownload(previewFile)">下载文件</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import SparkMD5 from 'spark-md5'

import {onMounted, ref} from 'vue'
import { useRouter } from 'vue-router'
import {ElMessage, ElMessageBox} from 'element-plus'
import { useUserStore } from '../stores/user'
import { formatFileSize, formatDate } from '../utils'
import type { CloudFile } from '../types'
import axios from 'axios'

const router = useRouter()

const viewMode = ref<'grid' | 'list'>('list')

const showUploader = ref(false)
const fileInput = ref<HTMLInputElement | null>(null)
const currentPath = ref<string[]>(['文档', '项目文件', '2024年度'])
const downloadingFiles = ref<Set<string>>(new Set());


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

let nowfilePid = 0;
let nowUserId = localStorage.getItem("UserId") || '2'; // 默认用户ID为2
onMounted(() => {
//  userId = localStorage.getItem("UserId");
  nowfilePid = 0;
  getFileList(nowfilePid, nowUserId);
})



const getFileList = async(pid, userId) =>{
  try {
    const response = await axios.get(`/files/get/${pid}`, {
      params: {
        userId: userId,
        delFlag: 2,
      }
    });
    console.log('请求成功:', response.data);
    updatetable(response.data.data);
    nowfilePid = pid;
    // 处理响应数据
  } catch (error) {
    console.error('请求失败:', error);
  }
}

let copied_fileId = ref(-1);

const handleCopy = (row) => {
  copied_fileId.value = row.fileId;
  ElMessage.success('文件复制成功');
}

const handlePaste = async () => {
  if (copied_fileId == -1){
    ElMessage.error("复制内容为空");
  }
  else{
    try {
      console.log("复制文件", copied_fileId);
      const response = await axios.get(`/files/copy/${copied_fileId.value}`, {
        params: {
          userId: localStorage.getItem("UserId") || '2', // 默认用户ID为2
          targetId: nowfilePid,
        }
      });
      console.log('请求成功:', response.data);
      await getFileList(nowfilePid, nowUserId);
      ElMessage.success('粘贴成功');
      // 处理响应数据
    } catch (error) {
      console.error('请求失败:', error);
    }
  }
}

const handleNewFolder = async () => {
    const {value: newFileName} = await ElMessageBox.prompt('请输入新的文件夹名', '新建文件夹', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
    });

    if (newFileName) {
      const response = await axios.get(`/files/newfolder/${nowfilePid}`, {
        params: {
          userId: nowUserId,
          newName: newFileName,
        }
      });
      console.log('请求成功:', response.data);
      await getFileList(nowfilePid, nowUserId);
      ElMessage.success('新建成功');
    }
}

const handleRecycle = async (row) =>{
  try {
    const response = await axios.get(`/files/recycle/${row.fileId}`, {
      params: {
        userId: localStorage.getItem("UserId") || '2', // 默认用户ID为2
        newDelFlag: 1,
      }
    });
    console.log('请求成功:', response.data);
    await getFileList(nowfilePid, nowUserId);
    // 处理响应数据
  } catch (error) {
    console.error('请求失败:', error);
  }
}

const handleRename = async (row) => {
  try {
    const { value: newFileName } = await ElMessageBox.prompt('请输入新的文件名', '重命名', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
    });

    if (newFileName) {
      const response = await axios.get(`/files/rename/${row.fileId}`, {
        params: {
          userId: localStorage.getItem("UserId") || '2', // 默认用户ID为2
          newName: newFileName,
        }
      });
      ElMessage.success('重命名成功');
      await getFileList(nowfilePid, nowUserId);
    }
  } catch (error) {
    console.error('重命名操作取消或出错:', error);
  }
}

const updatetable = (data) => {
  console.log(data);
  tableData.value = data
}

let tableData = ref([
  {
    fileId: 0,
    fileName: "我的文件",
  }
])

// 面包屑列表
const breadcrumbList = ref([
  {
    fileId: 0,
    fileName: "我的文件",
    userId: nowUserId,
  }
]);

// 更新面包屑列表
const updateBreadcrumb = (item) => {
  let i = 0;
  for(i; i < breadcrumbList.value.length; i++){
    if(breadcrumbList.value[i].fileId == item.fileId){
      breadcrumbList.value.length = i + 1;
      console.log(i);
      break;
    }
  }
  if(i == breadcrumbList.value.length){
    breadcrumbList.value.push(item);
  }
  console.log("面包屑",breadcrumbList.value);
}

// 处理面包屑点击事件
const handleBreadcrumbClick = async (item) => {
  await getFileList(item.fileId, item.userId);
  updateBreadcrumb(item);
}


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

  // 初始化已上传大小为之前的分片大小
  currentFile.uploadSize = chunkIndex * chunkSize;

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
    const chunkActualSize = end - start; // 当前分片实际大小
    const chunkFile = file.slice(start, end);
    let  userId=localStorage.getItem('UserId') || '2';                             //测试专用
    const formData = new FormData();
    formData.append('file', chunkFile);
    formData.append('fileName', file.name);
    formData.append('fileMd5', currentFile.md5 || '');
    formData.append('chunkIndex', i.toString());
    formData.append('chunks', chunks.toString());
    formData.append('filePid', currentFile.filePid);
    formData.append('userId',userId);
     if(currentFile.fileId){
        formData.append('fileId', currentFile.fileId); // 如果有文件 ID，添加到表单数据中
      }
    try {
      const response = await fetch('/fileup/uploadFile', {
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
      console.log(result);
        currentFile.fileId = result.data.fileId;
      }

      // 更新上传进度：使用实际字节数
      currentFile.uploadSize += chunkActualSize;
      currentFile.uploadProgress = Math.min(
        100,
        Math.floor((currentFile.uploadSize / fileSize) * 100)
      );

      // 上传完成
      if (
        result.data.status === STATUS.upload_seconds.value ||
        result.data.status === STATUS.upload_finish.value
      ) {
        currentFile.uploadProgress = 100;
        ElMessage.success(`文件 ${file.name} 上传成功`);
        getFileList(currentFile.filePid, userId);
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

// 预览相关状态
const previewDialogVisible = ref(false)
const previewType = ref('') // 'image', 'text', 'unsupported'
const previewUrl = ref('')
const previewContent = ref('')
const previewFileName = ref('')
const previewFileExtension = ref('')
const previewFile = ref(null)

// 支持预览的图片格式
const imageExtensions = ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp', 'svg']
// 支持预览的文本格式
const textExtensions = ['txt', 'md', 'json', 'xml', 'html', 'css', 'js', 'ts', 'java', 'py', 'php', 'c', 'cpp', 'cs', 'sql']
const urlPreFix = "file:///E:/IDEA java project/CloudDrive/";

// 获取文件扩展名
const getFileExtension = (fileName: string) => {
  if (!fileName || typeof fileName !== 'string') return ''
  const parts = fileName.split('.')
  return parts.length > 1 ? parts.pop()?.toLowerCase() || '' : ''
}

// 检查文件是否为图片
const isImageFile = (fileName: string) => {
  const ext = getFileExtension(fileName)
  return imageExtensions.includes(ext)
}

const isTextFile = (fileName: string) => {
  const ext = getFileExtension(fileName)
  return textExtensions.includes(ext)
}

const handleViewClick = async (item) => {
  if (item.folderType == 1) {
    getFileList(item.fileId, item.userId);
    updateBreadcrumb(item);
  } else {
    previewFile.value = item
    previewFileName.value = item.fileName
    previewFileExtension.value = getFileExtension(item.fileName)

    if (isImageFile(item.fileName)) {
      previewType.value = 'image'
      try {
        // 1. 创建下载链接获取code
        const userId = localStorage.getItem('UserId') || '2'; // 测试专用，实际应使用localStorage.getItem('userId')
        const createUrlRes = await axios.get(`/fileup/createDownloadUrl/${item.fileId}`, {
          params: { userId }
        });
        if (createUrlRes.data.code !== 200) {
          throw new Error(createUrlRes.data.message || '获取下载链接失败');
        }
        const  downloadcode  = createUrlRes.data.data; // 获取后端返回的下载code
        if (downloadcode== null || downloadcode === undefined) {
          throw new Error('获取下载链接失败');
        }

        // 2. 使用code下载文件
        const downloadRes = await axios.get(`/fileup/download/${downloadcode}`, {
          responseType: 'blob'
        });

        let blob = new Blob([downloadRes.data], { type: 'image/jpeg' })
        console.log(blob.type)
        // 创建下载链接
        const url = window.URL.createObjectURL(blob);

        window.open(url)

      } catch (error: any) {
        console.error('获取预览链接失败:', error);
        ElMessage.error(`预览失败: ${error.response?.data?.message || error.message || '未知错误'}`);
      }
    } else if (isTextFile(item.fileName)) {
      previewType.value = 'text'
      try {
        // 1. 创建下载链接获取code
        const userId = localStorage.getItem('UserId') || '2'; // 测试专用，实际应使用localStorage.getItem('userId')
        const createUrlRes = await axios.get(`/fileup/createDownloadUrl/${item.fileId}`, {
          params: { userId }
        });
        if (createUrlRes.data.code !== 200) {
          throw new Error(createUrlRes.data.message || '获取下载链接失败');
        }
        const  downloadcode  = createUrlRes.data.data; // 获取后端返回的下载code
        if (downloadcode== null || downloadcode === undefined) {
          throw new Error('获取下载链接失败');
        }

        // 2. 使用code下载文件
        const downloadRes = await axios.get(`/fileup/download/${downloadcode}`, {
          responseType: 'text'
        });
        let blob = new Blob([downloadRes.data], { type: 'text/plain' })
        console.log(blob.type)
        // 创建下载链接
        const url = window.URL.createObjectURL(blob);

        window.open(url)
      } catch (error: any) {
        console.error('获取文件内容失败:', error);
        ElMessage.error(`预览失败: ${error.response?.data?.message || error.message || '未知错误'}`);
      }
    } else {
      previewType.value = 'unsupported'
      previewDialogVisible.value = true
    }
  }
}

const handleClosePreview = () => {
  previewDialogVisible.value = false
  previewUrl.value = ''
  previewContent.value = ''
}


const handleDownload=async (row:any)=>{
 // 如果是文件夹，提示不能下载
  if (row.folderType === 1) {
    ElMessage.warning('文件夹不支持下载');
    return;
  }

  // 检查是否正在下载
  if (downloadingFiles.value.has(row.fileId)) {
    ElMessage.info('文件正在下载中，请稍候');
    return;
  }

  try {
    // 标记为下载中
    downloadingFiles.value.add(row.fileId);
    ElMessage.info(`开始下载: ${row.fileName}`);

    // 1. 创建下载链接获取code
    const userId = localStorage.getItem('UserId') || '2'; // 测试专用，实际应使用localStorage.getItem('userId')
    const createUrlRes = await axios.get(`/fileup/createDownloadUrl/${row.fileId}`, {
      params: { userId }
    });
    if (createUrlRes.data.code !== 200) {
      throw new Error(createUrlRes.data.message || '获取下载链接失败');
    }
    const  downloadcode  = createUrlRes.data.data; // 获取后端返回的下载code
    if (downloadcode== null || downloadcode === undefined) {
      throw new Error('获取下载链接失败');
    }

    // 2. 使用code下载文件
    const downloadRes = await axios.get(`/fileup/download/${downloadcode}`, {
      responseType: 'blob'
    });

    // 创建下载链接
    const url = window.URL.createObjectURL(new Blob([downloadRes.data]));
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
    ElMessage.error(`下载失败: ${error.response?.data?.message || error.message || '未知错误'}`);
  } finally {
    // 移除下载状态
    downloadingFiles.value.delete(row.fileId);
  }
}
const handleRowClick =  (row: any) => {

};
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

/* 预览对话框样式 */
.preview-image-container {
  display: flex;
  justify-content: center;
  align-items: center;
  max-height: 60vh;
  overflow: auto;
}

.preview-image {
  max-width: 100%;
  max-height: 60vh;
  object-fit: contain;
}

.preview-text-container {
  max-height: 60vh;
  overflow: auto;
}

.preview-text {
  font-family: monospace;
  font-size: 14px;
  white-space: pre-wrap;
  word-break: break-all;
  background-color: #f5f5f5;
  padding: 16px;
  border-radius: 4px;
}

.preview-unsupported {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 300px;
  color: #909399;
}

.preview-unsupported .el-icon {
  font-size: 48px;
  margin-bottom: 16px;
}
</style>
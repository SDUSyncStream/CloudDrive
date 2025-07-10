<template>
  <div class="admin-membership-levels-view">
    <h2>套餐管理</h2>

    <el-card shadow="hover" class="action-card">
      <el-button type="primary" @click="openAddLevelDialog">
        <el-icon><Plus /></el-icon> 新增套餐
      </el-button>
    </el-card>

    <el-card shadow="hover" class="table-card">
      <el-table
          :data="membershipLevels"
          v-loading="loading"
          style="width: 100%"
          stripe
          border
          height="calc(100vh - 250px)"
          empty-text="暂无套餐模板"
      >
        <el-table-column prop="name" label="套餐名称" width="150" show-overflow-tooltip></el-table-column>
        <el-table-column prop="storageQuota" label="存储配额" width="120">
          <template #default="{ row }">
            {{ formatFileSize(row.storageQuota) }}
          </template>
        </el-table-column>
        <el-table-column prop="maxFileSize" label="最大单文件" width="120">
          <template #default="{ row }">
            {{ formatFileSize(row.maxFileSize) }}
          </template>
        </el-table-column>
        <el-table-column prop="price" label="价格(¥)" width="100">
          <template #default="{ row }">
            {{ row.price.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="durationDays" label="时长(天)" width="100"></el-table-column>
        <el-table-column prop="features" label="特性描述" min-width="200" show-overflow-tooltip></el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="updatedAt" label="最后更新" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.updatedAt) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" fixed="right" width="150">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openEditLevelDialog(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="deleteLevel(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
        v-model="dialogVisible"
        :title="isEditMode ? '编辑套餐' : '新增套餐'"
        width="600px"
        @closed="resetDialogForm"
    >
      <el-form :model="levelForm" :rules="rules" ref="levelFormRef" label-width="120px">
        <el-form-item label="套餐名称" prop="name">
          <el-input v-model="levelForm.name" :disabled="isEditMode && levelForm.id === 'level-free'"></el-input>
        </el-form-item>
        <el-form-item label="存储配额" prop="storageQuotaGB">
          <el-input-number
              v-model="levelForm.storageQuotaGB"
              :min="levelForm.id === 'level-free' ? 1 : 1"
              :max="1024 * 10"
              :step="1"
              placeholder="单位: GB"
          ></el-input-number> GB
          <el-tooltip content="用户可用的总存储空间（单位：GB）" placement="right">
            <el-icon><QuestionFilled /></el-icon>
          </el-tooltip>
        </el-form-item>
        <el-form-item label="最大单文件" prop="maxFileSizeGB">
          <el-input-number
              v-model="levelForm.maxFileSizeGB"
              :min="levelForm.id === 'level-free' ? 0.1 : 0.1"
              :max="100"
              :step="0.1"
              :precision="1"
              placeholder="单位: GB"
          ></el-input-number> GB
          <el-tooltip content="单个文件最大上传大小（单位：GB）" placement="right">
            <el-icon><QuestionFilled /></el-icon>
          </el-tooltip>
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number
              v-model="levelForm.price"
              :min="0"
              :precision="2"
              :step="1.00"
              placeholder="单位: ¥"
              :disabled="levelForm.id === 'level-free'"
          ></el-input-number> ¥
        </el-form-item>
        <el-form-item label="时长" prop="durationDays">
          <el-input-number
              v-model="levelForm.durationDays"
              :min="0"
              :step="1"
              placeholder="单位: 天 (0为永久)"
              :disabled="levelForm.id === 'level-free'"
          ></el-input-number> 天
          <el-tooltip content="订阅的有效时长（0 表示永久有效，如免费版）" placement="right">
            <el-icon><QuestionFilled /></el-icon>
          </el-tooltip>
        </el-form-item>
        <el-form-item label="特性描述" prop="features">
          <el-input type="textarea" v-model="levelForm.features" :rows="3" placeholder="填写套餐的主要特性"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm" :loading="formSubmitting">
            {{ isEditMode ? '保存' : '添加' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  Plus,
  QuestionFilled,
} from '@element-plus/icons-vue'

// 假设你的 utils 文件中有这些格式化函数
import { formatFileSize } from '../utils'

// 用于格式化日期时间
const formatDateTime = (datetime: string | Date | null) => {
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

// --- 数据接口定义 ---
// 与后端 MembershipLevel 实体类保持一致 (驼峰命名)
interface MembershipLevel {
  id: string;
  name: string;
  storageQuota: number; // 字节 (Long)
  maxFileSize: number; // 字节 (Long)
  price: number; // BigDecimal
  durationDays: number; // Integer
  features: string;
  createdAt: string; // LocalDateTime
  updatedAt: string; // LocalDateTime
}

// --- 数据定义 ---

const membershipLevels = ref<MembershipLevel[]>([]) // 所有会员等级数据
const loading = ref(false) // 表格加载状态

// 对话框相关
const dialogVisible = ref(false)
const isEditMode = ref(false) // true: 编辑模式, false: 添加模式
const levelFormRef = ref<FormInstance>() // 对话框表单引用
const formSubmitting = ref(false) // 对话框表单提交状态

// 对话框表单数据 (storageQuotaGB 和 maxFileSizeGB 是为了方便用户输入，实际存储为字节)
const levelForm = reactive({
  id: '',
  name: '',
  storageQuotaGB: 1, // GB 为单位，用于输入框
  maxFileSizeGB: 0.1, // GB 为单位，用于输入框
  price: 0,
  durationDays: 0,
  features: '',
})

// 对话框表单验证规则
const rules: FormRules = reactive({
  name: [
    { required: true, message: '请输入套餐名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        // 检查名称是否重复，除了当前编辑的套餐
        const isDuplicate = membershipLevels.value.some(
            level => level.name === value && level.id !== levelForm.id
        );
        if (isDuplicate) {
          callback(new Error('套餐名称已存在'));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ],
  storageQuotaGB: [
    { required: true, message: '请输入存储配额', trigger: 'blur' },
    { type: 'number', min: 1, message: '存储配额必须大于0', trigger: 'blur', transform: (value) => Number(value) }
  ],
  maxFileSizeGB: [
    { required: true, message: '请输入最大单文件大小', trigger: 'blur' },
    { type: 'number', min: 0.01, message: '最大单文件大小必须大于0', trigger: 'blur', transform: (value) => Number(value) }
  ],
  price: [
    { required: true, message: '请输入价格', trigger: 'blur' },
    { type: 'number', min: 0, message: '价格不能为负数', trigger: 'blur', transform: (value) => Number(value) }
  ],
  durationDays: [
    { required: true, message: '请输入时长', trigger: 'blur' },
    { type: 'number', min: 0, message: '时长不能为负数', trigger: 'blur', transform: (value) => Number(value) },
    { validator: (rule, value, callback) => {
        if (value === 0 && levelForm.price !== 0) {
          callback(new Error('价格非0的套餐时长不能为0'));
        } else if (value !== 0 && levelForm.price === 0) {
          callback(new Error('价格为0的套餐时长必须为0（永久）'));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ],
  features: [
    { required: true, message: '请输入特性描述', trigger: 'blur' },
    { max: 500, message: '描述不能超过500个字符', trigger: 'blur' }
  ],
})

// --- 方法 ---

// 打开新增套餐对话框
const openAddLevelDialog = () => {
  isEditMode.value = false
  resetDialogForm() // 清空表单
  dialogVisible.value = true
}

// 打开编辑套餐对话框
const openEditLevelDialog = (level: MembershipLevel) => {
  isEditMode.value = true
  // 将数据填充到表单，并转换字节为 GB
  Object.assign(levelForm, {
    id: level.id,
    name: level.name,
    storageQuotaGB: level.storageQuota / (1024 * 1024 * 1024),
    maxFileSizeGB: level.maxFileSize / (1024 * 1024 * 1024),
    price: level.price,
    durationDays: level.durationDays,
    features: level.features,
  })
  dialogVisible.value = true
}

// 提交套餐表单（添加或编辑）
const submitForm = async () => {
  if (!levelFormRef.value) return

  try {
    formSubmitting.value = true
    await levelFormRef.value.validate()

    // 转换 GB 为字节，构建发送给后端的数据对象
    const payload: Partial<MembershipLevel> = {
      name: levelForm.name,
      storageQuota: Math.round(levelForm.storageQuotaGB * 1024 * 1024 * 1024),
      maxFileSize: Math.round(levelForm.maxFileSizeGB * 1024 * 1024 * 1024),
      price: levelForm.price,
      durationDays: levelForm.durationDays,
      features: levelForm.features,
    };

    let url = `/admin-api/membership-levels/add` // 新增
    let method = 'POST'
    let successMessage = '新增套餐成功！'

    if (isEditMode.value) {
      payload.id = levelForm.id; // 编辑时需要ID
      url = `/admin-api/membership-levels/update` // 更新
      method = 'PUT'
      successMessage = '编辑套餐成功！'
    }

    const response = await fetch(url, {
      method: method,
      headers: {
        'Content-Type': 'application/json',
        // 'Authorization': `Bearer ${localStorage.getItem('adminAccessToken')}` // 假设需要管理员Token
      },
      body: JSON.stringify(payload)
    })
    const result = await response.json()

    if (result.code === 200) {
      ElMessage.success(successMessage)
      dialogVisible.value = false
      fetchMembershipLevels() // 刷新列表
    } else {
      ElMessage.error(result.message || `${isEditMode.value ? '编辑' : '新增'}套餐失败！`)
    }
  } catch (error: any) {
    console.error('提交套餐表单错误:', error)
    ElMessage.error('操作失败，请检查网络或表单内容。')
  } finally {
    formSubmitting.value = false
  }
}

// 删除套餐
const deleteLevel = async (level: MembershipLevel) => {
  if (level.id === 'level-free') {
    ElMessage.warning('免费版套餐不可删除！');
    return;
  }
  ElMessageBox.confirm(
      `确定要删除套餐 "${level.name}" 吗？此操作可能会影响到已订阅此套餐的用户！`,
      '警告',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
      }
  )
      .then(async () => {
        try {
          const url = `/admin-api/membership-levels/delete/${level.id}`; // 对应后端的 DELETE 接口
          const response = await fetch(url, {
            method: 'DELETE',
            headers: {
              // 'Authorization': `Bearer ${localStorage.getItem('adminAccessToken')}`
            }
          })
          const result = await response.json()

          if (result.code === 200) {
            ElMessage.success('套餐删除成功！')
            fetchMembershipLevels() // 刷新列表
          } else {
            ElMessage.error(result.message || '套餐删除失败！')
          }
        } catch (error) {
          console.error('删除套餐错误:', error)
          ElMessage.error('删除失败，请检查网络。')
        }
      })
      .catch(() => {
        ElMessage.info('已取消删除操作。')
      })
}

// 清空并重置对话框表单
const resetDialogForm = () => {
  if (levelFormRef.value) {
    levelFormRef.value.resetFields()
  }
  Object.assign(levelForm, { // 确保清除所有数据并设置默认值
    id: '',
    name: '',
    storageQuotaGB: 1, // 默认 1GB
    maxFileSizeGB: 0.1, // 默认 0.1GB (100MB)
    price: 0,
    durationDays: 0,
    features: '',
  })
}

// 获取会员等级列表数据
const fetchMembershipLevels = async () => {
  loading.value = true
  try {
    const response = await fetch('/admin-api/membership-levels/getAll', { // 对应后端的 GET ALL 接口
      headers: {
        // 'Authorization': `Bearer ${localStorage.getItem('adminAccessToken')}`
      }
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const result = await response.json();

    if (result.code === 200) {
      // 确保后端返回的字段名与前端 interface MembershipLevel 的驼峰命名一致
      membershipLevels.value = result.data.map((level: any) => ({
        id: level.id,
        name: level.name,
        storageQuota: Number(level.storageQuota), // 确保是数字类型
        maxFileSize: Number(level.maxFileSize),   // 确保是数字类型
        price: Number(level.price),             // 确保是数字类型
        durationDays: Number(level.durationDays), // 确保是数字类型
        features: level.features,
        createdAt: level.createdAt,
        updatedAt: level.updatedAt,
      }));
    } else {
      ElMessage.error(result.message || '获取套餐列表失败！');
    }
  } catch (error) {
    console.error('获取会员等级列表失败:', error)
    ElMessage.error('获取套餐数据失败，请检查网络连接。')
  } finally {
    loading.value = false
  }
}

// 组件挂载时获取会员等级列表
onMounted(() => {
  fetchMembershipLevels()
})
</script>

<style scoped>
.admin-membership-levels-view {
  padding: 20px;
  background-color: #f0f2f5;
  min-height: calc(100vh - 100px);
  display: flex;
  flex-direction: column;
}

h2 {
  font-size: 24px;
  color: #333;
  margin-bottom: 20px;
}

.action-card {
  margin-bottom: 20px;
  padding: 15px 20px;
}

.table-card {
  flex: 1;
  display: flex;
  flex-direction: column;
}

/* 调整表格头部颜色和字体 */
.el-table :deep(.el-table__header-wrapper th) {
  background-color: #f5f7fa;
  color: #606266;
  font-weight: 600;
}

/* 调整输入框旁边的问号图标 */
.el-input-number + .el-tooltip .el-icon {
  margin-left: 8px;
  color: #909399;
  font-size: 16px;
  cursor: pointer;
}

.dialog-footer {
  text-align: right;
  padding-top: 20px;
}
</style>
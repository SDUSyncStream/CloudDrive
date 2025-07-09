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
        <el-table-column prop="storage_quota" label="存储配额" width="120">
          <template #default="{ row }">
            {{ formatFileSize(row.storage_quota) }}
          </template>
        </el-table-column>
        <el-table-column prop="max_file_size" label="最大单文件" width="120">
          <template #default="{ row }">
            {{ formatFileSize(row.max_file_size) }}
          </template>
        </el-table-column>
        <el-table-column prop="price" label="价格(¥)" width="100">
          <template #default="{ row }">
            {{ row.price.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="duration_days" label="时长(天)" width="100"></el-table-column>
        <el-table-column prop="features" label="特性描述" min-width="200" show-overflow-tooltip></el-table-column>
        <el-table-column prop="created_at" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.created_at) }}
          </template>
        </el-table-column>
        <el-table-column prop="updated_at" label="最后更新" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.updated_at) }}
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
        <el-form-item label="存储配额" prop="storage_quota_gb">
          <el-input-number
              v-model="levelForm.storage_quota_gb"
              :min="levelForm.id === 'level-free' ? 1 : 1"
              :max="1024 * 10"
              :step="1"
              placeholder="单位: GB"
          ></el-input-number> GB
          <el-tooltip content="用户可用的总存储空间（单位：GB）" placement="right">
            <el-icon><QuestionFilled /></el-icon>
          </el-tooltip>
        </el-form-item>
        <el-form-item label="最大单文件" prop="max_file_size_gb">
          <el-input-number
              v-model="levelForm.max_file_size_gb"
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
        <el-form-item label="时长" prop="duration_days">
          <el-input-number
              v-model="levelForm.duration_days"
              :min="levelForm.id === 'level-free' ? 0 : 1"
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
  QuestionFilled, // 疑问图标
} from '@element-plus/icons-vue'

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

// --- 数据接口定义 ---

interface MembershipLevel {
  id: string;
  name: string;
  storage_quota: number; // 字节
  max_file_size: number; // 字节
  price: number;
  duration_days: number;
  features: string;
  created_at: string;
  updated_at: string;
}

// --- 数据定义 ---

const membershipLevels = ref<MembershipLevel[]>([]) // 所有会员等级数据
const loading = ref(false) // 表格加载状态

// 对话框相关
const dialogVisible = ref(false)
const isEditMode = ref(false) // true: 编辑模式, false: 添加模式
const levelFormRef = ref<FormInstance>() // 对话框表单引用
const formSubmitting = ref(false) // 对话框表单提交状态

// 对话框表单数据 (storage_quota_gb 和 max_file_size_gb 是为了方便用户输入，实际存储为字节)
const levelForm = reactive({
  id: '',
  name: '',
  storage_quota_gb: 0, // GB 为单位，用于输入框
  max_file_size_gb: 0, // GB 为单位，用于输入框
  price: 0,
  duration_days: 0,
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
  storage_quota_gb: [
    { required: true, message: '请输入存储配额', trigger: 'blur' },
    { type: 'number', min: 1, message: '存储配额必须大于0', trigger: 'blur' }
  ],
  max_file_size_gb: [
    { required: true, message: '请输入最大单文件大小', trigger: 'blur' },
    { type: 'number', min: 0.1, message: '最大单文件大小必须大于0', trigger: 'blur' }
  ],
  price: [
    { required: true, message: '请输入价格', trigger: 'blur' },
    { type: 'number', min: 0, message: '价格不能为负数', trigger: 'blur' }
  ],
  duration_days: [
    { required: true, message: '请输入时长', trigger: 'blur' },
    { type: 'number', min: 0, message: '时长不能为负数', trigger: 'blur' },
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
    storage_quota_gb: level.storage_quota / (1024 * 1024 * 1024),
    max_file_size_gb: level.max_file_size / (1024 * 1024 * 1024),
    price: level.price,
    duration_days: level.duration_days,
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

    // 转换 GB 为字节
    const payload = {
      ...levelForm,
      storage_quota: Math.round(levelForm.storage_quota_gb * 1024 * 1024 * 1024),
      max_file_size: Math.round(levelForm.max_file_size_gb * 1024 * 1024 * 1024),
    } as Omit<MembershipLevel, 'created_at' | 'updated_at'>; // 临时类型断言，确保删除不需要的GB字段

    // 移除不应发送给后端的临时GB字段
    delete (payload as any).storage_quota_gb;
    delete (payload as any).max_file_size_gb;


    let url = '/api/admin/membership-levels'
    let method = 'POST'
    let message = '新增套餐成功！'

    if (isEditMode.value) {
      url = `/api/admin/membership-levels/${levelForm.id}`
      method = 'PUT'
      message = '编辑套餐成功！'
    } else {
      // 生成一个简单的UUID作为ID，实际应由后端生成
      payload.id = `level-${Date.now().toString(36)}`;
    }

    // 模拟 API 请求
    console.log(`Sending ${method} request to ${url} with payload:`, payload);
    const response = await new Promise(resolve => setTimeout(() => {
      // 模拟后端返回，如果名称重复则失败
      const isNameConflict = membershipLevels.value.some(
          level => level.name === payload.name && level.id !== payload.id
      );
      if (!isEditMode.value && isNameConflict) {
        resolve({ json: () => ({ code: 500, message: '套餐名称已存在' }) });
      } else {
        resolve({ json: () => ({ code: 200, message: message, data: { ...payload, created_at: new Date().toISOString(), updated_at: new Date().toISOString() } }) });
      }
    }, 800));
    const result = await (response as any).json();


    // const response = await fetch(url, {
    //   method: method,
    //   headers: {
    //     'Content-Type': 'application/json',
    //     'Authorization': `Bearer ${localStorage.getItem('adminAccessToken')}` // 假设需要管理员Token
    //   },
    //   body: JSON.stringify(payload)
    // })
    // const result = await response.json()

    if (result.code === 200) {
      ElMessage.success(message)
      dialogVisible.value = false
      fetchMembershipLevels() // 刷新列表
    } else {
      ElMessage.error(result.message || `${isEditMode.value ? '编辑' : '新增'}套餐失败！`)
    }
  } catch (error: any) {
    console.error('提交套餐表单错误:', error)
    if (error.isFormValidationError) {
      ElMessage.error('表单验证失败，请检查输入项。')
    } else {
      ElMessage.error('操作失败，请检查网络或表单内容。')
    }
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
          // 模拟 API 请求
          console.log(`Sending DELETE request to /api/admin/membership-levels/${level.id}`);
          const response = await new Promise(resolve => setTimeout(() => {
            resolve({ json: () => ({ code: 200, message: '删除成功' }) });
          }, 500));
          const result = await (response as any).json();

          // const response = await fetch(`/api/admin/membership-levels/${level.id}`, {
          //   method: 'DELETE',
          //   headers: {
          //     'Authorization': `Bearer ${localStorage.getItem('adminAccessToken')}`
          //   }
          // })
          // const result = await response.json()

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
    storage_quota_gb: 1, // 默认 1GB
    max_file_size_gb: 0.1, // 默认 0.1GB (100MB)
    price: 0,
    duration_days: 0,
    features: '',
  })
}

// 获取会员等级列表数据（模拟）
const fetchMembershipLevels = async () => {
  loading.value = true
  try {
    // 模拟从后端获取数据
    // const response = await fetch('/api/admin/membership-levels');
    // const data = await response.json();

    // 模拟你的 INSERT 语句数据
    const mockLevels: MembershipLevel[] = [
      { id: 'level-free', name: '免费版', storage_quota: 1073741824, max_file_size: 104857600, price: 0.00, duration_days: 0, features: '1GB存储空间,单文件100MB', created_at: '2023-01-01 00:00:00', updated_at: '2023-01-01 00:00:00' },
      { id: 'level-basic', name: '基础版', storage_quota: 10737418240, max_file_size: 1073741824, price: 9.99, duration_days: 30, features: '10GB存储空间,单文件1GB', created_at: '2023-01-01 00:00:00', updated_at: '2023-01-01 00:00:00' },
      { id: 'level-premium', name: '高级版', storage_quota: 107374182400, max_file_size: 5368709120, price: 19.99, duration_days: 30, features: '100GB存储空间,单文件5GB', created_at: '2023-01-01 00:00:00', updated_at: '2023-01-01 00:00:00' },
      { id: 'level-enterprise', name: '企业版', storage_quota: 1099511627776, max_file_size: 21474836480, price: 99.99, duration_days: 30, features: '1TB存储空间,单文件20GB', created_at: '2023-01-01 00:00:00', updated_at: '2023-01-01 00:00:00' },
      { id: 'level-pro', name: '专业版', storage_quota: 536870912000, max_file_size: 10737418240, price: 49.99, duration_days: 30, features: '500GB存储空间,单文件10GB,CDN加速', created_at: '2024-05-01 00:00:00', updated_at: '2024-05-01 00:00:00' },
    ]

    await new Promise(resolve => setTimeout(resolve, 500)) // 模拟网络延迟
    membershipLevels.value = mockLevels

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
<template>
  <div class="user-management-view">
    <h2>用户管理</h2>

    <el-card shadow="hover" class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户名/邮箱">
          <el-input v-model="searchForm.keyword" placeholder="输入用户名或邮箱" clearable></el-input>
        </el-form-item>
        <el-form-item label="用户等级">
          <el-select v-model="searchForm.userLevel" placeholder="选择用户等级" clearable>
            <el-option label="所有" value=""></el-option>
            <el-option label="普通用户" :value="0"></el-option>
            <el-option label="管理员" :value="1"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchUsers">
            <el-icon><Search /></el-icon> 搜索
          </el-button>
          <el-button @click="resetSearch">
            <el-icon><Refresh /></el-icon> 重置
          </el-button>
        </el-form-item>
        <el-button type="success" @click="openAddUserDialog">
          <el-icon><Plus /></el-icon> 添加新用户
        </el-button>
      </el-form>
      <div class="action-buttons">

      </div>
    </el-card>

    <el-card shadow="hover" class="table-card">
      <el-table
          :data="paginatedUsers"
          v-loading="loading"
          style="width: 100%"
          stripe
          border
          height="calc(100vh - 400px)"
          empty-text="暂无用户数据"
      >
        <el-table-column prop="id" label="ID" width="80" sortable></el-table-column>
        <el-table-column prop="username" label="用户名" width="150" show-overflow-tooltip></el-table-column>
        <el-table-column prop="email" label="邮箱" width="200" show-overflow-tooltip></el-table-column>
        <el-table-column prop="userlevel" label="用户等级" width="120" :formatter="formatUserLevel"></el-table-column>

        <el-table-column label="存储用量" width="180">
          <template #default="scope">
            <el-progress
                :percentage="calculateStoragePercentage(scope.row)"
                :color="getStorageColor(calculateStoragePercentage(scope.row))"
                :stroke-width="8"
                :format="() => `${formatFileSize(scope.row.storage_used)} / ${formatFileSize(scope.row.storage_quota)}`"
            />
          </template>
        </el-table-column>

        <el-table-column prop="created_at" label="注册时间" width="180" sortable>
          <template #default="scope">
            {{ formatTime(scope.row.created_at) }}
          </template>
        </el-table-column>
        <el-table-column prop="updated_at" label="最后更新" width="180" sortable>
          <template #default="scope">
            {{ formatTime(scope.row.updated_at) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" fixed="right" width="180">
          <template #default="scope">
            <el-button size="small" @click="openEditUserDialog(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteUser(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="filteredUsers.length"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          background
          class="pagination-container"
      ></el-pagination>
    </el-card>

    <el-dialog
        v-model="dialogVisible"
        :title="isEditMode ? '编辑用户' : '添加新用户'"
        width="500px"
        @closed="resetDialogForm"
    >
      <el-form :model="userForm" :rules="userFormRules" ref="userFormRef" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" :disabled="isEditMode"></el-input>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email"></el-input>
        </el-form-item>
        <el-form-item label="密码" :prop="isEditMode ? '' : 'password'">
          <el-input v-model="userForm.password" type="password" placeholder="留空则不修改密码" show-password></el-input>
        </el-form-item>
        <el-form-item label="用户等级" prop="userlevel">
          <el-select v-model="userForm.userlevel" placeholder="选择用户等级">
            <el-option label="普通用户" :value="0"></el-option>
            <el-option label="管理员" :value="1"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="存储配额" prop="storage_quota">
          <el-input-number
              v-model="userForm.storage_quota"
              :min="1"
              :max="1000"
              :step="10"
              placeholder="单位: GB"
          ></el-input-number> GB
          <el-tooltip content="用户可用的总存储空间（单位：GB）" placement="right">
            <el-icon><QuestionFilled /></el-icon>
          </el-tooltip>
        </el-form-item>
        <el-form-item label="已使用" prop="storage_used" v-if="isEditMode">
          <el-input-number
              v-model="userForm.storage_used"
              :min="0"
              :max="userForm.storage_quota"
              placeholder="单位: GB"
          ></el-input-number> GB
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitUserForm" :loading="formSubmitting">
            {{ isEditMode ? '保存' : '添加' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  Search,
  Refresh,
  Plus,
  QuestionFilled, // 疑问图标
  UserFilled, // 导入所需图标
  DataLine,
  Files,
  User,
} from '@element-plus/icons-vue'

// 假设你的 utils 文件中有这些格式化函数
import { formatFileSize } from '../utils'

// 为了模拟时间格式化，这里添加一个简单的函数
const formatTime = (timestamp: string) => {
  if (!timestamp) return '-'
  return new Date(timestamp).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}


// --- 数据定义 ---

// 搜索表单
const searchForm = reactive({
  keyword: '',
  userLevel: '' // 0: 普通用户, 1: 管理员, '': 所有
})

// 用户列表数据
interface User {
  id: string | number; // 根据你的ID类型
  userlevel: number;
  username: string;
  email: string;
  password_hash: string;
  avatar: string;
  storage_quota: number; // 单位：字节 (或根据你的数据库设计)
  storage_used: number;  // 单位：字节
  created_at: string;
  updated_at: string;
}

const users = ref<User[]>([])
const loading = ref(false) // 表格加载状态

// 分页
const currentPage = ref(1)
const pageSize = ref(10)

// 弹窗相关
const dialogVisible = ref(false)
const isEditMode = ref(false) // true: 编辑模式, false: 添加模式
const userFormRef = ref<FormInstance>() // 弹窗表单引用
const formSubmitting = ref(false) // 弹窗表单提交状态

const userForm = reactive({
  id: '',
  username: '',
  email: '',
  password: '', // 密码在编辑时可选填，添加时必填
  userlevel: 0,
  storage_quota: 10, // 默认10GB
  storage_used: 0, // 新增用户默认为0
})

// 弹窗表单验证规则
const userFormRules: FormRules = reactive({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' },
    // 可以在这里添加自定义验证，例如检查用户名是否已存在
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' }
  ],
  password: [
    { validator: (rule, value, callback) => {
        if (!isEditMode.value && !value) { // 添加模式下密码必填
          callback(new Error('请输入密码'))
        } else if (value && value.length < 6) { // 有密码输入时，至少6位
          callback(new Error('密码长度至少6位'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  userlevel: [
    { required: true, message: '请选择用户等级', trigger: 'change' }
  ],
  storage_quota: [
    { required: true, message: '请输入存储配额', trigger: 'blur' },
    { type: 'number', min: 1, max: 1000, message: '配额范围1-1000 GB', trigger: 'blur' }
  ],
  storage_used: [
    { validator: (rule, value, callback) => {
        if (value > userForm.storage_quota) {
          callback(new Error('已使用空间不能超过存储配额'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
})


// --- 计算属性 ---

// 根据搜索条件过滤用户
const filteredUsers = computed(() => {
  return users.value.filter(user => {
    const matchesKeyword = (user.username?.includes(searchForm.keyword) || user.email?.includes(searchForm.keyword))
    const matchesUserLevel = (searchForm.userLevel === '' || user.userlevel === searchForm.userLevel)
    return matchesKeyword && matchesUserLevel
  })
})

// 分页后的用户数据
const paginatedUsers = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredUsers.value.slice(start, end)
})

// --- 方法 ---

// 格式化用户等级显示
const formatUserLevel = (row: User) => {
  return row.userlevel === 1 ? '管理员' : '普通用户'
}

// 计算存储百分比
const calculateStoragePercentage = (row: User) => {
  if (row.storage_quota === 0) return 0;
  return Math.round((row.storage_used / row.storage_quota) * 100)
}

// 获取存储进度条颜色
const getStorageColor = (percentage: number) => {
  if (percentage < 50) return '#67C23A'
  if (percentage < 80) return '#E6A23C'
  return '#F56C6C'
}

// 搜索用户
const searchUsers = () => {
  currentPage.value = 1 // 搜索时重置回第一页
  // 实际项目中，这里会调用 API 重新获取数据
  fetchUsers()
}

// 重置搜索条件
const resetSearch = () => {
  searchForm.keyword = ''
  searchForm.userLevel = ''
  searchUsers() // 重置后重新搜索
}

// 打开添加用户对话框
const openAddUserDialog = () => {
  isEditMode.value = false
  resetDialogForm() // 清空表单
  dialogVisible.value = true
}

// 打开编辑用户对话框
const openEditUserDialog = (user: User) => {
  isEditMode.value = true
  // 将用户数据填充到表单
  Object.assign(userForm, {
    id: user.id,
    username: user.username,
    email: user.email,
    password: '', // 编辑时不显示旧密码，留空则不修改
    userlevel: user.userlevel,
    storage_quota: user.storage_quota / (1024 * 1024 * 1024), // 字节转GB
    storage_used: user.storage_used / (1024 * 1024 * 1024), // 字节转GB
  })
  dialogVisible.value = true
}

// 提交用户表单（添加或编辑）
const submitUserForm = async () => {
  if (!userFormRef.value) return

  try {
    formSubmitting.value = true
    await userFormRef.value.validate()

    const payload = { ...userForm }
    // 将GB转换为字节
    payload.storage_quota = payload.storage_quota * 1024 * 1024 * 1024
    payload.storage_used = payload.storage_used * 1024 * 1024 * 1024

    let url = '/api/admin/users'
    let method = 'POST'

    if (isEditMode.value) {
      url = `/api/admin/users/${userForm.id}`
      method = 'PUT'
      if (payload.password === '') {
        delete payload.password // 如果密码为空，不发送密码字段
      }
    } else {
      // 添加模式下，如果密码为空，不应通过验证，这里做个双重保险
      if (payload.password === '') {
        ElMessage.error('添加用户时密码不能为空！');
        return;
      }
    }

    // 模拟 API 请求
    console.log(`Sending ${method} request to ${url} with payload:`, payload);
    const response = await new Promise(resolve => setTimeout(() => {
      if (payload.username === 'testuser' && !isEditMode.value) {
        resolve({ json: () => ({ code: 500, message: '用户名已存在' }) });
      } else {
        resolve({ json: () => ({ code: 200, message: '操作成功', data: { ...payload, id: isEditMode.value ? payload.id : 'new-user-id-' + Date.now(), created_at: new Date().toISOString(), updated_at: new Date().toISOString() } }) });
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
      ElMessage.success(`${isEditMode.value ? '编辑' : '添加'}用户成功！`)
      dialogVisible.value = false
      fetchUsers() // 刷新用户列表
    } else {
      ElMessage.error(result.message || `${isEditMode.value ? '编辑' : '添加'}用户失败！`)
    }
  } catch (error: any) {
    console.error('提交用户表单错误:', error)
    if (error.isFormValidationError) { // Element Plus 验证错误会有 `isFormValidationError` 属性
      ElMessage.error('表单验证失败，请检查输入项。')
    } else {
      ElMessage.error('操作失败，请检查网络或表单内容。')
    }
  } finally {
    formSubmitting.value = false
  }
}

// 删除用户
const deleteUser = async (user: User) => {
  ElMessageBox.confirm(
      `确定要删除用户 "${user.username}" 吗？此操作不可逆！`,
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
          console.log(`Sending DELETE request to /api/admin/users/${user.id}`);
          const response = await new Promise(resolve => setTimeout(() => {
            resolve({ json: () => ({ code: 200, message: '删除成功' }) });
          }, 500));
          const result = await (response as any).json();

          // const response = await fetch(`/api/admin/users/${user.id}`, {
          //   method: 'DELETE',
          //   headers: {
          //     'Authorization': `Bearer ${localStorage.getItem('adminAccessToken')}`
          //   }
          // })
          // const result = await response.json()

          if (result.code === 200) {
            ElMessage.success('用户删除成功！')
            fetchUsers() // 刷新用户列表
          } else {
            ElMessage.error(result.message || '用户删除失败！')
          }
        } catch (error) {
          console.error('删除用户错误:', error)
          ElMessage.error('删除失败，请检查网络。')
        }
      })
      .catch(() => {
        ElMessage.info('已取消删除操作。')
      })
}

// 清空并重置对话框表单
const resetDialogForm = () => {
  if (userFormRef.value) {
    userFormRef.value.resetFields()
  }
  Object.assign(userForm, { // 确保清除所有数据
    id: '',
    username: '',
    email: '',
    password: '',
    userlevel: 0,
    storage_quota: 10,
    storage_used: 0,
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

// 获取用户列表数据（模拟）
const fetchUsers = async () => {
  loading.value = true
  try {
    // 模拟从后端获取数据
    // 实际的API请求可能是：
    // const response = await fetch('/api/admin/users?keyword=${searchForm.keyword}&userLevel=${searchForm.userLevel}');
    // const data = await response.json();

    // 模拟你的数据库截图数据
    const mockUsers: User[] = [
      { id: '1', userlevel: 0, username: 'Ono Kasumi', email: 'kona@outlook.com', password_hash: 'y1kSGuGgul', avatar: 'ZUu0RtL2bx', storage_quota: 658 * 1024 * 1024, storage_used: 394 * 1024 * 1024, created_at: '2019-08-01 08:36:59', updated_at: '2025-07-08 15:19:33' },
      { id: '2', userlevel: 0, username: 'Eleanor Henders', email: 'ehenderson@mail.com', password_hash: '6no7mq7l1', avatar: 'jl4SwNBVCK', storage_quota: 528 * 1024 * 1024, storage_used: 201 * 1024 * 1024, created_at: '2014-03-23 12:21:39', updated_at: '2025-07-08 15:19:34' },
      { id: '3', userlevel: 0, username: 'Saito Ikki', email: 'ikki4l2@icloud.com', password_hash: '0xMotWoqxw', avatar: 'VQv9mNkpA', storage_quota: 887 * 1024 * 1024, storage_used: 870 * 1024 * 1024, created_at: '2008-11-28 09:35:52', updated_at: '2025-07-08 15:19:36' },
      { id: 'admin-user-id', userlevel: 1, username: 'admin', email: 'admin@clouddrive.com', password_hash: '0xMotWoqxw', avatar: 'ninii', storage_quota: 1073741824, storage_used: 4534, created_at: '2025-07-08 11:54:48', updated_at: '2025-07-08 17:24:45' },
      // 额外一些模拟数据
      { id: '4', userlevel: 0, username: 'Wang Li', email: 'wangli@example.com', password_hash: 'hash123', avatar: 'abc', storage_quota: 10 * 1024 * 1024 * 1024, storage_used: 2 * 1024 * 1024 * 1024, created_at: '2024-01-15 10:00:00', updated_at: '2025-07-01 10:00:00' },
      { id: '5', userlevel: 0, username: 'Zhang San', email: 'zhangsan@example.com', password_hash: 'hash456', avatar: 'def', storage_quota: 20 * 1024 * 1024 * 1024, storage_used: 18 * 1024 * 1024 * 1024, created_at: '2023-05-20 11:30:00', updated_at: '2025-07-05 11:30:00' },
      { id: '6', userlevel: 1, username: 'super_admin', email: 'super@clouddrive.com', password_hash: 'superhash', avatar: 'xyz', storage_quota: 50 * 1024 * 1024 * 1024, storage_used: 10 * 1024 * 1024 * 1024, created_at: '2022-03-01 09:00:00', updated_at: '2025-07-08 10:00:00' },
      { id: '7', userlevel: 0, username: 'Liu Ming', email: 'liuming@example.com', password_hash: 'hash789', avatar: 'ghi', storage_quota: 5 * 1024 * 1024 * 1024, storage_used: 4 * 1024 * 1024 * 1024, created_at: '2024-11-11 14:00:00', updated_at: '2025-07-02 14:00:00' },
      { id: '8', userlevel: 0, username: 'Chen Ying', email: 'chenying@example.com', password_hash: 'hashabc', avatar: 'jkl', storage_quota: 15 * 1024 * 1024 * 1024, storage_used: 5 * 1024 * 1024 * 1024, created_at: '2023-01-01 08:00:00', updated_at: '2025-06-28 08:00:00' },
      { id: '9', userlevel: 0, username: 'Sun Hao', email: 'sunhao@example.com', password_hash: 'hashdef', avatar: 'mno', storage_quota: 8 * 1024 * 1024 * 1024, storage_used: 7 * 1024 * 1024 * 1024, created_at: '2024-03-03 16:00:00', updated_at: '2025-07-07 16:00:00' },
      { id: '10', userlevel: 0, username: 'Zhao Wei', email: 'zhaowei@example.com', password_hash: 'hashghi', avatar: 'pqr', storage_quota: 12 * 1024 * 1024 * 1024, storage_used: 6 * 1024 * 1024 * 1024, created_at: '2023-08-08 12:00:00', updated_at: '2025-07-03 12:00:00' },
      { id: '11', userlevel: 0, username: 'Zhou Jie', email: 'zhoujie@example.com', password_hash: 'hashjkl', avatar: 'stu', storage_quota: 7 * 1024 * 1024 * 1024, storage_used: 2 * 1024 * 1024 * 1024, created_at: '2024-02-20 09:00:00', updated_at: '2025-06-30 09:00:00' },
    ]

    await new Promise(resolve => setTimeout(resolve, 500)) // 模拟网络延迟
    users.value = mockUsers

  } catch (error) {
    console.error('获取用户列表失败:', error)
    ElMessage.error('获取用户数据失败，请检查网络连接。')
  } finally {
    loading.value = false
  }
}

// 组件挂载时获取用户列表
onMounted(() => {
  fetchUsers()
})
</script>

<style scoped>
.user-management-view {
  padding: 20px;
  background-color: #f0f2f5;
  min-height: calc(100vh - 100px); /* 留出头部和底部空间 */
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

.action-buttons {
  display: flex;
  justify-content: flex-start; /* 左对齐 */
}

.table-card {
  /* 确保表格卡片可以撑满剩余高度，并内部滚动 */
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

/* 调整存储用量进度条旁边的问号图标 */
.el-input-number + .el-tooltip .el-icon {
  margin-left: 8px;
  color: #909399;
  font-size: 16px;
  cursor: pointer;
}


.pagination-container {
  margin-top: 20px;
  justify-content: flex-end; /* 分页组件靠右显示 */
  display: flex;
}

/* 对话框底部按钮的样式 */
.dialog-footer {
  text-align: right;
  padding-top: 20px; /* 给按钮留出空间 */
}
</style>
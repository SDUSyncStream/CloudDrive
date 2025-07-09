<template>
  <div class="user-management-view">
    <h2>用户管理</h2>

    <el-card shadow="hover" class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户名/邮箱">
          <el-input v-model="searchForm.keyword" placeholder="输入用户名或邮箱" clearable></el-input>
        </el-form-item>
        <el-form-item label="用户等级">
          <el-select v-model="searchForm.userLevel" placeholder="选择用户等级" clearable style="width: 150px;">
            <el-option label="所有" value=""></el-option>
            <el-option label="普通用户" :value="0"></el-option>
            <el-option label="管理员" :value="1"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchUsers">
            <el-icon><Search /></el-icon> 搜索
          </el-button>

        </el-form-item>
        <el-button type="success" @click="openAddUserDialog">
          <el-icon><Plus /></el-icon> 添加新用户
        </el-button>
      </el-form>
    </el-card>

    <el-card shadow="hover" class="table-card">
      <el-table
          :data="paginatedUsers"
          v-loading="loading"
          style="width: 100%"
          stripe
          border
          height="calc(100vh - 250px)" empty-text="暂无用户数据"
      >
        <el-table-column prop="id" label="ID" width="100" sortable show-overflow-tooltip></el-table-column>
        <el-table-column prop="username" label="用户名" width="150" show-overflow-tooltip></el-table-column>
        <el-table-column prop="email" label="邮箱" width="200" show-overflow-tooltip></el-table-column>
        <el-table-column prop="userlevel" label="用户等级" width="120" :formatter="formatUserLevel"></el-table-column>

        <el-table-column label="存储用量" width="180">
          <template #default="scope">
            <el-progress
                :percentage="calculateStoragePercentage(scope.row)"
                :color="getStorageColor(calculateStoragePercentage(scope.row))"
                :stroke-width="8"
                :format="() => `${formatFileSize(scope.row.storageUsed)} / ${formatFileSize(scope.row.storageQuota)}`"
            />
          </template>
        </el-table-column>

        <el-table-column prop="createdAt" label="注册时间" width="180" sortable>
          <template #default="scope">
            {{ formatDateTime(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="updatedAt" label="最后更新" width="180" sortable>
          <template #default="scope">
            {{ formatDateTime(scope.row.updatedAt) }}
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
          <el-input v-model="userForm.password" type="password" placeholder="请输入密码" show-password></el-input>
        </el-form-item>
        <el-form-item label="用户等级" prop="userlevel" >
          <el-select v-model="userForm.userlevel" placeholder="选择用户等级">
            <el-option label="普通用户" :value="0"></el-option>
            <el-option label="管理员" :value="1"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="存储配额" prop="storageQuotaGB">
          <el-input-number
              v-model="userForm.storageQuotaGB"
              :min="1"
              :max="1000"
              :step="10"
              placeholder="单位: GB"
          ></el-input-number> GB
          <el-tooltip content="用户可用的总存储空间（单位：GB）" placement="right">
            <el-icon><QuestionFilled /></el-icon>
          </el-tooltip>
        </el-form-item>
        <el-form-item label="已使用" prop="storageUsedGB" v-if="isEditMode">
          <el-input-number
              v-model="userForm.storageUsedGB"
              :min="0"
              :max="userForm.storageQuotaGB"
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
  QuestionFilled,
} from '@element-plus/icons-vue'

// 假设你的 utils 文件中有这些格式化函数
import { formatFileSize } from '../utils'

// 用于格式化日期时间 (适配 LocalDateTime)
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

// --- 数据接口定义 ---
// 与后端 User 实体类保持一致 (驼峰命名)
interface User {
  id: string;
  userlevel: number;
  username: string;
  email: string;
  passwordHash?: string; // 密码哈希，可选，因为更新时可能不传
  avatar?: string;
  storageQuota: number; // 对应 Long
  storageUsed: number;  // 对应 Long
  createdAt: string;    // 对应 LocalDateTime
  updatedAt: string;    // 对应 LocalDateTime
}

// --- 数据定义 ---

// 搜索表单
const searchForm = reactive({
  keyword: '',
  userLevel: '' // 0: 普通用户, 1: 管理员, '': 所有
})

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
  password: '', // 用于前端输入密码，后端自行处理哈希
  userlevel: 0,
  storageQuotaGB: 10, // 默认10GB，用于前端GB单位输入
  storageUsedGB: 0, // 新增用户默认已用空间为0，用于前端GB单位输入
})

// 弹窗表单验证规则
const userFormRules: FormRules = reactive({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' },
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
  storageQuotaGB: [
    { required: true, message: '请输入存储配额', trigger: 'blur' },
    { type: 'number', min: 1, max: 1000, message: '配额范围1-1000 GB', trigger: 'blur' }
  ],
  storageUsedGB: [
    { validator: (rule, value, callback) => {
        // 只有编辑模式下才需要验证已使用空间
        if (isEditMode.value) {
          if (value === undefined || value === null) {
            callback(new Error('请输入已使用空间'));
          } else if (value !== undefined && value > userForm.storageQuotaGB) { // 检查值是否为undefined before comparison
            callback(new Error('已使用空间不能超过存储配额'))
          } else {
            callback()
          }
        } else {
          callback(); // 新增模式不验证这个字段
        }
      },
      trigger: 'blur'
    }
  ]
})

// --- 计算属性 ---

// 根据搜索条件过滤用户 (这里假设getAllUsers返回所有用户，前端再过滤)
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
  if (row.storageQuota === 0) return 0;
  return Math.round((row.storageUsed / row.storageQuota) * 100)
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
  // fetchUsers 已经会根据过滤后的 users.value 进行分页和展示
}

// 重置搜索条件
const resetSearch = () => {
  searchForm.keyword = ''
  searchForm.userLevel = ''
  currentPage.value = 1; // 重置搜索也重置到第一页
  fetchUsers();
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
  // 将用户数据填充到表单，并转换字节为GB
  Object.assign(userForm, {
    id: user.id,
    username: user.username,
    email: user.email,
    password: '', // 编辑时不显示旧密码，留空则不修改
    userlevel: user.userlevel,
    storageQuotaGB: user.storageQuota / (1024 * 1024 * 1024), // 字节转GB
    storageUsedGB: user.storageUsed / (1024 * 1024 * 1024), // 字节转GB
  })
  dialogVisible.value = true
}

// 提交用户表单（添加或编辑）
const submitUserForm = async () => {
  if (!userFormRef.value) return

  try {
    formSubmitting.value = true
    await userFormRef.value.validate()

    // 构建发送给后端的数据对象，字段名与后端 User 实体类保持一致 (驼峰命名)
    const payload: Partial<User> = { // Partial 使所有属性变为可选，因为新增时不传ID
      username: userForm.username,
      email: userForm.email,
      userlevel: userForm.userlevel,
      storageQuota: Math.round(userForm.storageQuotaGB * 1024 * 1024 * 1024), // GB转字节
    };

    // 密码字段处理
    if (userForm.password !== '') {
      payload.passwordHash = userForm.password; // 假设后端会将此字段作为密码进行哈希
    }

    let url = '';
    let method = '';
    let successMessage = '';

    if (isEditMode.value) {
      payload.id = userForm.id; // 编辑时需要ID
      payload.storageUsed = Math.round(userForm.storageUsedGB * 1024 * 1024 * 1024); // 编辑时修改已用空间
      url = '/admin-api/updateUser'; // <-- 修改点：API前缀
      method = 'PUT';
      successMessage = '编辑用户成功！';
    } else {
      // 新增用户默认已用空间为0字节
      payload.storageUsed = 0;
      url = '/admin-api/insertUser'; // <-- 修改点：API前缀
      method = 'POST';
      successMessage = '添加用户成功！';
      // 注意: 后端 insertUser 会自动生成ID, 前端不需要传递ID
      // 但为了匹配后端的@RequestBody User user，我们仍传递完整的User对象，后端会忽略ID。
    }

    const response = await fetch(url, {
      method: method,
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(payload)
    });

    const result = await response.json();

    if (result.code === 200) {
      ElMessage.success(successMessage);
      dialogVisible.value = false;
      fetchUsers(); // 刷新用户列表
    } else {
      ElMessage.error(result.message || `${isEditMode.value ? '编辑' : '添加'}用户失败！`);
    }

  } catch (error: any) {
    console.error('提交用户表单错误:', error);
    ElMessage.error('操作失败，请检查网络或表单内容。');
  } finally {
    formSubmitting.value = false;
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
          const url = `/admin-api/deleteUser/${user.id}`; // <-- 修改点：API前缀
          const response = await fetch(url, {
            method: 'DELETE',
            headers: {
              // 无需Authorization头
            }
          });

          const result = await response.json();

          if (result.code === 200) {
            ElMessage.success('用户删除成功！');
            fetchUsers(); // 刷新用户列表
          } else {
            ElMessage.error(result.message || '用户删除失败！');
          }
        } catch (error) {
          console.error('删除用户错误:', error);
          ElMessage.error('删除失败，请检查网络。');
        }
      })
      .catch(() => {
        ElMessage.info('已取消删除操作。');
      });
}

// 清空并重置对话框表单
const resetDialogForm = () => {
  if (userFormRef.value) {
    userFormRef.value.resetFields()
  }
  Object.assign(userForm, { // 确保清除所有数据并设置默认值
    id: '',
    username: '',
    email: '',
    password: '',
    userlevel: 0,
    storageQuotaGB: 10,
    storageUsedGB: 0,
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

// 获取用户列表数据
const fetchUsers = async () => {
  loading.value = true;
  try {
    const response = await fetch('/admin-api/getAllUsers', { // <-- 修改点：API前缀
      headers: {
        // 无需Authorization头
      }
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const result = await response.json();

    if (result.code === 200) {
      // 这里的 result.data 应该直接是 List<User>
      users.value = result.data.map((user: any) => ({
        ...user,
        // 确保字段名与前端User接口的驼峰命名一致
        createdAt: user.createdAt,
        updatedAt: user.updatedAt,
        storageQuota: Number(user.storageQuota),
        storageUsed: Number(user.storageUsed)
      }));
    } else {
      ElMessage.error(result.message || '获取用户列表失败！');
    }
  } catch (error) {
    console.error('获取用户列表失败:', error);
    ElMessage.error('获取用户数据失败，请检查网络连接。');
  } finally {
    loading.value = false;
  }
}

// 由于没有 accessToken 相关的重定向，这里不需要 useRouter 实例
// import { useRouter } from 'vue-router'; // 此行可以删除
// const router = useRouter(); // 此行可以删除

// 组件挂载时获取用户列表
onMounted(() => {
  fetchUsers();
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
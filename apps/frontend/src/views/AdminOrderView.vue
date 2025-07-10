<template>
  <div class="admin-subscriptions-view">
    <h2>订阅管理</h2>

    <!-- 搜索和筛选区域 -->
    <el-card shadow="hover" class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户ID/用户名">
          <el-input v-model="searchForm.userIdOrName" placeholder="输入用户ID或用户名" clearable></el-input>
        </el-form-item>
        <el-form-item label="会员等级">
          <el-select v-model="searchForm.membershipLevelId" placeholder="选择会员等级" clearable>
            <el-option label="所有" value=""></el-option>
            <el-option
                v-for="level in membershipLevels"
                :key="level.id"
                :label="level.name"
                :value="level.id"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="订阅状态">
          <el-select v-model="searchForm.status" placeholder="选择订阅状态" clearable>
            <el-option label="所有" value=""></el-option>
            <el-option label="活跃" value="active"></el-option>
            <el-option label="已过期" value="expired"></el-option>
            <el-option label="已取消" value="cancelled"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchSubscriptions">
            <el-icon><Search /></el-icon> 搜索
          </el-button>
          <el-button @click="resetSearch">
            <el-icon><Refresh /></el-icon> 重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 订阅列表表格 -->
    <el-card shadow="hover" class="table-card">
      <el-table
          :data="paginatedSubscriptions"
          v-loading="loading"
          style="width: 100%"
          stripe
          border
          height="calc(100vh - 250px)"
          empty-text="暂无订阅数据"
      >
        <el-table-column prop="id" label="订阅ID" width="100" show-overflow-tooltip></el-table-column>
        <el-table-column prop="user_id" label="用户" width="150" show-overflow-tooltip>
          <template #default="{ row }">
            {{ getUserNameById(row.user_id) || row.user_id }}
          </template>
        </el-table-column>
        <el-table-column prop="membership_level_id" label="会员等级" width="120">
          <template #default="{ row }">
            {{ getMembershipLevelName(row.membership_level_id) }}
          </template>
        </el-table-column>
        <el-table-column prop="start_date" label="开始日期" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.start_date) }}
          </template>
        </el-table-column>
        <el-table-column prop="end_date" label="结束日期" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.end_date) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getSubscriptionStatusTagType(row.status)">
              {{ formatSubscriptionStatus(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="payment_amount" label="支付金额" width="100">
          <template #default="{ row }">
            {{ row.payment_amount ? `¥${row.payment_amount.toFixed(2)}` : '--' }}
          </template>
        </el-table-column>
        <el-table-column prop="payment_method" label="支付方式" width="100"></el-table-column>
        <el-table-column prop="created_at" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.created_at) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" fixed="right" width="180">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="viewSubscriptionDetails(row)">详情</el-button>
            <el-button link type="success" size="small" v-if="row.status !== 'active'" @click="extendSubscription(row)">延长</el-button>
            <el-button link type="danger" size="small" v-if="row.status === 'active'" @click="cancelSubscription(row)">取消</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="filteredSubscriptions.length"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          background
          class="pagination-container"
      ></el-pagination>
    </el-card>

    <!-- 订阅详情对话框 -->
    <el-dialog
        v-model="detailsDialogVisible"
        title="订阅详情"
        width="600px"
    >
      <div v-if="currentSubscriptionDetails">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订阅ID">{{ currentSubscriptionDetails.id }}</el-descriptions-item>
          <el-descriptions-item label="用户ID">{{ currentSubscriptionDetails.user_id }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ getUserNameById(currentSubscriptionDetails.user_id) || currentSubscriptionDetails.user_id }}</el-descriptions-item>
          <el-descriptions-item label="会员等级">{{ getMembershipLevelName(currentSubscriptionDetails.membership_level_id) }}</el-descriptions-item>
          <el-descriptions-item label="开始日期">{{ formatDateTime(currentSubscriptionDetails.start_date) }}</el-descriptions-item>
          <el-descriptions-item label="结束日期">{{ formatDateTime(currentSubscriptionDetails.end_date) }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getSubscriptionStatusTagType(currentSubscriptionDetails.status)">
              {{ formatSubscriptionStatus(currentSubscriptionDetails.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="支付金额">{{ currentSubscriptionDetails.payment_amount ? `¥${currentSubscriptionDetails.payment_amount.toFixed(2)}` : '--' }}</el-descriptions-item>
          <el-descriptions-item label="支付方式">{{ currentSubscriptionDetails.payment_method || '--' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDateTime(currentSubscriptionDetails.created_at) }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ formatDateTime(currentSubscriptionDetails.updated_at) }}</el-descriptions-item>
        </el-descriptions>
        <el-divider />
        <h3>会员等级详情</h3>
        <el-descriptions :column="1" border v-if="currentMembershipLevelDetails">
          <el-descriptions-item label="等级名称">{{ currentMembershipLevelDetails.name }}</el-descriptions-item>
          <el-descriptions-item label="存储配额">{{ formatFileSize(currentMembershipLevelDetails.storage_quota) }}</el-descriptions-item>
          <el-descriptions-item label="最大单文件">{{ formatFileSize(currentMembershipLevelDetails.max_file_size) }}</el-descriptions-item>
          <el-descriptions-item label="价格">{{ `¥${currentMembershipLevelDetails.price.toFixed(2)} / ${currentMembershipLevelDetails.duration_days}天` }}</el-descriptions-item>
          <el-descriptions-item label="特性">{{ currentMembershipLevelDetails.features }}</el-descriptions-item>
        </el-descriptions>
        <p v-else>无法加载会员等级详情。</p>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailsDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  Refresh,
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

type SubscriptionStatus = 'active' | 'expired' | 'cancelled';

interface UserSubscription {
  id: string;
  user_id: string;
  membership_level_id: string;
  start_date: string;
  end_date: string;
  status: SubscriptionStatus;
  payment_method: string | null;
  payment_amount: number | null;
  created_at: string;
  updated_at: string;
}

// --- 数据定义 ---

// 搜索表单
const searchForm = reactive({
  userIdOrName: '',
  membershipLevelId: '',
  status: '',
})

const subscriptions = ref<UserSubscription[]>([]) // 所有订阅数据
const loading = ref(false) // 表格加载状态

// 分页
const currentPage = ref(1)
const pageSize = ref(10)

// 详情对话框
const detailsDialogVisible = ref(false)
const currentSubscriptionDetails = ref<UserSubscription | null>(null)
const currentMembershipLevelDetails = ref<MembershipLevel | null>(null)

// 模拟的会员等级数据 (来自你的 INSERT 语句)
const membershipLevels = ref<MembershipLevel[]>([
  { id: 'level-free', name: '免费版', storage_quota: 1073741824, max_file_size: 104857600, price: 0.00, duration_days: 0, features: '1GB存储空间,单文件100MB', created_at: '2023-01-01 00:00:00', updated_at: '2023-01-01 00:00:00' },
  { id: 'level-basic', name: '基础版', storage_quota: 10737418240, max_file_size: 1073741824, price: 9.99, duration_days: 30, features: '10GB存储空间,单文件1GB', created_at: '2023-01-01 00:00:00', updated_at: '2023-01-01 00:00:00' },
  { id: 'level-premium', name: '高级版', storage_quota: 107374182400, max_file_size: 5368709120, price: 19.99, duration_days: 30, features: '100GB存储空间,单文件5GB', created_at: '2023-01-01 00:00:00', updated_at: '2023-01-01 00:00:00' },
  { id: 'level-enterprise', name: '企业版', storage_quota: 1099511627776, max_file_size: 21474836480, price: 99.99, duration_days: 30, features: '1TB存储空间,单文件20GB', created_at: '2023-01-01 00:00:00', updated_at: '2023-01-01 00:00:00' },
])

// 模拟的用户ID到用户名的映射 (可复用自用户管理页面)
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

// 根据搜索条件过滤订阅
const filteredSubscriptions = computed(() => {
  return subscriptions.value.filter(sub => {
    const matchesUserIdOrName = !searchForm.userIdOrName ||
        sub.user_id?.includes(searchForm.userIdOrName) ||
        (userMap.value[sub.user_id]?.includes(searchForm.userIdOrName));

    const matchesMembershipLevel = searchForm.membershipLevelId === '' || sub.membership_level_id === searchForm.membershipLevelId;

    const matchesStatus = searchForm.status === '' || sub.status === searchForm.status;

    return matchesUserIdOrName && matchesMembershipLevel && matchesStatus;
  })
})

// 分页后的订阅数据
const paginatedSubscriptions = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredSubscriptions.value.slice(start, end)
})

// --- 方法 ---

// 根据用户ID获取用户名
const getUserNameById = (userId: string) => {
  return userMap.value[userId] || '未知用户';
}

// 根据会员等级ID获取会员等级名称
const getMembershipLevelName = (levelId: string) => {
  const level = membershipLevels.value.find(l => l.id === levelId);
  return level ? level.name : '未知等级';
}

// 格式化订阅状态
const formatSubscriptionStatus = (status: SubscriptionStatus) => {
  switch (status) {
    case 'active': return '活跃';
    case 'expired': return '已过期';
    case 'cancelled': return '已取消';
    default: return '未知';
  }
}

// 获取订阅状态标签类型
const getSubscriptionStatusTagType = (status: SubscriptionStatus) => {
  switch (status) {
    case 'active': return 'success';
    case 'expired': return 'info';
    case 'cancelled': return 'danger';
    default: return 'info';
  }
}

// 搜索订阅
const searchSubscriptions = () => {
  currentPage.value = 1 // 搜索时重置回第一页
  fetchSubscriptions() // 实际项目中会调用 API 重新获取数据
}

// 重置搜索条件
const resetSearch = () => {
  searchForm.userIdOrName = ''
  searchForm.membershipLevelId = ''
  searchForm.status = ''
  searchSubscriptions() // 重置后重新搜索
}

// 查看订阅详情
const viewSubscriptionDetails = (subscription: UserSubscription) => {
  currentSubscriptionDetails.value = subscription;
  currentMembershipLevelDetails.value = membershipLevels.value.find(
      level => level.id === subscription.membership_level_id
  ) || null;
  detailsDialogVisible.value = true;
}

// 延长订阅 (模拟操作)
const extendSubscription = async (subscription: UserSubscription) => {
  ElMessageBox.confirm(
      `确定要延长用户 "${getUserNameById(subscription.user_id)}" 的 "${getMembershipLevelName(subscription.membership_level_id)}" 订阅吗？`,
      '延长订阅',
      {
        confirmButtonText: '确定延长',
        cancelButtonText: '取消',
        type: 'info',
      }
  )
      .then(async () => {
        try {
          // 模拟 API 请求：延长订阅
          console.log(`Sending extend request for subscription: ${subscription.id}`);
          // 实际 API: await fetch(`/api/admin/subscriptions/extend/${subscription.id}`, { method: 'PUT', headers: { Authorization: 'Bearer ...' } });

          await new Promise(resolve => setTimeout(resolve, 500)); // 模拟延迟

          ElMessage.success('订阅已成功延长！')
          fetchSubscriptions() // 刷新列表
        } catch (error) {
          console.error('延长订阅错误:', error)
          ElMessage.error('延长订阅失败。')
        }
      })
      .catch(() => {})
}

// 取消订阅 (模拟操作)
const cancelSubscription = async (subscription: UserSubscription) => {
  ElMessageBox.confirm(
      `确定要取消用户 "${getUserNameById(subscription.user_id)}" 的 "${getMembershipLevelName(subscription.membership_level_id)}" 订阅吗？此操作不可逆！`,
      '取消订阅',
      {
        confirmButtonText: '确定取消',
        cancelButtonText: '取消',
        type: 'warning',
      }
  )
      .then(async () => {
        try {
          // 模拟 API 请求：取消订阅
          console.log(`Sending cancel request for subscription: ${subscription.id}`);
          // 实际 API: await fetch(`/api/admin/subscriptions/cancel/${subscription.id}`, { method: 'PUT', headers: { Authorization: 'Bearer ...' } });

          await new Promise(resolve => setTimeout(resolve, 500)); // 模拟延迟

          ElMessage.success('订阅已成功取消！')
          fetchSubscriptions() // 刷新列表
        } catch (error) {
          console.error('取消订阅错误:', error)
          ElMessage.error('取消订阅失败。')
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

// 获取订阅列表数据（模拟）
const fetchSubscriptions = async () => {
  loading.value = true
  try {
    // 模拟从后端获取数据
    // 实际的API请求可能是：
    // const response = await fetch(`/api/admin/subscriptions?userId=${searchForm.userIdOrName}&levelId=${searchForm.membershipLevelId}&status=${searchForm.status}`);
    // const data = await response.json();

    // 模拟的用户订阅数据
    const mockSubscriptions: UserSubscription[] = [
      { id: 'sub001', user_id: 'user001', membership_level_id: 'level-basic', start_date: '2025-06-01 10:00:00', end_date: '2025-07-01 10:00:00', status: 'active', payment_method: 'Alipay', payment_amount: 9.99, created_at: '2025-06-01 09:50:00', updated_at: '2025-06-01 09:50:00' },
      { id: 'sub002', user_id: 'user002', membership_level_id: 'level-premium', start_date: '2025-05-15 14:30:00', end_date: '2025-06-15 14:30:00', status: 'expired', payment_method: 'WeChat Pay', payment_amount: 19.99, created_at: '2025-05-15 14:20:00', updated_at: '2025-06-15 14:30:00' },
      { id: 'sub003', user_id: 'user003', membership_level_id: 'level-free', start_date: '2024-01-01 00:00:00', end_date: '2099-12-31 23:59:59', status: 'active', payment_method: null, payment_amount: 0.00, created_at: '2024-01-01 00:00:00', updated_at: '2024-01-01 00:00:00' },
      { id: 'sub004', user_id: '1000000001', membership_level_id: 'level-enterprise', start_date: '2025-07-01 09:00:00', end_date: '2025-07-31 09:00:00', status: 'active', payment_method: 'Bank Transfer', payment_amount: 99.99, created_at: '2025-07-01 08:50:00', updated_at: '2025-07-01 08:50:00' },
      { id: 'sub005', user_id: 'user001', membership_level_id: 'level-basic', start_date: '2025-04-01 10:00:00', end_date: '2025-05-01 10:00:00', status: 'cancelled', payment_method: 'Alipay', payment_amount: 9.99, created_at: '2025-04-01 09:50:00', updated_at: '2025-04-10 11:00:00' },
      { id: 'sub006', user_id: '1000000002', membership_level_id: 'level-premium', start_date: '2025-06-20 11:00:00', end_date: '2025-07-20 11:00:00', status: 'active', payment_method: 'WeChat Pay', payment_amount: 19.99, created_at: '2025-06-20 10:50:00', updated_at: '2025-06-20 10:50:00' },
      { id: 'sub007', user_id: 'user002', membership_level_id: 'level-basic', start_date: '2025-03-01 12:00:00', end_date: '2025-03-31 12:00:00', status: 'expired', payment_method: 'Alipay', payment_amount: 9.99, created_at: '2025-03-01 11:50:00', updated_at: '2025-03-31 12:00:00' },
    ]

    await new Promise(resolve => setTimeout(resolve, 500)) // 模拟网络延迟
    subscriptions.value = mockSubscriptions

  } catch (error) {
    console.error('获取订阅列表失败:', error)
    ElMessage.error('获取订阅数据失败，请检查网络连接。')
  } finally {
    loading.value = false
  }
}

// 组件挂载时获取订阅列表
onMounted(() => {
  fetchSubscriptions()
})
</script>

<style scoped>
.admin-subscriptions-view {
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
}

.search-form .el-form-item {
  margin-bottom: 0;
  margin-right: 20px;
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

.pagination-container {
  margin-top: 20px;
  justify-content: flex-end;
  display: flex;
}

/* 对话框底部按钮的样式 */
.dialog-footer {
  text-align: right;
  padding-top: 20px;
}
</style>
<template>
  <div class="admin-subscriptions-view">
    <h2>订阅管理</h2>

    <el-card shadow="hover" class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户ID/用户名">
          <el-input v-model="searchForm.userIdOrName" placeholder="输入用户ID或用户名" clearable></el-input>
        </el-form-item>
        <el-form-item label="会员等级">
          <el-select v-model="searchForm.membershipLevelId" placeholder="选择会员等级" clearable style="width: 150px;">
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
          <el-select v-model="searchForm.status" placeholder="选择订阅状态" clearable style="width: 150px;">
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
        <el-table-column prop="userId" label="用户" width="150" show-overflow-tooltip>
          <template #default="{ row }">
            {{ getUserNameById(row.userId) || row.userId }}
          </template>
        </el-table-column>
        <el-table-column prop="membershipLevelId" label="会员等级" width="120">
          <template #default="{ row }">
            {{ getMembershipLevelName(row.membershipLevelId) }}
          </template>
        </el-table-column>
        <el-table-column prop="startDate" label="开始日期" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.startDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="endDate" label="结束日期" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.endDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getSubscriptionStatusTagType(row.status)">
              {{ formatSubscriptionStatus(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="paymentAmount" label="支付金额" width="100">
          <template #default="{ row }">
            {{ row.paymentAmount !== null ? `¥${row.paymentAmount.toFixed(2)}` : '--' }}
          </template>
        </el-table-column>
        <el-table-column prop="paymentMethod" label="支付方式" width="100"></el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
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

    <el-dialog
        v-model="detailsDialogVisible"
        title="订阅详情"
        width="600px"
    >
      <div v-if="currentSubscriptionDetails">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订阅ID">{{ currentSubscriptionDetails.id }}</el-descriptions-item>
          <el-descriptions-item label="用户ID">{{ currentSubscriptionDetails.userId }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ getUserNameById(currentSubscriptionDetails.userId) || currentSubscriptionDetails.userId }}</el-descriptions-item>
          <el-descriptions-item label="会员等级">{{ getMembershipLevelName(currentSubscriptionDetails.membershipLevelId) }}</el-descriptions-item>
          <el-descriptions-item label="开始日期">{{ formatDateTime(currentSubscriptionDetails.startDate) }}</el-descriptions-item>
          <el-descriptions-item label="结束日期">{{ formatDateTime(currentSubscriptionDetails.endDate) }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getSubscriptionStatusTagType(currentSubscriptionDetails.status)">
              {{ formatSubscriptionStatus(currentSubscriptionDetails.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="支付金额">{{ currentSubscriptionDetails.paymentAmount !== null ? `¥${currentSubscriptionDetails.paymentAmount.toFixed(2)}` : '--' }}</el-descriptions-item>
          <el-descriptions-item label="支付方式">{{ currentSubscriptionDetails.paymentMethod || '--' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDateTime(currentSubscriptionDetails.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ formatDateTime(currentSubscriptionDetails.updatedAt) }}</el-descriptions-item>
        </el-descriptions>
        <el-divider />
        <h3>会员等级详情</h3>
        <el-descriptions :column="1" border v-if="currentMembershipLevelDetails">
          <el-descriptions-item label="等级名称">{{ currentMembershipLevelDetails.name }}</el-descriptions-item>
          <el-descriptions-item label="存储配额">{{ formatFileSize(currentMembershipLevelDetails.storageQuota) }}</el-descriptions-item>
          <el-descriptions-item label="最大单文件">{{ formatFileSize(currentMembershipLevelDetails.maxFileSize) }}</el-descriptions-item>
          <el-descriptions-item label="价格">{{ `¥${currentMembershipLevelDetails.price.toFixed(2)} / ${currentMembershipLevelDetails.durationDays}天` }}</el-descriptions-item>
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
import { formatFileSize } from '../utils' // 注意路径是否正确

// 用于格式化日期时间 (适配 LocalDateTime)
const formatDateTime = (datetime: string | null) => {
  if (!datetime) return '--'
  try {
    const date = new Date(datetime);
    if (isNaN(date.getTime())) {
      throw new Error('Invalid date string');
    }
    // 注意：Java 的 LocalDateTime 可能会被 JSON 序列化为 "YYYY-MM-DDTHH:MM:SS" 格式，
    // Date 构造函数可以处理，但如果包含毫秒，有时需要额外处理。
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
  storageQuota: number; // 字节
  maxFileSize: number; // 字节
  price: number;
  durationDays: number;
  features: string;
  createdAt: string;
  updatedAt: string;
}

// 注意：这里使用后端枚举的字符串值 'active', 'expired', 'cancelled'
type SubscriptionStatus = 'active' | 'expired' | 'cancelled';

// 与后端 UserSubscription 实体类保持一致 (驼峰命名)
interface UserSubscription {
  id: string;
  userId: string;
  membershipLevelId: string;
  startDate: string;
  endDate: string;
  status: SubscriptionStatus;
  paymentMethod: string | null;
  paymentAmount: number | null;
  createdAt: string;
  updatedAt: string;
}

// 与后端 User 实体类保持一致 (驼峰命名)
interface User {
  id: string;
  username: string;
  email: string;
  // ... 其他字段如果你需要
}

// --- 数据定义 ---

// 搜索表单
const searchForm = reactive({
  userIdOrName: '',
  membershipLevelId: '',
  status: '', // 后端枚举字符串
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

// 实际从后端获取的会员等级数据
const membershipLevels = ref<MembershipLevel[]>([])

// 实际从后端获取的用户数据，用于构建映射
const users = ref<User[]>([]);
const userMap = computed(() => {
  const map: { [key: string]: string } = {};
  users.value.forEach(user => {
    map[user.id] = user.username;
  });
  return map;
});


// --- 计算属性 ---

// 根据搜索条件过滤订阅
const filteredSubscriptions = computed(() => {
  return subscriptions.value.filter(sub => {
    const matchesUserIdOrName = !searchForm.userIdOrName ||
        sub.userId?.includes(searchForm.userIdOrName) ||
        (userMap.value[sub.userId]?.includes(searchForm.userIdOrName));

    const matchesMembershipLevel = searchForm.membershipLevelId === '' || sub.membershipLevelId === searchForm.membershipLevelId;

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
  currentPage.value = 1; // 搜索时重置回第一页
  // 由于后端 getAll 不支持筛选，这里只触发前端过滤
}

// 重置搜索条件
const resetSearch = () => {
  searchForm.userIdOrName = '';
  searchForm.membershipLevelId = '';
  searchForm.status = '';
  currentPage.value = 1; // 重置时也回到第一页
  // 不需要再次 fetch，因为 computed 会自动更新
}

// 查看订阅详情
const viewSubscriptionDetails = (subscription: UserSubscription) => {
  currentSubscriptionDetails.value = subscription;
  currentMembershipLevelDetails.value = membershipLevels.value.find(
      level => level.id === subscription.membershipLevelId
  ) || null;
  detailsDialogVisible.value = true;
}

// 延长订阅
const extendSubscription = async (subscription: UserSubscription) => {
  ElMessageBox.confirm(
      `确定要延长用户 "${getUserNameById(subscription.userId)}" 的 "${getMembershipLevelName(subscription.membershipLevelId)}" 订阅吗？`,
      '延长订阅',
      {
        confirmButtonText: '确定延长',
        cancelButtonText: '取消',
        type: 'info',
      }
  )
      .then(async () => {
        try {
          // 创建一个新的订阅对象，用于发送给后端更新
          const updatedSubscription: UserSubscription = { ...subscription };

          // 延长逻辑：如果已过期或已取消，则将结束日期延长 N 天（例如30天）并设置为活跃
          // 如果已经是活跃状态，则从当前结束日期延长 N 天
          let newEndDate = new Date(updatedSubscription.endDate);
          if (updatedSubscription.status !== 'active') {
            // 如果不是活跃状态，从当前日期算起
            newEndDate = new Date();
          }
          newEndDate.setDate(newEndDate.getDate() + 30); // 延长30天

          updatedSubscription.endDate = newEndDate.toISOString(); // 转换为ISO字符串
          updatedSubscription.status = 'active'; // 状态改为活跃

          const response = await fetch('/admin-api/subscriptions/update', {
            method: 'PUT',
            headers: {
              'Content-Type': 'application/json',
              // 'Authorization': `Bearer ${localStorage.getItem('adminAccessToken')}`
            },
            body: JSON.stringify(updatedSubscription)
          });

          const result = await response.json();

          if (result.code === 200) {
            ElMessage.success('订阅已成功延长！');
            await fetchSubscriptions(); // 刷新列表
          } else {
            ElMessage.error(result.message || '延长订阅失败！');
          }
        } catch (error) {
          console.error('延长订阅错误:', error);
          ElMessage.error('延长订阅失败，请检查网络。');
        }
      })
      .catch(() => {});
};


// 取消订阅
const cancelSubscription = async (subscription: UserSubscription) => {
  ElMessageBox.confirm(
      `确定要取消用户 "${getUserNameById(subscription.userId)}" 的 "${getMembershipLevelName(subscription.membershipLevelId)}" 订阅吗？此操作不可逆！`,
      '取消订阅',
      {
        confirmButtonText: '确定取消',
        cancelButtonText: '取消',
        type: 'warning',
      }
  )
      .then(async () => {
        try {
          const updatedSubscription: UserSubscription = { ...subscription };
          updatedSubscription.status = 'cancelled'; // 将状态设置为已取消
          // 取消时通常也会把结束日期设置为当前时间或不修改，这里根据业务需求决定。
          // updatedSubscription.endDate = new Date().toISOString(); // 可选：立即结束订阅

          const response = await fetch('/admin-api/subscriptions/update', {
            method: 'PUT',
            headers: {
              'Content-Type': 'application/json',
              // 'Authorization': `Bearer ${localStorage.getItem('adminAccessToken')}`
            },
            body: JSON.stringify(updatedSubscription)
          });

          const result = await response.json();

          if (result.code === 200) {
            ElMessage.success('订阅已成功取消！');
            await fetchSubscriptions(); // 刷新列表
          } else {
            ElMessage.error(result.message || '取消订阅失败！');
          }
        } catch (error) {
          console.error('取消订阅错误:', error);
          ElMessage.error('取消订阅失败，请检查网络。');
        }
      })
      .catch(() => {});
};

// 处理每页显示数量变化
const handleSizeChange = (val: number) => {
  pageSize.value = val;
  currentPage.value = 1; // 切换每页大小后回到第一页
}

// 处理当前页码变化
const handleCurrentChange = (val: number) => {
  currentPage.value = val;
}

// 获取用户列表数据 (用于用户名映射)
const fetchUsers = async () => {
  try {
    const response = await fetch('/admin-api/getAllUsers', { // 假设这是获取所有用户的API
      headers: {
        'Content-Type': 'application/json',
        // 'Authorization': `Bearer ${localStorage.getItem('adminAccessToken')}`
      }
    });
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    const result = await response.json();
    if (result.code === 200) {
      users.value = result.data;
    } else {
      console.error('获取用户列表失败:', result.message);
      ElMessage.error('无法获取用户列表，部分用户名可能无法显示。');
    }
  } catch (error) {
    console.error('获取用户列表错误:', error);
    ElMessage.error('获取用户数据失败，请检查网络连接。');
  }
};


// 获取会员等级列表数据
const fetchMembershipLevels = async () => {
  try {
    const response = await fetch('/admin-api/membership-levels/getAll', {
      headers: {
        'Content-Type': 'application/json',
        // 'Authorization': `Bearer ${localStorage.getItem('adminAccessToken')}`
      }
    });
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    const result = await response.json();
    if (result.code === 200) {
      // 确保字段名与前端 MembershipLevel 接口的驼峰命名一致
      membershipLevels.value = result.data.map((level: any) => ({
        id: level.id,
        name: level.name,
        storageQuota: Number(level.storageQuota),
        maxFileSize: Number(level.maxFileSize),
        price: Number(level.price),
        durationDays: Number(level.durationDays),
        features: level.features,
        createdAt: level.createdAt,
        updatedAt: level.updatedAt,
      }));
    } else {
      console.error('获取会员等级列表失败:', result.message);
      ElMessage.error('无法获取会员等级列表。');
    }
  } catch (error) {
    console.error('获取会员等级列表错误:', error);
    ElMessage.error('获取会员等级数据失败，请检查网络连接。');
  }
};


// 获取订阅列表数据
const fetchSubscriptions = async () => {
  loading.value = true;
  try {
    const response = await fetch('/admin-api/subscriptions/getAll', {
      headers: {
        'Content-Type': 'application/json',
        // 'Authorization': `Bearer ${localStorage.getItem('adminAccessToken')}`
      }
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const result = await response.json();

    if (result.code === 200) {
      // 确保字段名与前端 UserSubscription 接口的驼峰命名一致
      subscriptions.value = result.data.map((sub: any) => ({
        id: sub.id,
        userId: sub.userId,
        membershipLevelId: sub.membershipLevelId,
        startDate: sub.startDate,
        endDate: sub.endDate,
        status: sub.status, // 后端返回已经是字符串枚举值
        paymentMethod: sub.paymentMethod,
        paymentAmount: Number(sub.paymentAmount), // 确保是数字类型
        createdAt: sub.createdAt,
        updatedAt: sub.updatedAt,
      }));

      // 如果后端 getAll 接口不支持筛选，这里在前端进行一次初始过滤
      // 否则，你应该将 searchForm 的参数发送给后端
    } else {
      ElMessage.error(result.message || '获取订阅列表失败！');
    }
  } catch (error) {
    console.error('获取订阅列表失败:', error);
    ElMessage.error('获取订阅数据失败，请检查网络连接。');
  } finally {
    loading.value = false;
  }
};


// 组件挂载时执行
onMounted(async () => {
  // 先获取基础数据 (用户和会员等级)，再获取订阅数据
  await fetchUsers();
  await fetchMembershipLevels();
  await fetchSubscriptions();
});
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
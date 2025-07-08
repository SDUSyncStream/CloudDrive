<template>
  <div class="subscription-history-container">
    <div class="header">
      <h2>订阅历史</h2>
      <p>查看您的所有订阅记录和支付历史</p>
    </div>

    <!-- 订阅历史 -->
    <div class="subscription-section">
      <h3>
        <i class="el-icon-time"></i>
        订阅记录
      </h3>
      
      <el-table 
        :data="subscriptions" 
        style="width: 100%"
        v-loading="subscriptionLoading"
      >
        <el-table-column prop="membershipLevelName" label="会员等级" width="150">
          <template #default="scope">
            <el-tag 
              :type="scope.row.membershipLevelName === '免费版' ? 'info' : 'success'"
              size="small"
            >
              {{ scope.row.membershipLevelName }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="startDate" label="开始时间" width="150">
          <template #default="scope">
            {{ formatDate(scope.row.startDate) }}
          </template>
        </el-table-column>
        
        <el-table-column prop="endDate" label="结束时间" width="150">
          <template #default="scope">
            {{ formatDate(scope.row.endDate) }}
          </template>
        </el-table-column>
        
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag 
              :type="getStatusType(scope.row.status)"
              size="small"
            >
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="paymentAmount" label="支付金额" width="120">
          <template #default="scope">
            <span class="amount">¥{{ scope.row.paymentAmount }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="paymentMethod" label="支付方式" width="120">
          <template #default="scope">
            <span>{{ getPaymentMethodText(scope.row.paymentMethod) }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="daysRemaining" label="剩余天数" width="100">
          <template #default="scope">
            <span v-if="scope.row.isActive" class="remaining-days">
              {{ scope.row.daysRemaining }}天
            </span>
            <span v-else class="expired">-</span>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button 
              v-if="scope.row.status === 'active'"
              type="danger" 
              size="small"
              @click="handleCancelSubscription(scope.row)"
              :loading="cancelLoading === scope.row.id"
            >
              取消
            </el-button>
            <span v-else>-</span>
          </template>
        </el-table-column>
      </el-table>
      
      <div v-if="subscriptions.length === 0 && !subscriptionLoading" class="empty-state">
        <el-empty description="暂无订阅记录">
          <el-button type="primary" @click="$router.push('/vip/membership')">
            去选择会员
          </el-button>
        </el-empty>
      </div>
    </div>

    <!-- 支付历史 -->
    <div class="payment-section">
      <h3>
        <i class="el-icon-money"></i>
        支付历史
      </h3>
      
      <el-table 
        :data="paymentOrders" 
        style="width: 100%"
        v-loading="paymentLoading"
      >
        <el-table-column prop="orderNumber" label="订单号" width="200">
        </el-table-column>
        
        <el-table-column prop="membershipLevelName" label="会员等级" width="150">
          <template #default="scope">
            <el-tag type="success" size="small">
              {{ scope.row.membershipLevelName }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="amount" label="支付金额" width="120">
          <template #default="scope">
            <span class="amount">¥{{ scope.row.amount }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="paymentMethod" label="支付方式" width="120">
          <template #default="scope">
            <span>{{ getPaymentMethodText(scope.row.paymentMethod) }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="status" label="支付状态" width="100">
          <template #default="scope">
            <el-tag 
              :type="getPaymentStatusType(scope.row.status)"
              size="small"
            >
              {{ getPaymentStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="transactionId" label="交易号" width="150">
          <template #default="scope">
            <span v-if="scope.row.transactionId">{{ scope.row.transactionId }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="createdAt" label="创建时间" width="150">
          <template #default="scope">
            {{ formatDate(scope.row.createdAt) }}
          </template>
        </el-table-column>
        
        <el-table-column prop="paidAt" label="支付时间" width="150">
          <template #default="scope">
            <span v-if="scope.row.paidAt">{{ formatDate(scope.row.paidAt) }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
      </el-table>
      
      <div v-if="paymentOrders.length === 0 && !paymentLoading" class="empty-state">
        <el-empty description="暂无支付记录">
          <el-button type="primary" @click="$router.push('/vip/membership')">
            去选择会员
          </el-button>
        </el-empty>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { membershipApi } from '../api/membership'
import type { UserSubscription, PaymentOrder } from '../types'

// 响应式数据
const subscriptions = ref<UserSubscription[]>([])
const paymentOrders = ref<PaymentOrder[]>([])
const subscriptionLoading = ref(false)
const paymentLoading = ref(false)
const cancelLoading = ref('')

// 获取用户ID
const getCurrentUserId = () => {
  return 'user-001' // 临时使用固定用户ID
}

// 格式化日期
const formatDate = (date: Date | string) => {
  const d = new Date(date)
  return d.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

// 获取状态类型
const getStatusType = (status: string) => {
  const typeMap: Record<string, string> = {
    'active': 'success',
    'expired': 'warning',
    'cancelled': 'danger'
  }
  return typeMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    'active': '有效',
    'expired': '已过期',
    'cancelled': '已取消'
  }
  return textMap[status] || status
}

// 获取支付状态类型
const getPaymentStatusType = (status: string) => {
  const typeMap: Record<string, string> = {
    'paid': 'success',
    'pending': 'warning',
    'failed': 'danger',
    'cancelled': 'info'
  }
  return typeMap[status] || 'info'
}

// 获取支付状态文本
const getPaymentStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    'paid': '已支付',
    'pending': '待支付',
    'failed': '支付失败',
    'cancelled': '已取消'
  }
  return textMap[status] || status
}

// 获取支付方式文本
const getPaymentMethodText = (method: string) => {
  const methodMap: Record<string, string> = {
    'alipay': '支付宝',
    'wechat': '微信支付',
    'bank_card': '银行卡',
    'bank_transfer': '银行转账'
  }
  return methodMap[method] || method || '-'
}

// 获取用户订阅历史
const fetchSubscriptions = async () => {
  subscriptionLoading.value = true
  try {
    const userId = getCurrentUserId()
    const response = await membershipApi.getUserSubscriptions(userId)
    if (response.data.code === 200) {
      subscriptions.value = response.data.data
    }
  } catch (error) {
    console.error('获取订阅历史失败:', error)
    ElMessage.error('获取订阅历史失败')
  } finally {
    subscriptionLoading.value = false
  }
}

// 获取用户支付历史
const fetchPaymentOrders = async () => {
  paymentLoading.value = true
  try {
    const userId = getCurrentUserId()
    const response = await membershipApi.getUserPaymentOrders(userId)
    if (response.data.code === 200) {
      paymentOrders.value = response.data.data
    }
  } catch (error) {
    console.error('获取支付历史失败:', error)
    ElMessage.error('获取支付历史失败')
  } finally {
    paymentLoading.value = false
  }
}

// 取消订阅
const handleCancelSubscription = async (subscription: UserSubscription) => {
  try {
    await ElMessageBox.confirm(
      `确定要取消【${subscription.membershipLevelName}】订阅吗？取消后将无法享受会员特权。`,
      '取消订阅',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    cancelLoading.value = subscription.id
    
    const response = await membershipApi.cancelSubscription(subscription.id)
    if (response.data.code === 200) {
      ElMessage.success('订阅已取消')
      await fetchSubscriptions() // 刷新订阅列表
    } else {
      ElMessage.error('取消订阅失败: ' + response.data.message)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消订阅失败:', error)
      ElMessage.error('取消订阅失败')
    }
  } finally {
    cancelLoading.value = ''
  }
}

// 初始化
onMounted(async () => {
  await Promise.all([
    fetchSubscriptions(),
    fetchPaymentOrders()
  ])
})
</script>

<style scoped>
.subscription-history-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.header {
  text-align: center;
  margin-bottom: 40px;
}

.header h2 {
  color: #333;
  font-size: 28px;
  margin-bottom: 10px;
}

.header p {
  color: #666;
  font-size: 16px;
}

.subscription-section,
.payment-section {
  margin-bottom: 40px;
}

.subscription-section h3,
.payment-section h3 {
  margin-bottom: 20px;
  color: #333;
  font-size: 20px;
  display: flex;
  align-items: center;
}

.subscription-section h3 i,
.payment-section h3 i {
  margin-right: 8px;
  color: #409eff;
}

.amount {
  font-weight: bold;
  color: #e6a23c;
}

.remaining-days {
  color: #67c23a;
  font-weight: bold;
}

.expired {
  color: #909399;
}

.empty-state {
  margin-top: 40px;
  text-align: center;
}

.el-table {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.el-table :deep(.el-table__header) {
  background-color: #f5f7fa;
}

.el-table :deep(.el-table__header th) {
  background-color: #f5f7fa;
  color: #333;
  font-weight: 600;
}

.el-table :deep(.el-table__row:hover > td) {
  background-color: #f5f7fa;
}
</style>
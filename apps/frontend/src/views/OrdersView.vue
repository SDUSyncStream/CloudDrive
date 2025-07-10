<template>
  <div class="orders-container">
    <div class="header">
      <div class="header-content">
        <div class="header-text">
          <h2>订单中心</h2>
          <p>查看您的所有订单记录</p>
        </div>
        <el-button 
          type="primary" 
          :icon="HomeFilled"
          @click="goToMain"
          class="back-to-main-btn"
        >
          返回主页
        </el-button>
      </div>
    </div>

    <!-- 订单状态筛选 -->
    <div class="order-filters">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="全部" name="all"></el-tab-pane>
        <el-tab-pane label="待支付" name="pending"></el-tab-pane>
        <el-tab-pane label="已完成" name="paid"></el-tab-pane>
        <el-tab-pane label="已取消" name="cancelled"></el-tab-pane>
      </el-tabs>
    </div>

    <!-- 订单列表 -->
    <div class="orders-list" v-loading="loading">
      <div v-if="orders.length === 0" class="empty-state">
        <el-empty description="暂无订单记录" />
      </div>
      
      <div v-else>
        <div 
          v-for="order in orders" 
          :key="order.id"
          class="order-card"
          @click="handleViewOrder(order)"
        >
          <div class="order-header">
            <div class="order-info">
              <h3>{{ order.membershipLevelName }}</h3>
              <p class="order-id">订单号：{{ order.id }}</p>
            </div>
            <div class="order-status">
              <el-tag 
                :type="getStatusType(order.status)"
                size="large"
              >
                {{ getStatusText(order.status) }}
              </el-tag>
            </div>
          </div>
          
          <div class="order-content">
            <div class="order-details">
              <div class="detail-item">
                <span class="label">创建时间：</span>
                <span>{{ formatDate(order.createdAt) }}</span>
              </div>
              <div class="detail-item">
                <span class="label">支付方式：</span>
                <span>{{ getPaymentMethodText(order.paymentMethod) }}</span>
              </div>
              <div class="detail-item" v-if="order.transactionId">
                <span class="label">交易号：</span>
                <span>{{ order.transactionId }}</span>
              </div>
            </div>
            
            <div class="order-amount">
              <div class="amount-text">
                <span class="symbol">¥</span>
                <span class="price">{{ order.amount }}</span>
              </div>
            </div>
          </div>
          
          <div class="order-actions">
            <el-button 
              type="primary" 
              link 
              @click.stop="handleViewOrder(order)"
            >
              查看详情
            </el-button>
            <el-button 
              v-if="order.status === 'pending'"
              type="warning"
              link
              @click.stop="handlePayOrder(order)"
            >
              立即支付
            </el-button>
            <el-button 
              v-if="order.status === 'pending'"
              type="danger"
              link
              @click.stop="handleCancelOrder(order)"
            >
              取消订单
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination" v-if="total > 0">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { HomeFilled } from '@element-plus/icons-vue'
import { membershipApi } from '../api/membership'
import type { PaymentOrder } from '../types'

const router = useRouter()

// 响应式数据
const orders = ref<PaymentOrder[]>([])
const loading = ref(false)
const activeTab = ref('all')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 获取用户ID
const getCurrentUserId = () => {
  const userId = localStorage.getItem('UserId')
  return userId || ''
}

// 格式化日期
const formatDate = (date: Date | string) => {
  const d = new Date(date)
  return d.toLocaleString('zh-CN')
}

// 获取订单状态类型
const getStatusType = (status: string) => {
  switch (status) {
    case 'pending':
      return 'warning'
    case 'paid':
      return 'success'
    case 'cancelled':
      return 'info'
    case 'failed':
      return 'danger'
    default:
      return 'info'
  }
}

// 获取订单状态文本
const getStatusText = (status: string) => {
  switch (status) {
    case 'pending':
      return '待支付'
    case 'paid':
      return '已完成'
    case 'cancelled':
      return '已取消'
    case 'failed':
      return '支付失败'
    default:
      return '未知状态'
  }
}

// 获取支付方式文本
const getPaymentMethodText = (method: string) => {
  switch (method) {
    case 'alipay':
      return '支付宝'
    case 'wechat':
      return '微信支付'
    case 'bank_card':
      return '银行卡'
    default:
      return method
  }
}

// 获取订单列表
const fetchOrders = async () => {
  loading.value = true
  try {
    const userId = getCurrentUserId()
    if (!userId) {
      ElMessage.error('请先登录')
      return
    }

    const response = await membershipApi.getUserPaymentOrders(userId)
    if (response.data.code === 200) {
      let allOrders = response.data.data || []
      
      // 根据选中的tab过滤订单
      if (activeTab.value !== 'all') {
        allOrders = allOrders.filter(order => order.status === activeTab.value)
      }
      
      total.value = allOrders.length
      
      // 分页处理
      const startIndex = (currentPage.value - 1) * pageSize.value
      const endIndex = startIndex + pageSize.value
      orders.value = allOrders.slice(startIndex, endIndex)
    } else {
      ElMessage.error('获取订单列表失败')
    }
  } catch (error) {
    console.error('获取订单列表失败:', error)
    ElMessage.error('获取订单列表失败')
  } finally {
    loading.value = false
  }
}

// 处理标签页切换
const handleTabChange = (tabName: string) => {
  activeTab.value = tabName
  currentPage.value = 1
  fetchOrders()
}

// 处理页面大小变化
const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  fetchOrders()
}

// 处理当前页变化
const handleCurrentChange = (page: number) => {
  currentPage.value = page
  fetchOrders()
}

// 返回主页
const goToMain = () => {
  router.push('/main/files')
}

// 查看订单详情
const handleViewOrder = (order: PaymentOrder) => {
  router.push(`/orders/${order.id}`)
}

// 支付订单
const handlePayOrder = async (order: PaymentOrder) => {
  try {
    const transactionId = 'TXN' + Date.now()
    const response = await membershipApi.processPayment(order.id, transactionId)
    
    if (response.data.code === 200) {
      ElMessage.success('支付成功！')
      await fetchOrders()
    } else {
      ElMessage.error('支付失败: ' + response.data.message)
    }
  } catch (error) {
    console.error('支付失败:', error)
    ElMessage.error('支付失败')
  }
}

// 取消订单
const handleCancelOrder = async (order: PaymentOrder) => {
  try {
    await ElMessageBox.confirm(
      '确定要取消这个订单吗？',
      '取消订单',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const response = await membershipApi.cancelPaymentOrder(order.id)
    if (response.data.code === 200) {
      ElMessage.success('订单已取消')
      await fetchOrders()
    } else {
      ElMessage.error('取消订单失败: ' + response.data.message)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消订单失败:', error)
      ElMessage.error('取消订单失败')
    }
  }
}

// 初始化
onMounted(() => {
  fetchOrders()
})
</script>

<style scoped>
.orders-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.header {
  margin-bottom: 30px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-text {
  text-align: center;
  flex: 1;
}

.header-text h2 {
  color: #333;
  font-size: 28px;
  margin-bottom: 10px;
}

.header-text p {
  color: #666;
  font-size: 16px;
}

.back-to-main-btn {
  flex-shrink: 0;
  margin-left: 20px;
}

.order-filters {
  margin-bottom: 20px;
}

.orders-list {
  min-height: 400px;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 400px;
}

.order-card {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: white;
}

.order-card:hover {
  border-color: #409eff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.order-info h3 {
  margin: 0 0 8px 0;
  color: #333;
  font-size: 18px;
  font-weight: 600;
}

.order-id {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.order-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.order-details {
  flex: 1;
}

.detail-item {
  display: flex;
  margin-bottom: 8px;
  font-size: 14px;
}

.detail-item .label {
  color: #666;
  min-width: 80px;
}

.order-amount {
  text-align: right;
}

.amount-text {
  display: flex;
  align-items: baseline;
  justify-content: flex-end;
  font-size: 24px;
  font-weight: bold;
  color: #e6a23c;
}

.symbol {
  font-size: 16px;
  margin-right: 4px;
}

.order-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  border-top: 1px solid #f0f0f0;
  padding-top: 16px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .orders-container {
    padding: 10px;
  }
  
  .header-content {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .header-text {
    text-align: center;
  }
  
  .back-to-main-btn {
    margin-left: 0;
    width: 100%;
  }
  
  .order-card {
    padding: 15px;
  }
  
  .order-content {
    flex-direction: column;
    gap: 16px;
  }
  
  .order-amount {
    text-align: left;
  }
  
  .order-actions {
    justify-content: center;
  }
}
</style>
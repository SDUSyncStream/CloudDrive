<template>
  <div class="order-detail-container">
    <div class="header">
      <el-button 
        :icon="ArrowLeft" 
        @click="goBack"
        class="back-btn"
      >
        返回订单列表
      </el-button>
      <h2>订单详情</h2>
    </div>

    <div v-loading="loading" class="detail-content">
      <div v-if="order" class="order-detail-card">
        <!-- 订单状态卡片 -->
        <div class="status-section">
          <div class="status-header">
            <div class="status-info">
              <el-tag 
                :type="getStatusType(order.status)"
                size="large"
                class="status-tag"
              >
                {{ getStatusText(order.status) }}
              </el-tag>
              <h3>{{ order.membershipLevelName }}</h3>
            </div>
            <div class="amount-info">
              <div class="amount">
                <span class="symbol">¥</span>
                <span class="price">{{ order.amount }}</span>
              </div>
              <div class="amount-label">订单金额</div>
            </div>
          </div>
          
          <!-- 进度条 -->
          <div class="progress-section">
            <el-steps 
              :active="getOrderStep(order.status)" 
              finish-status="success"
              process-status="process"
            >
              <el-step title="创建订单" :description="formatDate(order.createdAt)"></el-step>
              <el-step 
                title="支付完成" 
                :description="order.status === 'paid' ? formatDate(order.updatedAt) : ''"
              ></el-step>
              <el-step 
                title="订单完成" 
                :description="order.status === 'paid' ? '服务已激活' : ''"
              ></el-step>
            </el-steps>
          </div>
        </div>

        <!-- 订单信息 -->
        <div class="info-section">
          <h4>订单信息</h4>
          <div class="info-grid">
            <div class="info-item">
              <span class="label">订单号</span>
              <span class="value">{{ order.id }}</span>
            </div>
            <div class="info-item">
              <span class="label">创建时间</span>
              <span class="value">{{ formatDate(order.createdAt) }}</span>
            </div>
            <div class="info-item">
              <span class="label">更新时间</span>
              <span class="value">{{ formatDate(order.updatedAt) }}</span>
            </div>
            <div class="info-item">
              <span class="label">支付方式</span>
              <span class="value">{{ getPaymentMethodText(order.paymentMethod) }}</span>
            </div>
            <div class="info-item" v-if="order.transactionId">
              <span class="label">交易号</span>
              <span class="value">{{ order.transactionId }}</span>
            </div>
            <div class="info-item">
              <span class="label">订单状态</span>
              <span class="value">{{ getStatusText(order.status) }}</span>
            </div>
          </div>
        </div>

        <!-- 商品信息 -->
        <div class="product-section" v-if="membershipLevel">
          <h4>服务详情</h4>
          <div class="product-card">
            <div class="product-info">
              <h5>{{ membershipLevel.name }}</h5>
              <div class="product-features">
                <div class="feature-item">
                  <el-icon><FolderOpened /></el-icon>
                  <span>{{ formatFileSize(membershipLevel.storageQuota) }} 存储空间</span>
                </div>
                <div class="feature-item">
                  <el-icon><Upload /></el-icon>
                  <span>单文件最大 {{ formatFileSize(membershipLevel.maxFileSize) }}</span>
                </div>
                <div class="feature-item" v-if="membershipLevel.features">
                  <el-icon><Star /></el-icon>
                  <span>{{ membershipLevel.features }}</span>
                </div>
                <div class="feature-item" v-if="membershipLevel.durationDays > 0">
                  <el-icon><Clock /></el-icon>
                  <span>有效期 {{ membershipLevel.durationDays }} 天</span>
                </div>
              </div>
            </div>
            <div class="product-price">
              <span class="price-symbol">¥</span>
              <span class="price-amount">{{ membershipLevel.price }}</span>
            </div>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="actions-section">
          <el-button 
            v-if="order.status === 'pending'"
            type="primary"
            size="large"
            @click="handlePayOrder"
            :loading="paymentLoading"
          >
            立即支付
          </el-button>
          <el-button 
            v-if="order.status === 'pending'"
            type="danger"
            size="large"
            plain
            @click="handleCancelOrder"
            :loading="cancelLoading"
          >
            取消订单
          </el-button>
          <el-button 
            v-if="order.status === 'paid'"
            type="success"
            size="large"
            @click="goToMembership"
          >
            查看会员中心
          </el-button>
        </div>
      </div>

      <div v-else-if="!loading" class="empty-state">
        <el-empty description="订单不存在或已被删除" />
        <el-button type="primary" @click="goBack">返回订单列表</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, FolderOpened, Upload, Star, Clock } from '@element-plus/icons-vue'
import { membershipApi } from '../api/membership'
import { formatFileSize } from '../utils'
import type { PaymentOrder, MembershipLevel } from '../types'

const router = useRouter()
const route = useRoute()

// 响应式数据
const order = ref<PaymentOrder | null>(null)
const membershipLevel = ref<MembershipLevel | null>(null)
const loading = ref(false)
const paymentLoading = ref(false)
const cancelLoading = ref(false)

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

// 获取订单步骤
const getOrderStep = (status: string) => {
  switch (status) {
    case 'pending':
      return 0
    case 'paid':
      return 2
    case 'cancelled':
    case 'failed':
      return 0
    default:
      return 0
  }
}

// 获取订单详情
const fetchOrderDetail = async () => {
  loading.value = true
  try {
    const orderId = route.params.id as string
    if (!orderId) {
      ElMessage.error('订单ID不能为空')
      return
    }

    const response = await membershipApi.getPaymentOrder(orderId)
    if (response.data.code === 200) {
      order.value = response.data.data
      
      // 获取会员等级详情
      if (order.value?.membershipLevelId) {
        await fetchMembershipLevel(order.value.membershipLevelId)
      }
    } else {
      ElMessage.error('获取订单详情失败')
    }
  } catch (error) {
    console.error('获取订单详情失败:', error)
    ElMessage.error('获取订单详情失败')
  } finally {
    loading.value = false
  }
}

// 获取会员等级详情
const fetchMembershipLevel = async (levelId: string) => {
  try {
    const response = await membershipApi.getLevelById(levelId)
    if (response.data.code === 200) {
      membershipLevel.value = response.data.data
    }
  } catch (error) {
    console.error('获取会员等级详情失败:', error)
  }
}

// 支付订单
const handlePayOrder = async () => {
  if (!order.value) return

  paymentLoading.value = true
  try {
    const transactionId = 'TXN' + Date.now()
    const response = await membershipApi.processPayment(order.value.id, transactionId)
    
    if (response.data.code === 200) {
      ElMessage.success('支付成功！')
      await fetchOrderDetail()
    } else {
      ElMessage.error('支付失败: ' + response.data.message)
    }
  } catch (error) {
    console.error('支付失败:', error)
    ElMessage.error('支付失败')
  } finally {
    paymentLoading.value = false
  }
}

// 取消订单
const handleCancelOrder = async () => {
  if (!order.value) return

  try {
    await ElMessageBox.confirm(
      '确定要取消这个订单吗？取消后无法恢复。',
      '取消订单',
      {
        confirmButtonText: '确定取消',
        cancelButtonText: '保留订单',
        type: 'warning'
      }
    )
    
    cancelLoading.value = true
    const response = await membershipApi.cancelPaymentOrder(order.value.id)
    if (response.data.code === 200) {
      ElMessage.success('订单已取消')
      await fetchOrderDetail()
    } else {
      ElMessage.error('取消订单失败: ' + response.data.message)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消订单失败:', error)
      ElMessage.error('取消订单失败')
    }
  } finally {
    cancelLoading.value = false
  }
}

// 返回上一页
const goBack = () => {
  router.push('/orders')
}

// 前往会员中心
const goToMembership = () => {
  router.push('/vip')
}

// 初始化
onMounted(() => {
  fetchOrderDetail()
})
</script>

<style scoped>
.order-detail-container {
  padding: 20px;
  max-width: 1000px;
  margin: 0 auto;
}

.header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 30px;
}

.back-btn {
  flex-shrink: 0;
}

.header h2 {
  margin: 0;
  color: #333;
  font-size: 24px;
}

.detail-content {
  min-height: 500px;
}

.order-detail-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.status-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 30px;
}

.status-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 30px;
}

.status-info {
  flex: 1;
}

.status-tag {
  margin-bottom: 12px;
}

.status-info h3 {
  margin: 0;
  font-size: 28px;
  font-weight: 600;
}

.amount-info {
  text-align: right;
}

.amount {
  display: flex;
  align-items: baseline;
  justify-content: flex-end;
  font-size: 36px;
  font-weight: bold;
  margin-bottom: 4px;
}

.symbol {
  font-size: 24px;
  margin-right: 4px;
}

.amount-label {
  font-size: 14px;
  opacity: 0.9;
}

.progress-section {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  padding: 20px;
}

.progress-section :deep(.el-steps) {
  --el-text-color-primary: white;
  --el-text-color-regular: rgba(255, 255, 255, 0.8);
  --el-border-color-light: rgba(255, 255, 255, 0.3);
}

.info-section,
.product-section {
  padding: 30px;
  border-bottom: 1px solid #f0f0f0;
}

.info-section h4,
.product-section h4 {
  margin: 0 0 20px 0;
  color: #333;
  font-size: 18px;
  font-weight: 600;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 16px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f8f9fa;
}

.info-item .label {
  color: #666;
  font-weight: 500;
}

.info-item .value {
  color: #333;
  font-weight: 600;
}

.product-card {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.product-info {
  flex: 1;
}

.product-info h5 {
  margin: 0 0 16px 0;
  color: #333;
  font-size: 20px;
  font-weight: 600;
}

.product-features {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 12px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
  font-size: 14px;
}

.feature-item .el-icon {
  color: #409eff;
}

.product-price {
  display: flex;
  align-items: baseline;
  font-size: 24px;
  font-weight: bold;
  color: #e6a23c;
}

.price-symbol {
  font-size: 16px;
  margin-right: 4px;
}

.actions-section {
  padding: 30px;
  text-align: center;
  background: #fafafa;
}

.actions-section .el-button {
  margin: 0 8px;
  min-width: 120px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 400px;
  gap: 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .order-detail-container {
    padding: 10px;
  }
  
  .header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .status-header {
    flex-direction: column;
    gap: 20px;
  }
  
  .amount-info {
    text-align: left;
  }
  
  .info-grid {
    grid-template-columns: 1fr;
  }
  
  .product-card {
    flex-direction: column;
    gap: 16px;
  }
  
  .product-features {
    grid-template-columns: 1fr;
  }
  
  .actions-section .el-button {
    display: block;
    margin: 8px auto;
    width: 100%;
    max-width: 200px;
  }
}
</style>
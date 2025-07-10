<template>
  <div class="membership-container">
    <div class="header">
      <h2>会员中心</h2>
      <p>升级您的CloudDrive体验，享受更多存储空间和专属特权</p>
    </div>

    <!-- 当前订阅状态 -->
    <div class="current-subscription" v-if="currentSubscription">
      <el-card class="subscription-card">
        <div class="subscription-info">
          <div class="subscription-badge">
            <el-tag :type="currentSubscription.isActive ? 'success' : 'danger'">
              {{ currentSubscription.isActive ? '有效' : '已过期' }}
            </el-tag>
          </div>
          <h3>{{ currentSubscription.membershipLevelName }}</h3>
          <p class="subscription-time">
            <span>开始时间：{{ formatDate(currentSubscription.startDate) }}</span>
            <span>结束时间：{{ formatDate(currentSubscription.endDate) }}</span>
          </p>
          <p class="subscription-remaining" v-if="currentSubscription.isActive">
            剩余 {{ currentSubscription.daysRemaining }} 天
          </p>
        </div>
        <div class="subscription-actions">
          <el-button 
            v-if="currentSubscription.status === 'active'"
            type="danger" 
            @click="handleCancelSubscription"
            :loading="cancelLoading"
          >
            取消订阅
          </el-button>
        </div>
      </el-card>
    </div>

    <!-- 会员等级列表 -->
    <div class="membership-levels">
      <h3>选择会员等级</h3>
      <div class="levels-grid">
        <div 
          v-for="level in membershipLevels" 
          :key="level.id"
          class="level-card"
          :class="{ 'recommended': level.isRecommended }"
        >
          <div class="level-header">
            <h4>{{ level.name }}</h4>
            <div class="level-price">
              <span class="price-symbol">¥</span>
              <span class="price-amount">{{ level.price }}</span>
              <span class="price-period" v-if="level.durationDays > 0">
                /{{ level.durationDays }}天
              </span>
            </div>
          </div>
          
          <div class="level-features">
            <div class="feature-item">
              <i class="el-icon-folder-opened"></i>
              <span>{{ level.storageQuotaFormatted }} 存储空间</span>
            </div>
            <div class="feature-item">
              <i class="el-icon-upload"></i>
              <span>单文件最大 {{ level.maxFileSizeFormatted }}</span>
            </div>
            <div class="feature-item" v-if="level.features">
              <i class="el-icon-star-on"></i>
              <span>{{ level.features }}</span>
            </div>
          </div>

          <div class="level-actions">
            <el-button 
              type="primary" 
              :disabled="level.price === 0"
              @click="handleSelectLevel(level)"
              :loading="selectedLevelId === level.id && orderLoading"
            >
              {{ level.price === 0 ? '免费版' : '立即购买' }}
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 支付方式对话框 -->
    <el-dialog 
      v-model="paymentDialogVisible" 
      title="选择支付方式" 
      width="500px"
      :close-on-click-modal="false"
    >
      <div class="payment-options">
        <div class="selected-level-info">
          <h4>{{ selectedLevel?.name }}</h4>
          <p class="payment-amount">支付金额：¥{{ selectedLevel?.price }}</p>
        </div>
        
        <div class="payment-methods">
          <el-radio-group v-model="selectedPaymentMethod">
            <el-radio label="alipay">
              <i class="payment-icon alipay"></i>
              支付宝
            </el-radio>
            <el-radio label="wechat">
              <i class="payment-icon wechat"></i>
              微信支付
            </el-radio>
            <el-radio label="bank_card">
              <i class="payment-icon bank-card"></i>
              银行卡
            </el-radio>
          </el-radio-group>
        </div>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="paymentDialogVisible = false">取消</el-button>
          <el-button 
            type="primary" 
            @click="handlePayment"
            :loading="paymentLoading"
            :disabled="!selectedPaymentMethod"
          >
            确认支付
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { membershipApi } from '../api/membership'
import type { MembershipLevel, UserSubscription } from '../types'

// 响应式数据
const membershipLevels = ref<MembershipLevel[]>([])
const currentSubscription = ref<UserSubscription | null>(null)
const paymentDialogVisible = ref(false)
const selectedLevel = ref<MembershipLevel | null>(null)
const selectedLevelId = ref('')
const selectedPaymentMethod = ref('')
const orderLoading = ref(false)
const paymentLoading = ref(false)
const cancelLoading = ref(false)

// 获取用户ID (这里应该从用户状态或token中获取)
const getCurrentUserId = () => {
  // 临时使用固定用户ID，实际应用中应从用户状态获取
  //return 'user-001'
  const userId = localStorage.getItem('UserId')
  if (!userId) {
    console.warn('No UserId found in localStorage')
    return ''
  }
  return userId
}

// 格式化日期
const formatDate = (date: Date | string) => {
  const d = new Date(date)
  return d.toLocaleDateString('zh-CN')
}

// 获取会员等级列表
const fetchMembershipLevels = async () => {
  try {
    const response = await membershipApi.getAllLevels()
    if (response.data.code === 200) {
      membershipLevels.value = response.data.data
    }
  } catch (error) {
    console.error('获取会员等级失败:', error)
    ElMessage.error('获取会员等级失败')
  }
}

// 获取当前订阅
const fetchCurrentSubscription = async () => {
  try {
    const userId = getCurrentUserId()
    if (!userId) {
      console.warn('无法获取用户ID')
      return
    }
    
    console.log('正在获取用户订阅信息:', userId)
    const response = await membershipApi.getCurrentSubscription(userId)
    console.log('订阅信息响应:', response.data)
    
    if (response.data.code === 200) {
      currentSubscription.value = response.data.data
      console.log('当前订阅已更新:', currentSubscription.value)
    } else {
      console.log('获取订阅失败:', response.data.message)
      currentSubscription.value = null
    }
  } catch (error) {
    console.log('暂无有效订阅或获取失败:', error)
    currentSubscription.value = null
  }
}

// 选择会员等级
const handleSelectLevel = (level: MembershipLevel) => {
  if (level.price === 0) {
    ElMessage.info('免费版无需购买')
    return
  }
  
  selectedLevel.value = level
  selectedLevelId.value = level.id
  selectedPaymentMethod.value = ''
  paymentDialogVisible.value = true
}

// 处理支付
const handlePayment = async () => {
  if (!selectedLevel.value || !selectedPaymentMethod.value) {
    ElMessage.error('请选择支付方式')
    return
  }

  paymentLoading.value = true
  
  try {
    const userId = getCurrentUserId()
    
    // 创建支付订单
    const orderResponse = await membershipApi.createPaymentOrder({
      userId,
      membershipLevelId: selectedLevel.value.id,
      paymentMethod: selectedPaymentMethod.value
    })
    
    if (orderResponse.data.code === 200) {
      const order = orderResponse.data.data
      
      // 模拟支付成功
      const transactionId = 'TXN' + Date.now()
      const paymentResponse = await membershipApi.processPayment(order.id, transactionId)
      
      if (paymentResponse.data.code === 200) {
        ElMessage.success('支付成功！')
        paymentDialogVisible.value = false
        
        // 延迟刷新当前订阅状态，确保后端数据已更新
        setTimeout(async () => {
          await fetchCurrentSubscription()
        }, 1000)
        
        // 立即刷新一次
        await fetchCurrentSubscription()
      } else {
        ElMessage.error('支付失败: ' + paymentResponse.data.message)
      }
    } else {
      ElMessage.error('创建订单失败: ' + orderResponse.data.message)
    }
  } catch (error) {
    console.error('支付失败:', error)
    ElMessage.error('支付失败')
  } finally {
    paymentLoading.value = false
    selectedLevelId.value = ''
  }
}

// 取消订阅
const handleCancelSubscription = async () => {
  if (!currentSubscription.value) return
  
  try {
    await ElMessageBox.confirm(
      '确定要取消当前订阅吗？取消后将无法享受会员特权。',
      '取消订阅',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    cancelLoading.value = true
    
    const response = await membershipApi.cancelSubscription(currentSubscription.value.id)
    if (response.data.code === 200) {
      ElMessage.success('订阅已取消')
      await fetchCurrentSubscription()
    } else {
      ElMessage.error('取消订阅失败: ' + response.data.message)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消订阅失败:', error)
      ElMessage.error('取消订阅失败')
    }
  } finally {
    cancelLoading.value = false
  }
}

// 初始化
onMounted(async () => {
  await Promise.all([
    fetchMembershipLevels(),
    fetchCurrentSubscription()
  ])
})
</script>

<style scoped>
.membership-container {
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

.current-subscription {
  margin-bottom: 40px;
}

.subscription-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.subscription-card :deep(.el-card__body) {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.subscription-info h3 {
  margin: 10px 0;
  font-size: 24px;
}

.subscription-time {
  margin: 10px 0;
  opacity: 0.9;
}

.subscription-time span {
  margin-right: 20px;
}

.subscription-remaining {
  font-size: 18px;
  font-weight: bold;
}

.membership-levels h3 {
  margin-bottom: 20px;
  color: #333;
}

.levels-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.level-card {
  border: 2px solid #e4e7ed;
  border-radius: 8px;
  padding: 20px;
  transition: all 0.3s ease;
}

.level-card:hover {
  border-color: #409eff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.level-card.recommended {
  border-color: #67c23a;
  position: relative;
}

.level-card.recommended::before {
  content: '推荐';
  position: absolute;
  top: -10px;
  right: 20px;
  background: #67c23a;
  color: white;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 12px;
}

.level-header {
  text-align: center;
  margin-bottom: 20px;
}

.level-header h4 {
  margin: 0 0 10px 0;
  color: #333;
  font-size: 20px;
}

.level-price {
  display: flex;
  align-items: baseline;
  justify-content: center;
  margin-bottom: 10px;
}

.price-symbol {
  font-size: 16px;
  color: #666;
}

.price-amount {
  font-size: 32px;
  font-weight: bold;
  color: #e6a23c;
}

.price-period {
  font-size: 14px;
  color: #666;
  margin-left: 5px;
}

.level-features {
  margin-bottom: 20px;
}

.feature-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  color: #666;
}

.feature-item i {
  margin-right: 8px;
  color: #409eff;
}

.level-actions {
  text-align: center;
}

.level-actions .el-button {
  width: 100%;
}

.payment-options {
  margin-bottom: 20px;
}

.selected-level-info {
  text-align: center;
  margin-bottom: 20px;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
}

.selected-level-info h4 {
  margin: 0 0 10px 0;
  color: #333;
}

.payment-amount {
  font-size: 24px;
  font-weight: bold;
  color: #e6a23c;
  margin: 0;
}

.payment-methods .el-radio {
  display: block;
  margin: 15px 0;
  padding: 15px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.payment-methods .el-radio:hover {
  border-color: #409eff;
  background: #f0f9ff;
}

.payment-methods .el-radio.is-checked {
  border-color: #409eff;
  background: #f0f9ff;
}

.payment-icon {
  width: 20px;
  height: 20px;
  margin-right: 10px;
  display: inline-block;
  background-size: contain;
  background-repeat: no-repeat;
  background-position: center;
}

.payment-icon.alipay {
  background-color: #1677ff;
  border-radius: 4px;
}

.payment-icon.wechat {
  background-color: #07c160;
  border-radius: 4px;
}

.payment-icon.bank-card {
  background-color: #f5222d;
  border-radius: 4px;
}

.dialog-footer {
  text-align: right;
}
</style>
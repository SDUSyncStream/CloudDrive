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
      </el-card>
    </div>

    <!-- 会员等级列表 -->
    <div class="membership-levels">
      <h3>选择会员等级</h3>
      <div class="levels-grid">
        <div 
          v-for="group in groupedLevels" 
          :key="group.name"
          class="level-card"
          :class="{ 
            'recommended': group.isRecommended, 
            'disabled': group.isDisabled,
            'current-level': group.name === currentUserLevel
          }"
          @mouseenter="handleCardHover(group.name, true)"
          @mouseleave="handleCardHover(group.name, false)"
        >
          <div class="level-header">
            <h4>{{ group.name }}</h4>
            <div class="level-price">
              <span class="price-from" v-if="group.monthlyOption && group.monthlyOption.price > 0">
                <span class="price-symbol">¥</span>
                <span class="price-amount">{{ group.monthlyOption.price }}</span>
                <span class="price-period">/月起</span>
              </span>
              <span v-else-if="group.name === '免费版'" class="free-label">
                免费使用
              </span>
            </div>
          </div>
          
          <div class="level-features">
            <div class="feature-item">
              <i class="el-icon-folder-opened"></i>
              <span>{{ group.storageQuotaFormatted }} 存储空间</span>
            </div>
            <div class="feature-item">
              <i class="el-icon-upload"></i>
              <span>单文件最大 {{ group.maxFileSizeFormatted }}</span>
            </div>
            <div class="feature-item" v-if="group.features">
              <i class="el-icon-star-on"></i>
              <span>{{ group.features }}</span>
            </div>
          </div>

          <div class="level-actions">
            <el-button 
              type="primary" 
              :disabled="group.name === currentUserLevel || group.name === '免费版' || group.isDisabled"
              @click="handleSelectLevelGroup(group)"
              :loading="selectedLevelId === group.name && orderLoading"
            >
              {{ getButtonText(group) }}
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 支付方式对话框 -->
    <el-dialog 
      v-model="paymentDialogVisible" 
      title="选择套餐" 
      width="600px"
      :close-on-click-modal="false"
    >
      <div class="payment-options">
        <div class="selected-level-info">
          <h4>{{ selectedGroupLevel?.name }}</h4>
          <p class="level-description">{{ selectedGroupLevel?.features }}</p>
        </div>
        
        <!-- 套餐期间选择 -->
        <div class="duration-selection">
          <h5>选择套餐期间</h5>
          <el-radio-group v-model="selectedDuration" class="duration-options">
            <el-radio 
              v-if="selectedGroupLevel?.monthlyOption" 
              :label="selectedGroupLevel.monthlyOption.id"
              class="duration-card"
            >
              <div class="duration-content">
                <div class="duration-header">
                  <span class="duration-label">月付套餐</span>
                  <span class="duration-price">
                    ¥{{ selectedGroupLevel.monthlyOption.price }}/月
                  </span>
                </div>
                <div class="duration-desc">{{ selectedGroupLevel.monthlyOption.durationDays }}天有效期</div>
              </div>
            </el-radio>
          </el-radio-group>
        </div>
        
        <!-- 支付方式选择 -->
        <div class="payment-methods" v-if="selectedDuration">
          <h5>选择支付方式</h5>
          <el-radio-group v-model="selectedPaymentMethod" class="payment-method-options">
            <el-radio label="alipay" class="payment-method-item">
              <i class="payment-icon alipay"></i>
              支付宝
            </el-radio>
            <el-radio label="wechat" class="payment-method-item">
              <i class="payment-icon wechat"></i>
              微信支付
            </el-radio>
            <el-radio label="bank_card" class="payment-method-item">
              <i class="payment-icon bank-card"></i>
              银行卡
            </el-radio>
          </el-radio-group>
        </div>
        
        <!-- 订单总价 -->
        <div class="order-summary" v-if="selectedLevel">
          <div class="summary-item">
            <span>套餐名称：</span>
            <span>{{ selectedLevel.name }}</span>
          </div>
          <div class="summary-item total">
            <span>支付金额：</span>
            <span class="total-price">¥{{ selectedLevel.price }}</span>
          </div>
        </div>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="paymentDialogVisible = false">取消</el-button>
          <el-button 
            type="primary" 
            @click="handlePayment"
            :loading="paymentLoading"
            :disabled="!selectedDuration || !selectedPaymentMethod"
          >
            确认支付
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { membershipApi } from '../api/membership'
import type { MembershipLevel, UserSubscription } from '../types'

// 响应式数据
const membershipLevels = ref<MembershipLevel[]>([])
const groupedLevels = ref<any[]>([]) // 分组后的等级数据
const currentSubscription = ref<UserSubscription | null>(null)
const paymentDialogVisible = ref(false)
const selectedLevel = ref<MembershipLevel | null>(null)
const selectedGroupLevel = ref<any>(null) // 选择的分组等级
const selectedLevelId = ref('')
const selectedPaymentMethod = ref('')
const selectedDuration = ref('') // 选择的期间（月费/年费）
const orderLoading = ref(false)
const paymentLoading = ref(false)
const hoveredCard = ref('')
const currentUserLevel = ref('免费版') // 用户当前等级

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

// 获取当前用户等级的优先级
const getCurrentLevelPriority = () => {
  const levelPriorityMap = {
    '免费版': 0,
    '标准版': 10,
    '高级版': 20,
    '专业版': 30,
    '企业版': 40
  }
  return levelPriorityMap[currentUserLevel.value] || 0
}

// 获取指定等级的优先级
const getGroupPriority = (levelName: string) => {
  const levelPriorityMap = {
    '免费版': 0,
    '标准版': 10,
    '高级版': 20,
    '专业版': 30,
    '企业版': 40
  }
  return levelPriorityMap[levelName] || 0
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
      await groupMembershipLevels()
    }
  } catch (error) {
    console.error('获取会员等级失败:', error)
    ElMessage.error('获取会员等级失败')
  }
}

// 将会员等级分组并检查订阅状态
const groupMembershipLevels = async () => {
  const levels = membershipLevels.value
  
  // 直接使用月度会员等级，不再分组
  const grouped = new Map()
  
  levels.forEach(level => {
    // 只处理月度会员等级，忽略年度会员
    if (!level.name.includes('年度')) {
      grouped.set(level.name, {
        name: level.name,
        storageQuota: level.storageQuota,
        maxFileSize: level.maxFileSize,
        features: level.features,
        storageQuotaFormatted: level.storageQuotaFormatted,
        maxFileSizeFormatted: level.maxFileSizeFormatted,
        monthlyOption: level,
        yearlyOption: null,
        isRecommended: level.isRecommended || level.name === '高级版'
      })
    }
  })
  
  // 转换为数组并排序
  const sortedGroups = Array.from(grouped.values()).sort((a, b) => {
    const order = ['免费版', '标准版', '高级版', '专业版', '企业版']
    return order.indexOf(a.name) - order.indexOf(b.name)
  })
  
  // 使用后端API检查每个等级的订阅状态
  const userId = getCurrentUserId()
  if (userId) {
    for (const group of sortedGroups) {
      // 检查月费选项的订阅状态
      if (group.monthlyOption) {
        try {
          const response = await membershipApi.canSubscribeToLevel(userId, group.monthlyOption.id)
          group.canSubscribeMonthly = response.data.code === 200 ? response.data.data : true
        } catch (error) {
          group.canSubscribeMonthly = true
        }
      }
      
      // 只有月费选项，禁用状态基于月费选项
      group.isDisabled = !group.canSubscribeMonthly
      
      // 如果用户当前等级高于或等于推荐等级，取消推荐状态
      if (group.isRecommended && currentUserLevel.value) {
        const currentLevelPriority = getCurrentLevelPriority()
        const groupPriority = getGroupPriority(group.name)
        if (currentLevelPriority >= groupPriority) {
          group.isRecommended = false
        }
      }
    }
  }
  
  groupedLevels.value = sortedGroups
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
      // 更新用户当前等级
      if (currentSubscription.value) {
        currentUserLevel.value = currentSubscription.value.membershipLevelName
      } else {
        currentUserLevel.value = '免费版'
      }
      console.log('当前订阅已更新:', currentSubscription.value)
      console.log('当前用户等级:', currentUserLevel.value)
      // 重新分组会员等级以更新禁用状态和推荐状态
      await groupMembershipLevels()
    } else {
      console.log('获取订阅失败:', response.data.message)
      currentSubscription.value = null
      currentUserLevel.value = '免费版'
      // 即使获取失败也要重新分组以更新推荐状态
      await groupMembershipLevels()
    }
  } catch (error) {
    console.log('暂无有效订阅或获取失败:', error)
    currentSubscription.value = null
    currentUserLevel.value = '免费版'
    // 即使出错也要重新分组以更新推荐状态
    await groupMembershipLevels()
  }
}

// 选择会员等级组
const handleSelectLevelGroup = (group: any) => {
  if (group.name === currentUserLevel.value) {
    ElMessage.info(`您当前已是${group.name}`)
    return
  }
  
  if (group.name === '免费版') {
    ElMessage.info('免费版无需购买')
    return
  }
  
  if (group.isDisabled) {
    ElMessage.info('该会员等级已被您当前的订阅等级包含，无需重复购买')
    return
  }
  
  selectedGroupLevel.value = group
  selectedLevel.value = null
  selectedLevelId.value = group.name
  selectedPaymentMethod.value = ''
  selectedDuration.value = ''
  paymentDialogVisible.value = true
}

// 监听期间选择变化
const updateSelectedLevel = () => {
  if (!selectedDuration.value || !selectedGroupLevel.value) {
    selectedLevel.value = null
    return
  }
  
  // 根据选择的期间ID找到对应的会员等级
  const level = membershipLevels.value.find(l => l.id === selectedDuration.value)
  if (level) {
    selectedLevel.value = level
  }
}

// 添加监听器
watch(selectedDuration, updateSelectedLevel)

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

// 获取按钮文本
const getButtonText = (group: any) => {
  if (group.name === currentUserLevel.value) {
    return '当前版本'
  }
  if (group.name === '免费版') {
    return '免费版本'
  }
  if (group.isDisabled) {
    return '已订阅'
  }
  return '选择套餐'
}

// 处理卡片悬停
const handleCardHover = (cardName: string, isHovered: boolean) => {
  hoveredCard.value = isHovered ? cardName : ''
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
  justify-content: center;
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
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 30px;
  perspective: 1000px;
}

.level-card {
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
  border: 1px solid rgba(0, 0, 0, 0.05);
  border-radius: 20px;
  padding: 32px 24px;
  transition: all 0.15s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  box-shadow: 0 2px 20px rgba(0, 0, 0, 0.08);
  backdrop-filter: blur(10px);
  transform-style: preserve-3d;
  animation: cardEntry 0.4s cubic-bezier(0.4, 0, 0.2, 1) forwards;
  opacity: 0;
  transform: translateY(20px);
  will-change: transform, box-shadow;
}

.level-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.1) 0%, rgba(103, 194, 58, 0.1) 100%);
  opacity: 0;
  transition: opacity 0.1s cubic-bezier(0.4, 0, 0.2, 1);
  pointer-events: none;
}

.level-card:hover {
  transform: translateY(-6px) scale(1.01);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.12);
  border-color: rgba(64, 158, 255, 0.3);
}

.level-card:hover::before {
  opacity: 1;
}

.level-card.recommended {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-color: transparent;
  position: relative;
  animation: pulse-glow 1.5s ease-in-out infinite;
}

.level-card.recommended::before {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.1) 0%, rgba(255, 255, 255, 0.05) 100%);
}

.level-card.recommended::after {
  content: '推荐';
  position: absolute;
  top: -8px;
  right: 24px;
  background: linear-gradient(135deg, #ff6b6b 0%, #ffa726 100%);
  color: white;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(255, 107, 107, 0.3);
  animation: float 2s ease-in-out infinite;
}

@keyframes pulse-glow {
  0%, 100% {
    box-shadow: 0 20px 60px rgba(102, 126, 234, 0.2);
  }
  50% {
    box-shadow: 0 20px 60px rgba(102, 126, 234, 0.4);
  }
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-4px);
  }
}


.level-card:nth-child(1) { animation-delay: 0.05s; }
.level-card:nth-child(2) { animation-delay: 0.1s; }
.level-card:nth-child(3) { animation-delay: 0.15s; }
.level-card:nth-child(4) { animation-delay: 0.2s; }
.level-card:nth-child(5) { animation-delay: 0.25s; }

@keyframes cardEntry {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.level-card.recommended {
  animation: cardEntryRecommended 0.5s cubic-bezier(0.4, 0, 0.2, 1) forwards;
}

@keyframes cardEntryRecommended {
  0% {
    opacity: 0;
    transform: translateY(20px) scale(0.98);
  }
  70% {
    transform: translateY(-2px) scale(1.01);
  }
  100% {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.level-header {
  text-align: center;
  margin-bottom: 24px;
  position: relative;
}

.level-header h4 {
  margin: 0 0 16px 0;
  color: #333;
  font-size: 24px;
  font-weight: 700;
  letter-spacing: -0.5px;
  transition: all 0.15s cubic-bezier(0.4, 0, 0.2, 1);
}

.level-card.recommended .level-header h4 {
  color: white;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.level-price {
  display: flex;
  align-items: baseline;
  justify-content: center;
  margin-bottom: 16px;
  position: relative;
}

.price-symbol {
  font-size: 20px;
  color: #666;
  font-weight: 600;
  transition: all 0.3s ease;
}

.price-amount {
  font-size: 42px;
  font-weight: 800;
  color: #e6a23c;
  margin: 0 4px;
  position: relative;
  transition: all 0.15s cubic-bezier(0.4, 0, 0.2, 1);
}

.price-amount::before {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, #e6a23c, #f39c12);
  transform: scaleX(0);
  transition: transform 0.15s cubic-bezier(0.4, 0, 0.2, 1);
}

.level-card:hover .price-amount::before {
  transform: scaleX(1);
}

.price-period {
  font-size: 16px;
  color: #666;
  margin-left: 8px;
  font-weight: 500;
}

.level-card.recommended .price-symbol,
.level-card.recommended .price-period {
  color: rgba(255, 255, 255, 0.9);
}

.level-card.recommended .price-amount {
  color: #ffd700;
  text-shadow: 0 2px 8px rgba(255, 215, 0, 0.3);
}

.price-from {
  display: flex;
  align-items: baseline;
  justify-content: center;
  position: relative;
}

.free-label {
  font-size: 22px;
  color: #67c23a;
  font-weight: 700;
  text-shadow: 0 2px 4px rgba(103, 194, 58, 0.2);
  animation: shimmer 1.5s ease-in-out infinite;
}

@keyframes shimmer {
  0%, 100% {
    text-shadow: 0 2px 4px rgba(103, 194, 58, 0.2);
  }
  50% {
    text-shadow: 0 2px 8px rgba(103, 194, 58, 0.4);
  }
}

.level-features {
  margin-bottom: 24px;
}

.feature-item {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  color: #666;
  padding: 8px 0;
  border-radius: 8px;
  transition: all 0.15s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
}

.feature-item:hover {
  background: rgba(64, 158, 255, 0.05);
  transform: translateX(4px);
}

.feature-item i {
  margin-right: 12px;
  color: #409eff;
  font-size: 16px;
  width: 20px;
  text-align: center;
  transition: all 0.15s cubic-bezier(0.4, 0, 0.2, 1);
}

.feature-item:hover i {
  color: #1976d2;
  transform: scale(1.1);
}

.level-card.recommended .feature-item {
  color: rgba(255, 255, 255, 0.9);
}

.level-card.recommended .feature-item i {
  color: #ffd700;
}

.level-card.recommended .feature-item:hover {
  background: rgba(255, 255, 255, 0.1);
}

.level-actions {
  text-align: center;
  margin-top: 32px;
}

.level-actions .el-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 24px;
  transition: all 0.15s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.level-actions .el-button::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  transform: translate(-50%, -50%);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.level-actions .el-button:hover::before {
  width: 300px;
  height: 300px;
}

.level-actions .el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(64, 158, 255, 0.25);
}

.level-card.recommended .level-actions .el-button {
  background: linear-gradient(135deg, #ffd700 0%, #ffb300 100%);
  border: none;
  color: #333;
}

.level-card.recommended .level-actions .el-button:hover {
  box-shadow: 0 8px 25px rgba(255, 215, 0, 0.4);
}

.level-card .level-header::after {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.4), transparent);
  transition: left 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.level-card:hover .level-header::after {
  left: 100%;
}

.level-card.recommended .level-header::after {
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
}

.level-card.disabled {
  opacity: 0.6;
  background: linear-gradient(135deg, #f5f5f5 0%, #e8e8e8 100%);
  border-color: #ddd;
  cursor: not-allowed;
  pointer-events: none;
}

.level-card.disabled:hover {
  transform: none;
  box-shadow: 0 2px 20px rgba(0, 0, 0, 0.08);
}

.level-card.disabled .level-header h4 {
  color: #999;
}

.level-card.disabled .price-amount {
  color: #999;
}

.level-card.disabled .feature-item {
  color: #999;
}

.level-card.disabled .feature-item i {
  color: #ccc;
}

.level-card.disabled .level-actions .el-button {
  background-color: #f5f5f5;
  border-color: #ddd;
  color: #999;
}

/* 当前版本高亮样式 */
.level-card.current-level {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
  color: white;
  border-color: #67c23a;
  position: relative;
  transform: scale(1.02);
  box-shadow: 0 15px 40px rgba(103, 194, 58, 0.3);
}

.level-card.current-level::before {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.15) 0%, rgba(255, 255, 255, 0.05) 100%);
  opacity: 1;
}

.level-card.current-level::after {
  content: '当前版本';
  position: absolute;
  top: -8px;
  right: 24px;
  background: linear-gradient(135deg, #409eff 0%, #67c23a 100%);
  color: white;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
  animation: float 2s ease-in-out infinite;
}

.level-card.current-level .level-header h4 {
  color: white;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.level-card.current-level .price-symbol,
.level-card.current-level .price-period {
  color: rgba(255, 255, 255, 0.9);
}

.level-card.current-level .price-amount {
  color: #ffd700;
  text-shadow: 0 2px 8px rgba(255, 215, 0, 0.3);
}

.level-card.current-level .feature-item {
  color: rgba(255, 255, 255, 0.9);
}

.level-card.current-level .feature-item i {
  color: #ffd700;
}

.level-card.current-level .feature-item:hover {
  background: rgba(255, 255, 255, 0.1);
}

.level-card.current-level .level-actions .el-button {
  background: linear-gradient(135deg, #ffd700 0%, #ffb300 100%);
  border: none;
  color: #333;
  font-weight: 700;
}

.level-card.current-level .level-actions .el-button:hover {
  box-shadow: 0 8px 25px rgba(255, 215, 0, 0.4);
  transform: translateY(-2px);
}

.level-card.current-level .level-actions .el-button:disabled {
  background: linear-gradient(135deg, #ffd700 0%, #ffb300 100%);
  color: #333;
  opacity: 1;
}

.level-card.current-level:hover {
  transform: scale(1.02) translateY(-2px);
  box-shadow: 0 20px 50px rgba(103, 194, 58, 0.4);
}

/* 当前版本优先级高于推荐版本 */
.level-card.current-level.recommended::after {
  content: '当前版本';
  background: linear-gradient(135deg, #409eff 0%, #67c23a 100%);
}

.payment-options {
  margin-bottom: 20px;
  max-width: 100%;
  overflow: hidden;
}

.payment-options :deep(.el-radio) {
  margin-right: 0 !important;
  margin-bottom: 0 !important;
}

.payment-options :deep(.el-radio__label) {
  padding-left: 0 !important;
}

.payment-options :deep(.el-radio-group) {
  line-height: normal;
}

.payment-options :deep(.el-radio__inner) {
  width: 14px;
  height: 14px;
}

.selected-level-info {
  text-align: center;
  margin-bottom: 24px;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
}

.selected-level-info h4 {
  margin: 0 0 8px 0;
  color: #333;
  font-size: 20px;
}

.level-description {
  color: #666;
  font-size: 14px;
  margin: 0;
}

.duration-selection {
  margin-bottom: 24px;
}

.duration-selection h5 {
  margin: 0 0 16px 0;
  color: #333;
  font-size: 16px;
  font-weight: 600;
}

.duration-options {
  display: flex;
  flex-direction: column;
  gap: 12px;
  line-height: normal;
}

.duration-options :deep(.el-radio-group) {
  line-height: normal;
}

.duration-options :deep(.el-radio__inner) {
  width: 14px;
  height: 14px;
}

.duration-options :deep(.el-radio) {
  margin-right: 0 !important;
  margin-bottom: 0 !important;
  display: block !important;
  width: 100% !important;
  height: auto !important;
}

.duration-options :deep(.el-radio__input) {
  position: absolute !important;
  top: 50% !important;
  left: 16px !important;
  transform: translateY(-50%) !important;
  z-index: 1 !important;
  margin: 0 !important;
}

.duration-options :deep(.el-radio__label) {
  padding-left: 0 !important;
  width: 100% !important;
  display: block !important;
  font-size: inherit !important;
  line-height: inherit !important;
}

.duration-card {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 0;
  margin: 0;
  transition: all 0.3s ease;
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
  display: block;
  position: relative;
}

.duration-card:hover {
  border-color: #409eff;
}

.duration-card.is-checked {
  border-color: #409eff;
  background: #f0f9ff;
}

.recommended-duration {
  border-color: #67c23a;
}

.recommended-duration.is-checked {
  border-color: #67c23a;
  background: #f0f9ff;
}

.duration-content {
  padding: 16px 16px 16px 45px;
  width: 100%;
  box-sizing: border-box;
  overflow: hidden;
}

.duration-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.duration-label {
  font-weight: 600;
  color: #333;
  display: flex;
  align-items: center;
  gap: 8px;
}

.duration-price {
  font-size: 18px;
  font-weight: bold;
  color: #e6a23c;
}

.duration-desc {
  color: #666;
  font-size: 14px;
}

.save-tip {
  color: #67c23a;
  margin-left: 8px;
  font-weight: 500;
}

.payment-methods h5 {
  margin: 0 0 16px 0;
  color: #333;
  font-size: 16px;
  font-weight: 600;
}

.payment-method-options {
  display: flex;
  gap: 12px;
}

.payment-method-options :deep(.el-radio) {
  margin-right: 0 !important;
  flex: 1 !important;
  height: auto !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
}

.payment-method-options :deep(.el-radio__label) {
  padding-left: 0 !important;
  width: 100% !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  font-size: inherit !important;
  line-height: inherit !important;
}

.payment-method-item {
  flex: 1;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 12px;
  margin: 0;
  text-align: center;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.payment-method-item:hover {
  border-color: #409eff;
}

.payment-method-item.is-checked {
  border-color: #409eff;
  background: #f0f9ff;
}

.payment-method-options :deep(.el-radio__input) {
  position: absolute !important;
  top: 50% !important;
  left: 8px !important;
  transform: translateY(-50%) !important;
  z-index: 1 !important;
  margin: 0 !important;
}

.order-summary {
  margin-top: 24px;
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.summary-item:last-child {
  margin-bottom: 0;
}

.summary-item.total {
  border-top: 1px solid #e4e7ed;
  padding-top: 8px;
  margin-top: 8px;
  font-weight: 600;
}

.total-price {
  font-size: 20px;
  color: #e6a23c;
  font-weight: bold;
}

.payment-amount {
  font-size: 24px;
  font-weight: bold;
  color: #e6a23c;
  margin: 0;
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

/* 响应式布局 */
@media (max-width: 768px) {
  .payment-method-options {
    flex-direction: column;
  }
  
  .payment-method-options :deep(.el-radio) {
    flex: none;
  }
  
  .payment-method-item {
    flex: none;
  }
  
  .duration-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
  
  .duration-price {
    font-size: 16px;
  }
  
  .save-tip {
    margin-left: 0;
    display: block;
    margin-top: 4px;
  }
  
  .duration-content {
    padding: 16px 16px 16px 35px;
  }
  
  .duration-options :deep(.el-radio__input) {
    top: 50% !important;
    left: 12px !important;
    transform: translateY(-50%) !important;
  }
  
  .payment-method-options :deep(.el-radio__input) {
    top: 50% !important;
    left: 6px !important;
    transform: translateY(-50%) !important;
  }
}
</style>
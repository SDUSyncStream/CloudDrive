import { apiClient } from './index'
import type { 
  MembershipLevel, 
  UserSubscription, 
  PaymentOrder, 
  CreatePaymentOrderRequest,
  ApiResponse 
} from '../types'

export const membershipApi = {
  // 获取所有会员等级
  getAllLevels: (): Promise<ApiResponse<MembershipLevel[]>> => {
    return apiClient.get('/api/membership/levels')
  },

  // 根据ID获取会员等级
  getLevelById: (id: string): Promise<ApiResponse<MembershipLevel>> => {
    return apiClient.get(`/api/membership/levels/${id}`)
  },

  // 根据名称获取会员等级
  getLevelByName: (name: string): Promise<ApiResponse<MembershipLevel>> => {
    return apiClient.get(`/api/membership/levels/name/${name}`)
  },

  // 获取用户所有订阅
  getUserSubscriptions: (userId: string): Promise<ApiResponse<UserSubscription[]>> => {
    return apiClient.get(`/api/subscription/user/${userId}`)
  },

  // 获取用户当前有效订阅
  getCurrentSubscription: (userId: string): Promise<ApiResponse<UserSubscription>> => {
    return apiClient.get(`/api/subscription/user/${userId}/current`)
  },

  // 取消订阅
  cancelSubscription: (subscriptionId: string): Promise<ApiResponse<string>> => {
    return apiClient.post(`/api/subscription/${subscriptionId}/cancel`)
  },

  // 创建支付订单
  createPaymentOrder: (request: CreatePaymentOrderRequest): Promise<ApiResponse<PaymentOrder>> => {
    return apiClient.post('/api/payment/orders', request)
  },

  // 处理支付
  processPayment: (orderId: string, transactionId: string): Promise<ApiResponse<PaymentOrder>> => {
    return apiClient.post(`/api/payment/orders/${orderId}/pay`, null, {
      params: { transactionId }
    })
  },

  // 获取支付订单详情
  getPaymentOrder: (orderId: string): Promise<ApiResponse<PaymentOrder>> => {
    return apiClient.get(`/api/payment/orders/${orderId}`)
  },

  // 获取用户所有支付订单
  getUserPaymentOrders: (userId: string): Promise<ApiResponse<PaymentOrder[]>> => {
    return apiClient.get(`/api/payment/orders/user/${userId}`)
  }
}

export default membershipApi
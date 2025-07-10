import { apiClient } from './index'
import type { 
  MembershipLevel, 
  UserSubscription, 
  PaymentOrder, 
  CreatePaymentOrderRequest,
  ApiResponse 
} from '../types'
import type { AxiosResponse } from 'axios'

export const membershipApi = {
  // 获取所有会员等级
  getAllLevels: (): Promise<AxiosResponse<ApiResponse<MembershipLevel[]>>> => {
    return apiClient.get('/membership/levels')
  },

  // 根据ID获取会员等级
  getLevelById: (id: string): Promise<AxiosResponse<ApiResponse<MembershipLevel>>> => {
    return apiClient.get(`/membership/levels/${id}`)
  },


  // 获取用户所有订阅
  getUserSubscriptions: (userId: string): Promise<AxiosResponse<ApiResponse<UserSubscription[]>>> => {
    return apiClient.get(`/subscription/user/${userId}`)
  },

  // 获取用户当前有效订阅
  getCurrentSubscription: (userId: string): Promise<AxiosResponse<ApiResponse<UserSubscription>>> => {
    return apiClient.get(`/subscription/user/${userId}/current`)
  },

  // 取消订阅
  cancelSubscription: (subscriptionId: string): Promise<AxiosResponse<ApiResponse<string>>> => {
    return apiClient.post(`/subscription/${subscriptionId}/cancel`)
  },

  // 创建支付订单
  createPaymentOrder: (request: CreatePaymentOrderRequest): Promise<AxiosResponse<ApiResponse<PaymentOrder>>> => {
    return apiClient.post('/payment/orders', request)
  },

  // 处理支付
  processPayment: (orderId: string, transactionId: string): Promise<AxiosResponse<ApiResponse<PaymentOrder>>> => {
    return apiClient.post(`/payment/orders/${orderId}/pay`, null, {
      params: { transactionId }
    })
  },

  // 获取支付订单详情
  getPaymentOrder: (orderId: string): Promise<AxiosResponse<ApiResponse<PaymentOrder>>> => {
    return apiClient.get(`/payment/orders/${orderId}`)
  },

  // 获取用户所有支付订单
  getUserPaymentOrders: (userId: string): Promise<AxiosResponse<ApiResponse<PaymentOrder[]>>> => {
    return apiClient.get(`/payment/orders/user/${userId}`)
  },

  // 检查用户是否可以订阅特定等级
  canSubscribeToLevel: (userId: string, membershipLevelId: string): Promise<AxiosResponse<ApiResponse<boolean>>> => {
    return apiClient.get(`/subscription/user/${userId}/can-subscribe/${membershipLevelId}`)
  },

  // 取消支付订单
  cancelPaymentOrder: (orderId: string): Promise<AxiosResponse<ApiResponse<PaymentOrder>>> => {
    return apiClient.post(`/payment/orders/${orderId}/cancel`)
  }
}

export default membershipApi
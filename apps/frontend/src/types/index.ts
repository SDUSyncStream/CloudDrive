// 本地类型定义（从shared-types复制）
export interface User {
  id: string;
  username: string;
  email: string;
  avatar?: string;
  createdAt: Date;
  updatedAt: Date;
  // 扩展字段
  storageUsed?: number;  // 已使用存储空间（字节）
  storageQuota?: number; // 总存储配额（字节）
  membershipLevelId?: string; // 会员等级
}

export interface CloudFile {
  id: string;
  name: string;
  path: string;
  size: number;
  type: string;
  mimeType: string;
  isDirectory: boolean;
  parentId?: string;
  ownerId: string;
  createdAt: Date;
  updatedAt: Date;
}

export interface ApiResponse<T = any> {
  code: number;
  message: string;
  data: T;
  timestamp: number;
}

export interface PageRequest {
  page: number;
  pageSize: number;
  sortBy?: string;
  sortOrder?: 'asc' | 'desc';
}

export interface PageResponse<T = any> {
  list: T[];
  total: number;
  page: number;
  pageSize: number;
  totalPages: number;
}

export interface FileUploadRequest {
  file: File;
  parentId?: string;
  onProgress?: (progress: number) => void;
}

export interface FileUploadResponse {
  fileId: string;
  fileName: string;
  fileUrl: string;
}

export interface MembershipLevel {
  id: string;
  name: string;
  storageQuota: number;
  maxFileSize: number;
  price: number;
  durationDays: number;
  features: string;
  storageQuotaFormatted: string;
  maxFileSizeFormatted: string;
  isRecommended: boolean;
}

export interface UserSubscription {
  id: string;
  userId: string;
  membershipLevelId: string;
  membershipLevelName: string;
  startDate: Date;
  endDate: Date;
  status: string;
  paymentMethod: string;
  paymentAmount: number;
  createdAt: Date;
  isActive: boolean;
  daysRemaining: number;
}

export interface PaymentOrder {
  id: string;
  userId: string;
  membershipLevelId: string;
  membershipLevelName: string;
  orderNumber: string;
  amount: number;
  paymentMethod: string;
  status: string;
  transactionId?: string;
  paidAt?: Date;
  createdAt: Date;
}

export interface CreatePaymentOrderRequest {
  userId: string;
  membershipLevelId: string;
  paymentMethod: string;
}
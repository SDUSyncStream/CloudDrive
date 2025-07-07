// 用户相关类型
export interface User {
  id: string;
  username: string;
  email: string;
  avatar?: string;
  createdAt: Date;
  updatedAt: Date;
}

// 文件相关类型
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

// API响应类型
export interface ApiResponse<T = any> {
  code: number;
  message: string;
  data: T;
  timestamp: number;
}

// 分页类型
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

// 文件上传类型
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
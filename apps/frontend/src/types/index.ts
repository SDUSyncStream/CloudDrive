// 本地类型定义（从shared-types复制）
export interface User {
  id: string;
  username: string;
  email: string;
  avatar?: string;
  createdAt: Date;
  updatedAt: Date;
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
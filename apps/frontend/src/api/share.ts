import axios from 'axios'

export function getShareList(params?: any) {
  // 从localStorage获取用户ID
  const userId = localStorage.getItem('UserId')
  const requestParams = {
    ...params,
    userId: userId
  }
  return axios.get('/share/loadShareList', { params: requestParams })
}

export function cancelShare(shareId: string) {
  // 用 URLSearchParams 方式，确保参数名为 shareIds
  const params = new URLSearchParams();
  params.append('shareIds', shareId);
  // 如有 userId 也可一并传递
  const userId = localStorage.getItem('UserId');
  if (userId) params.append('userId', userId);
  return axios.post('/share/cancelShare', params);
}

export function cleanInvalidLinks() {
  // 需要后端支持清理失效链接的接口，如无可忽略
  return Promise.resolve()
} 
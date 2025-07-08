import request from './index';

export const login = async (data) => {
  return await request({
    method: 'post',
    url: '/auth/login',  // 相对于baseURL的路径
    data
  });
}
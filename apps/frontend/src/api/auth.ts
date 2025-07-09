import request from './index';

export const login = async (data) => {
  return await request({
    method: 'post',
    url: '/auth/login',  // 相对于baseURL的路径
    data
  });
}

export const logout = async () => {
  return await request({
    method: 'post',
    url: '/auth/logout'  // 相对于baseURL的路径
  });
}

export const register = async (data) => {
  return await request({
    method: 'post',
    url: '/auth/register',  // 相对于baseURL的路径
    data
  });
}
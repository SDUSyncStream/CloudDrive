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

export const getUserInfo = async (userId) => {
  return await request({
    method: 'get',
    url: `/user/profile/${userId}`  // 获取用户信息
  });
}

export const updateUserInfo = async (data: any) => {
  return await request({
    method: 'put',
    url: '/user/profile',  // 更新用户信息
    data
  });
}

export const uploadAvatar = async (file: File) => {
  const formData = new FormData();
  formData.append('avatar', file);
  formData.append('userId', localStorage.getItem('UserId') || ''); // 添加用户ID到表单数据
  
  return await request({
    method: 'post',
    url: '/user/avatar',  // 上传头像
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
}

export const newPwd = async (data) => {
  return await request({
    method: 'post',
    url: '/user/newpwd',
    data
  })
}

export const getVerifyCode = async (params) => {
  return await request({
    method: 'post',
    url: '/mail/forgot-password',
    params
  })
}

export const verifyCode = async (params) => {
  return await request({
    method: 'post',
    url: '/mail/verify',
    params
  })
}

export const rePwd = async (data) => {
  return await request({
    method: 'post',
    url: '/auth/refresh',
    data
  })
}

export const getAvatarAndStorage = async (userId) => {
  return await request({
    method: 'get',
    url: `/user/avatarandstorage/${userId}`,  // 获取用户头像和存储信息
  });
}
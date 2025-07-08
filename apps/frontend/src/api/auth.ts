import api from './index';

export const login = async (username,password) => {
  return await api({
    method: 'POST',
    url: '/api/auth/login',
    data: { username, password }
  });
}
  
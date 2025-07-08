import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User } from '../types'

export const useUserStore = defineStore('user', () => {
  const user = ref<User | null>(null)
  const token = ref<string | null>(localStorage.getItem('accessToken'))
  
  const isAuthenticated = computed(() => !!token.value)
  
  function setUser(userData: User) {
    user.value = userData
  }
  
  function setToken(tokenValue: string) {
    token.value = tokenValue
    localStorage.setItem('accessToken', tokenValue)
  }
  
  function logout() {
    user.value = null
    token.value = null
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
  }
  
  return {
    user,
    token,
    isAuthenticated,
    setUser,
    setToken,
    logout
  }
})
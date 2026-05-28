import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/api'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userId = ref(localStorage.getItem('userId') || '')
  const username = ref(localStorage.getItem('username') || '')
  const userInfo = ref<any>(null)
  const isAdmin = ref(localStorage.getItem('isAdmin') === 'true')
  
  const isLoggedIn = computed(() => !!localStorage.getItem('token'))
  
  function login(tokenVal: string, userIdVal: string, usernameVal: string) {
    token.value = tokenVal
    userId.value = userIdVal
    username.value = usernameVal
    localStorage.setItem('token', tokenVal)
    localStorage.setItem('userId', userIdVal)
    localStorage.setItem('username', usernameVal)
  }
  
  function logout() {
    token.value = ''
    userId.value = ''
    username.value = ''
    userInfo.value = null
    isAdmin.value = false
    localStorage.removeItem('token')
    localStorage.removeItem('userId')
    localStorage.removeItem('username')
    localStorage.removeItem('isAdmin')
  }
  
  async function fetchUserInfo() {
    try {
      const response = await api.get('/admin/users/current')
      userInfo.value = response.data.user
      isAdmin.value = response.data.isAdmin || false
      localStorage.setItem('isAdmin', String(isAdmin.value))
      return userInfo.value
    } catch (error) {
      console.error('获取用户信息失败:', error)
      // 如果接口不存在，尝试旧接口
      try {
        const response = await api.get('/auth/user')
        userInfo.value = response.data
        return userInfo.value
      } catch (e) {
        return null
      }
    }
  }
  
  return {
    token,
    userId,
    username,
    userInfo,
    isAdmin,
    isLoggedIn,
    login,
    logout,
    fetchUserInfo
  }
})


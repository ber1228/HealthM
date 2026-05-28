import axios from 'axios'
import { useUserStore } from '@/stores/user'

const api = axios.create({
  baseURL: '/api',
  timeout: 60000 // 将超时时间从30000ms改为60000ms
})

api.interceptors.request.use(
  (config) => {
    // 直接从 localStorage 读取 token，确保一致性
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
      console.log('API 请求 - 添加 token:', token.substring(0, 20) + '...')
    } else {
      console.log('API 请求 - 没有 token')
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      const userStore = useUserStore()
      const currentPath = window.location.pathname
      const reqUrl: string = error.config?.url || ''
      const isOnPublicPage = currentPath.startsWith('/announcements')
      const isPublicRequest = reqUrl.startsWith('/announcements') || reqUrl.startsWith('/uploads')
      if (!isOnPublicPage && !isPublicRequest && currentPath !== '/login' && currentPath !== '/register') {
        userStore.logout()
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  }
)

export default api

// reminders 专用 API 封装
export const remindersApi = {
  list() {
    return api.get('/reminders')
  },
  create(data: { reminderType: string; reminderTime: string; daysOfWeek?: string }) {
    return api.post('/reminders', data)
  },
  update(id: number, data: { reminderTime?: string; daysOfWeek?: string; isEnabled?: boolean }) {
    return api.put(`/reminders/${id}`, data)
  },
  remove(id: number) {
    return api.delete(`/reminders/${id}`)
  }
}

// foods 专用 API 封装（作为菜谱搜索数据源）
export const foodsApi = {
  list() {
    return api.get('/foods')
  },
  adminList() {
    return api.get('/foods/admin/list')
  },
  search(keyword: string) {
    return api.get('/foods/search', { params: { keyword } })
  },
  byCategory(category: string) {
    return api.get(`/foods/category/${encodeURIComponent(category)}`)
  },
  detail(id: number) {
    return api.get(`/foods/${id}`)
  },
  page(params: { keyword?: string; category?: string; page?: number; size?: number }) {
    return api.get('/foods/page', { params })
  },
  recommendations(limit = 5) {
    return api.get('/foods/recommendations', { params: { limit } })
  },
  create(data: any) {
    return api.post('/foods', data)
  },
  update(id: number, data: any) {
    return api.put(`/foods/${id}`, data)
  },
  delete(id: number) {
    return api.delete(`/foods/${id}`)
  }
}

// users 用户管理 API
export const usersApi = {
  list() {
    return api.get('/admin/users')
  },
  getById(id: number) {
    return api.get(`/admin/users/${id}`)
  },
  updateRole(id: number, role: string) {
    return api.put(`/admin/users/${id}/role`, { role })
  },
  delete(id: number) {
    return api.delete(`/admin/users/${id}`)
  },
  getCurrent() {
    return api.get('/admin/users/current')
  }
}

// announcements 公告 API
export const announcementsApi = {
  // 用户端
  list() {
    return api.get('/announcements')
  },
  getById(id: number) {
    return api.get(`/announcements/${id}`)
  },
  markRead(id: number) {
    return api.post(`/announcements/${id}/read`)
  },
  // 管理员端
  adminList() {
    return api.get('/announcements/admin/list')
  },
  create(data: any) {
    return api.post('/announcements', data)
  },
  update(id: number, data: any) {
    return api.put(`/announcements/${id}`, data)
  },
  delete(id: number) {
    return api.delete(`/announcements/${id}`)
  },
  togglePin(id: number, isPinned: boolean) {
    return api.put(`/announcements/${id}/pin`, JSON.stringify(isPinned), {
      headers: { 'Content-Type': 'application/json' }
    })
  }
}


// recipes 菜谱 API
export const recipesApi = {
  // 用户端
  page(params: { keyword?: string; category?: string; difficulty?: string; taste?: string; minDuration?: number; maxDuration?: number; page?: number; size?: number }) {
    return api.get('/recipes/page', { params })
  },
  detail(id: number) {
    return api.get(`/recipes/${id}`)
  },
  comment(id: number, content: string) {
    return api.post(`/recipes/${id}/comments`, { content })
  },
  // 管理端
  adminPage(params: { keyword?: string; category?: string; difficulty?: string; taste?: string; minDuration?: number; maxDuration?: number; published?: boolean; page?: number; size?: number }) {
    return api.get('/recipes/admin/page', { params })
  },
  create(data: any) {
    return api.post('/recipes', data)
  },
  update(id: number, data: any) {
    return api.put(`/recipes/${id}`, data)
  },
  delete(id: number) {
    return api.delete(`/recipes/${id}`)
  },
  publish(id: number, published: boolean) {
    return api.put(`/recipes/${id}/publish`, null, { params: { published } })
  }
}

// 文件上传 API
export const uploadApi = {
  image(formData: FormData) {
    return api.post('/upload/image', formData, {headers: {'Content-Type': 'multipart/form-data'}})
  }
}
  // 营养分析 API
export const nutritionAnalysisApi = {
  getDailyAnalysis(date: string) {
    // 确保日期格式正确
    let formattedDate = date;
    if (date.includes('T')) {
      formattedDate = date.split('T')[0];
    }
    console.log('API调用日期:', formattedDate);
    return api.get(`/record/nutrition/analysis`, {
      params: { date: formattedDate }
    });
  }
}

// AI推荐 API 封装
export const aiRecommendApi = {
  generate(data: { age?: number; gender?: string; height?: number; weight?: number; goal?: string; dietaryPreference?: string }) {
    return api.post('/ai/recommend', data)
  },
  // 新增：AI聊天接口
  chat(message: string, systemPrompt?: string) {
    return api.post('/ai/chat', { message, systemPrompt })
  },
  // 新增：清除聊天历史接口
  clearChat() {
    return api.post('/ai/chat/clear')
  }
}

// AI 食物识别 API（拍照/语音）
export const foodRecognitionApi = {
  analyzePhoto(file: File, mealType: string = 'lunch') {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('mealType', mealType)
    return api.post('/food-recognition/photo', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      timeout: 120000,
    })
  },
  analyzeVoiceText(text: string, mealType?: string) {
    return api.post('/food-recognition/voice', { text, mealType }, {
      timeout: 60000,
    })
  },
  confirmAndSave(foods: any[], mealType: string, date?: string) {
    return api.post('/food-recognition/confirm', { foods, mealType, date })
  },
}


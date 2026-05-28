<template>
  <div class="ai-recommend-page">
    <el-card class="recommend-card">
      <template #header>
        <div class="card-header">
          <i-ep-user class="header-icon" />
          <span>AI饮食推荐</span>
        </div>
      </template>

      <el-form :model="userInfo" label-width="100px" class="user-info-form">
        <!-- 基础信息分组 -->
        <div class="form-section">
          <div class="section-title">
            <i-ep-info-filled class="section-icon" />
            <span>基础信息</span>
          </div>

          <el-row :gutter="30">
            <el-col :span="12">
              <el-form-item label="年龄">
                <el-input-number
                    v-model="userInfo.age"
                    :min="1"
                    :max="120"
                    style="width: 100%"
                    placeholder="请输入年龄"
                />
              </el-form-item>
            </el-col>

            <el-col :span="12">
              <el-form-item label="性别">
                <el-select
                    v-model="userInfo.gender"
                    placeholder="请选择性别"
                    style="width: 100%"
                >
                  <el-option label="男" value="男" />
                  <el-option label="女" value="女" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="30">
            <el-col :span="12">
              <el-form-item label="身高(cm)">
                <el-input-number
                    v-model="userInfo.height"
                    :min="100"
                    :max="250"
                    style="width: 100%"
                    placeholder="请输入身高"
                />
              </el-form-item>
            </el-col>

            <el-col :span="12">
              <el-form-item label="体重(kg)">
                <el-input-number
                    v-model="userInfo.weight"
                    :min="30"
                    :max="200"
                    style="width: 100%"
                    placeholder="请输入体重"
                />
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <!-- 目标偏好分组 -->
        <div class="form-section">
          <div class="section-title">
            <i-ep-target class="section-icon" />
            <span>目标偏好</span>
          </div>

          <el-row :gutter="30">
            <el-col :span="12">
              <el-form-item label="目标">
                <el-select
                    v-model="userInfo.goal"
                    placeholder="请选择目标"
                    style="width: 100%"
                >
                  <el-option label="减重" value="减重" />
                  <el-option label="增重" value="增重" />
                  <el-option label="维持体重" value="维持体重" />
                  <el-option label="增肌" value="增肌" />
                </el-select>
              </el-form-item>
            </el-col>

            <el-col :span="12">
              <el-form-item label="饮食偏好">
                <el-select
                    v-model="userInfo.dietaryPreference"
                    placeholder="请选择饮食偏好"
                    style="width: 100%"
                >
                  <el-option label="素食" value="素食" />
                  <el-option label="荤素搭配" value="荤素搭配" />
                  <el-option label="低碳水" value="低碳水" />
                  <el-option label="高蛋白" value="高蛋白" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <!-- 按钮区域 -->
        <div class="button-group">
          <el-form-item>
            <el-button
                type="primary"
                @click="generateRecommendation"
                :loading="loading"
                class="action-button primary-button"
            >
              <i-ep-magic-stick v-if="!loading" /> {{ loading ? '生成中...' : '获取AI推荐' }}
            </el-button>
            <el-button
                @click="resetForm"
                class="action-button secondary-button"
            >
              <i-ep-refresh /> 重置
            </el-button>
            <el-button
                @click="$router.push('/ai-chat')"
                class="action-button secondary-button"
            >
              <el-icon><ChatDotRound /></el-icon> 与AI小李聊天
            </el-button>
          </el-form-item>
          <p class="hint-text">点击获取您的专属饮食计划</p>
        </div>
      </el-form>
    </el-card>

    <!-- 推荐结果卡片 -->
    <el-card v-if="recommendation" class="result-card">
      <template #header>
        <div class="card-header">
          <i-ep-document class="header-icon" />
          <span>AI推荐结果</span>
        </div>
      </template>

      <transition name="slide-fade">
        <div class="recommendation-content" v-html="formatRecommendation(recommendation)"></div>
      </transition>
    </el-card>

    <!-- 错误提示 -->
    <transition name="fade">
      <el-alert
          v-if="errorMessage"
          :title="errorMessage"
          type="error"
          show-icon
          closable
          class="error-alert"
          @close="errorMessage = ''"
      />
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import axios from '@/api/index'
import { ElMessage } from 'element-plus'
import { MagicStick, Refresh, ChatDotRound } from '@element-plus/icons-vue'

interface UserInfo {
  age?: number
  gender?: string
  height?: number
  weight?: number
  goal?: string
  dietaryPreference?: string
}

const userInfo = ref<UserInfo>({
  age: undefined,
  gender: '',
  height: undefined,
  weight: undefined,
  goal: '',
  dietaryPreference: ''
})

const loading = ref(false)
const recommendation = ref('')
const errorMessage = ref('')

const generateRecommendation = async () => {
  // 添加函数开始执行的调试信息
  console.log('generateRecommendation函数开始执行')
  
  if (!validateForm()) {
    console.log('表单验证失败，函数返回')
    return
  }
  
  console.log('表单验证通过，继续执行')
  
  loading.value = true
  errorMessage.value = ''
  recommendation.value = ''
  
  try {
    console.log('准备发送请求，用户数据:', userInfo.value)
    
    // 临时增加超时时间到30秒
    const response = await axios.post('/ai/recommend', userInfo.value, { timeout: 30000 })
    
    // 添加调试信息
    console.log('成功收到API响应:', response)
    console.log('响应数据:', response.data)
    
    if (response && response.data) {
      console.log('响应包含数据，success字段:', response.data.success)
      
      if (response.data.success === true) {
        // 修改这一行，将data改为recommendation
        recommendation.value = response.data.recommendation || ''
        console.log('设置recommendation.value:', recommendation.value)
        ElMessage.success(response.data.message || '推荐生成成功')
      } else {
        const errorMsg = response.data.message || response.data.data || '推荐生成失败'
        errorMessage.value = errorMsg
        ElMessage.error(errorMsg)
      }
    } else {
      const errorMsg = '服务器响应格式错误: 未收到有效数据'
      errorMessage.value = errorMsg
      ElMessage.error(errorMsg)
    }
  } catch (error: any) {
    console.error('API调用失败，完整错误信息:', error)
    console.error('错误类型:', typeof error)
    console.error('错误构造函数:', error.constructor.name)
    
    let errorMsg = '未知错误'
    if (error.response) {
      console.error('错误响应:', error.response)
      console.error('错误响应状态:', error.response.status)
      console.error('错误响应数据:', error.response.data)
      
      if (error.response.data) {
        errorMsg = error.response.data.message ||
            error.response.data.data ||
            error.response.data.error ||
            error.response.statusText ||
            `服务器错误: ${error.response.status}`
      } else {
        errorMsg = `服务器错误: ${error.response.status}`
      }
    } else if (error.request) {
      console.error('请求发送但未收到响应:', error.request)
      errorMsg = '网络连接失败，请检查网络或稍后重试'
    } else {
      console.error('请求配置错误:', error.message)
      errorMsg = error.message || '请求配置错误'
    }
    
    if (errorMsg === 'No message available' || !errorMsg) {
      errorMsg = '服务器未返回有效信息，请稍后重试'
    }
    
    errorMessage.value = errorMsg
    ElMessage.error(errorMsg)
  } finally {
    loading.value = false
    console.log('函数执行完成')
  }
}

const validateForm = (): boolean => {
  if (!userInfo.value.age) {
    ElMessage.warning('请输入年龄')
    return false
  }
  if (!userInfo.value.gender) {
    ElMessage.warning('请选择性别')
    return false
  }
  if (!userInfo.value.height) {
    ElMessage.warning('请输入身高')
    return false
  }
  if (!userInfo.value.weight) {
    ElMessage.warning('请输入体重')
    return false
  }
  if (!userInfo.value.goal) {
    ElMessage.warning('请选择目标')
    return false
  }
  if (!userInfo.value.dietaryPreference) {
    ElMessage.warning('请选择饮食偏好')
    return false
  }
  return true
}

const resetForm = () => {
  userInfo.value = {
    age: undefined,
    gender: '',
    height: undefined,
    weight: undefined,
    goal: '',
    dietaryPreference: ''
  }
  recommendation.value = ''
  errorMessage.value = ''
}

const formatRecommendation = (text: string): string => {
  return text.replace(/\n/g, '<br>')
}
</script>

<style scoped>
.ai-recommend-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 30px;
  background: linear-gradient(135deg, #f0fff0, #e6f7e6);
  min-height: 100vh;
}

.recommend-card {
  margin-bottom: 25px;
  border-radius: 16px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  border: none;
  background: linear-gradient(135deg, #ffffff, #f9fff9);
}

.card-header {
  font-weight: bold;
  font-size: 22px;
  color: #2d5a27;
  padding: 20px 25px;
  background: linear-gradient(135deg, #4caf50, #2e7d32);
  border-radius: 16px 16px 0 0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-icon {
  font-size: 24px;
}

.form-section {
  margin-bottom: 30px;
  padding: 20px;
  background: #fafafa;
  border-radius: 12px;
  box-shadow: inset 0 2px 4px rgba(0,0,0,0.05);
}

.section-title {
  font-weight: 600;
  font-size: 18px;
  color: #2e7d32;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-icon {
  color: #4caf50;
  font-size: 20px;
}

.user-info-form {
  max-width: 800px;
  margin: 0 auto;
  padding: 30px;
}

.button-group {
  text-align: center;
  margin-top: 20px;
}

.action-button {
  height: 48px;
  font-size: 16px;
  border-radius: 24px;
  padding: 0 25px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin: 0 10px;
  transition: all 0.3s ease;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.primary-button {
  background: linear-gradient(135deg, #4caf50, #2e7d32);
  border: none;
  color: white;
  font-weight: 600;
}

.primary-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 12px rgba(76, 175, 80, 0.3);
  background: linear-gradient(135deg, #43a047, #1b5e20);
}

.secondary-button {
  background: #f5f5f5;
  border: 1px solid #ddd;
  color: #555;
}

.secondary-button:hover {
  background: #e0e0e0;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.hint-text {
  color: #777;
  font-size: 14px;
  margin-top: 15px;
}

.result-card {
  margin-bottom: 25px;
  border-radius: 16px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  border: none;
  background: linear-gradient(135deg, #ffffff, #f9fff9);
}

.recommendation-content {
  line-height: 1.8;
  padding: 30px;
  background: #f8fdf8;
  border-radius: 12px;
  border-left: 5px solid #4caf50;
  font-size: 16px;
  color: #333;
  white-space: pre-line;
}

.error-alert {
  margin-bottom: 25px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

/* 动画效果 */
.slide-fade-enter-active {
  transition: all 0.3s ease-out;
}

.slide-fade-enter-from {
  transform: translateX(20px);
  opacity: 0;
}

.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s;
}

.fade-enter-from, .fade-leave-to {
  opacity: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .ai-recommend-page {
    padding: 15px;
  }

  .user-info-form {
    padding: 15px;
  }

  .el-row {
    flex-direction: column;
    gap: 15px;
  }

  .el-col {
    width: 100%;
    margin-bottom: 15px;
  }

  .action-button {
    width: 100%;
    margin: 10px 0;
  }

  .button-group {
    margin-top: 10px;
  }
}
</style>

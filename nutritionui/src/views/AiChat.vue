<template>
  <div class="ai-chat-page">
    <el-card class="chat-card">
      <template #header>
        <div class="card-header">
          <el-icon class="header-icon"><ChatDotRound /></el-icon>
          <span>AI小李 - 饮食顾问</span>
          <el-button 
            type="text" 
            @click="clearChatHistory" 
            class="clear-button"
            :disabled="loading"
          >
            <el-icon><Delete /></el-icon>
            清除历史
          </el-button>
        </div>
      </template>

      <!-- 聊天消息区域 -->
      <div class="chat-messages" ref="messagesContainer">
        <!-- 欢迎消息 -->
        <div v-if="messages.length === 0" class="welcome-message">
          <el-icon class="welcome-icon"><Sunrise /></el-icon>
          <p>你好！我是AI小李，你的专属饮食顾问。</p>
          <p>有什么关于饮食、营养的问题都可以问我哦！</p>
        </div>

        <!-- 聊天消息列表 -->
        <div 
          v-for="(message, index) in messages" 
          :key="index"
          :class="['message-item', message.role]"
        >
          <div class="message-content">
            <div class="message-header">
              <span class="message-role">{{ message.role === 'user' ? '我' : 'AI小李' }}</span>
              <span class="message-time">{{ formatTime(message.timestamp) }}</span>
            </div>
            <div class="message-text" v-html="formatMessage(message.content)"></div>
          </div>
        </div>

        <!-- 加载中提示 -->
        <div v-if="loading" class="loading-message">
          <el-icon class="loading-icon"><Loading /></el-icon>
          <span>AI思考中...</span>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="chat-input-container">
        <el-input
          v-model="inputMessage"
          placeholder="请输入您的问题..."
          type="textarea"
          :rows="3"
          :autosize="{ minRows: 3, maxRows: 6 }"
          resize="none"
          @keyup.enter.exact="sendMessage"
        >
          <template #append>
            <el-button 
              type="primary" 
              @click="sendMessage" 
              :disabled="!inputMessage.trim() || loading"
              class="send-button"
            >
              <!-- 直接使用图标组件，不需要导入 -->
              <el-icon><ChatDotRound /></el-icon> 发送
            </el-button>
          </template>
        </el-input>
      </div>
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
import { ref, onMounted, nextTick } from 'vue'
import { aiRecommendApi } from '@/api/index'
// 添加ElMessage导入
import { ElMessage } from 'element-plus'

interface ChatMessage {
  role: 'user' | 'assistant'
  content: string
  timestamp: Date
}

const messages = ref<ChatMessage[]>([])
const inputMessage = ref<string>('')
const loading = ref<boolean>(false)
const errorMessage = ref<string>('')
const messagesContainer = ref<HTMLElement | null>(null)

// 自动滚动到底部
const scrollToBottom = async (): Promise<void> => {
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

// 格式化消息
const formatMessage = (content: string): string => {
  // 将换行符转换为<br>
  return content.replace(/\n/g, '<br>')
}

// 格式化时间
const formatTime = (date: Date): string => {
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')
  return `${hours}:${minutes}`
}

// 发送消息
const sendMessage = async (): Promise<void> => {
  if (!inputMessage.value.trim() || loading.value) return

  const userMessage = inputMessage.value.trim()
  inputMessage.value = ''

  // 添加用户消息到聊天列表
  messages.value.push({
    role: 'user',
    content: userMessage,
    timestamp: new Date()
  })

  await scrollToBottom()
  loading.value = true
  errorMessage.value = ''

  try {
    // 调用AI聊天接口
    const response = await aiRecommendApi.chat(userMessage)
    
    if (response && response.data && response.data.success) {
      // 添加AI回复到聊天列表
      messages.value.push({
        role: 'assistant',
        content: response.data.response || '',
        timestamp: new Date()
      })
      await scrollToBottom()
    } else {
      throw new Error(response.data?.error || 'AI回复失败')
    }
  } catch (error: any) {
    console.error('聊天失败:', error)
    let errorMsg = '聊天失败，请稍后重试'
    if (error.response && error.response.data) {
      errorMsg = error.response.data.error || errorMsg
    }
    errorMessage.value = errorMsg
    ElMessage.error(errorMsg)
  } finally {
    loading.value = false
  }
}

// 清除聊天历史
const clearChatHistory = async (): Promise<void> => {
  try {
    await aiRecommendApi.clearChat()
    messages.value = []
    ElMessage.success('聊天历史已清除')
  } catch (error: any) {
    console.error('清除聊天历史失败:', error)
    let errorMsg = '清除聊天历史失败，请稍后重试'
    if (error.response && error.response.data) {
      errorMsg = error.response.data.error || errorMsg
    }
    ElMessage.error(errorMsg)
  }
}

onMounted(() => {
  // 可以在这里加载历史记录（如果需要的话）
  // loadChatHistory()
})
</script>

<style scoped>
.ai-chat-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 30px;
  background: linear-gradient(135deg, #f0fff0, #e6f7e6);
  min-height: 100vh;
}

.chat-card {
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
  justify-content: space-between;
  gap: 10px;
}

.header-icon {
  font-size: 24px;
}

.clear-button {
  color: white;
  font-size: 14px;
}

.chat-messages {
  height: 500px;
  overflow-y: auto;
  padding: 20px;
  background: #f8fdf8;
  border-radius: 0;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.welcome-message {
  text-align: center;
  color: #777;
  padding: 40px 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.welcome-icon {
  font-size: 48px;
  color: #4caf50;
  margin-bottom: 15px;
}

.message-item {
  display: flex;
  max-width: 85%;
}

.message-item.user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.message-item.assistant {
  align-self: flex-start;
  flex-direction: row;
}

.message-content {
  background: white;
  padding: 15px 20px;
  border-radius: 18px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.message-item.user .message-content {
  background: linear-gradient(135deg, #4caf50, #2e7d32);
  color: white;
  border-bottom-right-radius: 4px;
}

.message-item.assistant .message-content {
  border-bottom-left-radius: 4px;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 14px;
  opacity: 0.8;
}

.message-text {
  line-height: 1.6;
  font-size: 15px;
  word-wrap: break-word;
}

.loading-message {
  align-self: center;
  color: #4caf50;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px;
}

.loading-icon {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.chat-input-container {
  padding: 20px;
  background: white;
  border-radius: 0 0 16px 16px;
}

.send-button {
  height: 40px;
  border-radius: 20px;
  background: linear-gradient(135deg, #4caf50, #2e7d32);
  border: none;
  color: white;
  font-weight: 500;
}

.send-button:hover {
  background: linear-gradient(135deg, #43a047, #1b5e20);
}

.error-alert {
  margin-bottom: 25px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

/* 滚动条样式 */
.chat-messages::-webkit-scrollbar {
  width: 8px;
}

.chat-messages::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

.chat-messages::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

.chat-messages::-webkit-scrollbar-thumb:hover {
  background: #a1a1a1;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .ai-chat-page {
    padding: 15px;
  }

  .chat-messages {
    height: 400px;
    padding: 15px;
  }

  .message-item {
    max-width: 95%;
  }

  .card-header {
    font-size: 18px;
    padding: 15px 20px;
  }
}
</style>
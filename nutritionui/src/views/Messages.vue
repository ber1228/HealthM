<template>
  <div class="messages-container">
    <el-card>
      <template #header>
        <div class="messages-header">
          <h2>消息提醒</h2>
          <div class="header-actions">
            <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="badge">
              <el-button type="primary" @click="loadMessages">刷新</el-button>
            </el-badge>
            <el-button type="success" @click="markAllAsRead" :disabled="unreadCount === 0">
              全部已读
            </el-button>
          </div>
        </div>
      </template>
      
      <!-- 消息筛选 -->
      <div class="message-filters">
        <el-radio-group v-model="filterType" @change="loadMessages">
          <el-radio-button label="all">全部</el-radio-button>
          <el-radio-button label="unread">未读</el-radio-button>
          <el-radio-button label="reminder">饮食提醒</el-radio-button>
          <el-radio-button label="nutrition">营养提醒</el-radio-button>
          <el-radio-button label="target">目标提醒</el-radio-button>
        </el-radio-group>
      </div>
      
      <!-- 消息列表 -->
      <div class="messages-list">
        <div v-if="filteredMessages.length === 0" class="empty-state">
          <el-empty description="暂无消息" />
        </div>
        
        <div v-else>
          <div v-for="message in filteredMessages" :key="message.id" class="message-item" :class="{ unread: !message.isRead }">
            <div class="message-content">
              <div class="message-header">
                <h3 class="message-title">{{ message.title }}</h3>
                <div class="message-meta">
                  <el-tag :type="getMessageTypeTag(message.messageType)" size="small">
                    {{ getMessageTypeLabel(message.messageType) }}
                  </el-tag>
                  <span class="message-time">{{ formatTime(message.createdAt) }}</span>
                </div>
              </div>
              <div class="message-body">
                <p>{{ message.content }}</p>
              </div>
            </div>
            <div class="message-actions">
              <el-button
                v-if="!message.isRead"
                type="primary"
                size="small"
                @click="markAsRead(message.id)"
              >
                标记已读
              </el-button>
              <el-button
                type="danger"
                size="small"
                @click="deleteMessage(message.id)"
              >
                删除
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </el-card>
    
    <!-- 发送提醒对话框 -->
    <el-dialog v-model="showSendDialog" title="发送提醒" width="500px">
      <el-form :model="sendForm" label-width="100px">
        <el-form-item label="提醒类型">
          <el-select v-model="sendForm.type" placeholder="请选择提醒类型">
            <el-option label="饮食提醒" value="reminder" />
            <el-option label="营养提醒" value="nutrition" />
            <el-option label="目标提醒" value="target" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="sendForm.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input
            v-model="sendForm.content"
            type="textarea"
            :rows="3"
            placeholder="请输入提醒内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showSendDialog = false">取消</el-button>
        <el-button type="primary" @click="sendMessage">发送</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import api from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'

const messages = ref<any[]>([])
const filterType = ref('all')
const unreadCount = ref(0)
const showSendDialog = ref(false)

const sendForm = ref({
  type: 'reminder',
  title: '',
  content: ''
})

const filteredMessages = computed(() => {
  if (filterType.value === 'all') {
    return messages.value
  } else if (filterType.value === 'unread') {
    return messages.value.filter(msg => !msg.isRead)
  } else {
    return messages.value.filter(msg => msg.messageType === filterType.value)
  }
})

const loadMessages = async () => {
  try {
    const response = await api.get('/messages')
    messages.value = response.data
    await loadUnreadCount()
  } catch (error) {
    console.error('加载消息失败:', error)
    messages.value = []
  }
}

const loadUnreadCount = async () => {
  try {
    const response = await api.get('/messages/count')
    unreadCount.value = response.data.count
  } catch (error) {
    console.error('加载未读数量失败:', error)
  }
}

const markAsRead = async (id: number) => {
  try {
    await api.put(`/messages/${id}/read`)
    ElMessage.success('已标记为已读')
    await loadMessages()
  } catch (error: any) {
    ElMessage.error(error.response?.data || '操作失败')
  }
}

const markAllAsRead = async () => {
  try {
    await ElMessageBox.confirm('确定要将所有消息标记为已读吗？', '确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await api.put('/messages/read-all')
    ElMessage.success('已全部标记为已读')
    await loadMessages()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data || '操作失败')
    }
  }
}

const deleteMessage = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这条消息吗？', '确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await api.delete(`/messages/${id}`)
    ElMessage.success('删除成功')
    await loadMessages()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data || '删除失败')
    }
  }
}

const sendMessage = async () => {
  try {
    const endpoint = getSendEndpoint(sendForm.value.type)
    await api.post(endpoint, {
      message: sendForm.value.content
    })
    
    ElMessage.success('提醒发送成功')
    showSendDialog.value = false
    sendForm.value = {
      type: 'reminder',
      title: '',
      content: ''
    }
    await loadMessages()
  } catch (error: any) {
    ElMessage.error(error.response?.data || '发送失败')
  }
}

const getSendEndpoint = (type: string) => {
  switch (type) {
    case 'reminder':
      return '/messages/meal-reminder'
    case 'nutrition':
      return '/messages/nutrition-reminder'
    case 'target':
      return '/messages/target-reminder'
    default:
      return '/messages/meal-reminder'
  }
}

const getMessageTypeLabel = (type: string) => {
  const labels: { [key: string]: string } = {
    reminder: '饮食提醒',
    nutrition: '营养提醒',
    target: '目标提醒'
  }
  return labels[type] || type
}

const getMessageTypeTag = (type: string) => {
  const tags: { [key: string]: string } = {
    reminder: 'primary',
    nutrition: 'warning',
    target: 'success'
  }
  return tags[type] || 'info'
}

const formatTime = (time: string) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

onMounted(() => {
  loadMessages()
})
</script>

<style scoped>
.messages-container {
  padding: 20px;
}

h2 {
  margin: 0;
  color: #2d8659;
  font-weight: 600;
}

.messages-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.badge {
  margin-right: 10px;
}

.message-filters {
  margin-bottom: 20px;
  padding: 15px;
  background: linear-gradient(135deg, #f0f9f6 0%, #e8f5e8 100%);
  border-radius: 8px;
}

.messages-list {
  max-height: 600px;
  overflow-y: auto;
}

.message-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 20px;
  margin-bottom: 15px;
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e0e0e0;
  transition: all 0.3s ease;
  position: relative;
}

.message-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.message-item.unread {
  border-left: 4px solid #4facfe;
  background: linear-gradient(135deg, #f8fcff 0%, #f0f9ff 100%);
}

.message-item.unread::before {
  content: '';
  position: absolute;
  top: 20px;
  right: 20px;
  width: 8px;
  height: 8px;
  background-color: #4facfe;
  border-radius: 50%;
}

.message-content {
  flex: 1;
  margin-right: 15px;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 10px;
}

.message-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #2d8659;
  line-height: 1.4;
}

.message-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

.message-time {
  font-size: 12px;
  color: #909399;
}

.message-body {
  margin-bottom: 10px;
}

.message-body p {
  margin: 0;
  color: #606266;
  line-height: 1.6;
  font-size: 14px;
}

.message-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex-shrink: 0;
}

.empty-state {
  text-align: center;
  padding: 40px 0;
}

:deep(.el-card) {
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
  border: 1px solid rgba(168, 230, 207, 0.3);
}

:deep(.el-card__header) {
  background: linear-gradient(135deg, #f0f9f6 0%, #e8f5e8 100%);
  border-bottom: 1px solid rgba(168, 230, 207, 0.3);
}

:deep(.el-button) {
  border-radius: 8px;
  transition: all 0.3s ease;
}

:deep(.el-button--primary) {
  background-color: #4facfe;
  border-color: #4facfe;
}

:deep(.el-button--primary:hover) {
  background-color: #39a1ff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(79, 172, 254, 0.4);
}

:deep(.el-button--success) {
  background-color: #67c23a;
  border-color: #67c23a;
}

:deep(.el-button--success:hover) {
  background-color: #5daf34;
  transform: translateY(-2px);
}

:deep(.el-button--danger) {
  background-color: #f56c6c;
  border-color: #f56c6c;
}

:deep(.el-button--danger:hover) {
  background-color: #f78989;
  transform: translateY(-2px);
}

:deep(.el-button:disabled) {
  opacity: 0.5;
  cursor: not-allowed;
}

:deep(.el-button:disabled:hover) {
  transform: none;
  box-shadow: none;
}

:deep(.el-radio-button__inner) {
  border-radius: 8px;
  border-color: #d9d9d9;
  color: #5a7c65;
  font-weight: 500;
}

:deep(.el-radio-button__orig-radio:checked + .el-radio-button__inner) {
  background-color: #4facfe;
  border-color: #4facfe;
  color: white;
}

:deep(.el-tag) {
  border-radius: 6px;
  font-weight: 500;
}

:deep(.el-tag--primary) {
  background-color: #e6f7ff;
  border-color: #4facfe;
  color: #4facfe;
}

:deep(.el-tag--warning) {
  background-color: #fdf6ec;
  border-color: #e6a23c;
  color: #e6a23c;
}

:deep(.el-tag--success) {
  background-color: #f0f9ff;
  border-color: #67c23a;
  color: #67c23a;
}

:deep(.el-badge__content) {
  background-color: #f56c6c;
  border-color: #f56c6c;
}

:deep(.el-dialog) {
  border-radius: 12px;
}

:deep(.el-form-item__label) {
  color: #5a7c65;
  font-weight: 500;
}

:deep(.el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

:deep(.el-textarea__inner) {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

:deep(.el-select) {
  width: 100%;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .messages-header {
    flex-direction: column;
    align-items: stretch;
    gap: 15px;
  }
  
  .header-actions {
    justify-content: center;
  }
  
  .message-item {
    flex-direction: column;
    align-items: stretch;
  }
  
  .message-content {
    margin-right: 0;
    margin-bottom: 15px;
  }
  
  .message-header {
    flex-direction: column;
    align-items: stretch;
    gap: 10px;
  }
  
  .message-meta {
    justify-content: space-between;
  }
  
  .message-actions {
    flex-direction: row;
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .messages-container {
    padding: 10px;
  }
  
  .message-item {
    padding: 15px;
  }
  
  .message-title {
    font-size: 14px;
  }
  
  .message-body p {
    font-size: 13px;
  }
}
</style>


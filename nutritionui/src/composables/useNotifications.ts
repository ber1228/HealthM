import { ref } from 'vue'
import api from '@/api'
import { ElNotification } from 'element-plus'

// 存储未读消息
const unreadMessages = ref<any[]>([])
let lastCheckTime = Date.now()

// 轮询检查新消息
export function useNotifications() {
  const checkNewMessages = async () => {
    try {
      const response = await api.get('/messages/unread')
      const messages = response.data || []
      
      console.log('检查新消息 - 当前时间:', new Date().toLocaleString())
      console.log('检查新消息 - 未读消息数:', messages.length)
      console.log('检查新消息 - lastCheckTime:', new Date(lastCheckTime).toLocaleString())
      
      // 只显示新消息（比上次检查时间更晚的消息）
      const newMessages = messages.filter((msg: any) => {
        const msgTime = new Date(msg.createdAt).getTime()
        const isNew = msgTime > lastCheckTime
        if (isNew) {
          console.log('发现新消息:', msg.title, '创建时间:', msg.createdAt)
        }
        return isNew
      })
      
      if (newMessages.length > 0) {
        console.log('发现', newMessages.length, '条新消息')
        // 显示所有新消息（按创建时间排序）
        const sortedMessages = newMessages.sort((a: any, b: any) => 
          new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime()
        )
        
        // 显示最新的一条消息
        const latestMessage = sortedMessages[sortedMessages.length - 1]
        console.log('显示最新消息:', latestMessage.title)
        showNotification(latestMessage)
        
        // 更新未读消息列表
        unreadMessages.value = messages
        lastCheckTime = Date.now()
        console.log('更新lastCheckTime为:', new Date(lastCheckTime).toLocaleString())
      }
    } catch (error) {
      console.error('检查新消息失败:', error)
    }
  }
  
  const showNotification = (message: any) => {
    const title = message.title || '系统提醒'
    const type = getMessageType(message.messageType)
    
    ElNotification({
      title: title,
      message: message.content || '',
      type: type,
      duration: 5000,
      position: 'top-right',
      onClick: () => {
        // 点击通知可以跳转到消息页面
        window.location.href = '/messages'
      }
    })
  }
  
  const getMessageType = (msgType: string) => {
    const types: Record<string, any> = {
      'reminder': 'info',
      'nutrition': 'warning',
      'target': 'success'
    }
    return types[msgType] || 'info'
  }
  
  // 开始轮询（每10秒检查一次，方便测试）
  const startPolling = () => {
    console.log('开始轮询新消息，每10秒检查一次')
    // 立即检查一次
    checkNewMessages()
    // 然后每10秒检查一次
    setInterval(checkNewMessages, 10000)
  }
  
  return {
    checkNewMessages,
    startPolling,
    unreadMessages
  }
}


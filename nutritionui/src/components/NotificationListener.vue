<template>
  <div></div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useNotifications } from '@/composables/useNotifications'

const { startPolling } = useNotifications()

onMounted(() => {
  console.log('NotificationListener 组件已挂载')
  // 检查是否已登录
  const token = localStorage.getItem('token')
  console.log('当前登录状态:', token ? '已登录' : '未登录')
  if (token) {
    console.log('开始启动消息轮询...')
    startPolling()
    console.log('消息轮询已启动')
  } else {
    console.log('用户未登录，不启动消息轮询')
  }
})
</script>


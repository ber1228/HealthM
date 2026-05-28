<template>
  <NotificationListener />
  <router-view />

  <!-- 固定在右下角的AI聊天机器人图标 -->
  <div class="ai-chat-button" @click="$router.push('/ai-chat')">
    <el-icon class="robot-icon">
      <ChatDotRound />
    </el-icon>
    <span class="chat-text">AI聊天</span>
  </div>
</template>

<script setup lang="ts">
import NotificationListener from './components/NotificationListener.vue'
import { ChatDotRound } from '@element-plus/icons-vue'
</script>

<style scoped>
/* AI聊天机器人图标样式 */
.ai-chat-button {
  position: fixed;
  bottom: 30px;
  right: 30px;
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #4caf50, #2e7d32);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
  z-index: 9999;
}

.ai-chat-button:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.2);
}

.robot-icon {
  font-size: 32px;
  color: white;
}

.chat-text {
  position: absolute;
  right: 70px;
  background: rgba(0, 0, 0, 0.7);
  color: white;
  padding: 8px 12px;
  border-radius: 4px;
  font-size: 14px;
  opacity: 0;
  transition: opacity 0.3s ease;
  white-space: nowrap;
}

.ai-chat-button:hover .chat-text {
  opacity: 1;
}
</style>


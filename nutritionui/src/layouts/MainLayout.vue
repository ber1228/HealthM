<template>
  <el-container class="main-layout">
    <!-- 顶部导航栏 -->
    <el-header class="header">
      <div class="header-content">
        <!-- 左侧Logo -->
        <div class="logo">
          <h2>健康饮食推荐系统</h2>
        </div>
        
        <!-- 中间导航菜单 -->
        <el-menu
          :default-active="activeMenu"
          class="top-menu"
          mode="horizontal"
          router
        >

          <el-menu-item index="/dashboard">
            <el-icon><House /></el-icon>
            <span>首页</span>
          </el-menu-item>
          <el-menu-item index="/airecommend">
            <el-icon><MagicStick /></el-icon>
            <span>AI推荐</span>
          </el-menu-item>
          <!-- 新增：AI聊天菜单项 -->
          <el-menu-item index="/ai-chat">
            <el-icon><ChatDotRound /></el-icon>
            <span>AI聊天</span>
          </el-menu-item>
          <el-menu-item index="/plan">
            <el-icon><Document /></el-icon>
            <span>饮食方案</span>
          </el-menu-item>
          <el-menu-item index="/record">
            <el-icon><Edit /></el-icon>
            <span>饮食记录</span>
          </el-menu-item>
          <el-menu-item index="/analysis">
            <el-icon><DataAnalysis /></el-icon>
            <span>营养分析</span>
          </el-menu-item>
          <el-menu-item index="/recipes">
            <el-icon><Document /></el-icon>
            <span>菜谱</span>
          </el-menu-item>
          <el-menu-item index="/statistics">
            <el-icon><PieChart /></el-icon>
            <span>数据统计</span>
          </el-menu-item>
          <el-menu-item index="/messages">
            <el-icon><Message /></el-icon>
            <span>消息提醒</span>
          </el-menu-item>
          <el-menu-item index="/reminders">
            <el-icon><AlarmClock /></el-icon>
            <span>提醒设置</span>
          </el-menu-item>
          <template v-if="userStore.isAdmin">
            <el-sub-menu index="/admin">
              <template #title>
                <el-icon><Setting /></el-icon>
                <span>管理中心</span>
              </template>
              <el-menu-item index="/admin/foods">
                <el-icon><Document /></el-icon>
                <span>食物管理</span>
              </el-menu-item>
              <el-menu-item index="/admin/recipes">
                <el-icon><Document /></el-icon>
                <span>菜谱管理</span>
              </el-menu-item>
              <el-menu-item index="/admin/users">
                <el-icon><UserFilled /></el-icon>
                <span>用户管理</span>
              </el-menu-item>
              <el-menu-item index="/admin/announcements">
                <el-icon><Bell /></el-icon>
                <span>公告管理</span>
              </el-menu-item>
            </el-sub-menu>
          </template>
          <el-menu-item index="/profile">
            <el-icon><User /></el-icon>
            <span>个人中心</span>
          </el-menu-item>
        </el-menu>
        
        <!-- 右侧用户信息 -->
        <div class="header-right">
          <span class="welcome-text">欢迎, {{ userStore.username }}</span>
          <el-button @click="logout">退出登录</el-button>
        </div>
      </div>
    </el-header>
    
    <!-- 主内容区域 -->
    <el-main class="main-content">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

// 导入所需的图标组件 - 添加缺失的ChatDotRound和MagicStick图标
import { House, MagicStick, ChatDotRound, Document, Edit, DataAnalysis, PieChart, Message, AlarmClock, UserFilled, Bell, User, Setting } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => {
  // 处理子菜单项的激活状态
  const path = route.path
  return path.startsWith('/admin') ? '/admin' : path
})

const logout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.main-layout {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  background-color: #ffffff;
  border-bottom: 1px solid #e4e7ed;
  padding: 0;
  height: auto;
  min-height: 60px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  z-index: 100;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
  padding: 0 20px;
  max-width: 100%;
  overflow: hidden;
}

.logo {
  display: flex;
  align-items: center;
  margin-right: 20px;
  min-width: 120px;
}

.logo h2 {
  color: #303133;
  font-size: 18px;
  font-weight: 600;
  margin: 0;
}

.top-menu {
  flex: 1;
  display: flex;
  justify-content: center;
  border: none;
  background-color: transparent;
  height: 100%;
  min-width: 0;
}

.top-menu .el-menu-item,
.top-menu .el-sub-menu {
  height: 60px;
  line-height: 60px;
  padding: 0 18px;
  min-width: 60px;
}

.top-menu .el-sub-menu .el-sub-menu__title {
  height: 60px;
  line-height: 60px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
  min-width: 200px;
  justify-content: flex-end;
}

.welcome-text {
  font-size: 14px;
  color: #606266;
}

.main-content {
  background-color: #f5f5f5;
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .top-menu .el-menu-item,
  .top-menu .el-sub-menu {
    padding: 0 12px;
  }
  
  .welcome-text {
    display: none;
  }
}

@media (max-width: 992px) {
  .header-content {
    flex-wrap: wrap;
    padding: 0 10px;
  }
  
  .top-menu {
    order: 3;
    width: 100%;
    overflow-x: auto;
    white-space: nowrap;
    justify-content: flex-start;
  }
  
  .logo {
    margin-right: 10px;
  }
}

@media (max-width: 768px) {
  .top-menu .el-menu-item span,
  .top-menu .el-sub-menu__title span {
    display: none;
  }
  
  .top-menu .el-menu-item,
  .top-menu .el-sub-menu {
    padding: 0 8px;
    min-width: 50px;
  }
  
  .header-content {
    padding: 0 5px;
  }
  
  .main-content {
    padding: 10px;
  }
}
</style>


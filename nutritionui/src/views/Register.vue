<template>
  <div class="register-container">
    <!-- 食物粒子背景 -->
    <div class="food-particles">
      <div v-for="particle in particles" :key="particle.id" 
           class="food-particle" 
           :style="particle.style"
           :class="particle.type">
        {{ particle.emoji }}
      </div>
    </div>
    
    <el-card class="register-card">
      <h2>注册</h2>
      <el-form :model="form" label-width="100px">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item>
          <div class="button-group">
            <el-button type="primary" @click="register" :loading="loading">注册</el-button>
            <el-button @click="$router.push('/login')">返回登录</el-button>
          </div>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import api from '@/api'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const form = ref({
  username: '',
  password: '',
  phone: '',
  email: ''
})

const loading = ref(false)

// 静态食物粒子系统
const particles = ref<any[]>([])

// 食物表情符号
const foodEmojis = [
  '🍎', '🍊', '🍌', '🍇', '🍓', '🍑', '🥝', '🍅', '🥕', '🥒',
  '🥬', '🥦', '🍄', '🥔', '🍠', '🌽', '🥜', '🍞', '🥖', '🥨',
  '🧀', '🥚', '🍳', '🥓', '🥩', '🍗', '🍖', '🦴', '🍤', '🦐',
  '🐟', '🐠', '🐡', '🦀', '🦞', '🦑', '🍱', '🍘', '🍙', '🍚',
  '🍛', '🍜', '🍝', '🍠', '🍢', '🍣', '🍤', '🍥', '🥮', '🍡',
  '🥟', '🥠', '🥡', '🦀', '🦞', '🦐', '🦑', '🦪', '🍦', '🍧',
  '🍨', '🍩', '🍪', '🎂', '🍰', '🧁', '🥧', '🍫', '🍬', '🍭',
  '🍮', '🍯', '🍼', '🥛', '☕', '🍵', '🍶', '🍾', '🍷', '🍸',
  '🍹', '🍺', '🍻', '🥂', '🥃', '🥤', '🧃', '🧉', '🧊'
]

// 创建静态粒子
const createStaticParticle = (index: number) => {
  const emoji = foodEmojis[index % foodEmojis.length]
  const size = Math.random() * 15 + 20 // 20-35px
  
  // 随机分布在整个屏幕上，包括下半部分
  const x = Math.random() * window.innerWidth
  const y = Math.random() * window.innerHeight
  
  return {
    id: index,
    emoji,
    size,
    style: {
      left: x + 'px',
      top: y + 'px',
      fontSize: size + 'px'
    }
  }
}

// 初始化静态粒子
const initStaticParticles = () => {
  const particleCount = 80 // 更多粒子
  for (let i = 0; i < particleCount; i++) {
    particles.value.push(createStaticParticle(i))
  }
}

const register = async () => {
  loading.value = true
  try {
    const response = await api.post('/auth/register', form.value)
    const { token, userId, username } = response.data
    userStore.login(token, userId, username)
    ElMessage.success('注册成功')
    router.push('/')
  } catch (error: any) {
    ElMessage.error(error.response?.data || '注册失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  initStaticParticles()
})
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: #ffffff;
  position: relative;
  overflow: hidden;
}


/* 食物粒子样式 */
.food-particles {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 0;
}

.food-particle {
  position: absolute;
  font-size: 20px;
  opacity: 0.6;
  pointer-events: none;
  user-select: none;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.1));
}

.register-card {
  width: 520px;
  padding: 40px;
  position: relative;
  z-index: 10;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  transition: all 0.3s ease;
}

.register-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}

.register-card h2 {
  text-align: center;
  margin-bottom: 30px;
  font-size: 28px;
  color: #2d8659;
  font-weight: 600;
  letter-spacing: 0.5px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

:deep(.el-form-item__label) {
  color: #5a7c65;
  font-weight: 500;
}

:deep(.el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-1px);
}

.button-group {
  display: flex;
  gap: 15px;
  width: 100%;
}

:deep(.el-button--primary) {
  background-color: #409eff;
  border-color: #409eff;
  flex: 1;
  padding: 12px;
  font-size: 16px;
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.3s ease;
}

:deep(.el-button--primary:hover) {
  background-color: #66b1ff;
  border-color: #66b1ff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
}

:deep(.el-button:not(.el-button--primary)) {
  background-color: #ffffff;
  border: 1px solid #dcdfe6;
  color: #606266;
  flex: 1;
  padding: 12px;
  font-size: 16px;
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.3s ease;
}

:deep(.el-button:not(.el-button--primary):hover) {
  background-color: #ecf5ff;
  border-color: #409eff;
  color: #409eff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .register-card {
    width: 90%;
    margin: 20px;
    padding: 30px;
  }
  
  .food-particle {
    font-size: 16px;
  }
}

@media (max-width: 480px) {
  .register-card {
    padding: 20px;
  }
  
  .register-card h2 {
    font-size: 24px;
  }
  
  .food-particle {
    font-size: 14px;
  }
}
</style>


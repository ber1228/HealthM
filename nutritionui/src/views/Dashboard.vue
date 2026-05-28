<template>
  <div class="dashboard-container">
    <!-- 3D背景粒子效果 -->
    <div class="bg-particles"></div>
    <div class="floating-shapes">
      <div class="shape shape-1"></div>
      <div class="shape shape-2"></div>
      <div class="shape shape-3"></div>
    </div>

    <!-- 顶部导航栏 -->
    <div class="top-nav">
      <div class="logo">
        <i class="fa fa-leaf"></i>
        <span>健康饮食助手</span>
      </div>
      <div class="user-info" v-if="userName">
      </div>
    </div>

    <!-- 英雄区分屏设计 -->
    <el-card class="hero-card glass-effect">
      <div class="hero split-screen">
        <div class="hero-left">
          <div class="hero-welcome" v-if="showWelcome">
            <h1 class="hero-title">健康饮食，<span class="accent">开启美好生活</span></h1>
            <p class="hero-desc">个性化饮食方案与科学营养搭配，助你轻松达成目标。</p>
            <div class="hero-actions">
              <el-button type="primary" @click="$router.push('/plan')" class="primary-btn">立即开始</el-button>
              <el-button @click="$router.push('/analysis')" class="secondary-btn">查看营养分析</el-button>
            </div>
          </div>
          <transition name="fade" appear>
            <div class="user-welcome" v-if="!showWelcome">
              <div class="welcome-text">
                <h1 class="hero-title">欢迎回来，<span class="accent">{{ userName || '用户' }}</span></h1>
                <p class="hero-desc">今天感觉如何？准备好开始你的健康之旅了吗？</p>
              </div>
              <div class="user-stats">
                <div class="stat-item 3d-hover" @click="$router.push('/reminders')">
                  <div class="stat-value">喝水提醒</div>
                  <div class="stat-label">记得多喝水保持身体水分</div>
                </div>
                <div class="stat-item 3d-hover" @click="$router.push('/airecommend')">
                  <div class="stat-value">AI推荐</div>
                  <div class="stat-label">获取专属饮食建议</div>
                </div>
              </div>
            </div>
          </transition>
        </div>
        <div class="hero-right">
          <el-carousel height="280px" :interval="4000" arrow="hover" indicator-position="outside" autoplay>
            <el-carousel-item v-for="(item,i) in banners" :key="i" class="banner-item">
              <div class="banner" :style="{ backgroundImage: 'url(' + item.image + ')' }" @click="goBanner(item)">
                <div class="banner-mask">
                  <div class="banner-title">{{ item.title }}</div>
                  <div class="banner-sub">{{ item.subtitle }}</div>
                </div>
                <!-- 3D悬停光效 -->
                <div class="banner-hover-light"></div>
              </div>
            </el-carousel-item>
          </el-carousel>
        </div>
      </div>
    </el-card>

    <!-- 健康数据概览 - 3D卡片效果 -->
    <transition name="slide-up" appear>
      <el-card class="health-stats-card glass-effect">
        <div class="card-header">
          <div class="card-title">健康数据概览</div>
        </div>
        <div class="health-stats">
              <div class="health-stat-item 3d-card" :class="getBmiCardClass(bmi)" @click="$router.push('/profile')">
                <div class="stat-content">
                  <div class="stat-title">BMI指数</div>
                  <div class="stat-number bmi-number">{{ bmi || '--' }}</div>
                  <div class="stat-status bmi-status">
                    <i class="fa" :class="getBmiArrowClass(bmi)"></i> {{ getBmiStatus(bmi) }}
                  </div>
                  <!-- 新增身高体重信息 -->
                  <div class="bmi-details">
                    <span>身高{{ userStore.userInfo?.height || '--' }}cm</span>
                    <span>体重{{ userStore.userInfo?.weight || '--' }}kg</span>
                  </div>
                </div>
              </div>
              <div class="health-stat-item 3d-card calorie-card" @click="$router.push('/analysis')">
                <div class="stat-content">
                  <div class="stat-title">今日卡路里</div>
                  <div class="stat-number calorie-number">{{ caloriesToday || '--' }} / {{ caloriesGoal || '--' }} kcal</div>
                  <div class="calorie-progress">
                    <el-progress :percentage="getCaloriesProgress" :stroke-width="20" :text-inside="false" height="20" show-text="false" />
                  </div>
                </div>
              </div>
              <div class="health-stat-item 3d-card goal-card" @click="$router.push('/profile')">
                <div class="stat-content">
                  <div class="stat-title">当前目标</div>
                  <div class="stat-number goal-number">{{ currentGoal || '未设置' }}</div>
                  <div class="goal-status">
        <span class="goal-indicator"></span>
        <span class="goal-status-text">进行中</span>
      </div>
                </div>
              </div>
            </div>
      </el-card>
    </transition>

    <!-- 平台动态 + 推荐菜谱 - 分屏布局 -->
    <el-row :gutter="24" style="margin-top: 24px">
      <el-col :span="10">
        <el-card class="news-card glass-effect">
          <div class="card-header">
            <div class="card-title">平台动态</div>
            <div class="card-action" @click="$router.push('/messages')">
              更多 <i class="fa fa-angle-right"></i>
            </div>
          </div>
          <el-timeline>
            <el-timeline-item
                v-for="(n,i) in latestNews"
                :key="i"
                :timestamp="n.time"
                type="primary"
                placement="top"
            >
              <div class="news-item 3d-hover" @click="goNews(n)">
                <span class="news-type">{{ n.type }}</span>
                <span class="news-title">{{ n.title }}</span>
                <div class="news-ripple"></div>
              </div>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
      <el-col :span="14">
        <el-card class="recipes-card glass-effect">
          <div class="card-header">
            <div class="card-title">推荐菜谱</div>
            <div class="card-action" @click="$router.push('/recipes')">
              更多 <i class="fa fa-angle-right"></i>
            </div>
          </div>
          <div class="recipes">
            <div class="recipe recipe-main 3d-card" v-if="recommendations[0]" @click="goRecipe(recommendations[0])">
              <div class="thumb" :style="bgImg(recommendations[0])">
                <div class="thumb-hover"></div>
                <div class="recipe-badge">推荐</div>
              </div>
              <div class="info">
                <div class="title">{{ recommendations[0].name }}</div>
                <div class="badges">
                  <el-tag type="success" effect="light">亮点</el-tag>
                  <el-tag>{{ recommendations[0].category || '其他' }}</el-tag>
                </div>
                <div class="meta">{{ recommendations[0].category || '其他' }} · 热量: {{ recommendations[0].calories || '-' }} kcal</div>
              </div>
            </div>
            <div class="recipe-list">
              <div class="recipe tile 3d-card" v-for="r in recommendations.slice(1,5)" :key="r.id" @click="goRecipe(r)">
                <div class="thumb" :style="bgImg(r)">
                  <div class="thumb-hover"></div>
                </div>
                <div class="info">
                  <div class="title">{{ r.name }}</div>
                  <div class="meta">{{ r.category || '其他' }} · {{ r.calories || '-' }} kcal</div>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷入口 - 3D图标效果 -->
    <transition name="scale" appear>
      <el-card class="quick-links-card glass-effect" style="margin-top: 24px">
        <div class="card-title">快捷入口</div>
        <div class="quick-links">
          <!-- 菜谱分类 -->
          <div class="ql 3d-icon" @click="$router.push('/recipes')">
            <div class="ql-icon"><el-icon><Menu /></el-icon></div>
            <span>菜谱分类</span>
            <div class="ql-hover-bg"></div>
          </div>
          <!-- 公告 -->
          <div class="ql 3d-icon" @click="$router.push('/announcements')">
            <div class="ql-icon"><el-icon><Bell /></el-icon></div>
            <span>公告</span>
            <div class="ql-hover-bg"></div>
          </div>
          <!-- 留言板 -->
          <div class="ql 3d-icon" @click="$router.push('/messages')">
            <div class="ql-icon"><el-icon><ChatDotRound /></el-icon></div>
            <span>留言板</span>
            <div class="ql-hover-bg"></div>
          </div>
          <!-- 饮食记录 -->
          <div class="ql 3d-icon" @click="$router.push('/record')">
            <div class="ql-icon"><el-icon><EditPen /></el-icon></div>
            <span>饮食记录</span>
            <div class="ql-hover-bg"></div>
          </div>
          <!-- 营养提醒 -->
          <div class="ql 3d-icon" @click="$router.push('/reminders')">
            <div class="ql-icon"><el-icon><AlarmClock /></el-icon></div>
            <span>营养提醒</span>
            <div class="ql-hover-bg"></div>
          </div>
          <!-- 数据分析 -->
          <div class="ql 3d-icon" @click="$router.push('/statistics')">
            <div class="ql-icon"><el-icon><TrendingUp /></el-icon></div>
            <span>数据分析</span>
            <div class="ql-hover-bg"></div>
          </div>
        </div>
      </el-card>
    </transition>

    <!-- 页脚 -->
    <footer class="page-footer">
      <div class="footer-content">
        <p>© 2023 健康饮食助手 | 科学饮食，健康生活</p>
        <div class="footer-links">
          <a href="#">关于我们</a>
          <a href="#">使用帮助</a>
          <a href="#">联系我们</a>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
// 原有脚本代码保持不变
import { ref, onMounted, computed, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import {foodsApi, nutritionAnalysisApi, recipesApi} from '@/api'
import api from '@/api'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

// 粒子背景效果
let particlesInterval: any = null

function createParticles() {
  const container = document.querySelector('.bg-particles')
  if (!container) return

  container.innerHTML = ''

  for (let i = 0; i < 20; i++) {
    const particle = document.createElement('div')
    particle.className = 'particle'

    const size = Math.random() * 8 + 2
    const left = Math.random() * 100
    const top = Math.random() * 100

    const opacity = Math.random() * 0.1 + 0.05
    const hue = Math.random() * 20 + 190

    particle.style.width = `${size}px`
    particle.style.height = `${size}px`
    particle.style.left = `${left}%`
    particle.style.top = `${top}%`
    particle.style.backgroundColor = `hsla(${hue}, 80%, 60%, ${opacity})`
    particle.style.animationDuration = `${Math.random() * 20 + 10}s`
    particle.style.animationDelay = `${Math.random() * 5}s`

    container.appendChild(particle)
  }
}

// 轮播图数据
type Banner = { image: string; title: string; subtitle: string; link?: string }
const banners = ref<Banner[]>([
  { image: 'https://pic.616pic.com/bg_w1180/00/06/17/XUyPmC32IR.jpg!/fw/1120', title: '时令推荐 · 秋季滋养', subtitle: '三文鱼与南瓜的黄金搭配', link: '/recipes' },
  { image: 'https://img95.699pic.com/photo/60015/4503.jpg_wh860.jpg', title: '轻食特辑 · 减脂优选', subtitle: '高蛋白低脂肪更自在', link: '/recipes' },
  { image: 'https://img95.699pic.com/photo/60044/0880.jpg_wh860.jpg', title: '家庭便当 · 省时省心', subtitle: '15分钟搞定营养午餐', link: '/recipes' },
])

// 用户相关数据
const userName = computed(() => userStore.username || userStore.userInfo?.name || '用户')

const showWelcome = ref(false)


// 健康数据
// 计算BMI
const bmi = computed(() => {
  const userInfo = userStore.userInfo
  if (!userInfo || !userInfo.height || !userInfo.weight || userInfo.height <= 0 || userInfo.weight <= 0) {
    return '--'
  }
  const heightInMeters = userInfo.height / 100
  const bmiValue = userInfo.weight / (heightInMeters * heightInMeters)
  return bmiValue.toFixed(1)
})
const caloriesToday = ref(0)
const caloriesGoal = ref(0)
const currentGoal = computed(() => userStore.userInfo?.healthGoal || '未设置')
const goalProgress = ref('进行中')
const activeDays = ref(15)

// 计算卡路里进度百分比
const getCaloriesProgress = computed(() => {
  if (!caloriesGoal.value || caloriesGoal.value === 0) return 0
  return Math.min(Math.round((caloriesToday.value / caloriesGoal.value) * 100), 100)
})

// 获取BMI状态
function getBmiStatus(bmiValue?: number): string {
  if (!bmiValue) return '--'
  if (bmiValue < 18.5) return '偏瘦'
  if (bmiValue < 24) return '正常'
  if (bmiValue < 28) return '超重'
  return '肥胖'
}

// 获取BMI对应的颜色
function getBmiColor(bmiValue?: number): string {
  if (!bmiValue) return 'normal'
  if (bmiValue < 18.5) return 'underweight'
  if (bmiValue < 24) return 'normal'
  if (bmiValue < 28) return 'overweight'
  return 'obese'
}

// 获取BMI状态对应的箭头图标
function getBmiArrowClass(bmiValue?: number): string {
  if (!bmiValue) return 'fa-minus';
  if (bmiValue < 18.5) return 'fa-arrow-up'; // 偏瘦建议增重
  if (bmiValue < 24) return 'fa-check'; // 正常
  return 'fa-arrow-down'; // 超重/肥胖建议减重
}

// 获取BMI卡片对应的CSS类
function getBmiCardClass(bmiValue?: number): string {
  if (!bmiValue) return 'bmi-card';
  if (bmiValue < 18.5) return 'bmi-card underweight';
  if (bmiValue < 24) return 'bmi-card normal';
  if (bmiValue < 28) return 'bmi-card overweight';
  return 'bmi-card obese';
}

// 平台动态数据
type News = {
  type: string;
  title: string;
  time: string;
  id?: number;
}
const latestNews = ref<News[]>([])

async function loadLatestNews() {
  try {
    const { data } = await api.get('/messages/latest?limit=5')
    latestNews.value = (data || []).map((msg: any) => ({
      type: getMessageTypeLabel(msg.messageType),
      title: msg.title || '无标题',
      time: formatDate(msg.createdAt),
      id: msg.id
    }))
  } catch (error) {
    console.error('加载平台动态失败:', error)
    latestNews.value = []
  }
}

function getMessageTypeLabel(type: string): string {
  const map: Record<string, string> = {
    'reminder': '提醒',
    'nutrition': '营养',
    'target': '目标',
    'announcement': '公告'
  }
  return map[type] || '通知'
}

function formatDate(dateStr?: string): string {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })
}

// 推荐菜谱数据
const recommendations = ref<any[]>([])
function bgImg(item: any) {
  if (item.coverImageUrl) {
    return {
      backgroundImage: `url(${item.coverImageUrl})`,
      backgroundSize: 'cover',
      backgroundPosition: 'center',
      backgroundBlendMode: 'overlay'
    }
  }
  const map: Record<string, string> = {
    '谷物': '/images/categories/grains.svg',
    '蛋白质': '/images/categories/protein.svg',
    '蔬菜': '/images/categories/vegetable.svg',
    '水果': '/images/categories/fruit.svg',
    '奶制品': '/images/categories/dairy.svg',
    '豆制品': '/images/categories/bean.svg',
    '坚果': '/images/categories/nuts.svg'
  }
  const url = map[item.category || ''] || '/images/categories/fallback.svg'
  return {
    backgroundImage: `url(${url})`,
    backgroundSize: 'cover',
    backgroundPosition: 'center',
    backgroundBlendMode: 'overlay'
  }
}

async function loadRecommendations() {
  try {
    // 使用recipesApi.page获取菜谱数据，限制返回5条
    const { data } = await recipesApi.page({
      page: 1,
      size: 5
    })
    recommendations.value = data.items
  } catch (error) {
    console.error('加载推荐菜谱失败:', error)
    recommendations.value = []
  }
}


// 导航函数
function goBanner(b: Banner) {
  if (b.link) {
    router.push(b.link)
  }
}

function goNews(n: News) {
  if (n.type === '公告') {
    router.push(`/announcements/${n.id || 1}`)
  } else {
    router.push('/messages')
  }
}

function goRecipe(item: any) {
  router.push(`/recipes/${item.id}`)
}

// 波纹效果函数
function createRipple(event: MouseEvent, element: HTMLElement) {
  const ripple = element.querySelector('.news-ripple') as HTMLElement
  if (!ripple) return

  const rect = element.getBoundingClientRect()
  const x = event.clientX - rect.left
  const y = event.clientY - rect.top

  ripple.style.width = ripple.style.height = `${Math.max(rect.width, rect.height)}px`
  ripple.style.left = `${x}px`
  ripple.style.top = `${y}px`

  ripple.classList.add('active')

  setTimeout(() => {
    ripple.classList.remove('active')
  }, 600)
}

// 绑定波纹效果事件
function bindRippleEvents() {
  const newsItems = document.querySelectorAll('.news-item')
  newsItems.forEach(item => {
    item.addEventListener('click', (e) => {
      createRipple(e as MouseEvent, item as HTMLElement)
    })
  })
}

onMounted(async () => {
  loadRecommendations()
  loadLatestNews()

  // 获取用户信息
  await userStore.fetchUserInfo()

  // 获取今日卡路里数据
  try {
    const today = new Date().toISOString().split('T')[0]
    console.log('正在获取卡路里数据，日期:', today)

    // 使用与 Analysis.vue 一致的方法获取营养数据
    const response = await nutritionAnalysisApi.getDailyAnalysis(today)

    console.log('API响应:', response)
    const res = response.data
    console.log('API响应数据:', res)

    if (res.code === 200) {
      console.log('指标列表:', res.data.indicators)
      // 找到热量指标并更新数据
      const caloriesIndicator = res.data.indicators.find((item: any) => {
        console.log('检查指标:', item.name)
        return item.name.includes('热量')
      })

      if (caloriesIndicator) {
        console.log('找到热量指标:', caloriesIndicator)
        caloriesToday.value = parseFloat(caloriesIndicator.actual || 0)
        caloriesGoal.value = parseFloat(caloriesIndicator.target || 0)
      } else {
        console.error('未找到热量指标')
      }
    } else {
      console.error('API返回错误:', res.msg)
    }
  } catch (error) {
    console.error('获取卡路里数据失败:', error)
  }

  createParticles()
  particlesInterval = setInterval(createParticles, 30000)

  setTimeout(() => {
    bindRippleEvents()
  }, 100)

  setTimeout(() => {
    showWelcome.value = false
  }, 500)
})

onUnmounted(() => {
  if (particlesInterval) {
    clearInterval(particlesInterval)
  }
})
</script>

<style scoped>
/* 全局样式与背景 */
.dashboard-container {
  padding: 0 24px 24px;
  animation: fadeIn 0.8s ease-out;
  background-color: #f8fafc;
  position: relative;
  min-height: 100vh;
  overflow-x: hidden;
  font-family: 'Inter', system-ui, -apple-system, sans-serif;
}

/* 顶部导航栏 */
.top-nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  margin-bottom: 24px;
  position: relative;
  z-index: 2;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 700;
  font-size: 18px;
  color: #1e293b;
}

.logo i {
  font-size: 24px;
  color: #4facfe;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  box-shadow: 0 4px 12px rgba(79, 172, 254, 0.2);
  transition: transform 0.3s ease;
}

.avatar:hover {
  transform: scale(1.1) rotate(5deg);
}

.user-name {
  font-weight: 500;
  color: #334155;
}

/* 3D浮动形状背景 */
.floating-shapes {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 0;
  overflow: hidden;
}

.shape {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.15;
  animation: float 30s infinite ease-in-out;
}

.shape-1 {
  width: 600px;
  height: 600px;
  background: linear-gradient(135deg, #4facfe, #00f2fe);
  top: -300px;
  right: -200px;
  animation-delay: -10s;
}

.shape-2 {
  width: 500px;
  height: 500px;
  background: linear-gradient(135deg, #7c3aed, #4f46e5);
  bottom: -250px;
  left: -150px;
  animation-delay: -5s;
}

.shape-3 {
  width: 400px;
  height: 400px;
  background: linear-gradient(135deg, #10b981, #34d399);
  top: 40%;
  left: 20%;
  animation-delay: 0s;
}

@keyframes float {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-50px) rotate(10deg); }
}

/* 粒子背景效果 */
.bg-particles {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 0;
  overflow: hidden;
}

.particle {
  position: absolute;
  border-radius: 50%;
  transform: translateY(100vh);
  animation: floatUp linear infinite;
  backdrop-filter: blur(2px);
}

@keyframes floatUp {
  0% {
    transform: translateY(100vh) translateX(0);
    opacity: 0;
  }
  10% {
    opacity: 1;
  }
  90% {
    opacity: 1;
  }
  100% {
    transform: translateY(-10vh) translateX(var(--random-offset));
    opacity: 0;
  }
}

/* 玻璃拟态效果增强 */
.glass-effect {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.25);
  box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.08);
  transition: all 0.3s ease;
}

.glass-effect:hover {
  background: rgba(255, 255, 255, 0.95);
  border-color: rgba(255, 255, 255, 0.35);
}

/* 分屏布局 */
.split-screen {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

/* 英雄区样式增强 */
.hero-card {
  margin-bottom: 24px;
  overflow: hidden;
  position: relative;
  z-index: 1;
  transition: transform 0.5s ease, box-shadow 0.5s ease;
  border-radius: 20px !important;
}

.hero-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.1);
}

.hero {
  align-items: center;
  position: relative;
  z-index: 1;
  padding: 32px 24px;
}

/* 标题与文字样式 - 焦点排版 */
.hero-left h1.hero-title {
  margin: 0 0 16px;
  font-size: 36px;
  font-weight: 800;
  line-height: 1.2;
  text-shadow: 0 1px 2px rgba(0,0,0,0.05);
  background: linear-gradient(90deg, #1e293b, #334155);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.hero-left h1 .accent {
  position: relative;
  display: inline-block;
  background: linear-gradient(90deg, #4facfe, #00f2fe);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.hero-left h1 .accent::after {
  content: '';
  position: absolute;
  bottom: -4px;
  left: 0;
  width: 100%;
  height: 3px;
  background: linear-gradient(90deg, #4facfe, #00f2fe);
  border-radius: 3px;
  transform: scaleX(0.9);
  transition: transform 0.3s ease;
  box-shadow: 0 2px 8px rgba(79, 172, 254, 0.3);
}

.hero-left h1:hover .accent::after {
  transform: scaleX(1);
}

.hero-desc {
  color: #475569;
  margin: 0 0 24px;
  font-size: 16px;
  line-height: 1.6;
  max-width: 80%;
  text-shadow: 0 1px 1px rgba(0,0,0,0.03);
}

/* 按钮样式增强 - 多色彩设计 */
.hero-actions {
  display: flex;
  gap: 16px;
}

.primary-btn {
  position: relative;
  overflow: hidden;
  background: linear-gradient(90deg, #4facfe, #00f2fe);
  border: none;
}

.primary-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent);
  transition: left 0.6s ease;
}

.primary-btn:hover::before {
  left: 100%;
}

.secondary-btn {
  position: relative;
  overflow: hidden;
  background-color: #ffffff;
  color: #4facfe;
  border-color: #4facfe;
}

.secondary-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(79, 172, 254, 0.1), transparent);
  transition: left 0.6s ease;
}

.secondary-btn:hover::before {
  left: 100%;
}

/* 轮播图增强 */
.hero-right {
  position: relative;
}

.hero-right::before {
  content: '';
  position: absolute;
  inset: -12px;
  background: rgba(255,255,255,0.1);
  backdrop-filter: blur(10px);
  border-radius: 24px;
  z-index: -1;
  border: 1px solid rgba(255,255,255,0.2);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.05);
}

.hero-right .banner-item {
  border-radius: 16px;
  overflow: hidden;
  transition: transform 0.5s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

.hero-right .banner-item:hover {
  transform: translateY(-5px) scale(1.02);
}

.hero-right .banner {
  height: 280px;
  background-size: cover;
  background-position: center;
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.5s ease;
  position: relative;
  filter: brightness(1);
}

.hero-right .banner:hover {
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.15);
  filter: brightness(1.05);
}

.banner-mask {
  height: 100%;
  width: 100%;
  background: linear-gradient( to top, rgba(0,0,0,0.7) 0%, rgba(0,0,0,0) 60% );
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  padding: 24px;
  color: #fff;
  transform: translateY(10px);
  opacity: 0.9;
  transition: all 0.3s ease;
  z-index: 1;
  position: relative;
}

.banner:hover .banner-mask {
  transform: translateY(0);
  opacity: 1;
}

.banner-title {
  font-weight: 700;
  font-size: 20px;
  margin-bottom: 8px;
  text-shadow: 0 2px 4px rgba(0,0,0,0.3);
  position: relative;
}

.banner-title::after {
  content: '';
  position: absolute;
  bottom: -4px;
  left: 0;
  width: 60px;
  height: 2px;
  background-color: rgba(255,255,255,0.8);
  border-radius: 1px;
  transition: width 0.3s ease;
}

.banner:hover .banner-title::after {
  width: 100%;
}

.banner-sub {
  opacity: 0.9;
  font-size: 14px;
  text-shadow: 0 1px 2px rgba(0,0,0,0.3);
}

/* 轮播图悬停3D光效 */
.banner-hover-light {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: radial-gradient(circle at center, rgba(255,255,255,0) 0%, rgba(255,255,255,0.1) 70%, rgba(255,255,255,0) 100%);
  opacity: 0;
  transition: opacity 0.5s ease;
  pointer-events: none;
  z-index: 0;
}

.banner:hover .banner-hover-light {
  opacity: 1;
  animation: lightPulse 2s infinite ease-in-out;
}

@keyframes lightPulse {
  0%, 100% {
    transform: scale(1);
    opacity: 0.7;
  }
  50% {
    transform: scale(1.1);
    opacity: 0.3;
  }
}

/* 用户欢迎区域增强 */
.user-welcome {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.welcome-text {
  animation: slideInLeft 0.8s ease-out;
}

.user-stats {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-top: 12px;
}


.stat-item {
  background: rgba(255, 255, 255, 0.8);
  border-radius: 12px;
  padding: 20px 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  text-align: center;
  border: 1px solid rgba(79, 172, 254, 0.2);
  box-shadow: 0 4px 12px rgba(0,0,0,0.03);
  position: relative;
  overflow: hidden;
  backdrop-filter: blur(4px);
}

.stat-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 3px;
  background: linear-gradient(90deg, #4facfe, #00f2fe);
  transform: scaleX(0);
  transition: transform 0.3s ease;
  box-shadow: 0 2px 8px rgba(79, 172, 254, 0.3);
}

.stat-item:hover::before {
  transform: scaleX(1);
}

.stat-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(79, 172, 254, 0.15);
  background: rgba(255, 255, 255, 1);
  border-color: rgba(79, 172, 254, 0.3);
}

.stat-value {
  font-size: 28px;
  font-weight: 800;
  margin-bottom: 6px;
  transition: color 0.3s ease;
  background: linear-gradient(90deg, #4facfe, #39a1ff);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.stat-item:hover .stat-value {
  background: linear-gradient(90deg, #39a1ff, #00f2fe);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.stat-label {
  font-size: 14px;
  color: #6b7280;
}

/* 健康数据概览样式增强 */
.health-stats-card {
  margin-bottom: 24px;
  border-radius: 20px !important;
}

/* 卡片头部样式 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.card-action {
  font-size: 14px;
  color: #4facfe;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
  transition: all 0.3s ease;
}

.card-action:hover {
  color: #00f2fe;
  transform: translateX(3px);
}

.health-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-top: 16px;
}

.health-stat-item {
  padding: 24px;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  border: none;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

/* BMI卡片样式 */
.bmi-card {
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  border: none;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  color: white;
}

/* 新增BMI详情信息样式 */
.bmi-details {
  display: flex;
  justify-content: space-between;
  margin-top: 16px;
  font-size: 14px;
  opacity: 0.9;
}

/* 不同BMI状态的颜色 */
.bmi-card.underweight {
  background: linear-gradient(135deg, #39a1ff, #0ea5e9); /* 蓝色 */
}

.bmi-card.normal {
  background: linear-gradient(135deg, #2d8659, #22c55e); /* 绿色 */
}

.bmi-card.overweight {
  background: linear-gradient(135deg, #ff9a56, #ff6b6b); /* 橙色 */
}

.bmi-card.obese {
  background: linear-gradient(135deg, #f44336, #ef4444); /* 红色 - 保持原有的红色用于肥胖状态 */
}

.bmi-number {
  font-size: 48px;
  font-weight: 800;
  margin: 12px 0;
  background: none;
  -webkit-background-clip: unset;
  background-clip: unset;
  color: white;
}

.bmi-status {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 500;
  background: rgba(255, 255, 255, 0.2);
  padding: 6px 12px;
  border-radius: 20px;
  width: fit-content;
}

.bmi-status i {
  font-size: 14px;
}

/* 卡路里卡片样式 */
.calorie-card {
  background: linear-gradient(135deg, #8b5cf6, #a78bfa); /* 紫色渐变 */
  color: white;
}

.calorie-number {
  font-size: 24px;
  font-weight: 700;
  margin: 12px 0;
  background: none;
  -webkit-background-clip: unset;
  background-clip: unset;
  color: white;
}

.calorie-progress {
  position: relative;
  margin-top: 16px;
}

.calorie-progress .el-progress-bar {
  height: 20px;
  border-radius: 10px;
  overflow: hidden;
}

.calorie-progress .el-progress-bar__outer {
  height: 20px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 10px;
}

.calorie-progress .el-progress-bar__inner {
  height: 20px;
  background: linear-gradient(90deg, #ff6b6b, #ffa500);
  border-radius: 10px;
}
.progress-percentage {
  position: absolute;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
  color: white;
  font-size: 14px;
  font-weight: 700;
  background: transparent;
  width: auto;
  height: auto;
  border-radius: 0;
}

/* 当前目标卡片样式 */
.goal-card {
  background: linear-gradient(135deg, #06b6d4, #22d3ee); /* 青色渐变 */
  color: white;
}

.goal-number {
  font-size: 32px;
  font-weight: 800;
  margin: 12px 0;
  background: none;
  -webkit-background-clip: unset;
  background-clip: unset;
  color: white;
}

.goal-status {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 12px;
}

.goal-indicator {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #ffffff;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(255, 255, 255, 0.7);
  }
  70% {
    box-shadow: 0 0 0 10px rgba(255, 255, 255, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(255, 255, 255, 0);
  }
}

.goal-status-text {
  font-size: 14px;
  font-weight: 500;
  color: white;
}

/* 移除原有的stat-icon样式影响 */
.health-stat-item .stat-icon {
  display: none;
}

.stat-content .stat-title {
  font-size: 16px;
  font-weight: 500;
  opacity: 0.8;
  margin-bottom: 0;
}

.stat-content .stat-number {
  font-size: 20px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 6px;
  background: linear-gradient(90deg, #1f2937, #334155);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.stat-content .stat-status {
  font-size: 13px;
  padding: 3px 8px;
  border-radius: 4px;
  display: inline-block;
}

.stat-status.status-normal {
  background-color: #f0fdf4;
  color: #2d8659;
}

.stat-status.status-underweight {
  background-color: #dbeafe;
  color: #3b82f6;
}

.stat-status.status-overweight {
  background-color: #fef3c7;
  color: #f59e0b;
}

.stat-status.status-obese {
  background-color: #fee2e2;
  color: #ef4444;
}

.stat-content {
  transform: translateZ(20px);
}

/* 卡片标题样式增强 - 焦点排版 */
.card-title {
  font-weight: 700;
  margin-bottom: 0;
  font-size: 18px;
  position: relative;
  display: inline-block;
  padding-left: 4px;
  background: linear-gradient(90deg, #1f2937, #334155);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.card-title::after {
  content: '';
  position: absolute;
  bottom: -6px;
  left: 0;
  width: 40px;
  height: 3px;
  background: linear-gradient(90deg, #4facfe, #00f2fe);
  border-radius: 3px;
  transition: width 0.3s ease;
  box-shadow: 0 2px 8px rgba(79, 172, 254, 0.3);
}

.el-card:hover .card-title::after {
  width: 60px;
}

/* 平台动态样式增强 */
.news-card,
.recipes-card {
  height: 100%;
  display: flex;
  flex-direction: column;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  border-radius: 20px !important;
}

.news-card:hover,
.recipes-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.1);
}

.news-item {
  display: flex;
  gap: 10px;
  align-items: center;
  cursor: pointer;
  padding: 10px 12px;
  transition: all 0.2s ease;
  border-radius: 8px;
  margin-bottom: 4px;
  position: relative;
  overflow: hidden;
}

.news-item:hover {
  background: #f0f7ff;
  transform: translateX(5px);
  border-left: 3px solid #4facfe;
}

.news-type {
  font-size: 12px;
  color: #4facfe;
  background: #eef2ff;
  padding: 2px 8px;
  border-radius: 999px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.news-item:hover .news-type {
  background: #4facfe;
  color: #ffffff;
}

.news-title {
  cursor: pointer;
  color: #374151;
  flex: 1;
  transition: color 0.2s ease;
  line-height: 1.5;
}

.news-title:hover {
  color: #4facfe;
}

/* 新闻条目波纹效果 */
.news-ripple {
  position: absolute;
  border-radius: 50%;
  background-color: rgba(79, 172, 254, 0.1);
  transform: scale(0);
  transition: transform 0.6s ease, opacity 0.6s ease;
  pointer-events: none;
}

.news-ripple.active {
  transform: scale(10);
  opacity: 0;
}

/* 菜谱推荐样式增强 */
.recipes {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
}

.recipe {
  background: #ffffff;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid #e5e7eb;
  cursor: pointer;
  transition: transform 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94), box-shadow 0.4s ease;
  display: flex;
  flex-direction: column;
  position: relative;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.03);
}

.recipe::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(to top, rgba(0,0,0,0.05) 0%, transparent 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
  z-index: 1;
  pointer-events: none;
}

.recipe:hover::before {
  opacity: 1;
}

.recipe-main {
  grid-row: span 2;
  display: flex;
  flex-direction: column;
}

.recipe .thumb {
  background-size: cover;
  background-position: center;
  height: 140px;
  transition: transform 0.7s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  position: relative;
  overflow: hidden;
  filter: brightness(0.95);
}

.recipe .thumb::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0,0,0,0.03);
  transition: background 0.3s ease;
}

.recipe:hover .thumb {
  transform: scale(1.08);
  filter: brightness(1.05);
}

.recipe:hover .thumb::after {
  background: rgba(0,0,0,0);
}

/* 菜谱图片悬停效果 */
.thumb-hover {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(to top, rgba(0,0,0,0.2) 0%, rgba(0,0,0,0) 60%);
  opacity: 0;
  transition: opacity 0.3s ease;
  pointer-events: none;
  z-index: 1;
}

.recipe:hover .thumb-hover {
  opacity: 1;
}

/* 推荐菜谱标签 */
.recipe-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  background: linear-gradient(135deg, #4facfe, #00f2fe);
  color: white;
  font-size: 12px;
  padding: 3px 10px;
  border-radius: 4px;
  font-weight: 500;
  z-index: 2;
  box-shadow: 0 2px 8px rgba(79, 172, 254, 0.3);
  transition: transform 0.3s ease;
}

.recipe-main:hover .recipe-badge {
  transform: scale(1.1) rotate(3deg);
}

.recipe-main .thumb {
  height: 180px;
  flex-shrink: 0;
}

.recipe .info {
  padding: 16px;
  flex: 1;
  display: flex;
  flex-direction: column;
  position: relative;
  z-index: 2;
}

.recipe .title {
  font-weight: 700;
  margin-bottom: 8px;
  line-height: 1.4;
  font-size: 16px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  transition: color 0.3s ease;
  background: linear-gradient(90deg, #111827, #334155);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.recipe:hover .title {
  background: linear-gradient(90deg, #4facfe, #39a1ff);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.recipe .meta {
  font-size: 12px;
  color: #6b7280;
  margin-top: auto;
  display: flex;
  align-items: center;
  gap: 8px;
}

.recipe .meta::before {
  content: '';
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background-color: #cbd5e1;
}

.recipe-list {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.recipe .badges {
  display: flex;
  gap: 8px;
  margin: 8px 0;
}

/* Element Plus 标签样式增强 - 多色彩设计 */
:deep(.el-tag) {
  border-radius: 8px;
  padding: 0 8px;
  height: 22px;
  line-height: 20px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(0,0,0,0.03);
}

:deep(.el-tag--success) {
  background-color: #f0fdf4;
  color: #2d8659;
  border-color: #dcfce7;
}

.recipe:hover :deep(.el-tag) {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0,0,0,0.05);
}

.recipe:hover :deep(.el-tag--success) {
  background-color: #2d8659;
  color: #ffffff;
  border-color: #2d8659;
}

/* 快捷入口样式增强 - 3D图标效果 */
.quick-links-card {
  margin-bottom: 24px;
  border-radius: 20px !important;
}

.quick-links {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 20px;
  margin-top: 16px;
}


.ql {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  justify-content: center;
  padding: 24px 12px;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  cursor: pointer;
  background: #ffffff;
  transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  position: relative;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.03);
}

.ql::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, rgba(79, 70, 229, 0.05) 0%, transparent 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.ql:hover::before {
  opacity: 1;
}

.ql-icon {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

/* 多色彩快捷入口图标 */
.ql:nth-child(1) .ql-icon {
  background: linear-gradient(135deg, rgba(236, 72, 153, 0.1), rgba(236, 72, 153, 0.05));
  color: #ec4899;
}

.ql:nth-child(2) .ql-icon {
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.1), rgba(16, 185, 129, 0.05));
  color: #10b981;
}

.ql:nth-child(3) .ql-icon {
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.1), rgba(59, 130, 246, 0.05));
  color: #3b82f6;
}

.ql:nth-child(4) .ql-icon {
  background: linear-gradient(135deg, rgba(245, 158, 11, 0.1), rgba(245, 158, 11, 0.05));
  color: #f59e0b;
}

.ql:nth-child(5) .ql-icon {
  background: linear-gradient(135deg, rgba(139, 92, 246, 0.1), rgba(139, 92, 246, 0.05));
  color: #8b5cf6;
}

.ql:nth-child(6) .ql-icon {
  background: linear-gradient(135deg, rgba(239, 68, 68, 0.1), rgba(239, 68, 68, 0.05));
  color: #ef4444;
}

.ql span {
  font-size: 14px;
  font-weight: 500;
  transition: color 0.3s ease;
}

/* 快捷入口悬浮背景 */
.ql-hover-bg {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  background: radial-gradient(circle, rgba(79, 70, 229, 0.1) 0%, rgba(79, 70, 229, 0) 70%);
  border-radius: 50%;
  transform: translate(-50%, -50%);
  transition: width 0.5s ease, height 0.5s ease;
  pointer-events: none;
}

.ql:hover .ql-hover-bg {
  width: 200%;
  height: 200%;
}

/* 查看更多链接增强 */
.view-more {
  text-align: right;
  cursor: pointer;
  font-size: 14px;
  padding: 12px 0 8px;
  transition: all 0.3s ease;
  margin-top: auto;
  display: inline-block;
  position: relative;
  align-self: flex-end;
  background: linear-gradient(90deg, #4facfe, #39a1ff);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.view-more::after {
  content: '';
  position: absolute;
  bottom: 8px;
  left: 0;
  width: 0;
  height: 1px;
  background: linear-gradient(90deg, #4facfe, #39a1ff);
  transition: width 0.3s ease;
}

.view-more:hover {
  background: linear-gradient(90deg, #39a1ff, #00f2fe);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.view-more:hover::after {
  width: 100%;
}

/* 页脚样式 */
.page-footer {
  margin-top: 40px;
  padding: 24px 0;
  border-top: 1px solid rgba(229, 231, 235, 0.5);
  position: relative;
  z-index: 1;
}

.footer-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.footer-content p {
  color: #6b7280;
  font-size: 14px;
  margin: 0;
}

.footer-links {
  display: flex;
  gap: 24px;
}

.footer-links a {
  color: #6b7280;
  font-size: 14px;
  text-decoration: none;
  transition: color 0.3s ease;
}

.footer-links a:hover {
  color: #4facfe;
}

/* Element Plus 组件样式覆盖增强 */
:deep(.el-card) {
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(229, 231, 235, 0.7);
  transition: all 0.4s ease;
  overflow: hidden;
}

:deep(.el-card:hover) {
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
}

:deep(.el-button--primary) {
  background-color: #4facfe;
  border-color: #4facfe;
  border-radius: 8px;
  padding: 10px 24px;
  transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  box-shadow: 0 4px 12px rgba(79, 172, 254, 0.2);
}

/* 动画效果 */
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes slideInLeft {
  from { opacity: 0; transform: translateX(-20px); }
  to { opacity: 1; transform: translateX(0); }
}

/* 响应式调整 */
@media (max-width: 1024px) {
  .split-screen {
    grid-template-columns: 1fr;
  }

  .health-stats {
    grid-template-columns: 1fr;
  }

  .quick-links {
    grid-template-columns: repeat(3, 1fr);
  }

  .recipes {
    grid-template-columns: 1fr;
  }

  .recipe-list {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 768px) {
  .recipe-list {
    grid-template-columns: 1fr;
  }

  .quick-links {
    grid-template-columns: repeat(2, 1fr);
  }

  .el-row {
    flex-direction: column;
  }

  .el-col {
    width: 100% !important;
    margin-bottom: 24px;
  }
}
</style>

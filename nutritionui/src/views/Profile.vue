<template>
  <div class="profile-container">
    <!-- 页面标题栏 -->
    <div class="page-header">
      <h1>个人信息管理</h1>
      <p class="header-desc">完善您的个人信息，获取更精准的营养建议</p>
    </div>

    <!-- 信息卡片 -->
    <el-card class="profile-card">
      <!-- 头像区域 -->
      <div class="avatar-section">
        <div class="avatar-container">
          <el-avatar :size="100" class="user-avatar">
            <img
                :src="avatarUrl"
                alt="用户头像"
                @error="handleAvatarError"
                @load="handleAvatarLoad"
            >
          </el-avatar>
        </div>
      </div>

      <!-- 表单内容 -->
      <el-form
          :model="form"
          label-width="120px"
          class="profile-form"
          :rules="rules"
          ref="formRef"
      >
        <!-- 表单内容保持不变 -->
        <el-form-item label="用户名">
          <el-input v-model="form.username" disabled />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="请输入邮箱" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="年龄" prop="age">
              <el-input-number
                  v-model="form.age"
                  :min="1"
                  :max="150"
                  placeholder="请输入年龄"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别">
              <el-radio-group v-model="form.gender" class="gender-group">
                <el-radio :label="0" class="gender-option">女</el-radio>
                <el-radio :label="1" class="gender-option">男</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="身高(cm)" prop="height">
              <el-input-number
                  v-model="form.height"
                  :min="50"
                  :max="250"
                  :precision="1"
                  placeholder="请输入身高"
                  @change="calculateBMI"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="体重(kg)" prop="weight">
              <el-input-number
                  v-model="form.weight"
                  :min="20"
                  :max="200"
                  :precision="1"
                  placeholder="请输入体重"
                  @change="calculateBMI"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- BMI显示区域 -->
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="BMI指数">
              <div class="bmi-info">
                <span class="bmi-value">{{ bmiValue }}</span>
                <span class="bmi-category" :class="bmiCategoryClass">{{ bmiCategory }}</span>
                <p class="bmi-desc">
                  BMI是评估成人体重与身高比例是否健康的指标
                </p>
              </div>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="活动量" prop="activityLevel">
              <el-select v-model="form.activityLevel" placeholder="请选择活动量">
                <el-option label="久坐" :value="1" />
                <el-option label="轻度活动" :value="2" />
                <el-option label="中度活动" :value="3" />
                <el-option label="高强度活动" :value="4" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="健康目标" prop="healthGoal">
              <el-select
                  v-model="form.healthGoal"
                  @change="onGoalChange"
                  placeholder="请选择健康目标"
              >
                <el-option label="减重" value="减重" />
                <el-option label="增肌" value="增肌" />
                <el-option label="维持健康" value="维持健康" />
                <el-option label="控制血糖" value="控制血糖" />
                <el-option label="控制血压" value="控制血压" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item
            v-if="showTargetInput"
            :label="targetLabel"
            prop="targetValue"
        >
          <div class="target-input-group">
            <el-input-number
                v-model="form.targetValue"
                :min="getMinValue"
                :max="getMaxValue"
                :precision="1"
                placeholder="请输入目标值"
            />
            <span class="target-unit">{{ targetUnit }}</span>
          </div>
        </el-form-item>

        <el-form-item class="form-actions">
          <el-button
              type="primary"
              @click="updateInfo"
              :loading="loading"
              class="save-btn"
          >
            <el-icon v-if="loading" class="loading-icon"><Loading /></el-icon>
            <span>保存信息</span>
          </el-button>
          <el-button
              type="default"
              @click="resetForm"
              class="reset-btn"
          >
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, reactive, watch } from 'vue'
import { ElMessage, ElForm } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import api from '@/api'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const formRef = ref<InstanceType<typeof ElForm>>()
const avatarUrl = ref('https://cube.elemecdn.com/3/7c/3ea6beec6434fd2932672686dd1e141.png')
const avatarLoading = ref(false)

// 表单数据
const form = ref({
  username: '',
  phone: '',
  email: '',
  age: null as number | null,
  gender: 0,
  height: null as number | null,
  weight: null as number | null,
  activityLevel: 1,
  healthGoal: '维持健康',
  targetValue: null as number | null
})

// 新增BMI相关响应式变量
const bmiValue = ref('')
const bmiCategory = ref('')
const bmiCategoryClass = ref('')

// 表单验证规则
const rules = reactive({
  phone: [
    {
      pattern: /^1[3-9]\d{9}$/,
      message: '请输入正确的手机号',
      trigger: 'blur'
    }
  ],
  email: [
    {
      pattern: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/,
      message: '请输入正确的邮箱格式',
      trigger: 'blur'
    }
  ],
  age: [
    { type: 'number', message: '请输入年龄', trigger: 'blur' }
  ],
  height: [
    { type: 'number', message: '请输入身高', trigger: 'blur' }
  ],
  weight: [
    { type: 'number', message: '请输入体重', trigger: 'blur' }
  ]
})

const loading = ref(false)
const showTargetInput = ref(false)

// 目标值相关计算属性
const targetLabel = computed(() => {
  switch(form.value.healthGoal) {
    case '减重': return '目标减重(kg)'
    case '增肌': return '目标增肌(kg)'
    case '控制血糖': return '目标血糖值'
    case '控制血压': return '目标血压值'
    default: return '目标值'
  }
})

const targetUnit = computed(() => {
  switch(form.value.healthGoal) {
    case '减重':
    case '增肌': return 'kg'
    case '控制血糖': return 'mmol/L'
    case '控制血压': return 'mmHg'
    default: return ''
  }
})

const getMinValue = computed(() => {
  return form.value.healthGoal === '控制血糖' ? 3.9 : 0
})

const getMaxValue = computed(() => {
  switch(form.value.healthGoal) {
    case '控制血糖': return 7.2
    case '控制血压': return 180
    default: return 200
  }
})

// 健康目标变更处理
const onGoalChange = () => {
  const goalsNeedingTarget = ['减重', '增肌', '控制血糖', '控制血压']
  showTargetInput.value = goalsNeedingTarget.includes(form.value.healthGoal)

  // 切换目标时清空目标值
  if (!showTargetInput.value) {
    form.value.targetValue = null
  }
}

// BMI计算方法
const calculateBMI = () => {
  // 清空现有值
  bmiValue.value = ''
  bmiCategory.value = ''
  bmiCategoryClass.value = ''

  // 验证身高体重是否有效
  if (!form.value.height || !form.value.weight || form.value.height <= 0 || form.value.weight <= 0) {
    return
  }

  // 计算BMI：体重(kg) / 身高(m)的平方
  const heightInMeters = form.value.height / 100
  const bmi = form.value.weight / (heightInMeters * heightInMeters)
  bmiValue.value = bmi.toFixed(1)

  // 确定BMI分类
  if (bmi < 18.5) {
    bmiCategory.value = '偏瘦'
    bmiCategoryClass.value = 'underweight'
  } else if (bmi < 24) {
    bmiCategory.value = '正常'
    bmiCategoryClass.value = 'normal'
  } else if (bmi < 28) {
    bmiCategory.value = '超重'
    bmiCategoryClass.value = 'overweight'
  } else {
    bmiCategory.value = '肥胖'
    bmiCategoryClass.value = 'obese'
  }
}

// 监听身高体重变化，自动计算BMI
watch(
    () => [form.value.height, form.value.weight],
    () => {
      calculateBMI()
    }
)

// 头像处理
const handleAvatarLoad = () => {
  console.log('头像加载成功:', avatarUrl.value)
  avatarLoading.value = false
}

const handleAvatarError = (e: Event) => {
  console.error('头像加载失败:', e)
  const img = e.target as HTMLImageElement
  console.error('失败的头像URL:', img.src)

  // 使用默认头像
  const defaultAvatar = "https://ts3.tc.mm.bing.net/th/id/OIP-C.kGTv-2K2MpPV18mbQ9V4NAAAAA?cb=ucfimg2&ucfimg=1&rs=1&pid=ImgDetMain&o=7&rm=3"
  img.src = defaultAvatar
  avatarUrl.value = defaultAvatar
  avatarLoading.value = false
}

const handleAvatarChange = async (e: Event) => {
  const fileInput = e.target as HTMLInputElement
  const file = fileInput.files?.[0]
  if (!file) return

  console.log('开始处理头像上传，文件信息:', {
    name: file.name,
    size: file.size,
    type: file.type
  })

  // 验证文件类型
  if (!file.type.startsWith('image/')) {
    ElMessage.error('请选择图片文件（JPG、PNG等格式）')
    fileInput.value = ''
    return
  }

  // 验证文件大小（限制为2MB）
  if (file.size > 2 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过2MB')
    fileInput.value = ''
    return
  }

  // 创建FormData对象
  const formData = new FormData()
  formData.append('avatar', file)

  try {
    // 获取token
    let token = ''
    if (userStore && userStore.token) {
      token = userStore.token
    } else {
      // 从localStorage获取
      token = localStorage.getItem('token') || ''
    }

    console.log('当前token:', token ? '存在' : '不存在')

    // 如果没有token，提示用户登录
    if (!token) {
      ElMessage.error('请先登录')
      fileInput.value = ''
      return
    }

    // 显示上传中状态
    const loadingMessage = ElMessage({
      type: 'info',
      message: '头像上传中...',
      duration: 0
    })

    // 发送请求，带上Authorization头
    const response = await api.post('/auth/avatar', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
        'Authorization': `Bearer ${token}`
      },
      timeout: 30000 // 30秒超时
    })

    loadingMessage.close()

    console.log('头像上传响应:', response.data)

    // 更新头像URL
    if (response.data && response.data.avatarUrl) {
      let newAvatarUrl = response.data.avatarUrl
      console.log('服务器返回的头像URL:', newAvatarUrl)

      // 构建完整URL
      // 如果返回的是相对路径（以/开头），拼接完整URL
      if (newAvatarUrl.startsWith('/')) {
        const baseUrl = window.location.protocol + '//' + window.location.host
        avatarUrl.value = baseUrl + newAvatarUrl
      } else {
        avatarUrl.value = newAvatarUrl
      }

      console.log('完整头像URL:', avatarUrl.value)
      ElMessage.success(response.data.message || '头像上传成功')

      // 同时更新用户信息中的头像URL
      if (userStore && userStore.updateAvatar) {
        userStore.updateAvatar(avatarUrl.value)
      }

      // 刷新用户信息，确保数据一致
      await fetchUserInfo()
    } else {
      ElMessage.error('上传成功但返回数据异常')
    }
  } catch (error: any) {
    console.error('头像上传失败:', error)

    // 更详细的错误处理
    if (error.response) {
      const { status, data } = error.response
      console.error('响应错误:', status, data)

      if (status === 401) {
        ElMessage.error('登录已过期，请重新登录')
      } else if (status === 400) {
        ElMessage.error(data.message || '上传失败，请检查文件格式和大小')
      } else if (status === 413) {
        ElMessage.error('文件太大，请选择2MB以内的图片')
      } else if (status === 500) {
        ElMessage.error('服务器内部错误，请稍后重试')
      } else {
        ElMessage.error(data?.message || `上传失败 (${status})`)
      }
    } else if (error.request) {
      console.error('请求错误:', error.request)
      ElMessage.error('网络请求失败，请检查网络连接')
    } else {
      console.error('其他错误:', error.message)
      ElMessage.error('请求配置错误: ' + error.message)
    }
  } finally {
    // 重置文件输入，允许重新选择同一文件
    fileInput.value = ''
  }
}

// 重置表单
const resetForm = () => {
  formRef.value?.resetFields()
  ElMessage.info('已重置表单')
}

// 获取用户信息
onMounted(async () => {
  console.log('当前页面URL:', window.location.href)
  console.log('API基础URL:', api.defaults.baseURL)
  console.log('用户store:', userStore)

  await fetchUserInfo()
})

const fetchUserInfo = async () => {
  try {
    console.log('开始获取用户信息')
    const info = await userStore.fetchUserInfo()
    console.log('获取到的用户信息:', info)

    if (info) {
      // 确保头像URL有正确的路径
      if (info.avatarUrl) {
        console.log('原始头像URL:', info.avatarUrl)

        // 处理头像URL
        let fullAvatarUrl = info.avatarUrl

        // 如果是相对路径，拼接完整URL
        if (fullAvatarUrl.startsWith('/')) {
          const baseUrl = window.location.protocol + '//' + window.location.host
          avatarUrl.value = baseUrl + fullAvatarUrl
        } else {
          avatarUrl.value = fullAvatarUrl
        }

        console.log('处理后的头像URL:', avatarUrl.value)

        // 测试头像URL
        testImageUrl(avatarUrl.value)
      } else {
        console.log('用户没有设置头像，使用默认头像')
      }

      form.value = {
        username: info.username || '',
        phone: info.phone || '',
        email: info.email || '',
        age: info.age || null,
        gender: info.gender !== undefined ? info.gender : 0,
        height: info.height ? info.height : null,
        weight: info.weight ? info.weight : null,
        activityLevel: info.activityLevel || 1,
        healthGoal: info.healthGoal || '维持健康',
        targetValue: info.targetValue || null
      }

      console.log('表单数据已更新:', form.value)

      // 根据健康目标显示/隐藏目标值输入框
      const goalsNeedingTarget = ['减重', '增肌', '控制血糖', '控制血压']
      showTargetInput.value = goalsNeedingTarget.includes(form.value.healthGoal)

      // 计算BMI
      setTimeout(calculateBMI, 0)
    } else {
      console.warn('获取的用户信息为空')
    }
  } catch (error: any) {
    console.error('获取用户信息失败:', error)
    if (error.response?.status === 401) {
      console.log('用户未登录或token过期')
    } else if (error.response?.status !== 401) {
      ElMessage.error('获取用户信息失败: ' + (error.response?.data?.message || error.message))
    }
  }
}

// 测试图片URL是否有效
const testImageUrl = (url: string) => {
  avatarLoading.value = true
  const img = new Image()
  img.onload = () => {
    console.log('头像URL有效，已成功加载:', url)
    avatarLoading.value = false
  }
  img.onerror = () => {
    console.error('头像URL无效，无法加载:', url)

    // 如果URL无效，尝试不同的路径格式
    if (url.includes('/api/avatars/')) {
      // 尝试去掉/api前缀
      const altUrl = url.replace('/api/avatars/', '/avatars/')
      console.log('尝试替代URL:', altUrl)

      // 测试替代URL
      testAlternativeImageUrl(altUrl)
    } else if (url.includes('/avatars/')) {
      // 尝试添加/api前缀
      const altUrl = url.replace('/avatars/', '/api/avatars/')
      console.log('尝试替代URL:', altUrl)

      // 测试替代URL
      testAlternativeImageUrl(altUrl)
    } else {
      avatarLoading.value = false
    }
  }
  img.src = url
}

const testAlternativeImageUrl = (url: string) => {
  const img = new Image()
  img.onload = () => {
    console.log('替代URL有效:', url)
    // 如果找到有效的URL，更新头像
    avatarUrl.value = url
    avatarLoading.value = false
  }
  img.onerror = () => {
    console.error('替代URL无效:', url)
    avatarLoading.value = false
  }
  img.src = url
}

// 更新信息
const updateInfo = async () => {
  // 表单验证
  try {
    await formRef.value?.validate()
  } catch (error) {
    ElMessage.warning('请检查表单填写是否正确')
    return
  }

  loading.value = true
  try {
    // 获取token
    const token = userStore.token || localStorage.getItem('token') || ''
    if (!token) {
      ElMessage.error('请先登录')
      loading.value = false
      return
    }

    await api.put('/auth/user', form.value, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })

    ElMessage.success('更新成功')
    await fetchUserInfo()
  } catch (error: any) {
    console.error('更新信息失败:', error)
    if (error.response) {
      ElMessage.error(error.response.data?.message || '更新失败')
    } else {
      ElMessage.error('网络错误，请检查连接')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.profile-container {
  padding: 20px;
  background: linear-gradient(135deg, #e6f7f0 0%, #d4ede4 100%);
  min-height: 100vh;
}

/* 页面标题 */
.page-header {
  text-align: center;
  margin-bottom: 30px;
  padding: 15px 0;
}

.page-header h1 {
  color: #2d8659;
  font-size: 24px;
  margin-bottom: 8px;
  font-weight: 600;
}

.header-desc {
  color: #6a9986;
  font-size: 14px;
  opacity: 0.9;
}

/* 信息卡片 */
.profile-card {
  max-width: 800px;
  margin: 0 auto;
  border-radius: 16px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  border: none;
  overflow: hidden;
  background-color: #fff;
  transition: transform 0.3s ease;
}

.profile-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.12);
}

/* 头像区域 */
.avatar-section {
  padding: 25px 0;
  border-bottom: 1px solid #f0f5f2;
  display: flex;
  justify-content: center;
}

.avatar-container {
  text-align: center;
}

.user-avatar {
  border: 4px solid #f0f7f4;
  transition: transform 0.3s ease;
  margin-bottom: 12px;
  cursor: pointer;
}

.user-avatar:hover {
  transform: scale(1.05);
}

.avatar-upload {
  display: inline-block;
  cursor: pointer;
}

.upload-btn {
  color: #4facfe;
  padding: 0;
  font-size: 14px;
  transition: color 0.2s;
}

.upload-btn:hover {
  color: #39a1ff;
}

.avatar-file {
  display: none;
}

.avatar-hint {
  font-size: 12px;
  color: #999;
  margin-top: 8px;
}

.avatar-loading {
  font-size: 12px;
  color: #4facfe;
  margin-top: 4px;
}

/* 表单样式 */
.profile-form {
  padding: 30px 40px 20px;
}

:deep(.el-form-item) {
  margin-bottom: 20px;
}

:deep(.el-form-item__label) {
  color: #5a7c65;
  font-weight: 500;
  font-size: 14px;
}

/* 输入框样式 */
:deep(.el-input__wrapper),
:deep(.el-select__wrapper),
:deep(.el-input-number__wrapper) {
  border-radius: 8px;
  border-color: #d9e9e1;
  transition: all 0.2s;
}

:deep(.el-input__wrapper:focus-within),
:deep(.el-select__wrapper:focus-within),
:deep(.el-input-number__wrapper:focus-within) {
  border-color: #4facfe;
  box-shadow: 0 0 0 2px rgba(79, 172, 254, 0.2);
}

/* 性别选择 */
.gender-group {
  display: flex;
  gap: 20px;
}

:deep(.gender-option .el-radio__label) {
  padding-left: 8px;
}

/* 目标值输入组 */
.target-input-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.target-unit {
  color: #909399;
  font-size: 14px;
  white-space: nowrap;
}

/* BMI相关样式 */
.bmi-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 10px;
  border-radius: 8px;
  background-color: #f9fafb;
}

.bmi-value {
  font-size: 24px;
  font-weight: 600;
  color: #4facfe;
}

.bmi-category {
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 14px;
  color: white;
}

.bmi-category.underweight {
  background-color: #4facfe;
}

.bmi-category.normal {
  background-color: #52c41a;
}

.bmi-category.overweight {
  background-color: #faad14;
}

.bmi-category.obese {
  background-color: #f5222d;
}

.bmi-desc {
  margin: 0;
  font-size: 12px;
  color: #8c8c8c;
}

/* 按钮区域 */
.form-actions {
  display: flex;
  justify-content: center;
  gap: 15px;
  margin-top: 30px;
}

.save-btn {
  background-color: #4facfe;
  border-color: #4facfe;
  border-radius: 8px;
  padding: 10px 30px;
  font-size: 15px;
  transition: all 0.3s ease;
}

:deep(.save-btn:hover) {
  background-color: #39a1ff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(79, 172, 254, 0.4);
}

.reset-btn {
  border-radius: 8px;
  padding: 10px 30px;
  font-size: 15px;
  transition: all 0.2s;
}

:deep(.reset-btn:hover) {
  background-color: #f5f7fa;
}

.loading-icon {
  margin-right: 6px;
  font-size: 16px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .profile-container {
    padding: 15px 10px;
  }

  .profile-card {
    width: 100%;
  }

  .profile-form {
    padding: 20px 15px;
  }

  :deep(.el-form-item) {
    margin-bottom: 15px;
  }

  .form-actions {
    flex-direction: column;
    gap: 10px;
  }

  .save-btn, .reset-btn {
    width: 100%;
  }
}
</style>
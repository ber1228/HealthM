<template>
  <div class="record-container">
    <el-card>
      <template #header>
        <div class="record-header">
          <h2>饮食记录</h2>
          <div class="header-actions">
            <el-date-picker
                v-model="selectedDate"
                type="date"
                @change="loadRecords"
                style="margin-right: 15px"
            />
            <el-button type="primary" @click="showAddDialog">
              添加记录
            </el-button>
            <el-button type="warning" @click="openPhotoInput">
              <el-icon><Camera /></el-icon> 拍照记录
            </el-button>
            <el-button type="success" @click="startVoiceRecording" :disabled="!speechSupported">
              <el-icon><Microphone /></el-icon> 语音记录
            </el-button>
            <el-button type="info" @click="analyzeRecords">
              分析记录
            </el-button>
          </div>
        </div>
      </template>

      <!-- 记录概览 -->
      <div v-if="records.length > 0" class="record-overview">
        <el-row :gutter="20">
          <el-col :span="6">
            <div class="overview-item">
              <div class="overview-icon calories">
                <i class="el-icon-fire"></i>
              </div>
              <div class="overview-content">
                <div class="overview-value">{{ totalCalories }}</div>
                <div class="overview-label">总热量(kcal)</div>
              </div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="overview-item">
              <div class="overview-icon protein">
                <i class="el-icon-food"></i>
              </div>
              <div class="overview-content">
                <div class="overview-value">{{ totalProtein }}</div>
                <div class="overview-label">蛋白质(g)</div>
              </div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="overview-item">
              <div class="overview-icon carbs">
                <i class="el-icon-grape"></i>
              </div>
              <div class="overview-content">
                <div class="overview-value">{{ totalCarbs }}</div>
                <div class="overview-label">碳水化合物(g)</div>
              </div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="overview-item">
              <div class="overview-icon fat">
                <i class="el-icon-dish"></i>
              </div>
              <div class="overview-content">
                <div class="overview-value">{{ totalFat }}</div>
                <div class="overview-label">脂肪(g)</div>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 记录列表 -->
      <div v-if="records.length > 0" class="record-list">
        <el-card v-for="group in groupMeals" :key="group.type" class="meal-card">
          <template #header>
            <div class="meal-header">
              <h3>{{ group.label }}</h3>
              <div class="meal-summary">
                <span class="meal-calories">{{ getMealCalories(group.items) }} kcal</span>
                <el-button size="small" @click="showAddDialog(group.type)">添加</el-button>
              </div>
            </div>
          </template>
          <el-table :data="group.items" border>
            <el-table-column prop="foodName" label="食材" />
            <el-table-column prop="amount" label="重量(g)" width="120" />
            <el-table-column label="操作" width="120">
              <template #default="scope">
                <el-button type="danger" size="small" @click="deleteRecord(scope.row.id)">
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </div>

      <!-- 空状态 -->
      <div v-else class="empty-state">
        <el-empty description="暂无饮食记录，请添加记录">
          <el-button type="primary" @click="showAddDialog">添加记录</el-button>
        </el-empty>
      </div>
    </el-card>

    <!-- 添加记录对话框 -->
    <el-dialog v-model="dialogVisible" title="添加饮食记录" width="500px">
      <el-form :model="recordForm" label-width="100px">
        <el-form-item label="食材名称">
          <el-autocomplete
              v-model="recordForm.foodName"
              :fetch-suggestions="searchFoods"
              placeholder="请输入食材名称或点击输入框查看所有食材"
              style="width: 100%"
              @select="handleFoodSelect"
              :trigger-on-focus="true"
              clearable
              :debounce="300"
              value-key="value"
              popper-class="food-autocomplete-popper"
          >
            <template #default="{ item }">
              <div class="food-suggestion-item">
                <span class="food-name">{{ item.value }}</span>
                <span class="food-category" v-if="item.food?.category">[{{ item.food.category }}]</span>
              </div>
            </template>
          </el-autocomplete>
          <div v-if="selectedFood" class="selected-food-info">
            <el-tag type="success" size="small">
              已选择：{{ selectedFood.name }}
            </el-tag>
            <span class="food-nutrition">
              热量：{{ selectedFood.calories }}kcal/100g | 
              蛋白质：{{ selectedFood.protein }}g/100g
            </span>
          </div>
          <div v-if="!selectedFood && recordForm.foodName" class="food-hint">
            <el-text type="info" size="small">
              将为您手动添加新食材：{{ recordForm.foodName }}
            </el-text>
          </div>
        </el-form-item>
        <el-form-item label="重量(g)">
          <el-input-number v-model="recordForm.amount" :min="1" :max="10000" style="width: 100%" />
        </el-form-item>
        <el-form-item label="餐次">
          <el-select v-model="recordForm.mealType" style="width: 100%">
            <el-option label="早餐" value="breakfast" />
            <el-option label="上午加餐" value="morning_snack" />
            <el-option label="午餐" value="lunch" />
            <el-option label="下午加餐" value="afternoon_snack" />
            <el-option label="晚餐" value="dinner" />
            <el-option label="晚上加餐" value="evening_snack" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="addRecord" :loading="loading">确定</el-button>
      </template>
    </el-dialog>

    <!-- Hidden photo input -->
    <input ref="photoInputRef" type="file" accept="image/*" capture="environment"
      style="display: none" @change="handlePhotoSelected" />

    <!-- Voice recording dialog -->
    <el-dialog v-model="showVoiceDialog" title="语音输入" width="420px" :close-on-click-modal="false">
      <div class="voice-container">
        <div class="voice-icon" :class="{ active: isListening }">
          <el-icon :size="52"><Microphone /></el-icon>
        </div>
        <p class="voice-hint">{{ isListening ? '正在聆听，请说出您吃了什么...' : '准备录音' }}</p>
        <p v-if="interimTranscript" class="voice-interim">{{ interimTranscript }}</p>
        <p v-if="transcript" class="voice-final">{{ transcript }}</p>
        <p v-if="voiceError" class="voice-error">{{ voiceError }}</p>
      </div>
      <template #footer>
        <el-button @click="cancelVoice">取消</el-button>
        <el-button v-if="isListening" type="danger" @click="stopVoiceRecording">
          停止录音
        </el-button>
        <el-button v-if="transcript && !isListening" type="primary" @click="submitVoiceText">
          分析内容
        </el-button>
        <el-button v-if="!isListening && !transcript" type="success" @click="restartVoice">
          重新录音
        </el-button>
      </template>
    </el-dialog>

    <!-- AI Recognition Result Dialog -->
    <FoodRecognitionDialog ref="recognitionDialogRef" @saved="onRecognitionSaved" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue'
import api from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Camera, Microphone } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import { useRouter } from 'vue-router'
import { useSpeechRecognition } from '@/composables/useSpeechRecognition'
import FoodRecognitionDialog from '@/components/FoodRecognitionDialog.vue'

const router = useRouter()

const today = dayjs().format('YYYY-MM-DD')
const selectedDate = ref(today)
const records = ref<any[]>([])
const dialogVisible = ref(false)
const loading = ref(false)
const foods = ref<any[]>([])
const selectedFood = ref<any>(null)
const foodsLoaded = ref(false)

// Photo & Voice recognition
const { isListening, transcript, interimTranscript, error: voiceError, isSupported: speechSupported, start, stop, reset } = useSpeechRecognition()
const photoInputRef = ref<HTMLInputElement>()
const recognitionDialogRef = ref<InstanceType<typeof FoodRecognitionDialog>>()
const showVoiceDialog = ref(false)

const recordForm = ref({
  foodName: '',
  amount: 100,
  mealType: 'breakfast'
})

const groupMeals = computed(() => {
  const groups: { [key: string]: { label: string; type: string; items: any[] } } = {
    breakfast: { label: '早餐', type: 'breakfast', items: [] },
    morning_snack: { label: '上午加餐', type: 'morning_snack', items: [] },
    lunch: { label: '午餐', type: 'lunch', items: [] },
    afternoon_snack: { label: '下午加餐', type: 'afternoon_snack', items: [] },
    dinner: { label: '晚餐', type: 'dinner', items: [] },
    evening_snack: { label: '晚上加餐', type: 'evening_snack', items: [] }
  }

  records.value.forEach(record => {
    if (groups[record.mealType]) {
      groups[record.mealType].items.push(record)
    }
  })

  return Object.values(groups).filter(group => group.items.length > 0)
})

const totalCalories = computed(() => {
  return records.value.reduce((sum, record) => sum + (record.calories || 0), 0).toFixed(1)
})

const totalProtein = computed(() => {
  return records.value.reduce((sum, record) => sum + (record.protein || 0), 0).toFixed(1)
})

const totalCarbs = computed(() => {
  return records.value.reduce((sum, record) => sum + (record.carbs || 0), 0).toFixed(1)
})

const totalFat = computed(() => {
  return records.value.reduce((sum, record) => sum + (record.fat || 0), 0).toFixed(1)
})

const loadRecords = async () => {
  const date = dayjs(selectedDate.value).format('YYYY-MM-DD')
  try {
    const response = await api.get(`/record/${date}`)
    records.value = response.data
  } catch (error) {
    console.error('加载记录失败:', error)
    records.value = []
  }
}

const loadFoods = async () => {
  try {
    console.log('=== 开始加载食材数据 ===')
    console.log('API请求URL: /foods')
    const response = await api.get('/foods')
    console.log('API响应状态:', response.status)
    console.log('API响应数据:', response.data)

    foods.value = response.data || []
    foodsLoaded.value = true
    console.log('食材数据设置完成，数量:', foods.value.length)
    console.log('前3条食材数据:', foods.value.slice(0, 3))

    // 验证数据格式
    if (foods.value.length > 0) {
      const firstFood = foods.value[0]
      console.log('第一条食材数据结构:', {
        id: firstFood.id,
        name: firstFood.name,
        category: firstFood.category,
        calories: firstFood.calories
      })
    }
  } catch (error) {
    console.error('=== 加载食材失败 ===')
    console.error('错误详情:', error)
    console.error('错误响应:', error.response)
    foodsLoaded.value = false
    ElMessage.error('加载食材数据失败，请刷新页面重试')
  }
}

const searchFoods = (queryString: string, callback: Function) => {
  console.log('=== 搜索食材调试信息 ===')
  console.log('查询字符串:', queryString)
  console.log('食材数据长度:', foods.value.length)

  if (!foods.value || foods.value.length === 0) {
    console.log('食材数据为空')
    callback([])
    return
  }

  // 如果没有查询字符串，显示前20个食材
  let results = foods.value
  if (queryString && queryString.trim()) {
    results = foods.value.filter(food =>
        food.name.toLowerCase().includes(queryString.toLowerCase())
    )
  } else {
    // 返回前20个结果
    results = foods.value.slice(0, 20)
  }

  const formattedResults = results.map(food => ({
    value: food.name,
    food: food
  }))

  console.log('搜索结果数量:', formattedResults.length)
  callback(formattedResults)
}

const handleFoodSelect = (item: any) => {
  selectedFood.value = item.food
}

const getMealCalories = (items: any[]) => {
  return items.reduce((sum, item) => sum + (item.calories || 0), 0).toFixed(1)
}

const showAddDialog = (mealType?: string) => {
  if (!foodsLoaded.value) {
    ElMessage.warning('食材数据正在加载中，请稍候...')
    return
  }

  recordForm.value = {
    foodName: '',
    amount: 100,
    mealType: mealType || 'breakfast'
  }
  selectedFood.value = null
  dialogVisible.value = true
}

const addRecord = async () => {
  if (!recordForm.value.foodName || !recordForm.value.amount) {
    ElMessage.warning('请填写完整信息')
    return
  }

  loading.value = true
  const date = dayjs(selectedDate.value).format('YYYY-MM-DD')

  try {
    const recordData = {
      ...recordForm.value,
      date,
      foodId: selectedFood.value ? selectedFood.value.id : null
    }
    await api.post('/record', recordData)
    ElMessage.success('添加成功')
    dialogVisible.value = false
    await loadRecords()
  } catch (error: any) {
    ElMessage.error(error.response?.data || '添加失败')
  } finally {
    loading.value = false
  }
}

const deleteRecord = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这条记录吗？', '确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await api.delete(`/record/${id}`)
    ElMessage.success('删除成功')
    await loadRecords()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data || '删除失败')
    }
  }
}

const analyzeRecords = () => {
  router.push('/analysis')
}

// === Photo Recognition ===
const openPhotoInput = () => {
  photoInputRef.value?.click()
}

const handlePhotoSelected = async (e: Event) => {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return

  if (file.size > 10 * 1024 * 1024) {
    ElMessage.warning('图片大小不能超过10MB')
    input.value = ''
    return
  }

  const mealType = inferMealType()
  recognitionDialogRef.value?.openWithPhoto(file, mealType)
  input.value = ''
}

// === Voice Recognition ===
const startVoiceRecording = () => {
  showVoiceDialog.value = true
  reset()
  nextTick(() => start())
}

const stopVoiceRecording = () => {
  stop()
}

const cancelVoice = () => {
  stop()
  showVoiceDialog.value = false
  reset()
}

const restartVoice = () => {
  reset()
  nextTick(() => start())
}

const submitVoiceText = () => {
  const text = transcript.value
  showVoiceDialog.value = false
  reset()
  if (text.trim()) {
    recognitionDialogRef.value?.openWithVoiceText(text)
  }
}

const onRecognitionSaved = () => {
  loadRecords()
}

const inferMealType = (): string => {
  const hour = new Date().getHours()
  if (hour < 10) return 'breakfast'
  if (hour < 14) return 'lunch'
  if (hour < 17) return 'afternoon_snack'
  return 'dinner'
}

onMounted(() => {
  loadRecords()
  loadFoods()
})
</script>

<style scoped>
.record-container {
  padding: 20px;
}

h2 {
  margin: 0;
  color: #2d8659;
  font-weight: 600;
}

h3 {
  margin: 0;
  color: #5a7c65;
  font-weight: 500;
}

.record-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.record-overview {
  margin-bottom: 30px;
}

.overview-item {
  display: flex;
  align-items: center;
  padding: 20px;
  background: linear-gradient(135deg, #f0f9f6 0%, #e8f5e8 100%);
  border-radius: 12px;
  border: 1px solid rgba(168, 230, 207, 0.3);
  transition: all 0.3s ease;
}

.overview-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.overview-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  font-size: 24px;
  color: white;
}

.overview-icon.calories {
  background: linear-gradient(135deg, #ff6b6b, #ff8e8e);
}

.overview-icon.protein {
  background: linear-gradient(135deg, #4facfe, #6bb6ff);
}

.overview-icon.carbs {
  background: linear-gradient(135deg, #67c23a, #85ce61);
}

.overview-icon.fat {
  background: linear-gradient(135deg, #e6a23c, #ebb563);
}

.overview-content {
  flex: 1;
}

.overview-value {
  font-size: 24px;
  font-weight: 600;
  color: #2d8659;
  margin-bottom: 5px;
}

.overview-label {
  font-size: 14px;
  color: #5a7c65;
}

.record-list {
  margin-top: 20px;
}

.meal-card {
  margin-bottom: 20px;
}

.meal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.meal-summary {
  display: flex;
  align-items: center;
  gap: 15px;
}

.meal-calories {
  font-weight: 600;
  color: #4facfe;
  font-size: 16px;
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

:deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
}

:deep(.el-table__header) {
  background-color: #f0f9f6;
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

.selected-food-info {
  margin-top: 8px;
  padding: 8px;
  background-color: #f0f9ff;
  border-radius: 6px;
  border-left: 3px solid #409eff;
}

.food-nutrition {
  margin-left: 10px;
  font-size: 12px;
  color: #606266;
}

.food-hint {
  margin-top: 8px;
  padding: 8px;
  background-color: #fdf6ec;
  border-radius: 6px;
  border-left: 3px solid #e6a23c;
}

/* 自动完成下拉框样式 */
:deep(.food-autocomplete-popper) {
  max-height: 300px;
  overflow-y: auto;
}

:deep(.food-autocomplete-popper .el-autocomplete-suggestion__item) {
  padding: 8px 12px;
  border-bottom: 1px solid #f0f0f0;
}

:deep(.food-autocomplete-popper .el-autocomplete-suggestion__item:hover) {
  background-color: #f0f9ff;
}

.food-suggestion-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.food-name {
  font-weight: 500;
  color: #2d8659;
}

.food-category {
  font-size: 12px;
  color: #909399;
}

:deep(.el-input-number) {
  width: 100%;
}

:deep(.el-select) {
  width: 100%;
}

:deep(.el-autocomplete) {
  width: 100%;
}

:deep(.el-date-editor) {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .record-header {
    flex-direction: column;
    align-items: stretch;
    gap: 15px;
  }

  .header-actions {
    justify-content: center;
    flex-wrap: wrap;
  }

  .overview-item {
    padding: 15px;
  }

  .overview-icon {
    width: 40px;
    height: 40px;
    font-size: 20px;
    margin-right: 10px;
  }

  .overview-value {
    font-size: 20px;
  }

  .overview-label {
    font-size: 12px;
  }

  .meal-header {
    flex-direction: column;
    align-items: stretch;
    gap: 10px;
  }

  .meal-summary {
    justify-content: space-between;
  }
}

@media (max-width: 480px) {
  .record-container {
    padding: 10px;
  }

  .overview-item {
    padding: 10px;
  }

  .overview-icon {
    width: 35px;
    height: 35px;
    font-size: 18px;
    margin-right: 8px;
  }

  .overview-value {
    font-size: 18px;
  }

  .overview-label {
    font-size: 11px;
  }

  .meal-calories {
    font-size: 14px;
  }
}

/* Voice recording styles */
.voice-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 0;
}

.voice-icon {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f0f0;
  color: #909399;
  transition: all 0.3s;
}

.voice-icon.active {
  background: linear-gradient(135deg, #ff6b6b, #ff8e8e);
  color: white;
  animation: pulse 1.5s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); box-shadow: 0 0 0 0 rgba(255, 107, 107, 0.4); }
  50% { transform: scale(1.05); box-shadow: 0 0 0 15px rgba(255, 107, 107, 0); }
}

.voice-hint {
  margin-top: 16px;
  color: #606266;
  font-size: 14px;
}

.voice-interim {
  margin-top: 12px;
  color: #909399;
  font-size: 13px;
  font-style: italic;
  max-width: 100%;
  word-break: break-all;
}

.voice-final {
  margin-top: 8px;
  color: #2d8659;
  font-size: 15px;
  font-weight: 500;
  max-width: 100%;
  word-break: break-all;
}

.voice-error {
  margin-top: 8px;
  color: #f56c6c;
  font-size: 13px;
}
</style>


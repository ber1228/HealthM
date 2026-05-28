<template>
  <el-dialog v-model="visible" title="AI 智能识别" width="700px" :close-on-click-modal="false">
    <!-- Loading state -->
    <div v-if="loading" class="loading-container">
      <el-icon class="is-loading" :size="48" color="#409eff"><Loading /></el-icon>
      <p class="loading-text">{{ loadingText }}</p>
    </div>

    <!-- Results -->
    <div v-else>
      <el-alert v-if="foods.length > 0" type="info" :closable="false" style="margin-bottom: 16px">
        AI 已识别出 {{ foods.length }} 种食物，请确认或修改后保存
      </el-alert>

      <el-table v-if="foods.length > 0" :data="foods" border style="width: 100%">
        <el-table-column label="食物名称" min-width="150">
          <template #default="{ row }">
            <el-input v-model="row.name" size="small" />
          </template>
        </el-table-column>
        <el-table-column label="重量(g)" width="110">
          <template #default="{ row }">
            <el-input-number v-model="row.weight" :min="1" :max="5000" size="small" controls-position="right" />
          </template>
        </el-table-column>
        <el-table-column label="匹配" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.matched ? 'success' : 'warning'" size="small">
              {{ row.matched ? '已匹配' : '未匹配' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="热量" width="90" align="center">
          <template #default="{ row }">
            <span>{{ row.nutrition?.calories ? Math.round(row.nutrition.calories) : '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="60" align="center">
          <template #default="{ $index }">
            <el-button type="danger" link size="small" @click="foods.splice($index, 1)">
              <el-icon><Delete /></el-icon>
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-else description="未能识别出食物，请重试或手动添加" />

      <!-- Meal type selector -->
      <div v-if="foods.length > 0" class="meal-type-row">
        <span class="meal-label">餐次：</span>
        <el-radio-group v-model="mealType" size="small">
          <el-radio-button value="breakfast">早餐</el-radio-button>
          <el-radio-button value="lunch">午餐</el-radio-button>
          <el-radio-button value="dinner">晚餐</el-radio-button>
          <el-radio-button value="snack">加餐</el-radio-button>
        </el-radio-group>
      </div>

      <!-- Date picker -->
      <div v-if="foods.length > 0" class="date-row">
        <span class="meal-label">日期：</span>
        <el-date-picker v-model="selectedDate" type="date" size="small" format="YYYY-MM-DD"
          value-format="YYYY-MM-DD" style="width: 160px" />
      </div>

      <!-- Add food manually -->
      <el-button v-if="foods.length > 0" text type="primary" size="small" style="margin-top: 8px"
        @click="addEmptyFood">
        + 手动添加食物
      </el-button>
    </div>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :disabled="foods.length === 0" :loading="saving" @click="handleConfirm">
        确认保存 ({{ foods.length }})
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Loading, Delete } from '@element-plus/icons-vue'
import { foodRecognitionApi } from '@/api'
import dayjs from 'dayjs'

const emit = defineEmits(['saved'])

const visible = ref(false)
const loading = ref(false)
const saving = ref(false)
const loadingText = ref('')
const foods = ref<any[]>([])
const mealType = ref('lunch')
const selectedDate = ref(dayjs().format('YYYY-MM-DD'))

const openWithPhoto = async (file: File, defaultMealType: string) => {
  visible.value = true
  loading.value = true
  loadingText.value = 'AI 正在分析食物图片，请稍候...'
  mealType.value = defaultMealType
  selectedDate.value = dayjs().format('YYYY-MM-DD')
  foods.value = []

  try {
    const res = await foodRecognitionApi.analyzePhoto(file, defaultMealType)
    if (res.data.success) {
      foods.value = res.data.foods || []
      if (res.data.mealType) mealType.value = res.data.mealType
    } else {
      ElMessage.error(res.data.error || '图片分析失败')
    }
  } catch (e: any) {
    ElMessage.error(e.response?.data?.error || '图片分析失败，请重试')
  } finally {
    loading.value = false
  }
}

const openWithVoiceText = async (text: string) => {
  visible.value = true
  loading.value = true
  loadingText.value = 'AI 正在解析语音内容...'
  selectedDate.value = dayjs().format('YYYY-MM-DD')
  foods.value = []

  try {
    const res = await foodRecognitionApi.analyzeVoiceText(text)
    if (res.data.success) {
      foods.value = res.data.foods || []
      if (res.data.mealType) mealType.value = res.data.mealType
    } else {
      ElMessage.error(res.data.error || '语音解析失败')
    }
  } catch (e: any) {
    ElMessage.error(e.response?.data?.error || '语音解析失败，请重试')
  } finally {
    loading.value = false
  }
}

const addEmptyFood = () => {
  foods.value.push({
    name: '',
    weight: 100,
    matched: false,
    nutrition: null,
  })
}

const handleConfirm = async () => {
  // Validate
  const validFoods = foods.value.filter(f => f.name && f.name.trim() && f.weight > 0)
  if (validFoods.length === 0) {
    ElMessage.warning('请至少添加一种食物')
    return
  }

  saving.value = true
  try {
    const res = await foodRecognitionApi.confirmAndSave(validFoods, mealType.value, selectedDate.value)
    if (res.data.success) {
      ElMessage.success(`成功保存 ${res.data.count || validFoods.length} 条饮食记录`)
      visible.value = false
      emit('saved')
    } else {
      ElMessage.error(res.data.error || '保存失败')
    }
  } catch (e: any) {
    ElMessage.error(e.response?.data?.error || '保存失败，请重试')
  } finally {
    saving.value = false
  }
}

defineExpose({ openWithPhoto, openWithVoiceText })
</script>

<style scoped>
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
}

.loading-text {
  margin-top: 16px;
  color: #606266;
  font-size: 14px;
}

.meal-type-row,
.date-row {
  display: flex;
  align-items: center;
  margin-top: 16px;
}

.meal-label {
  margin-right: 12px;
  font-size: 14px;
  color: #606266;
  white-space: nowrap;
}

:deep(.el-input-number--small) {
  width: 90px;
}

:deep(.el-table .el-input) {
  --el-input-border-color: transparent;
}

:deep(.el-table .el-input:hover) {
  --el-input-border-color: #c0c4cc;
}
</style>

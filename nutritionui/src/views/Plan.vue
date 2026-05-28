<template>
  <div class="plan-container">
    <el-card>
      <h2>饮食方案</h2>

      <!-- 日期选择和操作按钮 -->
      <div class="plan-controls">
        <el-date-picker
            v-model="selectedDate"
            type="date"
            @change="loadPlan"
            style="margin-right: 10px"
        />
        <el-button type="primary" @click="generatePlan">生成方案</el-button>
        <el-button type="success" @click="showRegenerateDialog = true">重新生成</el-button>
        <el-button type="info" @click="showWeeklyPlan = true">周方案</el-button>
      </div>

      <!-- 餐食类型筛选 -->
      <div v-if="plans.length > 0" class="meal-type-filter">
        <el-button-group>
          <el-button
              v-for="type in mealTypes"
              :key="type.value"
              :type="selectedMealType === type.value ? 'primary' : 'default'"
              :class="{'meal-filter-btn': true, 'active': selectedMealType === type.value}"
              @click="selectedMealType = type.value"
          >
            {{ type.icon }} {{ type.label }}
          </el-button>
          <el-button
              type="success"
              :class="{'meal-filter-btn': true, 'active': selectedMealType === 'all'}"
              @click="selectedMealType = 'all'"
          >
            📋 查看全部
          </el-button>
        </el-button-group>
      </div>

      <!-- 方案展示 -->
      <div v-if="plans.length > 0" class="plan-list">
        <el-card v-for="group in groupMeals" :key="group.type" style="margin-bottom: 20px">
          <template #header>
            <div class="meal-header">
              <h3>{{ group.label }}</h3>
              <el-button size="small" @click="showFoodReplaceDialog(group.items[0])">替换食材</el-button>
            </div>
          </template>
          <el-table :data="group.items" border>
            <el-table-column prop="foodName" label="食材" />
            <el-table-column prop="amount" label="重量(g)" />
            <el-table-column label="操作" width="120">
              <template #default="scope">
                <el-button size="small" @click="showFoodReplaceDialog(scope.row)">替换</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </div>

      <!-- 空状态 -->
      <div v-else class="empty-state">
        <el-empty description="暂无饮食方案，请先生成方案" />
      </div>
    </el-card>

    <!-- 重新生成对话框 -->
    <el-dialog v-model="showRegenerateDialog" title="重新生成方案" width="500px">
      <el-form :model="regenerateForm" label-width="100px">
        <el-form-item label="特殊需求">
          <el-input
              v-model="regenerateForm.requirements"
              type="textarea"
              placeholder="请输入您的特殊需求，如：不想吃主食、不想吃肉类、对海鲜过敏等"
              :rows="3"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRegenerateDialog = false">取消</el-button>
        <el-button type="primary" @click="regeneratePlan">重新生成</el-button>
      </template>
    </el-dialog>

    <!-- 食材替换对话框 -->
    <el-dialog v-model="showReplaceDialog" title="替换食材" width="800px">
      <el-tabs v-model="replaceTab">
        <el-tab-pane label="相似食材推荐" name="similar">
          <div v-if="similarFoods.length > 0">
            <h4>基于营养成分相似度推荐</h4>
            <el-table :data="similarFoods" border>
              <el-table-column prop="food.name" label="食材名称" />
              <el-table-column prop="similarity" label="相似度" width="100">
                <template #default="scope">
                  {{ (scope.row.similarity * 100).toFixed(1) }}%
                </template>
              </el-table-column>
              <el-table-column prop="food.calories" label="热量(kcal/100g)" width="120" />
              <el-table-column prop="food.protein" label="蛋白质(g/100g)" width="120" />
              <el-table-column prop="food.carbs" label="碳水(g/100g)" width="120" />
              <el-table-column prop="food.fat" label="脂肪(g/100g)" width="120" />
              <el-table-column label="操作" width="100">
                <template #default="scope">
                  <el-button size="small" @click="selectSimilarFood(scope.row.food)">选择</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
          <div v-else>
            <el-empty description="暂无相似食材推荐" />
          </div>
        </el-tab-pane>
        <el-tab-pane label="自定义替换" name="custom">
          <el-form :model="replaceForm" label-width="100px">
            <el-form-item label="当前食材">
              <el-input v-model="replaceForm.currentFood" disabled />
            </el-form-item>
            <el-form-item label="新食材">
              <el-select v-model="replaceForm.newFoodId" placeholder="请选择新食材" style="width: 100%">
                <el-option
                    v-for="food in foods"
                    :key="food.id"
                    :label="food.name"
                    :value="food.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="重量(g)">
              <el-input-number v-model="replaceForm.newAmount" :min="1" :max="1000" />
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>

      <!-- 营养成分对比 -->
      <div v-if="nutritionChange" class="nutrition-comparison">
        <h4>营养成分变化对比</h4>
        <el-table :data="nutritionChangeData" border>
          <el-table-column prop="nutrient" label="营养成分" />
          <el-table-column prop="change" label="变化量" width="120">
            <template #default="scope">
              <span :class="scope.row.changeClass">
                {{ scope.row.change > 0 ? '+' : '' }}{{ scope.row.change.toFixed(2) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="unit" label="单位" width="80" />
        </el-table>

        <!-- DRI符合性检查 -->
        <div v-if="driCheck" class="dri-check">
          <h4>营养符合性检查</h4>
          <div v-if="driCheck.isCompliant" class="compliant">
            <el-icon><Check /></el-icon>
            <span>替换后的方案符合营养标准</span>
          </div>
          <div v-else class="non-compliant">
            <el-icon><Warning /></el-icon>
            <span>需要注意以下问题：</span>
            <ul>
              <li v-for="warning in driCheck.warnings" :key="warning">{{ warning }}</li>
            </ul>
            <div class="suggestions">
              <strong>建议：</strong>
              <ul>
                <li v-for="suggestion in driCheck.suggestions" :key="suggestion">{{ suggestion }}</li>
              </ul>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="showReplaceDialog = false">取消</el-button>
        <el-button type="primary" @click="replaceFood">确认替换</el-button>
      </template>
    </el-dialog>

    <!-- 周方案对话框 -->
    <el-dialog v-model="showWeeklyPlan" title="周方案管理" width="800px">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="生成周方案" name="generate">
          <el-form :model="weeklyForm" label-width="100px">
            <el-form-item label="开始日期">
              <el-date-picker
                  v-model="weeklyForm.weekStart"
                  type="date"
                  placeholder="选择周开始日期"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="generateWeeklyPlan">生成周方案</el-button>
              <el-button type="info" @click="loadWeeklyPlan">查看周方案</el-button>
              <el-button type="success" @click="applyWeeklyPlan" :disabled="!weeklyForm.weekStart">应用周方案</el-button>
            </el-form-item>
          </el-form>

          <!-- 周方案展示 -->
          <div v-if="weeklyPlanData.length > 0" class="weekly-plan-display">
            <h4>周方案详情</h4>
            <el-collapse v-model="activeCollapse">
              <el-collapse-item
                  v-for="dayPlan in weeklyPlanData"
                  :key="dayPlan.date"
                  :title="`${dayPlan.date} (${getDayOfWeek(dayPlan.date)})`"
                  :name="dayPlan.date"
              >
                <div v-for="group in groupMealsByDate(dayPlan.plans)" :key="group.type" class="meal-group">
                  <h5>{{ group.label }}</h5>
                  <el-table :data="group.items" size="small" border>
                    <el-table-column prop="foodName" label="食材" width="120" />
                    <el-table-column prop="amount" label="重量(g)" width="80" />
                    <el-table-column label="操作" width="100">
                      <template #default="scope">
                        <el-button size="small" @click="replaceWeeklyFood(scope.row, dayPlan.date)">替换</el-button>
                      </template>
                    </el-table-column>
                  </el-table>
                </div>
              </el-collapse-item>
            </el-collapse>
          </div>
        </el-tab-pane>
        <el-tab-pane label="模板管理" name="template">
          <div class="template-section">
            <el-button type="success" @click="showSaveTemplateDialog = true">保存当前周为模板</el-button>
            <el-divider />
            <h4>我的模板</h4>
            <el-table :data="templates" border>
              <el-table-column prop="templateName" label="模板名称" />
              <el-table-column prop="weekStart" label="创建日期" />
              <el-table-column label="操作" width="300">
                <template #default="scope">
                  <el-button size="small" @click="viewTemplateDetail(scope.row)">查看详情</el-button>
                  <el-button size="small" @click="applyTemplate(scope.row)">应用</el-button>
                  <el-button size="small" type="danger" @click="deleteTemplate(scope.row.id)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>

    <!-- 保存模板对话框 -->
    <el-dialog v-model="showSaveTemplateDialog" title="保存模板" width="400px">
      <el-form :model="templateForm" label-width="100px">
        <el-form-item label="模板名称">
          <el-input v-model="templateForm.templateName" placeholder="请输入模板名称" />
        </el-form-item>
        <el-form-item label="周开始日期">
          <el-date-picker
              v-model="templateForm.weekStart"
              type="date"
              placeholder="选择周开始日期"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showSaveTemplateDialog = false">取消</el-button>
        <el-button type="primary" @click="saveTemplate">保存</el-button>
      </template>
    </el-dialog>

    <!-- 模板详情对话框 -->
    <el-dialog v-model="showTemplateDetailDialog" title="模板详情" width="1000px">
      <div v-if="currentTemplate">
        <div class="template-header">
          <h3>{{ currentTemplate.templateName }}</h3>
          <p>创建日期：{{ currentTemplate.weekStart }}</p>
        </div>

        <div v-if="templateDetailData.length > 0" class="template-detail">
          <el-collapse v-model="activeTemplateCollapse">
            <el-collapse-item
                v-for="dayPlan in templateDetailData"
                :key="dayPlan.date"
                :title="`${dayPlan.date} (${getDayOfWeek(dayPlan.date)})`"
                :name="dayPlan.date"
            >
              <div v-for="group in groupMealsByDate(dayPlan.plans)" :key="group.type" class="meal-group">
                <h5>{{ group.label }}</h5>
                <el-table :data="group.items" size="small" border>
                  <el-table-column prop="foodName" label="食材" width="120" />
                  <el-table-column prop="amount" label="重量(g)" width="80" />
                  <el-table-column prop="calories" label="热量(kcal)" width="100" />
                  <el-table-column prop="protein" label="蛋白质(g)" width="100" />
                  <el-table-column prop="carbs" label="碳水(g)" width="100" />
                  <el-table-column prop="fat" label="脂肪(g)" width="100" />
                  <el-table-column label="操作" width="120">
                    <template #default="scope">
                      <el-button size="small" @click="editTemplateFood(scope.row, dayPlan.date)">编辑</el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </el-collapse-item>
          </el-collapse>
        </div>

        <div v-else class="empty-template">
          <el-empty description="模板暂无数据" />
        </div>
      </div>

      <template #footer>
        <el-button @click="showTemplateDetailDialog = false">关闭</el-button>
        <el-button type="primary" @click="editTemplate">编辑模板</el-button>
      </template>
    </el-dialog>

    <!-- 编辑模板食材对话框 -->
    <el-dialog v-model="showEditTemplateDialog" title="编辑模板食材" width="800px">
      <el-form :model="editTemplateForm" label-width="100px">
        <el-form-item label="当前食材">
          <el-input v-model="editTemplateForm.currentFood" disabled />
        </el-form-item>
        <el-form-item label="新食材">
          <el-select v-model="editTemplateForm.newFoodId" placeholder="请选择新食材" style="width: 100%">
            <el-option
                v-for="food in foods"
                :key="food.id"
                :label="food.name"
                :value="food.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="重量(g)">
          <el-input-number v-model="editTemplateForm.newAmount" :min="1" :max="1000" />
        </el-form-item>
      </el-form>

      <!-- 营养成分对比 -->
      <div v-if="templateNutritionChange" class="nutrition-comparison">
        <h4>营养成分变化对比</h4>
        <el-table :data="templateNutritionChangeData" border>
          <el-table-column prop="nutrient" label="营养成分" />
          <el-table-column prop="change" label="变化量" width="120">
            <template #default="scope">
              <span :class="scope.row.changeClass">
                {{ scope.row.change > 0 ? '+' : '' }}{{ scope.row.change.toFixed(2) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="unit" label="单位" width="80" />
        </el-table>

        <!-- DRI符合性检查 -->
        <div v-if="templateDriCheck" class="dri-check">
          <h4>营养符合性检查</h4>
          <div v-if="templateDriCheck.isCompliant" class="compliant">
            <el-icon><Check /></el-icon>
            <span>修改后的模板符合营养标准</span>
          </div>
          <div v-else class="non-compliant">
            <el-icon><Warning /></el-icon>
            <span>需要注意以下问题：</span>
            <ul>
              <li v-for="warning in templateDriCheck.warnings" :key="warning">{{ warning }}</li>
            </ul>
            <div class="suggestions">
              <strong>建议：</strong>
              <ul>
                <li v-for="suggestion in templateDriCheck.suggestions" :key="suggestion">{{ suggestion }}</li>
              </ul>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="showEditTemplateDialog = false">取消</el-button>
        <el-button type="primary" @click="updateTemplateFood">确认修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import api from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'

const today = dayjs().format('YYYY-MM-DD')
const selectedDate = ref(today)
const selectedMealType = ref('all')
const mealTypes = ref([
  { value: 'breakfast', label: '早餐', icon: '🍳' },
  { value: 'morning_snack', label: '上午加餐', icon: '🥨' },
  { value: 'lunch', label: '午餐', icon: '🍱' },
  { value: 'afternoon_snack', label: '下午加餐', icon: '🍎' },
  { value: 'dinner', label: '晚餐', icon: '🍝' },
  { value: 'evening_snack', label: '晚上加餐', icon: '🥛' }
])
const plans = ref<any[]>([])
const foods = ref<any[]>([])
const templates = ref<any[]>([])
const weeklyPlanData = ref<any[]>([])
const activeCollapse = ref<string[]>([])
const similarFoods = ref<any[]>([])
const nutritionChange = ref<any>(null)
const driCheck = ref<any>(null)
const replaceTab = ref('similar')

// 模板相关
const templateDetailData = ref<any[]>([])
const activeTemplateCollapse = ref<string[]>([])
const currentTemplate = ref<any>(null)
const templateNutritionChange = ref<any>(null)
const templateDriCheck = ref<any>(null)

// 对话框状态
const showRegenerateDialog = ref(false)
const showReplaceDialog = ref(false)
const showWeeklyPlan = ref(false)
const showSaveTemplateDialog = ref(false)
const showTemplateDetailDialog = ref(false)
const showEditTemplateDialog = ref(false)
const activeTab = ref('generate')

// 表单数据
const regenerateForm = ref({
  requirements: ''
})

const replaceForm = ref({
  planId: null as number | null,
  currentFood: '',
  newFoodId: null as number | null,
  newAmount: 100,
  date: ''
})

const weeklyForm = ref({
  weekStart: today
})

const templateForm = ref({
  templateName: '',
  weekStart: today
})

const editTemplateForm = ref({
  planId: null as number | null,
  currentFood: '',
  newFoodId: null as number | null,
  newAmount: 100,
  templateId: null as number | null,
  date: ''
})

const groupMeals = computed(() => {
  const filteredPlans = selectedMealType.value === 'all'
      ? plans.value
      : plans.value.filter(plan => plan.mealType === selectedMealType.value)

  const groups: { [key: string]: { label: string; type: string; items: any[] } } = {
    breakfast: { label: '早餐', type: 'breakfast', items: [] },
    morning_snack: { label: '上午加餐', type: 'morning_snack', items: [] },
    lunch: { label: '午餐', type: 'lunch', items: [] },
    afternoon_snack: { label: '下午加餐', type: 'afternoon_snack', items: [] },
    dinner: { label: '晚餐', type: 'dinner', items: [] },
    evening_snack: { label: '晚上加餐', type: 'evening_snack', items: [] }
  }

  filteredPlans.forEach(plan => {
    if (groups[plan.mealType]) {
      groups[plan.mealType].items.push(plan)
    }
  })

  return Object.values(groups).filter(group => group.items.length > 0)
})

// 营养成分变化对比数据
const nutritionChangeData = computed(() => {
  if (!nutritionChange.value) return []

  return [
    {
      nutrient: '热量',
      change: nutritionChange.value.caloriesChange || 0,
      unit: 'kcal',
      changeClass: nutritionChange.value.caloriesChange > 0 ? 'increase' : 'decrease'
    },
    {
      nutrient: '蛋白质',
      change: nutritionChange.value.proteinChange || 0,
      unit: 'g',
      changeClass: nutritionChange.value.proteinChange > 0 ? 'increase' : 'decrease'
    },
    {
      nutrient: '碳水化合物',
      change: nutritionChange.value.carbsChange || 0,
      unit: 'g',
      changeClass: nutritionChange.value.carbsChange > 0 ? 'increase' : 'decrease'
    },
    {
      nutrient: '脂肪',
      change: nutritionChange.value.fatChange || 0,
      unit: 'g',
      changeClass: nutritionChange.value.fatChange > 0 ? 'increase' : 'decrease'
    },
    {
      nutrient: '膳食纤维',
      change: nutritionChange.value.fiberChange || 0,
      unit: 'g',
      changeClass: nutritionChange.value.fiberChange > 0 ? 'increase' : 'decrease'
    },
    {
      nutrient: '钙',
      change: nutritionChange.value.calciumChange || 0,
      unit: 'mg',
      changeClass: nutritionChange.value.calciumChange > 0 ? 'increase' : 'decrease'
    },
    {
      nutrient: '铁',
      change: nutritionChange.value.ironChange || 0,
      unit: 'mg',
      changeClass: nutritionChange.value.ironChange > 0 ? 'increase' : 'decrease'
    },
    {
      nutrient: '维生素C',
      change: nutritionChange.value.vitCChange || 0,
      unit: 'mg',
      changeClass: nutritionChange.value.vitCChange > 0 ? 'increase' : 'decrease'
    }
  ]
})

// 模板营养成分变化对比数据
const templateNutritionChangeData = computed(() => {
  if (!templateNutritionChange.value) return []

  return [
    {
      nutrient: '热量',
      change: templateNutritionChange.value.caloriesChange || 0,
      unit: 'kcal',
      changeClass: templateNutritionChange.value.caloriesChange > 0 ? 'increase' : 'decrease'
    },
    {
      nutrient: '蛋白质',
      change: templateNutritionChange.value.proteinChange || 0,
      unit: 'g',
      changeClass: templateNutritionChange.value.proteinChange > 0 ? 'increase' : 'decrease'
    },
    {
      nutrient: '碳水化合物',
      change: templateNutritionChange.value.carbsChange || 0,
      unit: 'g',
      changeClass: templateNutritionChange.value.carbsChange > 0 ? 'increase' : 'decrease'
    },
    {
      nutrient: '脂肪',
      change: templateNutritionChange.value.fatChange || 0,
      unit: 'g',
      changeClass: templateNutritionChange.value.fatChange > 0 ? 'increase' : 'decrease'
    },
    {
      nutrient: '膳食纤维',
      change: templateNutritionChange.value.fiberChange || 0,
      unit: 'g',
      changeClass: templateNutritionChange.value.fiberChange > 0 ? 'increase' : 'decrease'
    },
    {
      nutrient: '钙',
      change: templateNutritionChange.value.calciumChange || 0,
      unit: 'mg',
      changeClass: templateNutritionChange.value.calciumChange > 0 ? 'increase' : 'decrease'
    },
    {
      nutrient: '铁',
      change: templateNutritionChange.value.ironChange || 0,
      unit: 'mg',
      changeClass: templateNutritionChange.value.ironChange > 0 ? 'increase' : 'decrease'
    },
    {
      nutrient: '维生素C',
      change: templateNutritionChange.value.vitCChange || 0,
      unit: 'mg',
      changeClass: templateNutritionChange.value.vitCChange > 0 ? 'increase' : 'decrease'
    }
  ]
})

const generatePlan = async () => {
  const date = dayjs(selectedDate.value).format('YYYY-MM-DD')
  console.log('开始生成方案，日期:', date)
  try {
    const response = await api.post(`/plan/generate/${date}`)
    console.log('方案生成成功，返回数据:', response.data)
    ElMessage.success('方案生成成功')
    await loadPlan()
  } catch (error: any) {
    console.error('生成方案失败:', error)
    console.error('错误详情:', error.response)
    ElMessage.error(error.response?.data || '生成失败')
  }
}

const regeneratePlan = async () => {
  const date = dayjs(selectedDate.value).format('YYYY-MM-DD')
  try {
    await api.post(`/plan/regenerate/${date}`, {
      requirements: regenerateForm.value.requirements
    })
    ElMessage.success('方案重新生成成功')
    showRegenerateDialog.value = false
    regenerateForm.value.requirements = ''
    await loadPlan()
  } catch (error: any) {
    ElMessage.error(error.response?.data || '重新生成失败')
  }
}

const loadPlan = async () => {
  const date = dayjs(selectedDate.value).format('YYYY-MM-DD')
  try {
    console.log('加载方案，日期:', date)
    const response = await api.get(`/plan/${date}`)
    console.log('加载方案成功，返回数据:', response.data)
    plans.value = response.data || []
    console.log('方案列表:', plans.value)
  } catch (error: any) {
    console.error('加载方案失败:', error)
    console.error('错误详情:', error.response)
    plans.value = []
  }
}

const loadFoods = async () => {
  try {
    const response = await api.get('/foods')
    foods.value = response.data
  } catch (error) {
    console.error('加载食材失败:', error)
  }
}

const loadTemplates = async () => {
  try {
    const response = await api.get('/plan/templates')
    templates.value = response.data
  } catch (error) {
    console.error('加载模板失败:', error)
  }
}

const showFoodReplaceDialog = async (plan: any) => {
  replaceForm.value.planId = plan.id
  replaceForm.value.currentFood = plan.foodName
  replaceForm.value.newFoodId = null
  replaceForm.value.newAmount = plan.amount

  // 加载相似食材推荐
  try {
    const response = await api.get(`/plan/similar-foods/${plan.foodId}`)
    similarFoods.value = response.data || []
  } catch (error) {
    console.error('加载相似食材失败:', error)
    similarFoods.value = []
  }

  nutritionChange.value = null
  replaceTab.value = 'similar'
  showReplaceDialog.value = true
}

const selectSimilarFood = (food: any) => {
  replaceForm.value.newFoodId = food.id
  replaceForm.value.newAmount = replaceForm.value.newAmount
  replaceTab.value = 'custom'
}

const replaceFood = async () => {
  try {
    const response = await api.post('/plan/replace-food', {
      planId: replaceForm.value.planId,
      newFoodId: replaceForm.value.newFoodId,
      newAmount: replaceForm.value.newAmount,
      date: replaceForm.value.date // 添加日期参数用于周方案替换
    })

    // 显示营养成分变化和DRI检查结果
    nutritionChange.value = response.data.nutritionChange
    driCheck.value = response.data.driCheck

    // 根据DRI检查结果显示不同的消息和详细反馈
    if (driCheck.value.isCompliant) {
      ElMessage.success({
        message: '食材替换成功，营养搭配合理！',
        duration: 3000
      })
    } else {
      // 显示详细的警告信息
      const warningCount = driCheck.value.warnings ? driCheck.value.warnings.length : 0
      const suggestionCount = driCheck.value.suggestions ? driCheck.value.suggestions.length : 0

      ElMessageBox.alert(
          `食材替换成功，但发现 ${warningCount} 个营养问题需要注意：\n\n` +
          (driCheck.value.warnings ? driCheck.value.warnings.map((w: string) => `• ${w}`).join('\n') : '') +
          (suggestionCount > 0 ? `\n\n建议：\n${driCheck.value.suggestions.map((s: string) => `• ${s}`).join('\n')}` : ''),
          '营养搭配提醒',
          {
            confirmButtonText: '我知道了',
            type: 'warning',
            dangerouslyUseHTMLString: false
          }
      )
    }

    showReplaceDialog.value = false

    // 如果是周方案替换，重新加载周方案；否则重新加载日方案
    if (replaceForm.value.date) {
      await loadWeeklyPlan()
    } else {
      await loadPlan()
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data || '替换失败')
  }
}

const generateWeeklyPlan = async () => {
  const weekStart = dayjs(weeklyForm.value.weekStart).format('YYYY-MM-DD')
  try {
    await api.post(`/plan/weekly/generate/${weekStart}`)
    ElMessage.success('周方案生成成功')
    // 生成成功后自动加载周方案
    await loadWeeklyPlan()
  } catch (error: any) {
    ElMessage.error(error.response?.data || '生成失败')
  }
}

const loadWeeklyPlan = async () => {
  const weekStart = dayjs(weeklyForm.value.weekStart).format('YYYY-MM-DD')
  try {
    const response = await api.get(`/plan/weekly/${weekStart}`)
    weeklyPlanData.value = response.data.dailyPlans || []
    console.log('周方案数据:', weeklyPlanData.value)
  } catch (error: any) {
    console.error('加载周方案失败:', error)
    ElMessage.error(error.response?.data || '加载失败')
  }
}

const getDayOfWeek = (dateStr: string) => {
  const days = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  const date = new Date(dateStr)
  return days[date.getDay()]
}


const groupMealsByDate = (plans: any[]) => {
  const mealTypes = {
    breakfast: '早餐',
    morning_snack: '上午加餐',
    lunch: '午餐',
    afternoon_snack: '下午加餐',
    dinner: '晚餐',
    evening_snack: '晚上加餐'
  }

  const groups: any[] = []
  Object.keys(mealTypes).forEach(type => {
    const items = plans.filter(plan => plan.mealType === type)
    if (items.length > 0) {
      groups.push({
        type,
        label: mealTypes[type as keyof typeof mealTypes],
        items
      })
    }
  })

  return groups
}

const saveTemplate = async () => {
  const weekStart = dayjs(templateForm.value.weekStart).format('YYYY-MM-DD')
  try {
    await api.post('/plan/weekly/save-template', {
      weekStart,
      templateName: templateForm.value.templateName
    })
    ElMessage.success('模板保存成功')
    showSaveTemplateDialog.value = false
    templateForm.value.templateName = ''
    await loadTemplates()
  } catch (error: any) {
    ElMessage.error(error.response?.data || '保存失败')
  }
}

const applyTemplate = async (template: any) => {
  try {
    await ElMessageBox.confirm('确定要应用此模板吗？这将覆盖当前周的方案。', '确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const weekStart = dayjs().startOf('week').format('YYYY-MM-DD')
    await api.post('/plan/weekly/apply-template', {
      templateId: template.id,
      weekStart
    })
    ElMessage.success('模板应用成功')

    // 应用成功后自动加载周方案
    weeklyForm.value.weekStart = weekStart
    await loadWeeklyPlan()

    showWeeklyPlan.value = false
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data || '应用失败')
    }
  }
}

const deleteTemplate = async (templateId: number) => {
  try {
    await ElMessageBox.confirm('确定要删除此模板吗？', '确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await api.delete(`/plan/templates/${templateId}`)
    ElMessage.success('模板删除成功')
    await loadTemplates()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data || '删除失败')
    }
  }
}

// 查看模板详情
const viewTemplateDetail = async (template: any) => {
  currentTemplate.value = template
  try {
    const response = await api.get(`/plan/templates/${template.id}/detail`)
    templateDetailData.value = response.data.dailyPlans || []
    showTemplateDetailDialog.value = true
  } catch (error: any) {
    ElMessage.error(error.response?.data || '加载模板详情失败')
  }
}

// 编辑模板
const editTemplate = () => {
  showTemplateDetailDialog.value = false
  showEditTemplateDialog.value = true
}

// 编辑模板食材
const editTemplateFood = (plan: any, date: string) => {
  editTemplateForm.value = {
    planId: plan.id,
    currentFood: plan.foodName,
    newFoodId: plan.foodId,
    newAmount: plan.amount,
    templateId: currentTemplate.value.id,
    date: date
  }
  showEditTemplateDialog.value = true
}

// 更新模板食材
const updateTemplateFood = async () => {
  try {
    const response = await api.post('/plan/templates/update-food', {
      planId: editTemplateForm.value.planId,
      newFoodId: editTemplateForm.value.newFoodId,
      newAmount: editTemplateForm.value.newAmount,
      templateId: editTemplateForm.value.templateId,
      date: editTemplateForm.value.date
    })

    // 显示营养成分变化和DRI检查结果
    templateNutritionChange.value = response.data.nutritionChange
    templateDriCheck.value = response.data.driCheck

    // 根据DRI检查结果显示不同的消息和详细反馈
    if (templateDriCheck.value.isCompliant) {
      ElMessage.success({
        message: '模板食材修改成功，营养搭配合理！',
        duration: 3000
      })
    } else {
      // 显示详细的警告信息
      const warningCount = templateDriCheck.value.warnings ? templateDriCheck.value.warnings.length : 0
      const suggestionCount = templateDriCheck.value.suggestions ? templateDriCheck.value.suggestions.length : 0

      ElMessageBox.alert(
          `模板食材修改成功，但发现 ${warningCount} 个营养问题需要注意：\n\n` +
          (templateDriCheck.value.warnings ? templateDriCheck.value.warnings.map((w: string) => `• ${w}`).join('\n') : '') +
          (suggestionCount > 0 ? `\n\n建议：\n${templateDriCheck.value.suggestions.map((s: string) => `• ${s}`).join('\n')}` : ''),
          '模板营养搭配提醒',
          {
            confirmButtonText: '我知道了',
            type: 'warning',
            dangerouslyUseHTMLString: false
          }
      )
    }

    showEditTemplateDialog.value = false

    // 重新加载模板详情
    await viewTemplateDetail(currentTemplate.value)
  } catch (error: any) {
    ElMessage.error(error.response?.data || '修改失败')
  }
}

// 应用周方案到每日
const applyWeeklyPlan = async () => {
  if (!weeklyForm.value.weekStart) {
    ElMessage.warning('请先选择周开始日期')
    return
  }

  try {
    await ElMessageBox.confirm('确定要应用此周方案吗？这将覆盖对应日期的现有方案。', '确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const weekStart = dayjs(weeklyForm.value.weekStart).format('YYYY-MM-DD')
    await api.post('/plan/weekly/apply', {
      weekStart
    })

    ElMessage.success('周方案应用成功')
    showWeeklyPlan.value = false

    // 刷新当前日期的方案
    await loadPlan()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data || '应用失败')
    }
  }
}

// 替换周方案中的食材
const replaceWeeklyFood = (plan: any, date: string) => {
  replaceForm.value = {
    currentFood: plan.foodName,
    newFoodId: plan.foodId,
    newAmount: plan.amount,
    planId: plan.id,
    date: date
  }
  showReplaceDialog.value = true
}

onMounted(() => {
  loadPlan()
  loadFoods()
  loadTemplates()
})
</script>

<style scoped>
/* 全局动画定义 */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateX(-20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

/* 页面容器 */
.plan-container {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
  min-height: calc(100vh - 120px);
  background: linear-gradient(135deg, #f5f7fa 0%, #e4eaf0 100%);
  animation: fadeInUp 0.6s ease-out;
}

/* 主卡片样式 */
:deep(.el-card) {
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: none;
  margin-bottom: 24px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: #fff;
  animation: fadeInUp 0.6s ease-out;
  animation-fill-mode: both;
}

:deep(.el-card:nth-child(1)) { animation-delay: 0.1s; }
:deep(.el-card:nth-child(2)) { animation-delay: 0.2s; }
:deep(.el-card:nth-child(3)) { animation-delay: 0.3s; }
:deep(.el-card:nth-child(4)) { animation-delay: 0.4s; }
:deep(.el-card:nth-child(n+5)) { animation-delay: 0.5s; }

:deep(.el-card:hover) {
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
  transform: translateY(-4px);
}

/* 页面标题 */
h2 {
  color: #2c3e50;
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 24px;
  padding-bottom: 12px;
  border-bottom: 3px solid #3498db;
  display: inline-block;
  animation: slideIn 0.5s ease-out;
  position: relative;
  overflow: hidden;
}

h2::after {
  content: '';
  position: absolute;
  left: 0;
  bottom: -3px;
  height: 3px;
  width: 0;
  background: linear-gradient(90deg, #3498db, #2ecc71);
  transition: width 0.3s ease;
}

h2:hover::after {
  width: 100%;
}

/* 计划控制区 */
.plan-controls {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
  flex-wrap: wrap;
  align-items: center;
  animation: fadeInUp 0.5s ease-out 0.2s both;
}

/* 餐食类型筛选 */
.meal-type-filter {
  margin-bottom: 24px;
  animation: fadeInUp 0.5s ease-out 0.3s both;
}

.meal-filter-btn {
  border-radius: 20px !important;
  margin: 0 8px 8px 0 !important;
  transition: all 0.3s ease !important;
}

.meal-filter-btn:hover {
  transform: translateY(-2px) !important;
}

.meal-filter-btn.active {
  transform: translateY(-3px) !important;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15) !important;
}

/* 按钮样式优化 */
:deep(.el-button) {
  border-radius: 25px;
  font-weight: 600;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  padding: 10px 24px;
  font-size: 14px;
  position: relative;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

:deep(.el-button::before) {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s ease;
}

:deep(.el-button:hover::before) {
  left: 100%;
}

:deep(.el-button:hover) {
  transform: translateY(-3px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.18);
}

:deep(.el-button:active) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

:deep(.el-button--primary) {
  background: linear-gradient(135deg, #3498db 0%, #2980b9 100%);
  border: none;
}

:deep(.el-button--primary:hover) {
  background: linear-gradient(135deg, #2980b9 0%, #3498db 100%);
}

:deep(.el-button--success) {
  background: linear-gradient(135deg, #2ecc71 0%, #27ae60 100%);
  border: none;
}

:deep(.el-button--success:hover) {
  background: linear-gradient(135deg, #27ae60 0%, #2ecc71 100%);
}

:deep(.el-button--info) {
  background: linear-gradient(135deg, #95a5a6 0%, #7f8c8d 100%);
  border: none;
}

:deep(.el-button--info:hover) {
  background: linear-gradient(135deg, #7f8c8d 0%, #95a5a6 100%);
}

/* 用餐组卡片头部 */
.meal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  animation: fadeInUp 0.5s ease-out 0.3s both;
}

.meal-header h3 {
  color: #2c3e50;
  font-size: 20px;
  font-weight: 600;
  margin: 0;
  position: relative;
  padding-left: 24px;
}

.meal-header h3::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: linear-gradient(135deg, #3498db 0%, #2ecc71 100%);
  box-shadow: 0 2px 8px rgba(52, 152, 219, 0.4);
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  background-color: #f8f9fa;
  border-radius: 12px;
  animation: fadeInUp 0.5s ease-out 0.4s both;
  position: relative;
  overflow: hidden;
}

.empty-state::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #3498db, #2ecc71, #e74c3c, #f39c12);
  animation: pulse 2s infinite;
}

/* 表格样式优化 */
:deep(.el-table) {
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  animation: fadeInUp 0.5s ease-out 0.4s both;
}

:deep(.el-table__header-wrapper) {
  background: linear-gradient(135deg, #3498db 0%, #2980b9 100%);
  position: relative;
}

:deep(.el-table__header-wrapper::before) {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #ffffff, transparent, #ffffff);
  animation: slideIn 2s infinite;
}

:deep(.el-table__header th) {
  color: white;
  font-weight: 600;
  background-color: transparent !important;
  border-bottom: none;
  padding: 14px 8px;
  font-size: 14px;
}

:deep(.el-table__body tr) {
  transition: all 0.3s ease;
}

:deep(.el-table__body tr:hover > td) {
  background-color: #f5f7fa !important;
  transform: translateX(4px);
}

:deep(.el-table__body tr.el-table__row--striped) {
  background-color: #fafbfc;
}

:deep(.el-table__body td) {
  padding: 14px 8px;
  border-bottom: 1px solid #f0f2f5;
  font-size: 14px;
}

/* 营养成分对比 */
.nutrition-comparison {
  margin-top: 24px;
  padding: 24px;
  background: linear-gradient(135deg, #e8f4fd 0%, #d4e9fc 100%);
  border-radius: 12px;
  border: 1px solid #c5e1fa;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  animation: fadeInUp 0.5s ease-out 0.5s both;
  position: relative;
  overflow: hidden;
}

.nutrition-comparison::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #3498db, #2ecc71);
}

.nutrition-comparison:hover {
  box-shadow: 0 8px 24px rgba(52, 152, 219, 0.2);
  transform: translateY(-2px);
}

.nutrition-comparison h4 {
  margin-top: 0;
  margin-bottom: 20px;
  color: #2c3e50;
  font-size: 18px;
  font-weight: 600;
  border-bottom: 2px solid #3498db;
  padding-bottom: 12px;
  position: relative;
}

/* 营养变化样式 */
.increase {
  color: #e74c3c;
  font-weight: 600;
  animation: pulse 0.8s ease-out;
  display: inline-flex;
  align-items: center;
}

.increase::before {
  content: '↑';
  margin-right: 4px;
  font-size: 12px;
}

.decrease {
  color: #2ecc71;
  font-weight: 600;
  animation: pulse 0.8s ease-out;
  display: inline-flex;
  align-items: center;
}

.decrease::before {
  content: '↓';
  margin-right: 4px;
  font-size: 12px;
}

/* 符合营养标准 */
.compliant {
  display: flex;
  align-items: center;
  color: #27ae60;
  font-weight: 600;
  font-size: 14px;
  padding: 12px;
  background-color: #d4edda;
  border-radius: 8px;
  border-left: 4px solid #2ecc71;
  animation: fadeInUp 0.5s ease-out 0.6s both;
  transition: all 0.3s ease;
}

.compliant:hover {
  box-shadow: 0 4px 12px rgba(46, 204, 113, 0.2);
  transform: translateX(4px);
}

.compliant .el-icon {
  margin-right: 8px;
  font-size: 20px;
  color: #2ecc71;
  animation: pulse 1.5s infinite;
}

/* 不符合营养标准 */
.non-compliant {
  color: #e67e22;
  background-color: #fff9e6;
  padding: 16px;
  border-radius: 8px;
  border-left: 4px solid #e67e22;
  margin-top: 16px;
  animation: fadeInUp 0.5s ease-out 0.6s both;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(230, 126, 34, 0.1);
}

.non-compliant:hover {
  box-shadow: 0 4px 16px rgba(230, 126, 34, 0.2);
}

.non-compliant .el-icon {
  margin-right: 8px;
  font-size: 20px;
  color: #e67e22;
  animation: pulse 1.5s infinite;
}

.non-compliant ul {
  margin: 12px 0;
  padding-left: 24px;
}

.non-compliant li {
  margin: 6px 0;
  font-size: 14px;
  position: relative;
  padding-left: 12px;
}

.non-compliant li::before {
  content: '⚠';
  position: absolute;
  left: -24px;
  top: 2px;
  font-size: 12px;
}

.suggestions {
  margin-top: 16px;
  padding: 16px;
  background-color: #fff3cd;
  border-radius: 8px;
  border-left: 3px solid #ffc107;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(255, 193, 7, 0.1);
}

.suggestions:hover {
  box-shadow: 0 4px 12px rgba(255, 193, 7, 0.2);
}

.suggestions strong {
  color: #856404;
  font-size: 14px;
}

.suggestions ul {
  margin: 12px 0 0 0;
  padding-left: 20px;
}

.suggestions li {
  margin: 6px 0;
  color: #856404;
  font-size: 13px;
  position: relative;
  padding-left: 12px;
}

.suggestions li::before {
  content: '💡';
  position: absolute;
  left: -20px;
  top: 2px;
  font-size: 12px;
}

/* 周计划显示 */
.weekly-plan-display {
  margin-top: 24px;
  animation: fadeInUp 0.5s ease-out 0.4s both;
}

.meal-group {
  margin-bottom: 24px;
  padding: 20px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 12px;
  border-left: 4px solid #3498db;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.meal-group:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  transform: translateX(4px);
}

.meal-group h5 {
  margin-bottom: 16px;
  color: #2c3e50;
  font-size: 16px;
  font-weight: 600;
  position: relative;
  padding-left: 20px;
}

.meal-group h5::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #3498db;
}

/* 手风琴样式 */
:deep(.el-collapse) {
  border-radius: 12px;
  overflow: hidden;
  animation: fadeInUp 0.5s ease-out 0.5s both;
}

:deep(.el-collapse-item) {
  border-bottom: none;
  margin-bottom: 8px;
}

:deep(.el-collapse-item__header) {
  background: linear-gradient(135deg, #f5f7fa 0%, #e8f4fd 100%);
  border-radius: 12px;
  padding: 20px;
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid #e0e6ed;
  position: relative;
  overflow: hidden;
}

:deep(.el-collapse-item__header::after) {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #3498db, #2ecc71);
  transform: scaleX(0);
  transition: transform 0.3s ease;
}

:deep(.el-collapse-item__header:hover::after) {
  transform: scaleX(1);
}

:deep(.el-collapse-item__header:hover) {
  background: linear-gradient(135deg, #e8f4fd 0%, #d4e9fc 100%);
  color: #3498db;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

:deep(.el-collapse-item__content) {
  padding: 0;
  animation: fadeInUp 0.3s ease-out;
}

:deep(.el-collapse-item__wrap) {
  border: none;
}

/* 标签页样式 */
:deep(.el-tabs) {
  margin-top: 24px;
  animation: fadeInUp 0.5s ease-out 0.6s both;
}

:deep(.el-tabs__header) {
  margin-bottom: 24px;
  border-bottom: 2px solid #e0e6ed;
  position: relative;
}

:deep(.el-tabs__nav-wrap) {
  padding-left: 8px;
}

:deep(.el-tabs__item) {
  color: #7f8c8d;
  font-weight: 600;
  padding: 12px 24px;
  margin-right: 8px;
  border-radius: 8px 8px 0 0;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  font-size: 14px;
}

:deep(.el-tabs__item::after) {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 0;
  height: 3px;
  background: linear-gradient(90deg, #3498db, #2ecc71);
  transition: width 0.3s ease;
}

:deep(.el-tabs__item:hover) {
  color: #3498db;
  background-color: #f0f8ff;
  transform: translateY(-1px);
}

:deep(.el-tabs__item.is-active) {
  color: #2c3e50;
  background-color: #fff;
  border-bottom: 3px solid transparent;
  font-weight: 700;
}

:deep(.el-tabs__item.is-active::after) {
  width: 100%;
}

/* 表单样式 */
:deep(.el-form-item__label) {
  color: #2c3e50;
  font-weight: 600;
  font-size: 14px;
}

:deep(.el-input__wrapper),
:deep(.el-textarea__inner),
:deep(.el-date-editor),
:deep(.el-select__wrapper) {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid #dcdfe6;
  background-color: #fff;
}

:deep(.el-input__wrapper:hover),
:deep(.el-textarea__inner:hover),
:deep(.el-date-editor:hover),
:deep(.el-select__wrapper:hover) {
  border-color: #3498db;
  box-shadow: 0 6px 20px rgba(52, 152, 219, 0.25);
  transform: translateY(-2px);
}

:deep(.el-input__wrapper.is-focus),
:deep(.el-textarea__inner.is-focus),
:deep(.el-date-editor.is-focus),
:deep(.el-select__wrapper.is-focus) {
  border-color: #3498db;
  box-shadow: 0 8px 24px rgba(52, 152, 219, 0.3);
  transform: translateY(-2px);
}

:deep(.el-input__inner),
:deep(.el-textarea__inner) {
  font-size: 14px;
  padding: 10px 12px;
}

:deep(.el-textarea__inner) {
  resize: vertical;
  min-height: 100px;
}

/* 对话框样式 */
:deep(.el-dialog) {
  border-radius: 16px;
  box-shadow: 0 16px 50px rgba(0, 0, 0, 0.15);
  background: #fff;
  animation: fadeInUp 0.4s ease-out;
  overflow: hidden;
}

:deep(.el-dialog__header) {
  border-bottom: 1px solid #e0e6ed;
  padding: 24px 24px 16px 24px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  position: relative;
}

:deep(.el-dialog__header::before) {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 60px;
  height: 3px;
  background: linear-gradient(90deg, #3498db, #2ecc71);
}

:deep(.el-dialog__title) {
  font-size: 20px;
  font-weight: 700;
  color: #2c3e50;
}

:deep(.el-dialog__body) {
  padding: 24px;
  max-height: 60vh;
  overflow-y: auto;
}

:deep(.el-dialog__body::-webkit-scrollbar) {
  width: 8px;
}

:deep(.el-dialog__body::-webkit-scrollbar-track) {
  background: #f1f1f1;
  border-radius: 4px;
}

:deep(.el-dialog__body::-webkit-scrollbar-thumb) {
  background: #c1c1c1;
  border-radius: 4px;
}

:deep(.el-dialog__body::-webkit-scrollbar-thumb:hover) {
  background: #a1a1a1;
}

:deep(.el-dialog__footer) {
  border-top: 1px solid #e0e6ed;
  padding: 16px 24px;
  background-color: #f8f9fa;
}

/* 模板相关样式 */
.template-section {
  margin-top: 24px;
  animation: fadeInUp 0.5s ease-out 0.7s both;
}

.template-header {
  margin-bottom: 24px;
  padding: 24px;
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border-radius: 12px;
  border-left: 5px solid #3498db;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 16px rgba(52, 152, 219, 0.1);
  position: relative;
  overflow: hidden;
}

.template-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #3498db, #2ecc71);
}

.template-header:hover {
  box-shadow: 0 8px 30px rgba(52, 152, 219, 0.2);
  transform: translateY(-2px);
}

.template-header h3 {
  margin: 0 0 16px 0;
  color: #2c3e50;
  font-size: 22px;
  font-weight: 700;
  position: relative;
  padding-left: 32px;
}

.template-header h3::before {
  content: '📋';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  font-size: 20px;
}

.template-header p {
  margin: 0;
  color: #666;
  font-size: 14px;
  position: relative;
  padding-left: 32px;
}

.template-header p::before {
  content: '📅';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  font-size: 16px;
}

.template-detail {
  margin-top: 24px;
}

.empty-template {
  text-align: center;
  padding: 60px 20px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 12px;
  animation: fadeInUp 0.5s ease-out 0.8s both;
}

/* 统计和空状态 */
:deep(.el-empty) {
  padding: 60px 20px;
  animation: fadeInUp 0.5s ease-out 0.8s both;
}

:deep(.el-empty__description) {
  color: #95a5a6;
  font-size: 14px;
  margin-top: 16px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .plan-container {
    padding: 16px;
  }

  h2 {
    font-size: 24px;
  }

  .plan-controls {
    flex-direction: column;
    align-items: stretch;
  }

  :deep(.el-button) {
    width: 100%;
  }

  :deep(.el-table) {
    font-size: 13px;
  }

  :deep(.el-table__header-wrapper th) {
    padding: 12px 4px;
  }

  :deep(.el-table__body-wrapper td) {
    padding: 12px 4px;
  }

  .meal-group {
    padding: 16px;
  }

  .nutrition-comparison {
    padding: 16px;
  }
}

@media (max-width: 480px) {
  .meal-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  :deep(.el-dialog) {
    margin: 16px;
    width: auto !important;
  }

  :deep(.el-dialog__body) {
    padding: 16px;
  }
}
</style>


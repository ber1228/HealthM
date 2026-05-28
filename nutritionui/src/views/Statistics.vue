<template>
  <div class="statistics-container">
    <el-card>
      <h2>数据统计与可视化</h2>
      
      <!-- 统计控制 -->
      <div class="statistics-controls">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          @change="loadStatistics"
          style="margin-right: 20px"
        />
        <el-button type="primary" @click="loadStatistics">刷新数据</el-button>
      </div>
      
      <!-- 统计概览 -->
      <div class="statistics-overview">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-card class="overview-card">
              <div class="overview-item">
                <div class="overview-icon calories">
                  <i class="el-icon-fire"></i>
                </div>
                <div class="overview-content">
                  <div class="overview-value">{{ overviewData.avgCalories }}</div>
                  <div class="overview-label">平均热量(kcal)</div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="overview-card">
              <div class="overview-item">
                <div class="overview-icon protein">
                  <i class="el-icon-food"></i>
                </div>
                <div class="overview-content">
                  <div class="overview-value">{{ overviewData.avgProtein }}</div>
                  <div class="overview-label">平均蛋白质(g)</div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="overview-card">
              <div class="overview-item">
                <div class="overview-icon compliance">
                  <i class="el-icon-check"></i>
                </div>
                <div class="overview-content">
                  <div class="overview-value">{{ overviewData.complianceRate }}%</div>
                  <div class="overview-label">方案符合率</div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="overview-card">
              <div class="overview-item">
                <div class="overview-icon days">
                  <i class="el-icon-date"></i>
                </div>
                <div class="overview-content">
                  <div class="overview-value">{{ overviewData.totalDays }}</div>
                  <div class="overview-label">记录天数</div>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
      
      <!-- 图表展示 -->
      <el-tabs v-model="activeTab" @tab-change="switchTab">
        <el-tab-pane label="热量趋势" name="calories">
          <div id="caloriesChart" style="height: 400px"></div>
        </el-tab-pane>
        <el-tab-pane label="营养素对比" name="nutrients">
          <div id="nutrientsChart" style="height: 400px"></div>
        </el-tab-pane>
        <el-tab-pane label="饮食结构" name="structure">
          <div id="structureChart" style="height: 400px"></div>
        </el-tab-pane>
        <el-tab-pane label="营养雷达图" name="radar">
          <div id="radarChart" style="height: 400px"></div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
    
    <!-- 详细统计表格 -->
    <el-card style="margin-top: 20px">
      <template #header>
        <h3>详细统计数据</h3>
      </template>
      <el-table :data="statisticsData" border>
        <el-table-column prop="date" label="日期" width="120">
          <template #default="scope">
            {{ dayjs(scope.row.date).format('YYYY-MM-DD') }}
          </template>
        </el-table-column>
        <el-table-column label="热量(kcal)" width="120">
          <template #default="scope">
            {{ Number(scope.row.calories || scope.row.totalCalories || 0).toFixed(1) }}
          </template>
        </el-table-column>
        <el-table-column label="蛋白质(g)" width="100">
          <template #default="scope">
            {{ Number(scope.row.protein || scope.row.totalProtein || 0).toFixed(1) }}
          </template>
        </el-table-column>
        <el-table-column label="碳水化合物(g)" width="130">
          <template #default="scope">
            {{ Number(scope.row.carbs || scope.row.totalCarbs || 0).toFixed(1) }}
          </template>
        </el-table-column>
        <el-table-column label="脂肪(g)" width="100">
          <template #default="scope">
            {{ Number(scope.row.fat || scope.row.totalFat || 0).toFixed(1) }}
          </template>
        </el-table-column>
        <el-table-column label="膳食纤维(g)" width="120">
          <template #default="scope">
            {{ Number(scope.row.fiber || scope.row.totalFiber || 0).toFixed(1) }}
          </template>
        </el-table-column>
        <el-table-column label="钙(mg)" width="100">
          <template #default="scope">
            {{ Number(scope.row.calcium || scope.row.totalCalcium || 0).toFixed(1) }}
          </template>
        </el-table-column>
        <el-table-column label="铁(mg)" width="100">
          <template #default="scope">
            {{ Number(scope.row.iron || scope.row.totalIron || 0).toFixed(1) }}
          </template>
        </el-table-column>
        <el-table-column label="维生素C(mg)" width="120">
          <template #default="scope">
            {{ Number(scope.row.vitc || scope.row.totalVitc || 0).toFixed(1) }}
          </template>
        </el-table-column>
        <el-table-column prop="complianceRate" label="符合率(%)" width="100" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick, onMounted } from 'vue'
import * as echarts from 'echarts'
import api from '@/api'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'

const activeTab = ref('calories')
const dateRange = ref<[Date, Date]>([
  dayjs().subtract(7, 'day').toDate(),
  dayjs().toDate()
])

const overviewData = ref({
  avgCalories: 0,
  avgProtein: 0,
  complianceRate: 0,
  totalDays: 0
})

const statisticsData = ref<any[]>([])

let charts: { [key: string]: echarts.ECharts } = {}

const loadStatistics = async () => {
  if (!dateRange.value || dateRange.value.length !== 2) {
    ElMessage.warning('请选择日期范围')
    return
  }
  
  const startDate = dayjs(dateRange.value[0]).format('YYYY-MM-DD')
  const endDate = dayjs(dateRange.value[1]).format('YYYY-MM-DD')
  
  try {
    console.log('加载统计数据 - 日期范围:', startDate, '至', endDate)
    const response = await api.get('/record/statistics', {
      params: { startDate, endDate }
    })
    
    console.log('统计数据响应:', response.data)
    statisticsData.value = response.data
    
    // 计算概览数据
    calculateOverviewData(response.data)
    
    // 更新图表
    updateCharts(response.data)
    
  } catch (error: any) {
    console.error('加载统计数据失败:', error)
    ElMessage.error(error.response?.data?.error || error.response?.data || '加载失败')
  }
}

const calculateOverviewData = (data: any[]) => {
  if (data.length === 0) {
    overviewData.value = {
      avgCalories: 0,
      avgProtein: 0,
      complianceRate: 0,
      totalDays: 0
    }
    return
  }
  
  const totalCalories = data.reduce((sum, item) => sum + (item.calories || item.totalCalories || 0), 0)
  const totalProtein = data.reduce((sum, item) => sum + (item.protein || item.totalProtein || 0), 0)
  const totalCompliance = data.reduce((sum, item) => sum + (item.complianceRate || 0), 0)
  
  overviewData.value = {
    avgCalories: Math.round(totalCalories / data.length),
    avgProtein: Math.round(totalProtein / data.length * 10) / 10,
    complianceRate: Math.round(totalCompliance / data.length),
    totalDays: data.length
  }
}

const initCharts = () => {
  nextTick(() => {
    try {
      // 初始化热量趋势图
      const caloriesDom = document.getElementById('caloriesChart')
      if (caloriesDom && caloriesDom.clientWidth > 0) {
        charts.calories = echarts.init(caloriesDom)
      }
      
      // 初始化营养素对比图
      const nutrientsDom = document.getElementById('nutrientsChart')
      if (nutrientsDom && nutrientsDom.clientWidth > 0) {
        charts.nutrients = echarts.init(nutrientsDom)
      }
      
      // 初始化饮食结构图
      const structureDom = document.getElementById('structureChart')
      if (structureDom && structureDom.clientWidth > 0) {
        charts.structure = echarts.init(structureDom)
      }
      
      // 初始化雷达图
      const radarDom = document.getElementById('radarChart')
      if (radarDom && radarDom.clientWidth > 0) {
        charts.radar = echarts.init(radarDom)
      }
      
      // 监听窗口大小变化
      window.addEventListener('resize', () => {
        Object.values(charts).forEach(chart => {
          if (chart) {
            chart.resize()
          }
        })
      })
      
      // 默认显示热量趋势图
      updateCaloriesChart(statisticsData.value)
    } catch (error) {
      console.error('图表初始化失败:', error)
    }
  })
}

const updateCharts = (data: any[]) => {
  updateCaloriesChart(data)
  updateNutrientsChart(data)
  updateStructureChart(data)
  updateRadarChart(data)
}

const updateCaloriesChart = (data: any[]) => {
  const dates = data.map(item => dayjs(item.date).format('MM-DD'))
  const calories = data.map(item => Number(item.calories || item.totalCalories || 0))
  const targets = data.map(item => Number(item.targetCalories || 0))
  
  const option = {
    title: {
      text: '热量摄入趋势',
      left: 'center',
      textStyle: {
        color: '#2d8659',
        fontSize: 18
      }
    },
    tooltip: {
      trigger: 'axis',
      formatter: function(params: any) {
        let result = params[0].name + '<br/>'
        params.forEach((param: any) => {
          result += param.marker + param.seriesName + ': ' + param.value + ' kcal<br/>'
        })
        return result
      }
    },
    legend: {
      data: ['实际摄入', '目标摄入'],
      top: 30
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates,
      axisLabel: {
        color: '#5a7c65'
      }
    },
    yAxis: {
      type: 'value',
      name: '热量(kcal)',
      axisLabel: {
        color: '#5a7c65'
      }
    },
    series: [
      {
        name: '实际摄入',
        type: 'line',
        data: calories,
        smooth: true,
        lineStyle: {
          color: '#4facfe',
          width: 3
        },
        itemStyle: {
          color: '#4facfe'
        },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [{
              offset: 0, color: 'rgba(79, 172, 254, 0.3)'
            }, {
              offset: 1, color: 'rgba(79, 172, 254, 0.1)'
            }]
          }
        }
      },
      {
        name: '目标摄入',
        type: 'line',
        data: targets,
        smooth: true,
        lineStyle: {
          color: '#67c23a',
          width: 2,
          type: 'dashed'
        },
        itemStyle: {
          color: '#67c23a'
        }
      }
    ]
  }
  
  charts.calories?.setOption(option)
}

const updateNutrientsChart = (data: any[]) => {
  const dates = data.map(item => dayjs(item.date).format('MM-DD'))
  const protein = data.map(item => Number(item.protein || item.totalProtein || 0))
  const carbs = data.map(item => Number(item.carbs || item.totalCarbs || 0))
  const fat = data.map(item => Number(item.fat || item.totalFat || 0))
  
  const option = {
    title: {
      text: '营养素摄入对比',
      left: 'center',
      textStyle: {
        color: '#2d8659',
        fontSize: 18
      }
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    legend: {
      data: ['蛋白质', '碳水化合物', '脂肪'],
      top: 30
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: dates,
      axisLabel: {
        color: '#5a7c65'
      }
    },
    yAxis: {
      type: 'value',
      name: '重量(g)',
      axisLabel: {
        color: '#5a7c65'
      }
    },
    series: [
      {
        name: '蛋白质',
        type: 'bar',
        data: protein,
        itemStyle: {
          color: '#4facfe'
        }
      },
      {
        name: '碳水化合物',
        type: 'bar',
        data: carbs,
        itemStyle: {
          color: '#67c23a'
        }
      },
      {
        name: '脂肪',
        type: 'bar',
        data: fat,
        itemStyle: {
          color: '#e6a23c'
        }
      }
    ]
  }
  
  charts.nutrients?.setOption(option)
}

const updateStructureChart = (data: any[]) => {
  if (data.length === 0) return
  
  // 计算平均摄入量
  const avgProtein = data.reduce((sum, item) => sum + Number(item.protein || item.totalProtein || 0), 0) / data.length
  const avgCarbs = data.reduce((sum, item) => sum + Number(item.carbs || item.totalCarbs || 0), 0) / data.length
  const avgFat = data.reduce((sum, item) => sum + Number(item.fat || item.totalFat || 0), 0) / data.length
  
  const option = {
    title: {
      text: '饮食结构分析',
      left: 'center',
      textStyle: {
        color: '#2d8659',
        fontSize: 18
      }
    },
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c}g ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      top: 'middle',
      data: ['蛋白质', '碳水化合物', '脂肪']
    },
    series: [
      {
        name: '营养素',
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['60%', '50%'],
        data: [
          { value: avgProtein, name: '蛋白质', itemStyle: { color: '#4facfe' } },
          { value: avgCarbs, name: '碳水化合物', itemStyle: { color: '#67c23a' } },
          { value: avgFat, name: '脂肪', itemStyle: { color: '#e6a23c' } }
        ],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        },
        label: {
          formatter: '{b}: {c}g\n({d}%)',
          fontSize: 12
        }
      }
    ]
  }
  
  charts.structure?.setOption(option)
}

const updateRadarChart = (data: any[]) => {
  if (data.length === 0) return
  
  // 计算平均摄入量和目标值
  const avgData = {
    calories: data.reduce((sum, item) => sum + Number(item.calories || item.totalCalories || 0), 0) / data.length,
    protein: data.reduce((sum, item) => sum + Number(item.protein || item.totalProtein || 0), 0) / data.length,
    carbs: data.reduce((sum, item) => sum + Number(item.carbs || item.totalCarbs || 0), 0) / data.length,
    fat: data.reduce((sum, item) => sum + Number(item.fat || item.totalFat || 0), 0) / data.length,
    fiber: data.reduce((sum, item) => sum + Number(item.fiber || item.totalFiber || 0), 0) / data.length,
    calcium: data.reduce((sum, item) => sum + Number(item.calcium || item.totalCalcium || 0), 0) / data.length
  }
  
  const targetData = {
    calories: Number(data[0]?.targetCalories || 2000),
    protein: Number(data[0]?.targetProtein || 60),
    carbs: Number(data[0]?.targetCarbs || 300),
    fat: Number(data[0]?.targetFat || 65),
    fiber: Number(data[0]?.targetFiber || 25),
    calcium: Number(data[0]?.targetCalcium || 1000)
  }
  
  const option = {
    title: {
      text: '营养摄入雷达图',
      left: 'center',
      textStyle: {
        color: '#2d8659',
        fontSize: 18
      }
    },
    tooltip: {
      trigger: 'item'
    },
    legend: {
      data: ['实际摄入', '目标摄入'],
      top: 30
    },
    radar: {
      indicator: [
        { name: '热量', max: Math.max(avgData.calories, targetData.calories) * 1.2 },
        { name: '蛋白质', max: Math.max(avgData.protein, targetData.protein) * 1.2 },
        { name: '碳水化合物', max: Math.max(avgData.carbs, targetData.carbs) * 1.2 },
        { name: '脂肪', max: Math.max(avgData.fat, targetData.fat) * 1.2 },
        { name: '膳食纤维', max: Math.max(avgData.fiber, targetData.fiber) * 1.2 },
        { name: '钙', max: Math.max(avgData.calcium, targetData.calcium) * 1.2 }
      ],
      center: ['50%', '60%'],
      radius: '60%'
    },
    series: [
      {
        name: '营养摄入',
        type: 'radar',
        data: [
          {
            value: [avgData.calories, avgData.protein, avgData.carbs, avgData.fat, avgData.fiber, avgData.calcium],
            name: '实际摄入',
            itemStyle: {
              color: '#4facfe'
            },
            areaStyle: {
              color: 'rgba(79, 172, 254, 0.3)'
            }
          },
          {
            value: [targetData.calories, targetData.protein, targetData.carbs, targetData.fat, targetData.fiber, targetData.calcium],
            name: '目标摄入',
            itemStyle: {
              color: '#67c23a'
            },
            areaStyle: {
              color: 'rgba(103, 194, 58, 0.3)'
            }
          }
        ]
      }
    ]
  }
  
  charts.radar?.setOption(option)
}

const switchTab = (tabName: string) => {
  // 切换标签页时重新渲染对应图表
  nextTick(() => {
    // 延迟一下，确保DOM已经渲染
    setTimeout(() => {
      switch (tabName) {
        case 'calories':
          if (!charts.calories) {
            const dom = document.getElementById('caloriesChart')
            if (dom && dom.clientWidth > 0) {
              charts.calories = echarts.init(dom)
            }
          }
          updateCaloriesChart(statisticsData.value)
          break
        case 'nutrients':
          if (!charts.nutrients) {
            const dom = document.getElementById('nutrientsChart')
            if (dom && dom.clientWidth > 0) {
              charts.nutrients = echarts.init(dom)
            }
          }
          updateNutrientsChart(statisticsData.value)
          break
        case 'structure':
          if (!charts.structure) {
            const dom = document.getElementById('structureChart')
            if (dom && dom.clientWidth > 0) {
              charts.structure = echarts.init(dom)
            }
          }
          updateStructureChart(statisticsData.value)
          break
        case 'radar':
          if (!charts.radar) {
            const dom = document.getElementById('radarChart')
            if (dom && dom.clientWidth > 0) {
              charts.radar = echarts.init(dom)
            }
          }
          updateRadarChart(statisticsData.value)
          break
      }
    }, 100)
  })
}

onMounted(() => {
  loadStatistics()
  // 延迟初始化图表，确保DOM完全加载
  setTimeout(() => {
    initCharts()
  }, 300)
})
</script>

<style scoped>
.statistics-container {
  padding: 20px;
}

h2 {
  margin-bottom: 20px;
  color: #2d8659;
  font-weight: 600;
}

h3 {
  color: #5a7c65;
  font-weight: 500;
  margin: 0;
}

.statistics-controls {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 10px;
}

.statistics-overview {
  margin-bottom: 30px;
}

.overview-card {
  height: 100px;
  border-radius: 12px;
  background: linear-gradient(135deg, #f0f9f6 0%, #e8f5e8 100%);
  border: 1px solid rgba(168, 230, 207, 0.3);
  transition: all 0.3s ease;
}

.overview-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.overview-item {
  display: flex;
  align-items: center;
  height: 100%;
  padding: 20px;
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

.overview-icon.compliance {
  background: linear-gradient(135deg, #67c23a, #85ce61);
}

.overview-icon.days {
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

:deep(.el-card) {
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
  border: 1px solid rgba(168, 230, 207, 0.3);
}

:deep(.el-card__header) {
  background: linear-gradient(135deg, #f0f9f6 0%, #e8f5e8 100%);
  border-bottom: 1px solid rgba(168, 230, 207, 0.3);
}

:deep(.el-button--primary) {
  background-color: #4facfe;
  border-color: #4facfe;
  border-radius: 8px;
  transition: all 0.3s ease;
}

:deep(.el-button--primary:hover) {
  background-color: #39a1ff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(79, 172, 254, 0.4);
}

:deep(.el-tabs__header) {
  margin-bottom: 20px;
  background-color: #f0f9f6;
  border-radius: 8px;
  padding: 10px;
}

:deep(.el-tabs__item) {
  border-radius: 6px;
  transition: all 0.3s ease;
  color: #5a7c65;
  font-weight: 500;
}

:deep(.el-tabs__item.is-active) {
  color: #4facfe;
  background-color: rgba(79, 172, 254, 0.1);
}

:deep(.el-tabs__item:hover) {
  color: #4facfe;
  background-color: rgba(79, 172, 254, 0.1);
}

:deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
}

:deep(.el-table__header) {
  background-color: #f0f9f6;
}

:deep(.el-date-editor) {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

/* 图表容器样式 */
#caloriesChart,
#nutrientsChart,
#structureChart,
#radarChart {
  border-radius: 8px;
  background: linear-gradient(135deg, #fafafa 0%, #f5f5f5 100%);
  border: 1px solid rgba(168, 230, 207, 0.2);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .statistics-controls {
    flex-direction: column;
    align-items: stretch;
  }
  
  .statistics-controls .el-date-editor {
    margin-right: 0;
    margin-bottom: 10px;
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
}

@media (max-width: 480px) {
  .statistics-container {
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
}
</style>


<template>
  <div class="analysis-container">
    <!-- 头部：标题+时间维度切换 -->
    <div class="analysis-header">
      <h2 class="title">营养分析</h2>
      <!-- 日分析-->
      <span class="time-label">日分析</span>
      <!-- 日期选择器（只用于日分析） -->
      <el-date-picker
          v-model="selectedDate"
          type="date"
          placeholder="选择日期"
          @change="fetchNutritionData"
          class="date-picker"
      />
    </div>

    <!-- 核心营养指标展示 -->
    <div class="nutrition-cards">
      <el-row :gutter="20">
        <el-col :span="6" v-for="item in macronutrients" :key="item.name">
          <el-card class="indicator-card macronutrient-card" :style="getMacroCardStyle(item.name)">
            <div class="card-content">
              <div class="icon" :style="getIconStyle(item.name)">{{ getNutrientIcon(item.name) }}</div>
              <div class="info">
                <div class="value" :class="getValueClass(item.statusType)">{{ item.actual }}{{ item.unit }}</div>
                <div class="label">{{ item.name }}</div>
                <div class="target">目标: {{ item.target }}{{ item.unit }}</div>
              </div>
            </div>
            <div class="progress">
              <el-progress
                  :percentage="item.rate"
                  :color="getProgressColor(item.rate)"
                  :status="item.statusType === 'danger' ? 'exception' : (item.statusType === 'warning' ? 'warning' : '')"
              />
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 其他营养素指标 -->
    <div class="other-nutrition-cards">
      <el-row :gutter="16">
        <el-col :span="4" v-for="item in otherNutrients" :key="item.name">
          <el-card class="indicator-card other-nutrient-card" :style="getOtherCardStyle(item.name)">
            <div class="card-content">
              <div class="icon" :style="getOtherIconStyle(item.name)">{{ getNutrientIcon(item.name) }}</div>
              <div class="info">
                <div class="value" :class="getValueClass(item.statusType)">{{ item.actual }}{{ item.unit }}</div>
                <div class="label">{{ item.name }}</div>
                <div class="target">目标: {{ item.target }}{{ item.unit }}</div>
              </div>
            </div>
            <div class="progress">
              <el-progress
                  :percentage="item.rate"
                  :color="getProgressColor(item.rate)"
                  :status="item.statusType === 'danger' ? 'exception' : (item.statusType === 'warning' ? 'warning' : '')"
              />
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 可视化图表：趋势/占比 -->
    <div class="chart-container">
      <el-card class="chart-card">
        <template #header>
          <div class="card-header-flex">
            <span>{{ chartTitle }}</span>
          </div>
        </template>
        <div id="nutrition-chart" style="width: 100%; height: 450px;"></div>
      </el-card>
    </div>

    <!-- 详细分析报告 -->
    <div class="report-container">
      <el-card class="report-card" shadow="hover">
        <template #header>
          <div class="card-header-flex report-header">
            <div class="header-left">
              <span class="report-title">
                <i class="el-icon-document" style="margin-right: 8px; color: #409eff;"></i>
                营养分析报告
              </span>
            </div>
            <div class="header-right">
              <span class="report-date">
                <i class="el-icon-date" style="margin-right: 4px;"></i>
                {{ formatDate(selectedDate) }}
              </span>
            </div>
          </div>
        </template>
        <div class="report-content">
          <!-- 总结区域 -->
          <div class="summary-section" :class="getSummarySectionClass()">
            <div class="summary-header">
              <h3 class="section-title">
                <i class="el-icon-s-data" style="margin-right: 8px;"></i>
                分析总结
              </h3>
              <el-tag
                  v-if="detailReport.length > 0"
                  :type="getOverallStatusTag()"
                  size="small"
                  class="overall-status-tag"
              >
                {{ getOverallStatusText() }}
              </el-tag>
            </div>
            <div class="summary-body">
              <div class="summary-text-wrapper">
                <p class="summary-text">{{ reportSummary }}</p>
              </div>
              <div class="summary-icon">
                <i :class="getSummaryIconClass()" :style="{ color: getSummaryIconColor() }"></i>
              </div>
            </div>
          </div>

          <!-- 详细分析区域 -->
          <div class="details-section">
            <div class="details-header">
              <h3 class="section-title">
                <i class="el-icon-tickets" style="margin-right: 8px;"></i>
                详细分析
              </h3>
              <div class="status-legend">
                <div class="legend-item">
                  <span class="legend-dot success"></span>
                  <span class="legend-text">正常</span>
                </div>
                <div class="legend-item">
                  <span class="legend-dot warning"></span>
                  <span class="legend-text">不足</span>
                </div>
                <div class="legend-item">
                  <span class="legend-dot danger"></span>
                  <span class="legend-text">超标</span>
                </div>
              </div>
            </div>
            <el-table
                :data="detailReport"
                border
                class="report-table"
                :row-class-name="tableRowClassName"
                style="width: 100%"
                :header-cell-style="tableHeaderStyle"
            >
              <el-table-column prop="item" label="分析项" width="150">
                <template #default="scope">
                  <div class="analysis-item">
                    <span class="item-icon" :style="getItemIconStyle(scope.row.item)">{{ getItemIcon(scope.row.item) }}</span>
                    <span class="item-text">{{ scope.row.item }}</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="120">
                <template #default="scope">
                  <div class="status-wrapper">
                    <el-tag
                        :type="getStatusTagType(scope.row.statusType)"
                        class="status-tag"
                        :style="getStatusTagStyle(scope.row.statusType)"
                        size="small"
                    >
                      <i :class="getStatusIcon(scope.row.statusType)" style="margin-right: 4px;"></i>
                      {{ scope.row.status }}
                    </el-tag>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="suggestion" label="建议">
                <template #default="scope">
                  <div class="suggestion-wrapper" :class="getSuggestionClass(scope.row.statusType)">
                    <div class="suggestion-header">
                      <i :class="getSuggestionIcon(scope.row.statusType)" class="suggestion-icon"></i>
                      <span class="suggestion-title">营养建议</span>
                    </div>
                    <p class="suggestion-text">{{ scope.row.suggestion }}</p>
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <!-- 健康小贴士 -->
          <div v-if="detailReport.length > 0" class="health-tips">
            <h3 class="section-title">
              <i class="el-icon-light-raining" style="margin-right: 8px;"></i>
              健康小贴士
            </h3>
            <div class="tips-container">
              <div class="tip-card" v-for="(tip, index) in getHealthTips()" :key="index" :style="getTipCardStyle(index)">
                <div class="tip-icon">
                  <i :class="tip.icon"></i>
                </div>
                <div class="tip-content">
                  <h4 class="tip-title">{{ tip.title }}</h4>
                  <p class="tip-text">{{ tip.content }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, computed, onUnmounted } from 'vue';
import { ElMessage } from 'element-plus';
import * as echarts from 'echarts';
import { useUserStore } from '@/stores/user';
// 导入后端接口
import { nutritionAnalysisApi } from '@/api/index';

// 状态定义
const userStore = useUserStore();

const selectedDate = ref<string>(() => {
  const today = new Date();
  const year = today.getFullYear();
  const month = String(today.getMonth() + 1).padStart(2, '0');
  const day = String(today.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
});
const nutritionIndicators = ref<any[]>([]); // 核心营养指标
const chartInstance = ref<any>(null); // 图表实例
const chartTitle = ref<string>('当日营养分析图表');
const chartData = ref<any>(null); // 图表数据
const reportSummary = ref<string>(''); // 分析总结
const detailReport = ref<any[]>([]); // 详细分析报告
const loading = ref<boolean>(false); // 加载状态

// 计算属性：分离宏量营养素和其他营养素
const macronutrients = computed(() => {
  const macroKeys = ['热量', '蛋白质', '脂肪', '碳水化合物'];
  return nutritionIndicators.value.filter(item => macroKeys.includes(item.name));
});

const otherNutrients = computed(() => {
  const macroKeys = ['热量', '蛋白质', '脂肪', '碳水化合物'];
  return nutritionIndicators.value.filter(item => !macroKeys.includes(item.name));
});

// 计算总体状态
const overallStatus = computed(() => {
  if (detailReport.value.length === 0) return 'neutral';

  const statusCounts = {
    success: 0,
    warning: 0,
    danger: 0,
    info: 0
  };

  detailReport.value.forEach(item => {
    statusCounts[item.statusType as keyof typeof statusCounts]++;
  });

  if (statusCounts.danger > 0) return 'danger';
  if (statusCounts.warning > 0) return 'warning';
  if (statusCounts.success > detailReport.value.length * 0.8) return 'success';
  return 'info';
});

// 获取宏量卡片样式
const getMacroCardStyle = (name: string) => {
  const gradients: Record<string, string> = {
    '热量': 'background: linear-gradient(135deg, #ffe8d6 0%, #ffb347 100%); border-top: 4px solid #ff7b00;',
    '蛋白质': 'background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%); border-top: 4px solid #2196f3;',
    '脂肪': 'background: linear-gradient(135deg, #fce4ec 0%, #f8bbd9 100%); border-top: 4px solid #e91e63;',
    '碳水化合物': 'background: linear-gradient(135deg, #e8f5e8 0%, #c8e6c9 100%); border-top: 4px solid #4caf50;'
  };
  return gradients[name] || 'border-top: 4px solid #409eff; background: linear-gradient(135deg, #ffffff 0%, #f8fbff 100%);';
};

// 获取其他卡片样式
const getOtherCardStyle = (name: string) => {
  const colors: Record<string, string> = {
    '膳食纤维': '#4CAF50',
    '钠': '#2196F3',
    '钙': '#FF9800',
    '铁': '#F44336',
    '维生素C': '#9C27B0',
    '钾': '#00BCD4',
    '维生素A': '#FF5722'
  };
  const color = colors[name] || '#607D8B';
  return `border-left: 4px solid ${color}; background: linear-gradient(to right, ${color}15, #ffffff);`;
};

// 获取图标样式
const getIconStyle = (name: string) => {
  const colors: Record<string, string> = {
    '热量': '#FF5722',
    '蛋白质': '#2196F3',
    '脂肪': '#FF9800',
    '碳水化合物': '#4CAF50'
  };
  return `color: ${colors[name] || '#607D8B'};`;
};

const getOtherIconStyle = (name: string) => {
  const colors: Record<string, string> = {
    '膳食纤维': '#4CAF50',
    '钠': '#2196F3',
    '钙': '#FF9800',
    '铁': '#F44336',
    '维生素C': '#9C27B0',
    '钾': '#00BCD4',
    '维生素A': '#FF5722'
  };
  return `color: ${colors[name] || '#607D8B'};`;
};

// 获取数值样式
const getValueClass = (statusType: string) => {
  return `value-${statusType}`;
};

// 获取营养素图标
const getNutrientIcon = (name: string) => {
  const iconMap: Record<string, string> = {
    '热量': '🔥',
    '蛋白质': '🥚',
    '脂肪': '🥑',
    '碳水化合物': '🍚',
    '膳食纤维': '🥦',
    '钠': '🧂',
    '钙': '🥛',
    '铁': '🍖',
    '维生素A': '🥕',
    '维生素C': '🍊',
    '钾': '🍌'
  };
  return iconMap[name] || '❓';
};

// 获取项目图标
const getItemIcon = (item: string) => {
  const iconMap: Record<string, string> = {
    '热量': '🔥',
    '蛋白质': '🥚',
    '脂肪': '🥑',
    '碳水化合物': '🍚',
    '膳食纤维': '🥦',
    '钠': '🧂',
    '钙': '🥛',
    '铁': '🍖',
    '维生素C': '🍊',
    '钾': '🍌',
    '维生素A': '🥕'
  };
  return iconMap[item] || '📊';
};

const getItemIconStyle = (item: string) => {
  const colors: Record<string, string> = {
    '热量': '#FF5722',
    '蛋白质': '#2196F3',
    '脂肪': '#FF9800',
    '碳水化合物': '#4CAF50',
    '膳食纤维': '#8BC34A',
    '钠': '#03A9F4',
    '钙': '#FF9800',
    '铁': '#F44336',
    '维生素C': '#9C27B0',
    '钾': '#00BCD4',
    '维生素A': '#FF5722'
  };
  return `color: ${colors[item] || '#607D8B'}; font-size: 18px;`;
};

// 获取进度条颜色
const getProgressColor = (rate: number) => {
  if (rate < 80) return '#f56c6c'; // 红色 - 不足
  if (rate > 120) return '#e6a23c'; // 橙色 - 超标
  return '#67c23a'; // 绿色 - 正常
};

// 获取状态图标
const getStatusIcon = (statusType: string) => {
  const icons: Record<string, string> = {
    success: 'el-icon-success',
    warning: 'el-icon-warning',
    danger: 'el-icon-error',
    info: 'el-icon-info'
  };
  return icons[statusType] || 'el-icon-info';
};

// 获取状态标签样式
const getStatusTagStyle = (statusType: string) => {
  const styles: Record<string, string> = {
    success: 'background: linear-gradient(45deg, #67c23a, #85ce61); color: white; border: none;',
    warning: 'background: linear-gradient(45deg, #e6a23c, #ebb563); color: white; border: none;',
    danger: 'background: linear-gradient(45deg, #f56c6c, #f78989); color: white; border: none;',
    info: 'background: linear-gradient(45deg, #909399, #a6a9ad); color: white; border: none;'
  };
  return styles[statusType] || '';
};

// 获取建议图标
const getSuggestionIcon = (statusType: string) => {
  const icons: Record<string, string> = {
    success: 'el-icon-success',
    warning: 'el-icon-warning-outline',
    danger: 'el-icon-close',
    info: 'el-icon-info'
  };
  return icons[statusType] || 'el-icon-info';
};

// 获取建议样式
const getSuggestionClass = (statusType: string) => {
  return `suggestion-${statusType}`;
};

// 获取表格表头样式
const tableHeaderStyle = () => {
  return {
    'background': 'linear-gradient(45deg, #409eff, #66b1ff)',
    'color': 'white',
    'font-weight': '600',
    'border': 'none'
  };
};

// 获取总结区域样式
const getSummarySectionClass = () => {
  return `summary-${overallStatus.value}`;
};

// 获取总结图标类
const getSummaryIconClass = () => {
  const icons: Record<string, string> = {
    success: 'el-icon-success',
    warning: 'el-icon-warning',
    danger: 'el-icon-error',
    info: 'el-icon-info',
    neutral: 'el-icon-question'
  };
  return icons[overallStatus.value];
};

// 获取总结图标颜色
const getSummaryIconColor = () => {
  const colors: Record<string, string> = {
    success: '#67c23a',
    warning: '#e6a23c',
    danger: '#f56c6c',
    info: '#909399',
    neutral: '#909399'
  };
  return colors[overallStatus.value];
};

// 获取总体状态标签
const getOverallStatusTag = () => {
  const map: Record<string, string> = {
    success: 'success',
    warning: 'warning',
    danger: 'danger',
    info: 'info',
    neutral: 'info'
  };
  return map[overallStatus.value];
};

// 获取总体状态文本
const getOverallStatusText = () => {
  const map: Record<string, string> = {
    success: '优秀',
    warning: '良好',
    danger: '需改进',
    info: '一般',
    neutral: '暂无数据'
  };
  return map[overallStatus.value];
};

// 获取健康小贴士
const getHealthTips = () => {
  const tips = [
    {
      icon: 'el-icon-sunny',
      title: '均衡饮食',
      content: '保持食物多样性，确保各类营养素的均衡摄入'
    },
    {
      icon: 'el-icon-time',
      title: '定时定量',
      content: '规律进餐，避免暴饮暴食，控制每餐食量'
    },
    {
      icon: 'el-icon-watermelon',
      title: '多吃蔬果',
      content: '每天摄入足够的新鲜蔬菜和水果，补充维生素和膳食纤维'
    },
    {
      icon: 'el-icon-water-cup',
      title: '充足饮水',
      content: '每天保证充足的水分摄入，促进新陈代谢'
    }
  ];

  return tips;
};

// 获取小贴士卡片样式
const getTipCardStyle = (index: number) => {
  const colors = ['#409eff', '#67c23a', '#e6a23c', '#f56c6c'];
  return `border-left: 4px solid ${colors[index % colors.length]};`;
};

// 格式化日期
const formatDate = (dateStr: string) => {
  try {
    const date = new Date(dateStr);
    if (isNaN(date.getTime())) {
      return '无效日期';
    }
    return date.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  } catch (e) {
    return '日期格式错误';
  }
};

// 获取状态标签类型
const getStatusTagType = (statusType: string) => {
  const typeMap: Record<string, string> = {
    success: 'success',
    warning: 'warning',
    danger: 'danger',
    info: 'info'
  };
  return typeMap[statusType] || 'info';
};

// 表格行样式
const tableRowClassName = ({ row }: { row: any }) => {
  return `status-${row.statusType}`;
};

// 初始化图表
const initChart = () => {
  const chartDom = document.getElementById('nutrition-chart');
  if (chartDom) {
    chartInstance.value = echarts.init(chartDom);

    // 添加窗口大小变化监听
    const handleResize = () => {
      chartInstance.value?.resize();
    };
    window.addEventListener('resize', handleResize);

    // 在组件卸载时移除监听器
    onUnmounted(() => {
      window.removeEventListener('resize', handleResize);
      chartInstance.value?.dispose();
    });
  } else {
    console.error('图表容器未找到');
  }
};

// 获取日营养分析数据
const fetchNutritionData = async () => {
  loading.value = true;
  try {
    // 确保传递YYYY-MM-DD格式，避免时区问题
    let dateParam = selectedDate.value;

    // 处理不同类型的日期参数
    if (dateParam instanceof Date) {
      // 如果是Date对象，直接格式化
      const year = dateParam.getFullYear();
      const month = String(dateParam.getMonth() + 1).padStart(2, '0');
      const day = String(dateParam.getDate()).padStart(2, '0');
      dateParam = `${year}-${month}-${day}`;
      console.log('Date对象转换为字符串:', dateParam);
    } else if (typeof dateParam === 'string') {
      // 如果是字符串，提取日期部分
      if (dateParam.includes('T')) {
        dateParam = dateParam.split('T')[0];
        console.log('提取日期部分:', dateParam);
      }

      // 验证并格式化日期
      const dateObj = new Date(dateParam);
      if (isNaN(dateObj.getTime())) {
        console.error('无效的日期格式:', dateParam);
        // 使用当前日期作为后备
        const today = new Date();
        dateParam = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`;
      } else {
        // 格式化为YYYY-MM-DD
        const year = dateObj.getFullYear();
        const month = String(dateObj.getMonth() + 1).padStart(2, '0');
        const day = String(dateObj.getDate()).padStart(2, '0');
        dateParam = `${year}-${month}-${day}`;
      }
    } else {
      // 如果没有日期或类型不明确，使用今天
      const today = new Date();
      dateParam = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`;
      console.log('使用默认日期:', dateParam);
    }

    console.log('获取营养分析数据，日期参数:', dateParam);
    const response = await nutritionAnalysisApi.getDailyAnalysis(dateParam);
    console.log('API响应:', response);

    const res = response.data;
    console.log('API返回数据:', res);

    if (res.code === 200) {
      const data = res.data;
      console.log('解析后的数据:', data);

      // 清空现有数据
      nutritionIndicators.value = [];
      detailReport.value = [];

      // 处理营养指标数据
      if (data.indicators && data.indicators.length > 0) {
        data.indicators.forEach((item: any) => {
          console.log('指标数据:', item);
          const actualNum = parseFloat(item.actual) || 0;
          const targetNum = parseFloat(item.target) || 1;
          const rate = Math.min(Math.round((actualNum / targetNum) * 100), 200); // 限制最大200%

          let statusType = 'success';
          if (rate < 80) statusType = 'danger'; // 不足
          else if (rate > 120) statusType = 'warning'; // 超标
          else statusType = 'success'; // 正常

          nutritionIndicators.value.push({
            name: item.name,
            actual: actualNum.toFixed(1),
            target: targetNum.toFixed(1),
            unit: item.unit,
            rate: rate,
            statusType: statusType,
            gap: parseFloat(item.gap) || 0
          });
        });
      } else {
        console.log('没有指标数据，显示默认值');
        // 显示默认值
        const defaultIndicators = [
          { name: '热量', unit: 'kcal', actual: '0', target: '2000', gap: '-2000', rate: 0, statusType: 'danger' },
          { name: '蛋白质', unit: 'g', actual: '0', target: '70', gap: '-70', rate: 0, statusType: 'danger' },
          { name: '碳水化合物', unit: 'g', actual: '0', target: '300', gap: '-300', rate: 0, statusType: 'danger' },
          { name: '脂肪', unit: 'g', actual: '0', target: '60', gap: '-60', rate: 0, statusType: 'danger' }
        ];

        defaultIndicators.forEach(item => {
          nutritionIndicators.value.push({
            ...item,
            rate: 0,
            statusType: 'danger'
          });
        });
      }

      // 处理图表数据
      if (data.chartData && Object.keys(data.chartData).length > 0) {
        chartData.value = data.chartData;
        updateChart(chartData.value);
      } else {
        // 如果没有图表数据，显示空的图表
        showEmptyChart();
      }

      // 处理报告数据
      if (data.report) {
        reportSummary.value = data.report.summary || '今日尚未记录饮食，请先添加饮食记录进行分析。';
        detailReport.value = data.report.details || [];
      } else {
        reportSummary.value = '今日尚未记录饮食，请先添加饮食记录进行分析。';
        detailReport.value = [];
      }

      console.log('处理后的营养指标:', nutritionIndicators.value);
      console.log('处理后的报告总结:', reportSummary.value);

    } else {
      console.error('API返回错误:', res.msg);
      ElMessage.error(res.msg || '获取营养分析数据失败');
    }
  } catch (error: any) {
    console.error('获取营养分析数据失败:', error);

    // 详细的错误信息
    if (error.response) {
      console.error('响应状态:', error.response.status);
      console.error('响应数据:', error.response.data);

      // 如果是404错误，说明API路径不对
      if (error.response.status === 404) {
        ElMessage.error('API接口不存在，请检查后端服务');
      } else if (error.response.status === 400) {
        const errorData = error.response.data;
        if (errorData.msg && errorData.msg.includes('没有饮食记录')) {
          reportSummary.value = '今日尚未记录饮食，请先添加饮食记录进行分析。';
          nutritionIndicators.value = [];
          showEmptyChart();
        } else {
          ElMessage.error(errorData.msg || '请求参数错误');
        }
      } else {
        ElMessage.error('服务器错误: ' + error.response.status);
      }
    } else if (error.request) {
      console.error('请求未收到响应:', error.request);
      ElMessage.error('网络连接失败，请检查网络连接');
    } else {
      console.error('请求配置错误:', error.message);
      ElMessage.error('请求配置错误: ' + error.message);
    }
  } finally {
    loading.value = false;
  }
};

// 显示空的图表
const showEmptyChart = () => {
  if (!chartInstance.value) return;

  const option = {
    title: {
      text: '暂无饮食记录',
      subtext: '请先添加饮食记录',
      left: 'center',
      top: 'center',
      textStyle: {
        fontSize: 18,
        color: '#999'
      },
      subtextStyle: {
        fontSize: 14,
        color: '#ccc'
      }
    },
    graphic: {
      type: 'text',
      left: 'center',
      top: '40%',
      style: {
        text: '📊',
        fontSize: 60,
        fill: '#ddd'
      }
    }
  };

  chartInstance.value.setOption(option);
};

// 更新图表
const updateChart = (chartData: any) => {
  if (!chartInstance.value) return;

  console.log('更新图表数据:', chartData);

  // 检查是否有餐次饼图和营养素饼图
  if (chartData.mealChart && chartData.nutrientChart) {
    const mealChartData = chartData.mealChart;
    const nutrientChartData = chartData.nutrientChart;

    const option = {
      title: [
        {
          text: mealChartData.title,
          left: '25%',
          top: '10%',
          textAlign: 'center'
        },
        {
          text: nutrientChartData.title,
          left: '75%',
          top: '10%',
          textAlign: 'center'
        }
      ],
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: {c}kcal ({d}%)'
      },
      legend: [
        {
          orient: 'vertical',
          left: '10%',
          top: '20%',
          data: mealChartData.seriesData?.map((item: any) => item.name) || []
        },
        {
          orient: 'vertical',
          left: '60%',
          top: '20%',
          data: nutrientChartData.seriesData?.map((item: any) => item.name) || []
        }
      ],
      series: [
        {
          name: '各餐次热量占比',
          type: 'pie',
          radius: ['30%', '50%'],
          center: ['25%', '50%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 10,
            borderColor: '#fff',
            borderWidth: 2
          },
          label: {
            show: false,
            position: 'center'
          },
          emphasis: {
            label: {
              show: true,
              fontSize: 16,
              fontWeight: 'bold'
            },
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          },
          labelLine: {
            show: false
          },
          data: mealChartData.seriesData || []
        },
        {
          name: '营养素热量构成',
          type: 'pie',
          radius: ['30%', '50%'],
          center: ['75%', '50%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 10,
            borderColor: '#fff',
            borderWidth: 2
          },
          label: {
            show: false,
            position: 'center'
          },
          emphasis: {
            label: {
              show: true,
              fontSize: 16,
              fontWeight: 'bold'
            },
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          },
          labelLine: {
            show: false
          },
          data: nutrientChartData.seriesData || []
        }
      ]
    };

    chartInstance.value.setOption(option);
  } else if (chartData.data && chartData.legend) {
    // 处理单饼图格式
    const option = {
      title: {
        text: chartTitle.value,
        left: 'center',
        textStyle: {
          color: '#2d8659',
          fontSize: 18
        }
      },
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: {c} ({d}%)'
      },
      legend: {
        orient: 'vertical',
        left: 'left',
        data: chartData.legend
      },
      series: [
        {
          name: '营养摄入',
          type: 'pie',
          radius: ['40%', '70%'],
          avoidLabelOverlap: false,
          data: chartData.data,
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }
      ]
    };

    chartInstance.value.setOption(option);
  } else {
    showEmptyChart();
  }
};

// 监听日期变化
watch(selectedDate, (newDate) => {
  console.log('日期变化，重新获取数据:', newDate);
  fetchNutritionData();
});

// 初始化
onMounted(() => {
  initChart();
  fetchNutritionData();
});
</script>

<style scoped>
/* 基础样式保持不变 */
.analysis-container {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 120px);
}

.analysis-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  gap: 20px;
}

.title {
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.time-label {
  font-size: 16px;
  font-weight: 500;
  color: #409eff;
  background: #ecf5ff;
  padding: 6px 12px;
  border-radius: 4px;
  border: 1px solid #b3d8ff;
}

.date-picker {
  width: 200px;
}

.nutrition-cards {
  margin-bottom: 20px;
}

.other-nutrition-cards {
  margin-bottom: 20px;
}

.indicator-card {
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  border: none !important;
}

.indicator-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.12);
}

.card-content {
  display: flex;
  align-items: center;
  padding: 15px;
}

.icon {
  font-size: 24px;
  margin-right: 12px;
}

.info {
  flex: 1;
}

.value {
  font-size: 18px;
  font-weight: bold;
}

.value-success {
  color: #67c23a;
}

.value-warning {
  color: #e6a23c;
}

.value-danger {
  color: #f56c6c;
}

.value-info {
  color: #909399;
}

.label {
  font-size: 14px;
  color: #606266;
  margin: 4px 0;
}

.target {
  font-size: 12px;
  color: #909399;
}

.progress {
  padding: 0 15px 15px;
}

.chart-container {
  margin-bottom: 20px;
}

.chart-card {
  border-radius: 12px;
  overflow: hidden;
  min-height: 500px;
}

.card-header-flex {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 报告部分样式增强 */
.report-card {
  border-radius: 12px;
  overflow: hidden;
  border: none;
}

.report-header {
  background: linear-gradient(45deg, #f8f9fa, #e9ecef);
  padding: 12px 20px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-right {
  color: #666;
  font-size: 14px;
}

.report-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.overall-status-tag {
  font-weight: 600;
  border-radius: 4px;
}

.report-date {
  font-size: 14px;
  color: #666;
}

.report-content {
  padding: 20px;
}

/* 总结区域 */
.summary-section {
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 25px;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
}

.summary-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
}

.summary-success::before {
  background: linear-gradient(45deg, #67c23a, #85ce61);
}

.summary-warning::before {
  background: linear-gradient(45deg, #e6a23c, #ebb563);
}

.summary-danger::before {
  background: linear-gradient(45deg, #f56c6c, #f78989);
}

.summary-info::before {
  background: linear-gradient(45deg, #909399, #a6a9ad);
}

.summary-neutral::before {
  background: linear-gradient(45deg, #409eff, #66b1ff);
}

.summary-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.summary-body {
  display: flex;
  align-items: flex-start;
  gap: 20px;
}

.summary-text-wrapper {
  flex: 1;
  background: rgba(255, 255, 255, 0.9);
  padding: 15px;
  border-radius: 8px;
  border: 1px solid #ebeef5;
}

.summary-text {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  margin: 0;
}

.summary-icon {
  font-size: 48px;
  opacity: 0.3;
  margin-top: 10px;
}

/* 详细分析区域 */
.details-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.status-legend {
  display: flex;
  gap: 16px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.legend-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.legend-dot.success {
  background: #67c23a;
}

.legend-dot.warning {
  background: #e6a23c;
}

.legend-dot.danger {
  background: #f56c6c;
}

.legend-text {
  font-size: 12px;
  color: #606266;
}

.analysis-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.item-icon {
  font-size: 18px;
}

.item-text {
  font-weight: 500;
}

.status-wrapper {
  display: flex;
  align-items: center;
}

.status-tag {
  border-radius: 12px;
  padding: 2px 8px;
  font-weight: 500;
}

/* 建议区域 */
.suggestion-wrapper {
  padding: 12px;
  border-radius: 8px;
  margin: 4px 0;
}

.suggestion-success {
  background: linear-gradient(45deg, #f0f9eb, #edf7e7);
  border: 1px solid #e1f3d8;
}

.suggestion-warning {
  background: linear-gradient(45deg, #fdf6ec, #fcf2e7);
  border: 1px solid #faecd8;
}

.suggestion-danger {
  background: linear-gradient(45deg, #fef0f0, #fdeeeb);
  border: 1px solid #fde2e2;
}

.suggestion-info {
  background: linear-gradient(45deg, #f4f4f5, #f0f1f2);
  border: 1px solid #e9e9eb;
}

.suggestion-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.suggestion-icon {
  font-size: 16px;
}

.suggestion-success .suggestion-icon {
  color: #67c23a;
}

.suggestion-warning .suggestion-icon {
  color: #e6a23c;
}

.suggestion-danger .suggestion-icon {
  color: #f56c6c;
}

.suggestion-info .suggestion-icon {
  color: #909399;
}

.suggestion-title {
  font-weight: 600;
  font-size: 14px;
}

.suggestion-text {
  color: #606266;
  line-height: 1.5;
  font-size: 14px;
  margin: 0;
}

/* 健康小贴士 */
.health-tips {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px dashed #ebeef5;
}

.tips-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 16px;
  margin-top: 15px;
}

.tip-card {
  background: white;
  padding: 16px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  display: flex;
  gap: 12px;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.tip-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.tip-icon {
  font-size: 24px;
  color: #409eff;
  display: flex;
  align-items: center;
}

.tip-content {
  flex: 1;
}

.tip-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 6px 0;
}

.tip-text {
  font-size: 13px;
  color: #606266;
  line-height: 1.4;
  margin: 0;
}

/* 表格行样式增强 */
:deep(.el-table .status-success) {
  --el-table-tr-bg-color: rgba(103, 194, 58, 0.05);
}

:deep(.el-table .status-warning) {
  --el-table-tr-bg-color: rgba(230, 162, 60, 0.05);
}

:deep(.el-table .status-danger) {
  --el-table-tr-bg-color: rgba(245, 108, 108, 0.05);
}

:deep(.el-table .status-info) {
  --el-table-tr-bg-color: rgba(144, 147, 153, 0.05);
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 15px;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
}

/* 加载状态 */
.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.8);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
}
</style>
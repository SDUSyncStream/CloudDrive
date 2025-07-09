<template>
  <div class="admin-dashboard-view">
    <h2>控制台概览</h2>

    <el-row :gutter="20" class="dashboard-summary">
      <el-col :span="6">
        <el-card shadow="hover" class="summary-card">
          <div class="card-icon users-icon">
            <el-icon><UserFilled /></el-icon>
          </div>
          <div class="card-content">
            <div class="card-title">总用户数</div>
            <div class="card-value">{{ overviewData.totalUsers !== null ? overviewData.totalUsers : '--' }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="summary-card">
          <div class="card-icon storage-icon">
            <el-icon><DataLine /></el-icon>
          </div>
          <div class="card-content">
            <div class="card-title">总存储用量</div>
            <div class="card-value">{{ formatFileSize(overviewData.totalStorageUsed) }}</div>
            <div class="card-sub-value">
              (已分配: {{ formatFileSize(overviewData.totalStorageQuota) }})
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="summary-card">
          <div class="card-icon files-icon">
            <el-icon><Files /></el-icon>
          </div>
          <div class="card-content">
            <div class="card-title">文件总数</div>
            <div class="card-value">{{ overviewData.totalFiles !== null ? overviewData.totalFiles : '--' }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="summary-card">
          <div class="card-icon new-users-icon">
            <el-icon><User /></el-icon>
          </div>
          <div class="card-content">
            <div class="card-title">近7日新增用户</div>
            <div class="card-value">{{ overviewData.newUsersLast7Days !== null ? overviewData.newUsersLast7Days : '--' }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="dashboard-charts">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>存储空间使用趋势</span>
            </div>
          </template>
          <div class="chart-placeholder" ref="storageChart">
            <p v-if="loadingCharts">图表加载中...</p>
            <p v-else-if="!storageTrendData.labels.length">暂无数据</p>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>用户增长趋势</span>
            </div>
          </template>
          <div class="chart-placeholder" ref="userGrowthChart">
            <p v-if="loadingCharts">图表加载中...</p>
            <p v-else-if="!userGrowthData.labels.length">暂无数据</p>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import {
  UserFilled,
  DataLine,
  Files,
  User
} from '@element-plus/icons-vue'

import { formatFileSize } from '../utils' // 确保你的 utils 文件中有这个函数
import * as echarts from 'echarts'
import type { ECharts } from 'echarts'; // 引入 ECharts 类型

// --- 响应式数据 ---
const loadingCharts = ref(true); // 控制图表加载状态

// 概览数据，初始值设为 null 或 0，表示待加载
const overviewData = reactive({
  totalUsers: null as number | null,
  totalStorageUsed: null as number | null,
  totalStorageQuota: null as number | null,
  totalFiles: null as number | null,
  newUsersLast7Days: null as number | null,
});

// 存储空间使用趋势数据，初始为空
const storageTrendData = reactive<{ labels: string[]; data: number[] }>({
  labels: [],
  data: []
});

// 用户增长趋势数据，初始为空
const userGrowthData = reactive<{ labels: string[]; data: number[] }>({
  labels: [],
  data: []
});

// --- ECharts 实例引用 ---
const storageChart = ref<HTMLElement | null>(null);
const userGrowthChart = ref<HTMLElement | null>(null);
let myStorageChart: ECharts | null = null;
let myUserGrowthChart: ECharts | null = null;

// --- 方法 ---

// 初始化图表的方法
const initCharts = () => {
  if (storageChart.value && !myStorageChart) {
    myStorageChart = echarts.init(storageChart.value);
  }
  if (userGrowthChart.value && !myUserGrowthChart) {
    myUserGrowthChart = echarts.init(userGrowthChart.value);
  }

  // 配置存储空间使用趋势图
  if (myStorageChart) {
    myStorageChart.setOption({
      // title: { text: '存储空间使用趋势', left: 'center' }, // 根据需求决定是否保留标题
      tooltip: {
        trigger: 'axis',
        formatter: function (params: any) {
          if (params.length > 0) {
            const param = params[0];
            return `${param.name}<br/>${param.seriesName}: ${formatFileSize(param.value * 1024 * 1024 * 1024)}`; // 将GB转换为字节后格式化
          }
          return '';
        }
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        boundaryGap: false, // 让折线图从Y轴开始
        data: storageTrendData.labels
      },
      yAxis: {
        type: 'value',
        name: '存储 (GB)', // 明确Y轴单位
        axisLabel: {
          formatter: '{value}' // 直接显示数值，格式化在tooltip中处理
        }
      },
      series: [
        {
          name: '存储用量',
          type: 'line',
          smooth: true, // 平滑曲线
          data: storageTrendData.data,
          areaStyle: {
            // 区域填充
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(103, 194, 58, 0.3)' },
              { offset: 1, color: 'rgba(103, 194, 58, 0)' }
            ])
          },
          itemStyle: {
            color: '#67c23a' // 折线颜色
          }
        }
      ]
    });
    myStorageChart.resize(); // 确保图表适应容器大小
  }

  // 配置用户增长趋势图
  if (myUserGrowthChart) {
    myUserGrowthChart.setOption({
      // title: { text: '用户增长趋势', left: 'center' }, // 根据需求决定是否保留标题
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow'
        }
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: userGrowthData.labels,
        axisTick: {
          alignWithLabel: true
        }
      },
      yAxis: {
        type: 'value',
        name: '用户数'
      },
      series: [
        {
          name: '新增用户',
          type: 'bar',
          barWidth: '60%',
          data: userGrowthData.data,
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#409eff' },
              { offset: 1, color: '#79bbff' }
            ])
          },
          emphasis: {
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: '#337ecc' },
                { offset: 1, color: '#66b1ff' }
              ])
            }
          }
        }
      ]
    });
    myUserGrowthChart.resize(); // 确保图表适应容器大小
  }
};

// 获取仪表盘数据
const fetchDashboardData = async () => {
  loadingCharts.value = true;
  try {
    const response = await fetch('/admin-api/dashboard/all-data', {
      headers: {
        'Content-Type': 'application/json',
        // 'Authorization': `Bearer ${localStorage.getItem('adminAccessToken')}` // 如果需要认证
      }
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const result = await response.json();

    if (result.code === 200) {
      const data = result.data;

      // 更新概览数据
      overviewData.totalUsers = data.overview.totalUsers;
      overviewData.totalStorageUsed = data.overview.totalStorageUsed;
      overviewData.totalStorageQuota = data.overview.totalStorageQuota;
      overviewData.totalFiles = data.overview.totalFiles;
      overviewData.newUsersLast7Days = data.overview.newUsersLast7Days;

      // 更新存储趋势数据
      storageTrendData.labels = data.storageTrend.map((item: { date: string }) => {
        // 假设日期格式是 "YYYY-MM-DD"，这里提取月份和日期 "MM-DD"
        return item.date.substring(5); // "07-04"
      });
      storageTrendData.data = data.storageTrend.map((item: { storageUsed: number }) => {
        // 将字节转换为GB，以便图表显示，保留一位小数
        return parseFloat((item.storageUsed / (1024 * 1024 * 1024)).toFixed(1));
      });

      // 更新用户增长趋势数据
      userGrowthData.labels = data.userGrowthTrend.map((item: { date: string }) => {
        // 假设日期格式是 "YYYY-MM-DD"，这里提取月份和日期 "MM-DD"
        return item.date.substring(5);
      });
      userGrowthData.data = data.userGrowthTrend.map((item: { userCount: number }) => item.userCount);

      // 数据更新后重新初始化/更新图表
      initCharts();

    } else {
      ElMessage.error(result.message || '获取控制台数据失败！');
    }
  } catch (error) {
    console.error('获取控制台数据错误:', error);
    ElMessage.error('获取控制台数据失败，请检查网络连接。');
  } finally {
    loadingCharts.value = false;
  }
};

// --- 生命周期钩子 ---
onMounted(() => {
  // 在组件挂载后立即获取数据
  fetchDashboardData();

  // 监听窗口大小变化以重绘 ECharts 图表
  window.addEventListener('resize', () => {
    myStorageChart?.resize();
    myUserGrowthChart?.resize();
  });
});
</script>

<style scoped>
.admin-dashboard-view {
  padding: 20px;
  background-color: #f0f2f5; /* 与主布局的 el-main 背景色一致 */
}

h2 {
  font-size: 24px;
  color: #333;
  margin-bottom: 20px;
}

/* 概览卡片区域 */
.dashboard-summary {
  margin-bottom: 20px;
}

.summary-card {
  display: flex;
  align-items: center;
  padding: 20px;
  height: 120px; /* 统一卡片高度 */
  border-radius: 8px; /* 圆角 */
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05); /* 柔和阴影 */
  transition: all 0.3s ease; /* 过渡效果 */
}

.summary-card:hover {
  transform: translateY(-3px); /* 悬停上浮效果 */
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
}

.card-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%; /* 圆形图标背景 */
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  font-size: 32px;
  color: #fff;
}

.users-icon { background: linear-gradient(45deg, #409eff, #79bbff); } /* 蓝色 */
.storage-icon { background: linear-gradient(45deg, #67c23a, #95d475); } /* 绿色 */
.files-icon { background: linear-gradient(45deg, #e6a23c, #f5d6a7); } /* 黄色 */
.new-users-icon { background: linear-gradient(45deg, #f56c6c, #fab6b6); } /* 红色 */

.card-content {
  flex-grow: 1;
}

.card-title {
  font-size: 16px;
  color: #606266;
  margin-bottom: 8px;
}

.card-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.card-sub-value {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

/* 图表区域 */
.dashboard-charts {
  margin-bottom: 20px;
}

.dashboard-charts .el-card {
  height: 400px; /* 为图表预留高度 */
  display: flex;
  flex-direction: column;
}

.dashboard-charts .el-card :deep(.el-card__header) {
  padding-bottom: 12px; /* 调整头部内边距 */
}

.dashboard-charts .el-card :deep(.el-card__body) {
  flex-grow: 1; /* 使 body 填充剩余空间 */
  display: flex;
  /* 移除之前示例的居中p标签的样式，让echarts填充 */
  align-items: center; /* 移除 */
  justify-content: center; /* 移除 */
  font-size: 16px; /* 移除 */
  color: #909399; /* 移除 */
  padding: 10px; /* 调整内边距 */
}

.chart-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: #f8f8f8; /* 浅色背景 */
  border-radius: 4px;
  color: #a0a0a0;
  font-style: italic;
}

/* 表格区域 */
.dashboard-tables .el-card {
  min-height: 350px; /* 为表格预留最小高度 */
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}
</style>
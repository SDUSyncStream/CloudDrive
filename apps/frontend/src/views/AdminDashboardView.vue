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
            <div class="card-value">{{ totalUsers }}</div>
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
            <div class="card-value">{{ formatFileSize(totalStorageUsed) }}</div>
            <div class="card-sub-value">
              (已分配: {{ formatFileSize(totalStorageAllocated) }})
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
            <div class="card-value">{{ totalFiles }}</div>
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
            <div class="card-value">{{ newUsersLastWeek }}</div>
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
            <p>（这里将是**存储空间使用趋势图**，例如折线图）</p>
            <p>示例数据: {{ storageTrendData }}</p>
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
            <p>（这里将是**用户增长趋势图**，例如柱状图）</p>
            <p>示例数据: {{ userGrowthData }}</p>
          </div>
        </el-card>
      </el-col>
    </el-row>

<!--    <el-row :gutter="20" class="dashboard-tables">-->
<!--      <el-col :span="24">-->
<!--        <el-card shadow="hover">-->
<!--          <template #header>-->
<!--            <div class="card-header">-->
<!--              <span>近期操作日志</span>-->
<!--              <el-button type="text" @click="goToSystemLogs">查看更多</el-button>-->
<!--            </div>-->
<!--          </template>-->
<!--          <el-table :data="recentLogs" style="width: 100%" max-height="300">-->
<!--            <el-table-column prop="time" label="时间" width="180"></el-table-column>-->
<!--            <el-table-column prop="user" label="用户/系统" width="180"></el-table-column>-->
<!--            <el-table-column prop="action" label="操作内容"></el-table-column>-->
<!--            <el-table-column prop="ip" label="IP地址" width="150"></el-table-column>-->
<!--          </el-table>-->
<!--        </el-card>-->
<!--      </el-col>-->
<!--    </el-row>-->
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import {
  UserFilled, // 填充用户图标
  DataLine,   // 数据线图标（表示存储/数据）
  Files,      // 多个文件图标
  User        // 用户图标
} from '@element-plus/icons-vue' // 从 Element Plus 导入图标

import { formatFileSize } from '../utils' // 确保你的 utils 文件中有这个函数
import * as echarts from 'echarts';
const router = useRouter()

// --- 示例数据 ---
// 实际项目中，这些数据会从后端 API 获取
const totalUsers = ref(5280) // 总用户数量
const totalStorageUsed = ref(380 * 1024 * 1024 * 1024) // 380 GB
const totalStorageAllocated = ref(800 * 1024 * 1024 * 1024) // 800 GB
const totalFiles = ref(1250000) // 文件总数量
const newUsersLastWeek = ref(128) // 最近一周新增用户数量

// 存储空间使用趋势数据（示例 Echarts 数据格式）
const storageTrendData = ref({
  labels: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
  data: [350, 360, 370, 375, 380, 385, 390] // 单位：GB
})

// 用户增长趋势数据（示例 Echarts 数据格式）
const userGrowthData = ref({
  labels: ['1月', '2月', '3月', '4月', '5月', '6月'],
  data: [1000, 1200, 1500, 1800, 2200, 2500] // 每月总用户数
})

// 近期操作日志（示例数据）
const recentLogs = ref([
  { time: '2025-07-07 10:30', user: 'admin', action: '创建了新用户: user001', ip: '192.168.1.100' },
  { time: '2025-07-07 09:15', user: 'system', action: '存储清理任务完成', ip: 'N/A' },
  { time: '2025-07-06 18:00', user: 'user005', action: '上传了文件: report.pdf', ip: '203.0.113.50' },
  { time: '2025-07-06 14:20', user: 'admin', action: '修改了用户 user003 的存储配额', ip: '192.168.1.100' },
  { time: '2025-07-05 11:00', user: 'system', action: '数据库备份成功', ip: 'N/A' },
])

// --- 图表引用 ---
// ref 用于获取 DOM 元素，以便初始化 Echarts
const storageChart = ref<HTMLElement | null>(null)
const userGrowthChart = ref<HTMLElement | null>(null)

// --- 方法 ---

// 初始化图表的方法（需要 Echarts 库）
const initCharts = () => {
  // 在实际项目中，你会在这里导入 echarts 并初始化图表
  // 例如：



  if (storageChart.value) {
    const myStorageChart = echarts.init(storageChart.value);
    myStorageChart.setOption({
      title: { text: '存储空间使用趋势' },
      tooltip: {},
      xAxis: { data: storageTrendData.value.labels },
      yAxis: { type: 'value', name: '存储 (GB)' },
      series: [{
        name: '使用量',
        type: 'line',
        data: storageTrendData.value.data
      }]
    });
  }

  if (userGrowthChart.value) {
    const myUserGrowthChart = echarts.init(userGrowthChart.value);
    myUserGrowthChart.setOption({
      title: { text: '用户增长趋势' },
      tooltip: {},
      xAxis: { data: userGrowthData.value.labels },
      yAxis: { type: 'value', name: '用户数' },
      series: [{
        name: '用户数',
        type: 'bar',
        data: userGrowthData.value.data
      }]
    });
  }

  console.log('--- 提示：图表功能需要安装 Echarts 等库并在此处初始化 ---');
};

// 跳转到系统日志页面
const goToSystemLogs = () => {
  router.push('/admin/logs'); // 假设系统日志的路由是 /admin/logs
};

// 组件挂载后执行
onMounted(() => {
  // 在这里可以发起 API 请求来获取真实的统计数据
  // fetchDashboardData();

  // 初始化图表（如果 Echarts 已安装）
  initCharts();
});

// 模拟获取数据（实际会是 API 请求）
const fetchDashboardData = async () => {
  // try {
  //   const response = await fetch('/api/admin/dashboard-stats');
  //   const data = await response.json();
  //   if (data.code === 200) {
  //     totalUsers.value = data.data.totalUsers;
  //     totalStorageUsed.value = data.data.totalStorageUsed;
  //     totalStorageAllocated.value = data.data.totalStorageAllocated;
  //     totalFiles.value = data.data.totalFiles;
  //     newUsersLastWeek.value = data.data.newUsersLastWeek;
  //     storageTrendData.value = data.data.storageTrend;
  //     userGrowthData.value = data.data.userGrowth;
  //     recentLogs.value = data.data.recentLogs;
  //     // 数据更新后重新初始化图表
  //     initCharts();
  //   } else {
  //     ElMessage.error('获取控制台数据失败: ' + data.message);
  //   }
  // } catch (error) {
  //   console.error('获取控制台数据错误:', error);
  //   ElMessage.error('无法连接到服务器，请检查网络。');
  // }
};

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
  align-items: center;
  justify-content: center;
  font-size: 16px;
  color: #909399;
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
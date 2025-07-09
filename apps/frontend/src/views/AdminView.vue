<template>
  <div class="common-layout">
    <el-container>
      <el-header height="60px" class="main-header">
        <div class="header-left">
          <el-icon>
            <CloudDownload />
          </el-icon>
          <span>CloudDrive 管理后台</span>
        </div>
        <div class="header-actions">
<!--          <el-button type="primary" :icon="Bell" circle title="通知"></el-button>-->

          <el-dropdown @command="handleAdminAction">
            <el-avatar :src="adminStore.avatar || undefined">
              {{ adminStore.username?.charAt(0).toUpperCase() || 'A' }}
            </el-avatar>
            <template #dropdown>
              <el-dropdown-menu>
                <div class="admin-info-display">
                  <div class="admin-text">
                    <span>{{ adminStore.username || '管理员' }}</span>
                    <span class="admin-level"> (等级: {{ adminStore.userLevel || 'N/A' }})</span>
                  </div>
                  <div class="storage-info" v-if="adminStore.storageQuota !== null">
                    <div class="storage-text">
                      <span class="storage-used">{{ formatFileSize(adminStore.storageUsed || 0) }}</span>
                      <span class="storage-total">/ {{ formatFileSize(adminStore.storageQuota || 0) }}</span>
                    </div>
                    <el-progress
                        :percentage="storagePercentage"
                        :color="getStorageColor(storagePercentage)"
                        :stroke-width="6"
                        :show-text="false"
                    />
                    <div class="storage-upgrade" v-if="storagePercentage > 80">
                      <el-link type="primary" @click="handleAdminAction('upgrade-storage')">
                        <el-icon><Star /></el-icon>
                        提升管理后台容量
                      </el-link>
                    </div>
                  </div>
                </div>
                <el-divider style="margin: 8px 0;" />
                <el-dropdown-item command="admin-profile">个人设置</el-dropdown-item>
                <el-dropdown-item command="system-logs">系统日志</el-dropdown-item>
                <el-dropdown-item divided command="admin-logout">退出管理</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-container>
        <el-aside width="200px">
          <el-menu :default-active="activeIndex" class="el-menu-vertical-demo" @select="handleSelect" router>
            <el-menu-item index="/admin/dashboard">
              <el-icon><Monitor /></el-icon>
              <span>控制台</span>
            </el-menu-item>
            <el-menu-item index="/admin/users">
              <el-icon><User /></el-icon>
              <span>用户管理</span>
            </el-menu-item>
            <el-menu-item index="/admin/files">
              <el-icon><Folder /></el-icon>
              <span>文件管理</span>
            </el-menu-item>
            <el-menu-item index="/admin/orders">
              <el-icon><Tickets /></el-icon>
              <span>订单管理</span>
            </el-menu-item>
            <el-menu-item index="/admin/dish">
              <el-icon><Files /></el-icon>
              <span>套餐设置</span>
            </el-menu-item>
          </el-menu>
        </el-aside>
        <el-main>
          <RouterView></RouterView>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script lang="ts" setup>
import {
  Bell,
  Monitor,       // 控制台图标
  User,          // 用户图标
  Folder,        // 文件夹图标
  Setting,       // 设置图标
  Lock,          // 锁图标
  Star, Shop, List, Tickets, Files           // 星星图标
} from '@element-plus/icons-vue' // 导入所需的 Element Plus 图标

import { useAdminStore } from '../stores/admin' //
import { ElMessage } from 'element-plus'
import { useRouter, useRoute } from 'vue-router'
import { ref, watch, onMounted, computed } from 'vue'

// 具函数来格式化文件大小
import { formatFileSize } from '../utils'

const adminStore = useAdminStore() // 获取管理员 Store 实例
const router = useRouter()
const route = useRoute()

// 侧边菜单当前激活项
const activeIndex = ref('/admin/dashboard') // 默认激活控制台


// 存储容量数据 (从 adminStore 获取，或者在组件内部计算，这里为了演示，使用store中的数据)
// 确保 adminStore 中有 storageUsed 和 storageQuota 字段
const storageUsed = computed(() => adminStore.storageUsed || 0)
const storageTotal = computed(() => adminStore.storageQuota || 0)


// 计算存储使用百分比
const storagePercentage = computed(() => {
  if (storageTotal.value === 0) return 0; // 避免除以零
  return Math.round((storageUsed.value / storageTotal.value) * 100)
})

// 获取存储进度条颜色
const getStorageColor = (percentage: number) => {
  if (percentage < 50) return '#67C23A' // 绿色
  if (percentage < 80) return '#E6A23C' // 黄色
  return '#F56C6C' // 红色
}

// 监听路由变化，更新激活的菜单项
// 当路由改变时，确保侧边菜单的选中状态正确
watch(() => route.path, (newPath) => {
  // 检查新路径是否以 '/admin/' 开头，并设置为激活项
  if (newPath.startsWith('/admin/')) {
    activeIndex.value = newPath
  }
}, { immediate: true }) // immediate: true 确保在组件初次挂载时也执行一次

// 组件挂载时执行
onMounted(() => {
  // 确保管理员信息已加载，如果没有（例如直接刷新），可能需要重定向到登录页
  if (!adminStore.isLoggedIn) {
    ElMessage.warning('请先登录管理后台。')
    router.push('/admin-login') // 未登录则跳转到管理员登录页
  } else {
    router.push('/admin/dashboard') // 已登录则跳转到控制台
    // 如果需要，可以在这里触发重新加载管理员详细信息或存储配额的API
    // 例如：adminStore.fetchAdminDetails()
  }
})

/**
 * 处理管理员操作下拉菜单的命令
 * @param command 下拉菜单项的 command 值
 */
const handleAdminAction = (command: string) => {
  switch (command) {
    case 'admin-profile':
      router.push('/admin/profile') // 跳转到管理员个人设置页
      break
    case 'system-logs':
      router.push('/admin/logs') // 跳转到系统日志页
      break
    case 'upgrade-storage': // 示例，管理员后台可能不需要升级容量功能
      ElMessage.info('管理员后台容量管理功能待开发。')
      break;
    case 'admin-logout':
      adminStore.clearAdminLoginInfo() // 调用 Store 的 action 清除管理员状态
      ElMessage.success('已退出管理后台')
      router.push('/admin-login') // 退出后跳转回管理员登录页
      break
    default:
      break
  }
}

/**
 * 侧边菜单选择事件处理器
 * @param index 被选中菜单项的 index (即路由路径)
 * @param indexPath 选中菜单项的路径数组
 */
const handleSelect = (index: string, indexPath: string[]) => {
  // activeIndex.value = index // watch 已经处理
  router.push(index) // 根据选中项的 index 进行路由跳转
  console.log('菜单选中:', index, indexPath)
}

// 注意：handleOpen 和 handleClose 对于非SubMenu的el-menu-item可能不触发，这里保留
// const handleOpen = (key: string, keyPath: string[]) => {
//     console.log(key, keyPath)
// }
// const handleClose = (key: string, keyPath: string[]) => {
//     console.log(key, keyPath)
// }
</script>

<style scoped>
.common-layout {
  height: 100vh; /* 使整个布局占据视口高度 */
  display: flex;
  flex-direction: column;
}

.el-container {
  flex: 1; /* 使内部容器填充剩余空间 */
}

.main-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  border-bottom: 1px solid #e4e7ed;
  background-color: #ffffff; /* 白色背景 */
  color: #303133;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.08); /* 增加一点阴影 */
  z-index: 100; /* 确保在最上层 */
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px; /* 标题字号更大 */
  font-weight: 700; /* 更粗的字体 */
  color: #303133;
}
.header-left .el-icon {
  font-size: 24px; /* 图标稍大 */
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

/* 调整通知按钮的尺寸和颜色 */
.header-actions .el-button.is-circle {
  width: 40px;
  height: 40px;
  min-width: unset; /* 移除min-width限制 */
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px; /* 图标大小 */
  background-color: #67C23A; /* 绿色系 */
  border-color: #67C23A;
}
.header-actions .el-button.is-circle:hover {
  background-color: #5cb85c; /* 悬停颜色 */
  border-color: #5cb85c;
}

/* 调整头像大小 */
.el-avatar {
  width: 36px;
  height: 36px;
  font-size: 16px;
  line-height: 36px; /* 确保文字垂直居中 */
  background-color: #409eff; /* 默认蓝色背景 */
  cursor: pointer;
  margin-left: 8px; /* 与通知按钮的间距 */
}

.el-aside {
  border-right: 1px solid #e4e7ed;
  background-color: #ffffff;
  padding-top: 10px; /* 菜单顶部留白 */
}

.el-menu-vertical-demo {
  border-right: none; /* 移除菜单自带的右边框 */
  width: 100%; /* 确保菜单宽度占满侧边栏 */
}

.el-menu-item {
  height: 50px; /* 菜单项高度 */
  line-height: 50px;
  font-size: 16px; /* 菜单项字体大小 */
}

.el-menu-item .el-icon {
  font-size: 20px; /* 菜单项图标大小 */
  margin-right: 8px;
}

/* 确保主内容区有背景色，方便区分 */
.el-main {
  background-color: #f0f2f5; /* 浅灰色背景 */
  padding: 20px;
  overflow-y: auto; /* 允许内容滚动 */
}

/* 下拉菜单内的管理员信息显示样式 */
.admin-info-display {
  padding: 12px 16px;
  min-width: 220px; /* 确保信息有足够空间显示 */
}
.admin-text {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.admin-level {
  font-size: 12px;
  color: #909399;
  font-weight: normal;
}

/* 存储容量显示样式 (与HomeView类似，但可能数据源不同) */
.storage-info {
  padding: 8px 0; /* 内部不需要额外的padding */
  border-top: 1px solid #ebeef5; /* 与上方管理员信息分隔 */
  margin-top: 8px;
}

.storage-info .storage-text {
  margin-bottom: 6px;
  font-size: 12px;
}

.storage-info .el-progress {
  margin-bottom: 6px;
}

.storage-upgrade {
  margin-top: 6px;
  text-align: center;
}

.storage-upgrade .el-link {
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}
</style>
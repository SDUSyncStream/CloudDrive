<template>
    <div class="common-layout">
        <el-container>
            <el-header height="60px" class="main-header">
                <div class="header-left">
                    <el-icon>
                        <CloudDownload />
                    </el-icon>
                    CloudDrive
                </div>
                <div class="header-actions">
                    <!--令button为圆形-->
                    <el-button type="primary" :icon="Bell" circle title="通知"></el-button>
                    <!-- <el-button :icon="FolderAdd">新建文件夹</el-button> -->
                    <el-dropdown @command="handleUserAction">
                        <el-avatar :src="avatar || undefined">
                            {{ userStore.user?.username?.charAt(0).toUpperCase() }}
                        </el-avatar>
                        <template #dropdown>
                            <el-dropdown-menu>
                                <div class="storage-info">
                                    <div class="storage-text">
                                        <span class="storage-used">{{ formatFileSize(storageUsed) }}</span>
                                        <span class="storage-total">/ {{ formatFileSize(storageQuota) }}</span>
                                    </div>
                                    <el-progress 
                                        :percentage="storagePercentage" 
                                        :color="getStorageColor(storagePercentage)"
                                        :stroke-width="6"
                                        :show-text="false"
                                    />
                                    <div class="storage-upgrade" v-if="storagePercentage > 80">
                                        <el-link type="primary" @click="handleUserAction('vip')">
                                            <el-icon><Star /></el-icon>
                                            升级容量
                                        </el-link>
                                    </div>
                                </div>
                                <el-divider style="margin: 8px 0;" />
                                <el-dropdown-item command="profile">个人资料</el-dropdown-item>
                                <el-dropdown-item command="vip">会员中心</el-dropdown-item>
                                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                            </el-dropdown-menu>
                        </template>
                    </el-dropdown>
                </div>
            </el-header>

            <el-container>
                <el-aside width="200px">
                    <el-menu :default-active="activeIndex" class="el-menu-vertical-demo" @open="handleOpen" @close="handleClose" @select="handleSelect" router>
                        <el-menu-item index="/main/files">
                            <el-icon>
                                <Folder />
                            </el-icon>
                            <span>我的文件</span>
                        </el-menu-item>
                        <el-menu-item index="/main/shared">
                            <el-icon>
                                <Share />
                            </el-icon>
                            <span>我的分享</span>
                        </el-menu-item>
                        <el-menu-item index="/main/deleted">
                            <el-icon>
                                <DeleteFilled />
                            </el-icon>
                            <span>回收站</span>
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
import { Bell, Notification, UploadFilled, Star } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'
import { useRouter, useRoute } from 'vue-router'
import { ref, watch, onMounted, computed } from 'vue'
import { formatFileSize } from '../utils'
<<<<<<< Updated upstream
import { logout, getAvatarAndStorage } from '@/api/auth'
=======
import axios from "axios";
>>>>>>> Stashed changes

const userStore = useUserStore()
const router = useRouter()
const route = useRoute()

const activeIndex = ref('/main/files')

// 存储容量数据
const storageUsed = ref(0) // 5GB (示例数据)
const storageQuota = ref(0) // 20GB (示例数据)
const avatar = ref('')

// 计算存储使用百分比
const storagePercentage = computed(() => {
    return Math.round((storageUsed.value / storageQuota.value) * 100)
})

// 获取存储进度条颜色
const getStorageColor = (percentage: number) => {
    if (percentage < 50) return '#67C23A'
    if (percentage < 80) return '#E6A23C'
    return '#F56C6C'
}

// 监听路由变化，更新激活的菜单项
watch(() => route.path, (newPath) => {
    activeIndex.value = newPath
}, { immediate: true })

// 组件挂载时设置正确的激活菜单项
onMounted(() => {
    activeIndex.value = route.path
    // 获取用户存储使用情况
    // fetchStorageInfo()
    getAvatarAndStorage(localStorage.getItem('UserId') || '').then(response => {
        if (response.data.data) {
            storageUsed.value = parseInt(response.data.data.storageUsed) || 0
            storageQuota.value = parseInt(response.data.data.storageQuota) || 0
            avatar.value = response.data.data.avatar || ''
        }
    }).catch(error => {
        console.error('获取用户头像和存储信息失败:', error)
    })
})

// 获取存储使用情况
// const fetchStorageInfo = async () => {
//     try {
//         // TODO: 调用API获取真实的存储数据
//         // const response = await api.get('/user/storage')
//         // storageUsed.value = response.data.used
//         // storageTotal.value = response.data.total
        
//         // 模拟从用户store获取数据
//         const userPlan = (userStore.user as any)?.plan || 'free'
//         switch (userPlan) {
//             case 'free':
//                 storageTotal.value = 5 * 1024 * 1024 * 1024 // 5GB
//                 break
//             case 'premium':
//                 storageTotal.value = 100 * 1024 * 1024 * 1024 // 100GB
//                 break
//             case 'professional':
//                 storageTotal.value = 1024 * 1024 * 1024 * 1024 // 1TB
//                 break
//             default:
//                 storageTotal.value = 5 * 1024 * 1024 * 1024 // 默认5GB
//         }
//     } catch (error) {
//         console.error('获取存储信息失败:', error)
//     }
// }



const handleUserAction = (command: string) => {
    switch (command) {
        case 'profile':
            router.push('/profile')
            break
        case 'vip':
            router.push('/vip')
            break
        case 'logout':
            logout().then(() => {
                userStore.logout()
                ElMessage.success('已成功退出登录')
                router.push('/login')
            }).catch(error => {
                console.error('退出登录失败:', error)
                ElMessage.error('退出登录失败，请稍后重试')
            })
            break
        default:
            break
    }
}

const handleOpen = (key: string, keyPath: string[]) => {
    console.log(key, keyPath)
}

const handleClose = (key: string, keyPath: string[]) => {
    console.log(key, keyPath)
}

const handleSelect = (key: string, keyPath: string[]) => {
    activeIndex.value = key
    router.push(key)
    console.log(key, keyPath)

}
</script>

<style scoped>
.main-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0 20px;
    border-bottom: 1px solid #e4e7ed;
}

.header-left {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 18px;
    font-weight: 600;
    color: #303133;
}

.header-actions {
    display: flex;
    align-items: center;
    gap: 16px;
}

.el-menu-vertical-demo {
    border-right: none;
}

.el-aside {
    border-right: 1px solid #e4e7ed;
}

/* 存储容量显示样式 */
.storage-info {
    padding: 12px 16px;
    min-width: 200px;
}

.storage-text {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;
    font-size: 12px;
}

.storage-used {
    font-weight: 600;
    color: #303133;
}

.storage-total {
    color: #909399;
}

.storage-upgrade {
    margin-top: 8px;
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

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
                        <el-avatar :src="userStore.user?.avatar || undefined">
                            {{ userStore.user?.username?.charAt(0).toUpperCase() }}
                        </el-avatar>
                        <template #dropdown>
                            <el-dropdown-menu>
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
import { Bell, Notification, UploadFilled } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'
import { useRouter, useRoute } from 'vue-router'
import { ref, watch, onMounted } from 'vue'
const userStore = useUserStore()
const router = useRouter()
const route = useRoute()

const activeIndex = ref('/main/files')

// 监听路由变化，更新激活的菜单项
watch(() => route.path, (newPath) => {
    activeIndex.value = newPath
}, { immediate: true })

// 组件挂载时设置正确的激活菜单项
onMounted(() => {
    activeIndex.value = route.path
})


const handleUserAction = (command: string) => {
    switch (command) {
        case 'profile':
            router.push('/profile')
            break
        case 'vip':
            router.push('/vip')
            break
        case 'logout':
            userStore.logout()
            ElMessage.success('已退出登录')
            router.push('/login')
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
</style>

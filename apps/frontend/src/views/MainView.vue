<template>
    <div class="common-layout">
        <el-container>
            <el-header height="60px">
                <el-icon>
                    <CloudDownload />
                </el-icon>
                CloudDrive
                <div class="header-actions">
                    <!--令button为圆形-->
                    <el-button type="primary" :icon="Bell" circle></el-button>
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

                    <el-menu default-active="2" class="el-menu-vertical-demo" @open="handleOpen" @close="handleClose">
                        <el-menu-item index="1">
                            <el-icon>
                                <Folder />
                            </el-icon>
                            <span>我的文件</span>
                        </el-menu-item>
                        <el-menu-item index="2">
                            <el-icon>
                                <Share />
                            </el-icon>
                            <span>我的分享</span>
                        </el-menu-item>
                        <el-menu-item index="3">
                            <el-icon>
                                <DeleteFilled />
                            </el-icon>
                            <span>回收站</span>
                        </el-menu-item>
                    </el-menu>
                </el-aside>
                <el-main>
                    <el-header>
                        <div class="header">
                            <div class="main-header">
                                <el-button type="primary"><el-icon><Upload /></el-icon>上传文件</el-button>
                                <el-button><el-icon><FolderAdd /></el-icon>新建文件夹</el-button>
                            </div>
                        </div>
                    </el-header>
                    <el-table :data="tableData" stripe style="width: 100%">
                        <el-table-column type="selection" width="50" />
                        <el-table-column prop="date" label="Date" width="180" />
                        <el-table-column prop="name" label="Name" width="180" />
                        <el-table-column prop="address" label="Address" />
                    </el-table>
                </el-main>
            </el-container>
        </el-container>
    </div>
</template>

<script lang="ts" setup>
import { Bell, Notification, UploadFilled } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
const userStore = useUserStore()
const router = useRouter()

const tableData = [
    {
        date: '2016-05-03',
        name: 'Tom',
        address: 'No. 189, Grove St, Los Angeles',
    },
    {
        date: '2016-05-02',
        name: 'Tom',
        address: 'No. 189, Grove St, Los Angeles',
    },
    {
        date: '2016-05-04',
        name: 'Tom',
        address: 'No. 189, Grove St, Los Angeles',
    },
    {
        date: '2016-05-01',
        name: 'Tom',
        address: 'No. 189, Grove St, Los Angeles',
    },
]
const handleUserAction = (command: string) => {
    switch (command) {
        case 'profile':
            router.push('/profile')
            break
        case 'vip':
            router.push('/settings')
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
</script>

<style scoped>
.header-actions {
    float: right;
    display: flex;
    align-items: center;
    gap: 12px;
}

.main-header {
    display: flex;
    align-items: center;
    gap: 12px;
}
</style>

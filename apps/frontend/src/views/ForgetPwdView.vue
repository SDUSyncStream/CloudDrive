<template>
    <div class="miss-page">
        <el-card class="miss-card">
            <template #header>
                <div class="miss-header">
                    <el-icon size="32" color="#409eff">
                        <User />
                    </el-icon>
                    <span>找回 CloudDrive 账号</span>
                </div>
            </template>

            <el-form :model="missForm" :rules="rules" ref="formRef" @submit.prevent="handlemiss">
                <el-form-item prop="username">
                    <el-input v-model="missForm.username" placeholder="用户名" prefix-icon="User" size="large" />
                </el-form-item>

                <el-form-item prop="email">
                    <el-input v-model="missForm.email" placeholder="邮箱" prefix-icon="Message" size="large"
                        type="email" />
                </el-form-item>

                <el-form-item>
                    <el-button type="primary" size="large" style="width: 100%" :loading="loading" @click="handlemiss">
                        找回密码
                    </el-button>
                </el-form-item>
            </el-form>
            <div class="miss-footer">
                <el-link type="info" href="/login">
                    <el-icon>
                        <ArrowLeft />
                    </el-icon>
                    返回
                </el-link>
            </div>
        </el-card>

    </div>
</template>

<script lang="ts" setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance } from 'element-plus'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref<FormInstance>()
const loading = ref(false)

const missForm = reactive({
    username: '',
    email: ''
})

const rules = {
    username: [
        { required: true, message: '请输入用户名或邮箱', trigger: 'blur' }
    ],
    password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, message: '密码长度至少6位', trigger: 'blur' }
    ],
    email: [
        { required: true, message: '请输入邮箱', trigger: 'blur' },
        { type: 'email', message: '请输入有效的邮箱地址', trigger: ['blur', 'change'] }
    ]
}

const handlemiss = async () => {
    if (!formRef.value) return

    try {
        await formRef.value.validate()
        loading.value = true

        // TODO: 调用登录API
        // const response = await api.post('/auth/miss', missForm)

        // 模拟登录成功
        const mockUser = {
            id: 'user-1',
            username: missForm.username,
            email: 'user@example.com',
            avatar: '',
            createdAt: new Date(),
            updatedAt: new Date()
        }

        const mockToken = 'mock-jwt-token'

        userStore.setUser(mockUser)
        userStore.setToken(mockToken)

        ElMessage.success('注册成功')
        router.push('/login')

    } catch (error) {
        ElMessage.error('注册失败')
    } finally {
        loading.value = false
    }
}
</script>

<style scoped>
.miss-page {
    height: 100vh;
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.miss-card {
    width: 400px;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.miss-header {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 12px;
    font-size: 20px;
    font-weight: 600;
    color: #2c3e50;
}

.miss-footer {
    display: flex;
    justify-content: space-between;
    margin-top: 20px;
}
</style>
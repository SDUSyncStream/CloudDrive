<template>
    <div class="miss-page">
        <el-card class="miss-card">
            <template #header>
                <div class="miss-header">
                    <el-icon size="32" color="#409eff">
                        <Lock />
                    </el-icon>
                    <span>重置密码</span>
                </div>
            </template>

            <el-form :model="resetForm" :rules="rules" ref="formRef" @submit.prevent="handleResetPassword">
                <!-- 邮箱输入 -->
                <el-form-item prop="email">
                    <el-input 
                        v-model="resetForm.email" 
                        placeholder="请输入邮箱地址" 
                        prefix-icon="Message" 
                        size="large"
                        type="email" 
                    />
                </el-form-item>

                <!-- 验证码输入 -->
                <el-form-item prop="verificationCode">
                    <div class="verification-row">
                        <el-input 
                            v-model="resetForm.verificationCode" 
                            placeholder="请输入6位验证码" 
                            prefix-icon="Key" 
                            size="large"
                            maxlength="6"
                            class="verification-input"
                        />
                        <el-button 
                            type="primary" 
                            plain 
                            size="large"
                            :disabled="countdown > 0 || sendingCode"
                            :loading="sendingCode"
                            @click="sendVerificationCode"
                            class="send-code-btn"
                        >
                            {{ countdown > 0 ? `${countdown}s后重发` : '发送验证码' }}
                        </el-button>
                    </div>
                </el-form-item>

                <!-- 新密码输入 -->
                <el-form-item prop="newPassword">
                    <el-input 
                        v-model="resetForm.newPassword" 
                        placeholder="请输入新密码" 
                        prefix-icon="Lock" 
                        size="large"
                        type="password"
                        show-password
                    />
                </el-form-item>

                <!-- 确认新密码 -->
                <el-form-item prop="confirmPassword">
                    <el-input 
                        v-model="resetForm.confirmPassword" 
                        placeholder="请确认新密码" 
                        prefix-icon="Lock" 
                        size="large"
                        type="password"
                        show-password
                    />
                </el-form-item>

                <!-- 重置按钮 -->
                <el-form-item>
                    <el-button 
                        type="primary" 
                        size="large" 
                        style="width: 100%" 
                        :loading="loading" 
                        @click="handleResetPassword"
                    >
                        重置密码
                    </el-button>
                </el-form-item>
            </el-form>
            
            <div class="miss-footer">
                <el-link type="info" @click="router.push('/login')">
                    <el-icon>
                        <ArrowLeft />
                    </el-icon>
                    返回登录
                </el-link>
            </div>
        </el-card>
    </div>
</template>

<script lang="ts" setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance } from 'element-plus'
import { Lock, Message, Key, ArrowLeft } from '@element-plus/icons-vue'
import { getVerifyCode,verifyCode,rePwd } from '@/api/auth'
import { encryptPassword } from '@/utils/crypto'

const router = useRouter()

const formRef = ref<FormInstance>()
const loading = ref(false)
const sendingCode = ref(false)
const countdown = ref(0)

const resetForm = reactive({
    email: '',
    verificationCode: '',
    newPassword: '',
    confirmPassword: ''
})

// 验证规则
const rules = {
    email: [
        { required: true, message: '请输入邮箱地址', trigger: 'blur' },
        { type: 'email', message: '请输入有效的邮箱地址', trigger: ['blur', 'change'] }
    ],
    verificationCode: [
        { required: true, message: '请输入验证码', trigger: 'blur' },
        { len: 6, message: '验证码为6位数字', trigger: 'blur' },
        { pattern: /^\d{6}$/, message: '验证码必须为6位数字', trigger: 'blur' }
    ],
    newPassword: [
        { required: true, message: '请输入新密码', trigger: 'blur' },
        { min: 6, message: '密码长度至少6位', trigger: 'blur' }
    ],
    confirmPassword: [
        { required: true, message: '请确认新密码', trigger: 'blur' },
        {
            validator: (rule: any, value: string, callback: Function) => {
                if (value !== resetForm.newPassword) {
                    callback(new Error('两次输入的密码不一致'))
                } else {
                    callback()
                }
            },
            trigger: 'blur'
        }
    ]
}

// 发送验证码
const sendVerificationCode = async () => {
    // 先验证邮箱
    if (!resetForm.email) {
        ElMessage.warning('请先输入邮箱地址')
        return
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    if (!emailRegex.test(resetForm.email)) {
        ElMessage.warning('请输入有效的邮箱地址')
        return
    }

    try {
        sendingCode.value = true
        console.log('发送验证码到邮箱:', resetForm.email)

        const response = await getVerifyCode({
            email: resetForm.email
        })

        console.log('发送验证码响应:', response)

        if (response.data.success === true) {
            ElMessage.success('验证码已发送到您的邮箱，请查收')
            startCountdown()
        } else {
            ElMessage.error(response.data.message || '验证码发送失败')
        }
    } catch (error: any) {
        console.error('发送验证码错误:', error)
        if (error.response?.status === 404) {
            ElMessage.error('该邮箱未注册')
        } else if (error.response?.status === 429) {
            ElMessage.error('发送过于频繁，请稍后再试')
        } else {
            ElMessage.error(error.response?.data?.message || '验证码发送失败，请稍后重试')
        }
    } finally {
        sendingCode.value = false
    }
}

// 开始倒计时
const startCountdown = () => {
    countdown.value = 60
    const timer = setInterval(() => {
        countdown.value--
        if (countdown.value <= 0) {
            clearInterval(timer)
        }
    }, 1000)
}

// 重置密码
const handleResetPassword = async () => {
    if (!formRef.value) return

    try {
        await formRef.value.validate()
        loading.value = true

        console.log('开始重置密码...')

        // 加密新密码
        const encryptedPassword = await encryptPassword(resetForm.newPassword)
        console.log('密码加密完成')

        // 调用重置密码API
        const response = await verifyCode({
            email: resetForm.email,
            code: resetForm.verificationCode,
            type: 'FORGOT_PASSWORD'
        })

        console.log('重置密码响应:', response)

        if (response.data.success === true) {
            
            const res = await rePwd({
                email: resetForm.email,
                newPassword: encryptedPassword
            })
            if(res.data.code === 200)
            {
                ElMessage.success('密码重置成功，请使用新密码登录')
                router.push('/login')
            //提交更新密码操作
            }
            
        } else {
            ElMessage.error(response.data.message || '密码重置失败')
        }
    } catch (error: any) {
        console.error('重置密码错误:', error)
        if (error.response?.status === 400) {
            ElMessage.error('验证码错误或已过期')
        } else if (error.response?.status === 404) {
            ElMessage.error('该邮箱未注册')
        } else {
            ElMessage.error(error.response?.data?.message || '密码重置失败，请稍后重试')
        }
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
    width: 420px;
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

.verification-row {
    display: flex;
    gap: 10px;
    width: 100%;
}

.verification-input {
    flex: 1;
}

.send-code-btn {
    width: 120px;
    white-space: nowrap;
}

.miss-footer {
    display: flex;
    justify-content: center;
    margin-top: 20px;
}

.miss-footer .el-link {
    display: flex;
    align-items: center;
    gap: 4px;
}

/* 响应式设计 */
@media (max-width: 480px) {
    .miss-card {
        width: 90%;
        margin: 0 5%;
    }
    
    .verification-row {
        flex-direction: column;
        gap: 8px;
    }
    
    .send-code-btn {
        width: 100%;
    }
}
</style>
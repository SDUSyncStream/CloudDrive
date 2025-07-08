// src/stores/admin.ts
import { defineStore } from 'pinia'

// 定义管理员用户信息的接口
interface AdminUserState {
    userId: string | null;
    username: string | null;
    userLevel: number | null;
    email: string | null;
    avatar: string | null;
    storageQuota: number | null; // bigint 映射到 number
    storageUsed: number | null;  // bigint 映射到 number
    accessToken: string | null;
    refreshToken: string | null;
    isLoggedIn: boolean;
}

export const useAdminStore = defineStore('admin', {
    state: (): AdminUserState => ({
        // 从 localStorage 恢复时，使用 ?? null 确保是 null 而不是 undefined，避免类型错误
        userId: localStorage.getItem('adminUserId') ?? null,
        username: localStorage.getItem('adminUsername') ?? null,
        userLevel: localStorage.getItem('adminUserLevel') ? parseInt(localStorage.getItem('adminUserLevel')!) : null,
        email: localStorage.getItem('adminEmail') ?? null,
        avatar: localStorage.getItem('adminAvatar') ?? null,
        storageQuota: localStorage.getItem('adminStorageQuota') ? parseInt(localStorage.getItem('adminStorageQuota')!) : null,
        storageUsed: localStorage.getItem('adminStorageUsed') ? parseInt(localStorage.getItem('adminStorageUsed')!) : null,
        accessToken: localStorage.getItem('adminAccessToken') ?? null,
        refreshToken: localStorage.getItem('adminRefreshToken') ?? null,
        isLoggedIn: !!localStorage.getItem('adminAccessToken'),
    }),

    getters: {
        // ...
    },

    actions: {
        /**
         * 设置管理员登录信息
         * @param userData 包含所有后端返回的用户信息和Token
         */
        setAdminLoginInfo(userData: {
            userId: string;
            username: string;
            userLevel: number;
            // 将所有可能为 null 或 undefined 的字段都标记为可选 "?"
            // 或者在赋值时进行空值处理
            email?: string | null;         // <-- 标记为可选
            avatar?: string | null;
            storageQuota?: number | null;
            storageUsed?: number | null;
            accessToken?: string | null;
            refreshToken?: string | null;
        }) {
            this.userId = userData.userId;
            this.username = userData.username;
            this.userLevel = userData.userLevel;

            // 使用空值合并运算符 ?? 确保赋值为 null 而不是 undefined
            this.email = userData.email ?? null;
            this.avatar = userData.avatar ?? null;
            this.storageQuota = userData.storageQuota ?? null;
            this.storageUsed = userData.storageUsed ?? null;
            this.accessToken = userData.accessToken ?? null;
            this.refreshToken = userData.refreshToken ?? null;
            this.isLoggedIn = true;

            // 同时保存到 localStorage，对可能为 null 的值进行防御性处理
            // localStorage.setItem 只能存储字符串，所以 null/undefined 需要处理
            localStorage.setItem('adminUserId', userData.userId);
            localStorage.setItem('adminUsername', userData.username);
            localStorage.setItem('adminUserLevel', userData.userLevel.toString()); // number需要转string

            // 只有当值非空时才存入 localStorage
            if (userData.email !== undefined && userData.email !== null) {
                localStorage.setItem('adminEmail', userData.email);
            } else {
                localStorage.removeItem('adminEmail'); // 如果后端没返回或返回null，就清掉
            }
            if (userData.avatar !== undefined && userData.avatar !== null) {
                localStorage.setItem('adminAvatar', userData.avatar);
            } else {
                localStorage.removeItem('adminAvatar');
            }
            if (userData.storageQuota !== undefined && userData.storageQuota !== null) {
                localStorage.setItem('adminStorageQuota', userData.storageQuota.toString());
            } else {
                localStorage.removeItem('adminStorageQuota');
            }
            if (userData.storageUsed !== undefined && userData.storageUsed !== null) {
                localStorage.setItem('adminStorageUsed', userData.storageUsed.toString());
            } else {
                localStorage.removeItem('adminStorageUsed');
            }
            if (userData.accessToken !== undefined && userData.accessToken !== null) {
                localStorage.setItem('adminAccessToken', userData.accessToken);
            } else {
                localStorage.removeItem('adminAccessToken');
            }
            if (userData.refreshToken !== undefined && userData.refreshToken !== null) {
                localStorage.setItem('adminRefreshToken', userData.refreshToken);
            } else {
                localStorage.removeItem('adminRefreshToken');
            }
        },

        clearAdminLoginInfo() {
            // ... (清空逻辑保持不变，确保移除所有相关的 localStorage 项) ...
            this.userId = null;
            this.username = null;
            this.userLevel = null;
            this.email = null;
            this.avatar = null;
            this.storageQuota = null;
            this.storageUsed = null;
            this.accessToken = null;
            this.refreshToken = null;
            this.isLoggedIn = false;

            localStorage.removeItem('adminUserId');
            localStorage.removeItem('adminUsername');
            localStorage.removeItem('adminUserLevel');
            localStorage.removeItem('adminEmail');
            localStorage.removeItem('adminAvatar');
            localStorage.removeItem('adminStorageQuota');
            localStorage.removeItem('adminStorageUsed');
            localStorage.removeItem('adminAccessToken');
            localStorage.removeItem('adminRefreshToken');
        }
    }
})
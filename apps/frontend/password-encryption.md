# 密码加密功能说明

## 实现功能

已经为登录和注册功能添加了密码加密：

### 1. 加密算法
- 使用 Web Crypto API 的 SHA-256 算法
- 输出长度固定为64个十六进制字符（256位哈希）
- 确保加密后长度不超过255字符

### 2. 修改的文件

#### `src/utils/crypto.ts`
- `encryptPassword(password: string)`: 异步SHA-256加密函数
- `simpleHash(password: string)`: 同步简单哈希函数（备用）

#### `src/views/RegisterView.vue`
- 在发送注册请求前对密码进行加密
- 发送 `passwordHash` 字段到后端
- 添加了详细的错误处理和日志

#### `src/views/LoginView.vue`
- 在发送登录请求前对密码进行加密
- 发送 `passwordHash` 字段到后端
- 保持原有的错误处理逻辑

## 使用示例

```typescript
import { encryptPassword } from '@/utils/crypto'

// 加密密码
const originalPassword = "myPassword123"
const encryptedPassword = await encryptPassword(originalPassword)
console.log('原始密码:', originalPassword)
console.log('加密后:', encryptedPassword)
console.log('长度:', encryptedPassword.length) // 64字符
```

## 安全特性

1. **不可逆加密**: 使用SHA-256哈希，无法从加密结果还原原始密码
2. **长度限制**: 确保加密后长度不超过255字符，符合数据库字段要求
3. **前端加密**: 密码在发送到服务器之前就已加密，增加安全性

## 测试步骤

1. 打开注册页面，输入用户信息
2. 查看浏览器控制台，确认密码加密日志
3. 检查网络请求，确认发送的是加密后的 `passwordHash`
4. 测试登录功能，验证加密一致性

## 注意事项

- 后端需要接收 `passwordHash` 字段而不是 `password`
- 加密是单向的，后端应该存储哈希值进行比较
- 如需要盐值或更复杂的加密，可以扩展 `crypto.ts` 文件

## 兼容性

- 支持所有现代浏览器（支持Web Crypto API）
- 提供了简单哈希函数作为降级方案
- TypeScript类型安全

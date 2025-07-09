/**
 * 密码加密函数
 * 使用SHA-256算法对密码进行加密
 * @param password 原始密码
 * @returns 加密后的密码（最长255字符）
 */
export async function encryptPassword(password: string): Promise<string> {
  // 将字符串转换为 ArrayBuffer
  const encoder = new TextEncoder()
  const data = encoder.encode(password)
  
  // 使用 Web Crypto API 进行 SHA-256 哈希
  const hashBuffer = await crypto.subtle.digest('SHA-256', data)
  
  // 将 ArrayBuffer 转换为十六进制字符串
  const hashArray = Array.from(new Uint8Array(hashBuffer))
  const hashHex = hashArray.map(b => b.toString(16).padStart(2, '0')).join('')
  
  // 确保长度不超过255字符
  return hashHex.substring(0, 255)
}

/**
 * 简单的哈希函数（同步版本，用于兼容性）
 * @param password 原始密码
 * @returns 简单哈希后的密码
 */
export function simpleHash(password: string): string {
  let hash = 0
  if (password.length === 0) return hash.toString()
  
  for (let i = 0; i < password.length; i++) {
    const char = password.charCodeAt(i)
    hash = ((hash << 5) - hash) + char
    hash = hash & hash // 转换为32位整数
  }
  
  // 转换为正数并限制长度
  const hashStr = Math.abs(hash).toString(36) + Date.now().toString(36)
  return hashStr.substring(0, 255)
}

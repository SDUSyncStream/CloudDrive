package cn.sdu.clouddrive.fileshare.pojo.po;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户信息（适配新users表结构）
 */
public class UserInfo implements Serializable {
    private String id; // 主键
    private Integer userlevel; // 用户等级
    private String username; // 用户名
    private String email; // 邮箱
    private String passwordHash; // 密码哈希
    private String avatar; // 头像
    private Long storageQuota; // 总配额
    private Long storageUsed; // 已用空间
    private Date createdAt; // 创建时间
    private Date updatedAt; // 更新时间

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Integer getUserlevel() { return userlevel; }
    public void setUserlevel(Integer userlevel) { this.userlevel = userlevel; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public Long getStorageQuota() { return storageQuota; }
    public void setStorageQuota(Long storageQuota) { this.storageQuota = storageQuota; }

    public Long getStorageUsed() { return storageUsed; }
    public void setStorageUsed(Long storageUsed) { this.storageUsed = storageUsed; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "UserInfo{" +
                "id='" + id + '\'' +
                ", userlevel=" + userlevel +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", passwordHash='" + passwordHash + '\'' +
                ", avatar='" + avatar + '\'' +
                ", storageQuota=" + storageQuota +
                ", storageUsed=" + storageUsed +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}

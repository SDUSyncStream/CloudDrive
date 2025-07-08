package cn.sdu.clouddrive.admin.pojo;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;


@Data // Lombok 注解，自动生成 getter, setter, toString, equals, hashCode 方法
@TableName("users") // 指定实体类对应的数据库表名
public class User { // 建议将类名改为 User，更符合Java命名规范

    @TableId(value = "id") // 主键映射，value指定字段名，type指定ID生成策略
    private String id; // 数据库id是varchar(36)

    @TableField("userlevel") // 映射数据库 userlevel 字段
    private Integer userlevel; // 数据库是 int

    @TableField("username") // 映射数据库 username 字段
    private String username; // 数据库是 varchar(50)

    @TableField("email") // 映射数据库 email 字段
    private String email; // 数据库是 varchar(100)

    @TableField("password_hash") // 映射数据库 password_hash 字段
    private String passwordHash; // 数据库是 varchar(255)，Java命名规范将下划线转为驼峰

    @TableField("avatar") // 映射数据库 avatar 字段
    private String avatar; // 数据库是 varchar(255)

    @TableField("storage_quota") // 映射数据库 storage_quota 字段
    private Long storageQuota; // 数据库是 bigint，对应Java的Long

    @TableField("storage_used") // 映射数据库 storage_used 字段
    private Long storageUsed; // 数据库是 bigint，对应Java的Long

    @TableField("created_at") // 映射数据库 created_at 字段
    private LocalDateTime createdAt; // 数据库是 timestamp，对应Java 8的LocalDateTime

    @TableField("updated_at") // 映射数据库 updated_at 字段
    private LocalDateTime updatedAt; // 数据库是 timestamp，对应Java 8的LocalDateTime

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getUserlevel() {
        return userlevel;
    }

    public void setUserlevel(Integer userlevel) {
        this.userlevel = userlevel;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Long getStorageQuota() {
        return storageQuota;
    }

    public void setStorageQuota(Long storageQuota) {
        this.storageQuota = storageQuota;
    }

    public Long getStorageUsed() {
        return storageUsed;
    }

    public void setStorageUsed(Long storageUsed) {
        this.storageUsed = storageUsed;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

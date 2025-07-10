package cn.sdu.clouddrive.membership.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("users")
public class User {
    @TableId(value = "id")
    private String id;
    
    @TableField("userlevel")
    private Integer userlevel;
    
    @TableField("username")
    private String username;
    
    @TableField("email")
    private String email;
    
    @TableField("password_hash")
    private String passwordHash;
    
    @TableField("avatar")
    private String avatar;
    
    @TableField("storage_quota")
    private Long storageQuota;
    
    @TableField("storage_used")
    private Long storageUsed;
    
    @TableField("created_at")
    private LocalDateTime createdAt;
    
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
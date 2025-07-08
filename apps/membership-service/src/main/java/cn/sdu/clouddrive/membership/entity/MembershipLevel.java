package cn.sdu.clouddrive.membership.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("membership_levels")
public class MembershipLevel {
    
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;
    
    private String name;
    
    private Long storageQuota;
    
    private Long maxFileSize;
    
    private BigDecimal price;
    
    private Integer durationDays;
    
    private String features;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
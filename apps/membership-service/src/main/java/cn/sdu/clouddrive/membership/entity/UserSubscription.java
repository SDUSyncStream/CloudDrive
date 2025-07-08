package cn.sdu.clouddrive.membership.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("user_subscriptions")
public class UserSubscription {
    
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;
    
    private String userId;
    
    private String membershipLevelId;
    
    private LocalDateTime startDate;
    
    private LocalDateTime endDate;
    
    private String status; // active, expired, cancelled
    
    private String paymentMethod;
    
    private BigDecimal paymentAmount;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
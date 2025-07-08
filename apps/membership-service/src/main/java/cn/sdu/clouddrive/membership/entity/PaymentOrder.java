package cn.sdu.clouddrive.membership.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("payment_orders")
public class PaymentOrder {
    
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;
    
    private String userId;
    
    private String membershipLevelId;
    
    private String orderNumber;
    
    private BigDecimal amount;
    
    private String paymentMethod; // alipay, wechat, bank_card, bank_transfer
    
    private String status; // pending, paid, failed, cancelled
    
    private String transactionId;
    
    private LocalDateTime paidAt;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
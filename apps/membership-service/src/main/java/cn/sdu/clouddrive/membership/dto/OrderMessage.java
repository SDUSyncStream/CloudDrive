package cn.sdu.clouddrive.membership.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderMessage implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String orderId;
    private String orderNumber;
    private String userId;
    private String membershipLevelId;
    private BigDecimal amount;
    private String paymentMethod;
    private LocalDateTime createdAt;
    private LocalDateTime expireAt;
    
    public OrderMessage() {}
    
    public OrderMessage(String orderId, String orderNumber, String userId, 
                       String membershipLevelId, BigDecimal amount, 
                       String paymentMethod, LocalDateTime createdAt, 
                       LocalDateTime expireAt) {
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.userId = userId;
        this.membershipLevelId = membershipLevelId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.createdAt = createdAt;
        this.expireAt = expireAt;
    }
}
package cn.sdu.clouddrive.membership.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentOrderDTO {
    private String id;
    private String userId;
    private String membershipLevelId;
    private String membershipLevelName;
    private String orderNumber;
    private BigDecimal amount;
    private String paymentMethod;
    private String status;
    private String transactionId;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
}
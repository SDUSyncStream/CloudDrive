package cn.sdu.clouddrive.membership.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UserSubscriptionDTO {
    private String id;
    private String userId;
    private String membershipLevelId;
    private String membershipLevelName;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String status;
    private String paymentMethod;
    private BigDecimal paymentAmount;
    private LocalDateTime createdAt;
    private Boolean isActive;
    private Long daysRemaining;
}
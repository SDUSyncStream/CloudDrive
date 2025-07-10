// src/main/java/cn/sdu/clouddrive/admin/pojo/SubscriptionUpdateEmailMessage.java
package cn.sdu.clouddrive.admin.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionUpdateEmailMessage implements Serializable { // 实现 Serializable 便于在网络中传输
    private String userId;
    private String userEmail;
    private String membershipLevelId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String status;
    private String paymentAmount; // 字符串表示金额，避免精度问题或直接用 BigDecimal
    private String username; // 添加用户名，方便邮件称呼


}
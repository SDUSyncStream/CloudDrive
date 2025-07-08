package cn.sdu.clouddrive.membership.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class CreatePaymentOrderRequest {
    
    @NotBlank(message = "用户ID不能为空")
    private String userId;
    
    @NotBlank(message = "会员等级ID不能为空")
    private String membershipLevelId;
    
    @NotBlank(message = "支付方式不能为空")
    private String paymentMethod; // alipay, wechat, bank_card, bank_transfer
}
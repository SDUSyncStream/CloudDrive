package cn.sdu.clouddrive.membership.controller;

import cn.sdu.clouddrive.membership.common.Result;
import cn.sdu.clouddrive.membership.dto.CreatePaymentOrderRequest;
import cn.sdu.clouddrive.membership.dto.PaymentOrderDTO;
import cn.sdu.clouddrive.membership.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order-test")
public class OrderTestController {

    @Autowired
    private PaymentService paymentService;

    /**
     * 测试创建订单（会自动发送到RabbitMQ队列）
     */
    @PostMapping("/create-test-order")
    public Result<PaymentOrderDTO> createTestOrder(@RequestParam(required = false) String userId) {
        CreatePaymentOrderRequest request = new CreatePaymentOrderRequest();
        request.setUserId(userId != null ? userId : "292ddee0-518c-4ff9-9eb8-3feabbcaff27");
        request.setMembershipLevelId("level002"); // 使用标准版测试
        request.setPaymentMethod("alipay");
        
        try {
            PaymentOrderDTO order = paymentService.createTestPaymentOrder(request);
            return Result.success("测试订单创建成功，24小时后将自动取消", order);
        } catch (Exception e) {
            return Result.error("创建测试订单失败: " + e.getMessage());
        }
    }

    /**
     * 测试支付订单（会从队列中移除消息）
     */
    @PostMapping("/pay-order/{orderId}")
    public Result<PaymentOrderDTO> payTestOrder(@PathVariable String orderId) {
        try {
            PaymentOrderDTO order = paymentService.processPayment(orderId, "test-transaction-" + System.currentTimeMillis());
            return Result.success("订单支付成功", order);
        } catch (Exception e) {
            return Result.error("支付订单失败: " + e.getMessage());
        }
    }

    /**
     * 查看订单状态
     */
    @GetMapping("/order-status/{orderId}")
    public Result<PaymentOrderDTO> getOrderStatus(@PathVariable String orderId) {
        PaymentOrderDTO order = paymentService.getPaymentOrder(orderId);
        if (order == null) {
            return Result.error("订单不存在");
        }
        return Result.success("获取订单状态成功", order);
    }

    /**
     * 获取用户所有订单
     */
    @GetMapping("/user-orders/{userId}")
    public Result<?> getUserOrders(@PathVariable String userId) {
        try {
            return Result.success("获取用户订单成功", paymentService.getUserPaymentOrders(userId));
        } catch (Exception e) {
            return Result.error("获取用户订单失败: " + e.getMessage());
        }
    }
}
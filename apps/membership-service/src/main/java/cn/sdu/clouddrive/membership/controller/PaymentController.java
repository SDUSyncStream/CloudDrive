package cn.sdu.clouddrive.membership.controller;

import cn.sdu.clouddrive.membership.common.Result;
import cn.sdu.clouddrive.membership.dto.CreatePaymentOrderRequest;
import cn.sdu.clouddrive.membership.dto.PaymentOrderDTO;
import cn.sdu.clouddrive.membership.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = "*")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/orders")
    public Result<PaymentOrderDTO> createPaymentOrder(@Valid @RequestBody CreatePaymentOrderRequest request) {
        try {
            PaymentOrderDTO order = paymentService.createPaymentOrder(request);
            return Result.success("订单创建成功", order);
        } catch (Exception e) {
            return Result.error("创建订单失败: " + e.getMessage());
        }
    }

    @PostMapping("/orders/{orderId}/pay")
    public Result<PaymentOrderDTO> processPayment(@PathVariable String orderId, 
                                                 @RequestParam String transactionId) {
        try {
            PaymentOrderDTO order = paymentService.processPayment(orderId, transactionId);
            return Result.success("支付成功", order);
        } catch (Exception e) {
            return Result.error("支付失败: " + e.getMessage());
        }
    }

    @GetMapping("/orders/{orderId}")
    public Result<PaymentOrderDTO> getPaymentOrder(@PathVariable String orderId) {
        PaymentOrderDTO order = paymentService.getPaymentOrder(orderId);
        if (order != null) {
            return Result.success(order);
        } else {
            return Result.error("订单不存在");
        }
    }

    @GetMapping("/orders/user/{userId}")
    public Result<List<PaymentOrderDTO>> getUserPaymentOrders(@PathVariable String userId) {
        List<PaymentOrderDTO> orders = paymentService.getUserPaymentOrders(userId);
        return Result.success(orders);
    }
}
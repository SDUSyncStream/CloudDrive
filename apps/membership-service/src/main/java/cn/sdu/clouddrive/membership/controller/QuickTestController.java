package cn.sdu.clouddrive.membership.controller;

import cn.sdu.clouddrive.membership.common.Result;
import cn.sdu.clouddrive.membership.config.RabbitMQConfig;
import cn.sdu.clouddrive.membership.dto.OrderMessage;
import cn.sdu.clouddrive.membership.entity.PaymentOrder;
import cn.sdu.clouddrive.membership.service.OrderMessageProducer;
import cn.sdu.clouddrive.membership.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/quick-test")
public class QuickTestController {

    @Autowired
    private OrderMessageProducer orderMessageProducer;
    
    @Autowired
    private PaymentService paymentService;

    /**
     * 快速测试：直接发送2分钟TTL的消息到RabbitMQ
     */
    @PostMapping("/send-quick-message")
    public Result<String> sendQuickMessage() {
        try {
            // 创建一个测试订单消息
            OrderMessage orderMessage = new OrderMessage(
                "quick-test-" + System.currentTimeMillis(),
                "QUICK-" + System.currentTimeMillis(),
                "292ddee0-518c-4ff9-9eb8-3feabbcaff27",
                "level002",
                new BigDecimal("19.99"),
                "test",
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(2) // 2分钟后过期
            );
            
            orderMessageProducer.sendOrderMessage(orderMessage);
            
            return Result.success("快速测试消息发送成功！2分钟后检查是否被消费", 
                "订单ID: " + orderMessage.getOrderId());
        } catch (Exception e) {
            return Result.error("发送测试消息失败: " + e.getMessage());
        }
    }

    /**
     * 查看RabbitMQ队列配置信息
     */
    @GetMapping("/queue-config")
    public Result<?> getQueueConfig() {
        return Result.success("RabbitMQ队列配置", new Object() {
            public final String pendingQueue = RabbitMQConfig.ORDER_PENDING_QUEUE;
            public final String pendingExchange = RabbitMQConfig.ORDER_PENDING_EXCHANGE;
            public final String dlxExchange = RabbitMQConfig.ORDER_DLX_EXCHANGE;
            public final String cancelQueue = RabbitMQConfig.ORDER_CANCEL_QUEUE;
            public final long ttlHours = RabbitMQConfig.ORDER_TTL / 1000 / 60 / 60;
        });
    }

    /**
     * 检查指定订单状态
     */
    @GetMapping("/check-order/{orderId}")
    public Result<?> checkOrder(@PathVariable String orderId) {
        try {
            PaymentOrder order = paymentService.getById(orderId);
            if (order == null) {
                return Result.error("订单不存在");
            }
            return Result.success("订单状态", new Object() {
                public final String id = order.getId();
                public final String orderNumber = order.getOrderNumber();
                public final String status = order.getStatus();
                public final LocalDateTime createdAt = order.getCreatedAt();
                public final LocalDateTime updatedAt = order.getUpdatedAt();
            });
        } catch (Exception e) {
            return Result.error("查询订单失败: " + e.getMessage());
        }
    }
}
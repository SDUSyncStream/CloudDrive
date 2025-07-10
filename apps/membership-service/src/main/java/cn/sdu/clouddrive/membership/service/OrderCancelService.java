package cn.sdu.clouddrive.membership.service;

import cn.sdu.clouddrive.membership.config.RabbitMQConfig;
import cn.sdu.clouddrive.membership.dto.OrderMessage;
import cn.sdu.clouddrive.membership.entity.PaymentOrder;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class OrderCancelService {

    @Autowired
    private PaymentService paymentService;

    /**
     * 监听死信队列，处理过期订单
     * @param orderMessage 订单消息
     * @param channel RabbitMQ通道
     * @param deliveryTag 消息投递标签
     */
    @RabbitListener(queues = RabbitMQConfig.ORDER_CANCEL_QUEUE)
    public void handleExpiredOrder(@Payload OrderMessage orderMessage,
                                   Channel channel,
                                   @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            log.info("收到过期订单消息，订单ID: {}, 订单号: {}", 
                orderMessage.getOrderId(), orderMessage.getOrderNumber());

            // 检查订单当前状态
            PaymentOrder order = paymentService.getById(orderMessage.getOrderId());
            
            if (order == null) {
                log.warn("订单不存在，订单ID: {}", orderMessage.getOrderId());
                // 确认消息
                channel.basicAck(deliveryTag, false);
                return;
            }

            // 只处理待支付状态的订单
            if (!"pending".equals(order.getStatus())) {
                log.info("订单状态不是待支付，跳过处理，订单ID: {}, 当前状态: {}", 
                    orderMessage.getOrderId(), order.getStatus());
                // 确认消息
                channel.basicAck(deliveryTag, false);
                return;
            }

            // 执行订单取消逻辑
            cancelExpiredOrder(order);
            
            log.info("订单自动取消成功，订单ID: {}, 订单号: {}", 
                order.getId(), order.getOrderNumber());

            // 确认消息处理成功
            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            log.error("处理过期订单失败，订单ID: {}, 错误: {}", 
                orderMessage.getOrderId(), e.getMessage(), e);
            
            try {
                // 拒绝消息，不重新入队（避免无限重试）
                channel.basicNack(deliveryTag, false, false);
            } catch (Exception nackException) {
                log.error("拒绝消息失败: {}", nackException.getMessage(), nackException);
            }
        }
    }

    /**
     * 取消过期订单
     * @param order 订单对象
     */
    private void cancelExpiredOrder(PaymentOrder order) {
        try {
            // 更新订单状态为已取消
            order.setStatus("cancelled");
            order.setUpdatedAt(LocalDateTime.now());
            paymentService.updateById(order);
            
            log.info("订单已设置为取消状态，订单ID: {}", order.getId());
            
            // 这里可以添加其他取消订单的业务逻辑，例如：
            // 1. 发送订单取消通知邮件
            // 2. 释放库存（如果有的话）
            // 3. 记录订单取消日志
            // 4. 统计分析数据更新
            
        } catch (Exception e) {
            log.error("取消订单失败，订单ID: {}, 错误: {}", order.getId(), e.getMessage(), e);
            throw new RuntimeException("取消订单失败: " + e.getMessage());
        }
    }
}
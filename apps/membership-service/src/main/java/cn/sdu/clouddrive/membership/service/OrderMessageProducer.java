package cn.sdu.clouddrive.membership.service;

import cn.sdu.clouddrive.membership.config.RabbitMQConfig;
import cn.sdu.clouddrive.membership.dto.OrderMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OrderMessageProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送订单消息到待处理队列
     * @param orderMessage 订单消息
     */
    public void sendOrderMessage(OrderMessage orderMessage) {
        try {
            // 设置消息属性
            MessageProperties properties = new MessageProperties();
            properties.setMessageId(orderMessage.getOrderId());
            properties.setContentType("application/json");
            
            // 发送消息到待处理队列
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.ORDER_PENDING_EXCHANGE,
                RabbitMQConfig.ORDER_PENDING_ROUTING_KEY,
                orderMessage,
                message -> {
                    message.getMessageProperties().setMessageId(orderMessage.getOrderId());
                    return message;
                }
            );
            
            log.info("订单消息发送成功，订单ID: {}, 订单号: {}", 
                orderMessage.getOrderId(), orderMessage.getOrderNumber());
                
        } catch (Exception e) {
            log.error("订单消息发送失败，订单ID: {}, 错误: {}", 
                orderMessage.getOrderId(), e.getMessage(), e);
            throw new RuntimeException("发送订单消息失败: " + e.getMessage());
        }
    }

    /**
     * 确认订单消息（支付成功时调用）
     * 由于RabbitMQ的特性，我们无法直接从队列中删除特定消息
     * 这里我们采用发送确认消息的方式来标记订单已处理
     * @param orderId 订单ID
     */
    public void confirmOrderPayment(String orderId) {
        try {
            // 这里可以发送确认消息到另一个队列，或者使用其他机制
            // 由于RabbitMQ无法直接删除队列中的特定消息，
            // 我们在消费者端通过检查订单状态来判断是否需要取消
            log.info("订单支付确认，订单ID: {}", orderId);
        } catch (Exception e) {
            log.error("订单支付确认失败，订单ID: {}, 错误: {}", orderId, e.getMessage(), e);
        }
    }
}
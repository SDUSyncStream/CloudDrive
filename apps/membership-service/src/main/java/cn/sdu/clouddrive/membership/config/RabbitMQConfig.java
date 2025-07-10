package cn.sdu.clouddrive.membership.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    // 订单相关队列和交换机名称
    public static final String ORDER_PENDING_QUEUE = "membership.order.pending.queue";
    public static final String ORDER_PENDING_EXCHANGE = "membership.order.pending.exchange";
    public static final String ORDER_PENDING_ROUTING_KEY = "order.pending";
    
    // 死信交换机和队列
    public static final String ORDER_DLX_EXCHANGE = "membership.order.dlx.exchange";
    public static final String ORDER_CANCEL_QUEUE = "membership.order.cancel.queue";
    public static final String ORDER_EXPIRED_ROUTING_KEY = "order.expired";
    
    // TTL设置：24小时 = 86400000毫秒
    public static final long ORDER_TTL = 86400000L;

    /**
     * 创建死信交换机
     */
    @Bean
    public DirectExchange orderDlxExchange() {
        return new DirectExchange(ORDER_DLX_EXCHANGE, true, false);
    }

    /**
     * 创建死信队列（订单取消队列）
     */
    @Bean
    public Queue orderCancelQueue() {
        return QueueBuilder.durable(ORDER_CANCEL_QUEUE).build();
    }

    /**
     * 绑定死信队列到死信交换机
     */
    @Bean
    public Binding orderCancelBinding() {
        return BindingBuilder.bind(orderCancelQueue())
                .to(orderDlxExchange())
                .with(ORDER_EXPIRED_ROUTING_KEY);
    }

    /**
     * 创建主交换机（订单待处理交换机）
     */
    @Bean
    public DirectExchange orderPendingExchange() {
        return new DirectExchange(ORDER_PENDING_EXCHANGE, true, false);
    }

    /**
     * 创建主队列（订单待处理队列）
     * 配置TTL和死信交换机
     */
    @Bean
    public Queue orderPendingQueue() {
        Map<String, Object> args = new HashMap<>();
        // 设置消息TTL（24小时）
        args.put("x-message-ttl", ORDER_TTL);
        // 设置死信交换机
        args.put("x-dead-letter-exchange", ORDER_DLX_EXCHANGE);
        // 设置死信路由键
        args.put("x-dead-letter-routing-key", ORDER_EXPIRED_ROUTING_KEY);
        
        return QueueBuilder.durable(ORDER_PENDING_QUEUE)
                .withArguments(args)
                .build();
    }

    /**
     * 绑定主队列到主交换机
     */
    @Bean
    public Binding orderPendingBinding() {
        return BindingBuilder.bind(orderPendingQueue())
                .to(orderPendingExchange())
                .with(ORDER_PENDING_ROUTING_KEY);
    }
}
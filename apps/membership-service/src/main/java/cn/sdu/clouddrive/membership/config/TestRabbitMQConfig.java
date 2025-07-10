package cn.sdu.clouddrive.membership.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Profile("test") // 只在测试环境生效
public class TestRabbitMQConfig {

    // 测试用的短TTL队列配置
    public static final String TEST_ORDER_PENDING_QUEUE = "membership.test.order.pending.queue";
    public static final String TEST_ORDER_PENDING_EXCHANGE = "membership.test.order.pending.exchange";
    public static final String TEST_ORDER_PENDING_ROUTING_KEY = "test.order.pending";
    
    public static final String TEST_ORDER_DLX_EXCHANGE = "membership.test.order.dlx.exchange";
    public static final String TEST_ORDER_CANCEL_QUEUE = "membership.test.order.cancel.queue";
    public static final String TEST_ORDER_EXPIRED_ROUTING_KEY = "test.order.expired";
    
    // 测试TTL设置：2分钟 = 120000毫秒
    public static final long TEST_ORDER_TTL = 120000L;

    @Bean
    public DirectExchange testOrderDlxExchange() {
        return new DirectExchange(TEST_ORDER_DLX_EXCHANGE, true, false);
    }

    @Bean
    public Queue testOrderCancelQueue() {
        return QueueBuilder.durable(TEST_ORDER_CANCEL_QUEUE).build();
    }

    @Bean
    public Binding testOrderCancelBinding() {
        return BindingBuilder.bind(testOrderCancelQueue())
                .to(testOrderDlxExchange())
                .with(TEST_ORDER_EXPIRED_ROUTING_KEY);
    }

    @Bean
    public DirectExchange testOrderPendingExchange() {
        return new DirectExchange(TEST_ORDER_PENDING_EXCHANGE, true, false);
    }

    @Bean
    public Queue testOrderPendingQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-message-ttl", TEST_ORDER_TTL);
        args.put("x-dead-letter-exchange", TEST_ORDER_DLX_EXCHANGE);
        args.put("x-dead-letter-routing-key", TEST_ORDER_EXPIRED_ROUTING_KEY);
        
        return QueueBuilder.durable(TEST_ORDER_PENDING_QUEUE)
                .withArguments(args)
                .build();
    }

    @Bean
    public Binding testOrderPendingBinding() {
        return BindingBuilder.bind(testOrderPendingQueue())
                .to(testOrderPendingExchange())
                .with(TEST_ORDER_PENDING_ROUTING_KEY);
    }
}
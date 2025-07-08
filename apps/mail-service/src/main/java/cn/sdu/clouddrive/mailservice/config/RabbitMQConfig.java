package cn.sdu.clouddrive.mailservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // 邮件队列名称
    public static final String MAIL_QUEUE = "mail.queue";

    // 邮件交换机名称
    public static final String MAIL_EXCHANGE = "mail.exchange";

    // 邮件路由键
    public static final String MAIL_ROUTING_KEY = "mail.send";

    // 死信队列名称
    public static final String MAIL_DLQ = "mail.dlq";

    // 死信交换机名称
    public static final String MAIL_DLX = "mail.dlx";

    /**
     * 创建邮件队列
     */
    @Bean
    public Queue mailQueue() {
        return QueueBuilder.durable(MAIL_QUEUE)
                .withArgument("x-dead-letter-exchange", MAIL_DLX)
                .withArgument("x-dead-letter-routing-key", "mail.dead")
                .withArgument("x-message-ttl", 600000) // 10分钟TTL
                .build();
    }

    /**
     * 创建死信队列
     */
    @Bean
    public Queue mailDeadLetterQueue() {
        return QueueBuilder.durable(MAIL_DLQ).build();
    }

    /**
     * 创建邮件交换机
     */
    @Bean
    public DirectExchange mailExchange() {
        return new DirectExchange(MAIL_EXCHANGE);
    }

    /**
     * 创建死信交换机
     */
    @Bean
    public DirectExchange mailDeadLetterExchange() {
        return new DirectExchange(MAIL_DLX);
    }

    /**
     * 绑定邮件队列到交换机
     */
    @Bean
    public Binding mailBinding() {
        return BindingBuilder.bind(mailQueue())
                .to(mailExchange())
                .with(MAIL_ROUTING_KEY);
    }

    /**
     * 绑定死信队列到死信交换机
     */
    @Bean
    public Binding mailDeadLetterBinding() {
        return BindingBuilder.bind(mailDeadLetterQueue())
                .to(mailDeadLetterExchange())
                .with("mail.dead");
    }

    /**
     * 配置RabbitTemplate，使用JSON消息转换器
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(new Jackson2JsonMessageConverter());

        // 开启发送确认
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                System.out.println("消息发送成功");
            } else {
                System.err.println("消息发送失败: " + cause);
            }
        });

        // 开启返回确认
        template.setReturnsCallback(returned -> {
            System.err.println("消息被退回: " + returned.getMessage());
        });

        return template;
    }

    /**
     * 配置监听器容器工厂
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(new Jackson2JsonMessageConverter());

        // 设置并发消费者数量
        factory.setConcurrentConsumers(3);
        factory.setMaxConcurrentConsumers(10);

        // 手动确认模式
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);

        return factory;
    }
}

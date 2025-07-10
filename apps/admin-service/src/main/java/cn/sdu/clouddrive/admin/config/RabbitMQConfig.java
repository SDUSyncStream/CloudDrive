// src/main/java/cn/sdu/clouddrive/admin/config/RabbitMQConfig.java
package cn.sdu.clouddrive.admin.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange; // 导入直连交换机
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // 定义交换机和队列的名称，方便统一管理
    public static final String SUBSCRIPTION_UPDATE_EMAIL_EXCHANGE = "subscription.update.email.exchange";
    public static final String SUBSCRIPTION_UPDATE_EMAIL_QUEUE = "subscription.update.email.queue";
    public static final String SUBSCRIPTION_UPDATE_EMAIL_ROUTING_KEY = "subscription.update.email.queue"; // 路由键与队列名相同

    /**
     * 声明直连交换机 (Direct Exchange)
     *
     * @return DirectExchange 实例
     */
    @Bean
    public DirectExchange subscriptionUpdateEmailExchange() {
        // 创建一个名为 "subscription.update.email.exchange" 的直连交换机
        // durable: true 表示交换机持久化，RabbitMQ 重启后不会丢失
        return new DirectExchange(SUBSCRIPTION_UPDATE_EMAIL_EXCHANGE, true, false);
    }

    /**
     * 声明 RabbitMQ 队列
     *
     * @return Queue 实例
     */
    @Bean
    public Queue subscriptionUpdateEmailQueue() {
        // 创建一个名为 "subscription.update.email.queue" 的队列
        // durable: true 表示队列持久化，RabbitMQ 重启后不会丢失
        return new Queue(SUBSCRIPTION_UPDATE_EMAIL_QUEUE, true);
    }

    /**
     * 将队列绑定到交换机上
     *
     */
    @Bean
    public Binding bindingSubscriptionUpdateEmail() {
        return BindingBuilder.bind(subscriptionUpdateEmailQueue()) // 绑定队列
                .to(subscriptionUpdateEmailExchange()) // 到交换机
                .with(SUBSCRIPTION_UPDATE_EMAIL_ROUTING_KEY); // 使用指定的路由键
    }
}
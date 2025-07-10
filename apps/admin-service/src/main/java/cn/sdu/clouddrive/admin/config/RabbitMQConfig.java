// src/main/java/cn/sdu/clouddrive/admin/config/RabbitMQConfig.java
package cn.sdu.clouddrive.admin.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // 定义队列名称
    public static final String SUBSCRIPTION_UPDATE_EMAIL_QUEUE = "subscription.update.email.queue";

    /**
     * 声明 RabbitMQ 队列
     *
     * @return Queue 实例
     */
    @Bean
    public Queue subscriptionUpdateEmailQueue() {
        // 创建一个名为 "subscription.update.email.queue" 的队列。
        // 默认情况下，该队列是持久化的 (durable: true)，非排他的 (exclusive: false)，非自动删除的 (autoDelete: false)。
        return new Queue(SUBSCRIPTION_UPDATE_EMAIL_QUEUE, true);
    }

    // 如果你有交换机或绑定关系，也可以在这里声明
    // @Bean
    // public DirectExchange directExchange() {
    //     return new DirectExchange("your.exchange.name");
    // }
    //
    // @Bean
    // public Binding binding(Queue queue, DirectExchange exchange) {
    //     return BindingBuilder.bind(queue).to(exchange).with("your.routing.key");
    // }
}
// src/main/java/cn/sdu/clouddrive/admin/MQListener/SubscriptionUpdateEmailListener.java
package cn.sdu.clouddrive.admin.MQListener;

import cn.sdu.clouddrive.admin.Service.EmailService;
import cn.sdu.clouddrive.admin.pojo.SubscriptionUpdateEmailMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener; // 导入 RabbitListener 注解
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionUpdateEmailListener {

    @Autowired
    private EmailService emailService;

    // @RabbitListener 监听指定的队列
    @RabbitListener(queues = "subscription.update.email.queue")
    public void handleSubscriptionUpdateMessage(SubscriptionUpdateEmailMessage message) {
        System.out.println("收到订阅更新消息: " + message);
        try {
            // 调用邮件服务发送邮件
            emailService.sendSubscriptionUpdateEmail(message);
            System.out.println("邮件已发送给用户：" + message.getUserEmail());
        } catch (Exception e) {
            System.err.println("处理消息并发送邮件失败: " + e.getMessage());
            // 生产环境中，这里可能需要记录日志，或者将消息重新放入队列进行重试
        }
    }
}
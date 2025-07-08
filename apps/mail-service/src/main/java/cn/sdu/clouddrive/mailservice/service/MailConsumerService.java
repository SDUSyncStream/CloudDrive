package cn.sdu.clouddrive.mailservice.service;

import cn.sdu.clouddrive.mailservice.config.RabbitMQConfig;
import cn.sdu.clouddrive.mailservice.dto.MailMessage;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class MailConsumerService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private MailProducerService mailProducerService;

    /**
     * 监听邮件队列，处理邮件发送
     */
    @RabbitListener(queues = RabbitMQConfig.MAIL_QUEUE)
    public void handleMailMessage(MailMessage mailMessage, Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        try {
            log.info("开始处理邮件发送，收件人: {}, 类型: {}", mailMessage.getTo(), mailMessage.getMailType());

            // 发送邮件
            sendMail(mailMessage);

            // 手动确认消息
            channel.basicAck(deliveryTag, false);

            log.info("邮件发送成功，收件人: {}", mailMessage.getTo());

        } catch (Exception e) {
            log.error("邮件发送失败，收件人: {}, 错误: {}", mailMessage.getTo(), e.getMessage());

            // 判断是否需要重试
            if (!mailMessage.isMaxRetryReached()) {
                // 重新发送到队列进行重试
                mailProducerService.resendMailToQueue(mailMessage);

                // 确认当前消息（避免重复处理）
                channel.basicAck(deliveryTag, false);

                log.info("邮件将进行重试，收件人: {}, 当前重试次数: {}",
                        mailMessage.getTo(), mailMessage.getRetryCount());
            } else {
                // 超过最大重试次数，拒绝消息并发送到死信队列
                channel.basicNack(deliveryTag, false, false);

                log.error("邮件发送失败且超过最大重试次数，发送到死信队列，收件人: {}", mailMessage.getTo());
            }
        }
    }

    /**
     * 监听死信队列，记录失败的邮件
     */
    @RabbitListener(queues = RabbitMQConfig.MAIL_DLQ)
    public void handleDeadLetterMessage(MailMessage mailMessage, Message message, Channel channel) throws IOException {
        try {
            log.error("处理死信队列中的邮件，收件人: {}, 类型: {}, 重试次数: {}",
                    mailMessage.getTo(), mailMessage.getMailType(), mailMessage.getRetryCount());

            // TODO: 可以将失败的邮件信息保存到数据库或发送告警
            // saveFailedMailToDatabase(mailMessage);
            // sendAlertNotification(mailMessage);

            // 确认死信消息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

        } catch (Exception e) {
            log.error("处理死信队列消息失败: {}", e.getMessage());
            // 拒绝死信消息
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
        }
    }

    /**
     * 实际发送邮件的方法
     */
    private void sendMail(MailMessage mailMessage) {
        try {
            SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
            simpleMailMessage.setFrom("your-email@example.com"); // 需要配置发件人邮箱
            simpleMailMessage.setTo(mailMessage.getTo());
            simpleMailMessage.setSubject(mailMessage.getSubject());
            simpleMailMessage.setText(mailMessage.getContent());

            mailSender.send(simpleMailMessage);

            log.debug("邮件发送成功: {} -> {}", simpleMailMessage.getFrom(), mailMessage.getTo());

        } catch (Exception e) {
            log.error("JavaMailSender发送邮件失败: {}", e.getMessage());
            throw new RuntimeException("邮件发送失败", e);
        }
    }

    /**
     * 保存失败的邮件到数据库（可选实现）
     */
    private void saveFailedMailToDatabase(MailMessage mailMessage) {
        // TODO: 实现将失败的邮件信息保存到数据库
        log.info("保存失败邮件到数据库: {}", mailMessage.getTo());
    }

    /**
     * 发送告警通知（可选实现）
     */
    private void sendAlertNotification(MailMessage mailMessage) {
        // TODO: 实现告警通知，比如发送钉钉消息或短信
        log.info("发送邮件失败告警: {}", mailMessage.getTo());
    }
}

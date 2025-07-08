package cn.sdu.clouddrive.mailservice.service;

import cn.sdu.clouddrive.mailservice.config.RabbitMQConfig;
import cn.sdu.clouddrive.mailservice.dto.MailMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class MailProducerService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 发送验证码邮件到MQ队列
     */
    public void sendVerificationMail(String email, String username, String verificationCode) {
        // 检查Redis中是否已存在未过期的验证码
        String redisKey = "mail:verification:" + email;
        String existingCode = redisTemplate.opsForValue().get(redisKey);

        if (existingCode != null) {
            log.info("邮箱 {} 的验证码尚未过期，跳过发送", email);
            return;
        }

        // 创建邮件消息
        MailMessage mailMessage = MailMessage.createVerificationMail(email, username, verificationCode);

        // 发送到MQ队列
        sendMailToQueue(mailMessage);

        // 在Redis中记录已发送状态，防止重复发送
        redisTemplate.opsForValue().set(redisKey, verificationCode, 10, TimeUnit.MINUTES);

        log.info("验证码邮件已发送到MQ队列，邮箱: {}", email);
    }

    /**
     * 发送密码重置邮件到MQ队列
     */
    public void sendPasswordResetMail(String email, String username, String resetCode) {
        String redisKey = "mail:reset:" + email;
        String existingCode = redisTemplate.opsForValue().get(redisKey);

        if (existingCode != null) {
            log.info("邮箱 {} 的密码重置邮件尚未过期，跳过发送", email);
            return;
        }

        MailMessage mailMessage = MailMessage.createPasswordResetMail(email, username, resetCode);
        sendMailToQueue(mailMessage);

        redisTemplate.opsForValue().set(redisKey, resetCode, 10, TimeUnit.MINUTES);

        log.info("密码重置邮件已发送到MQ队列，邮箱: {}", email);
    }

    /**
     * 发送通知邮件到MQ队列
     */
    public void sendNotificationMail(String email, String username, String subject, String content) {
        MailMessage mailMessage = MailMessage.createNotificationMail(email, username, subject, content);
        sendMailToQueue(mailMessage);

        log.info("通知邮件已发送到MQ队列，邮箱: {}", email);
    }

    /**
     * 将邮件消息发送到MQ队列
     */
    private void sendMailToQueue(MailMessage mailMessage) {
        try {
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.MAIL_EXCHANGE,
                RabbitMQConfig.MAIL_ROUTING_KEY,
                mailMessage
            );
            log.debug("邮件消息已发送到MQ: {}", mailMessage.getTo());
        } catch (Exception e) {
            log.error("发送邮件消息到MQ失败: {}, 错误: {}", mailMessage.getTo(), e.getMessage());
            throw new RuntimeException("邮件消息发送失败", e);
        }
    }

    /**
     * 重发邮件到队列（用于重试机制）
     */
    public void resendMailToQueue(MailMessage mailMessage) {
        if (mailMessage.isMaxRetryReached()) {
            log.error("邮件重试次数已达上限，放弃发送: {}", mailMessage.getTo());
            return;
        }

        mailMessage.incrementRetryCount();
        sendMailToQueue(mailMessage);

        log.info("邮件重新发送到队列，邮箱: {}, 重试次数: {}",
                mailMessage.getTo(), mailMessage.getRetryCount());
    }
}

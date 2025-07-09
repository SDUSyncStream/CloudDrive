package cn.sdu.clouddrive.mailservice.service;

import cn.sdu.clouddrive.mailservice.config.RabbitMQConfig;
import cn.sdu.clouddrive.mailservice.dto.EmailVerificationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailMessageListener {

    @Autowired
    private EmailService emailService;

    @Autowired
    private VerificationCodeService verificationCodeService;

    /**
     * 监听邮件验证码发送请求
     */
    @RabbitListener(queues = RabbitMQConfig.EMAIL_QUEUE)
    public void handleEmailVerificationRequest(EmailVerificationRequest request) {
        try {
            log.info("收到邮件验证码发送请求，邮箱: {}, 类型: {}", request.getEmail(), request.getType());

            // 生成验证码
            String code = verificationCodeService.generateCode();

            // 存储验证码到Redis
            verificationCodeService.storeCode(request.getEmail(), code, request.getType());

            // 发送邮件
            emailService.sendVerificationCode(request.getEmail(), code, request.getType());

            log.info("邮件验证码处理完成，邮箱: {}, 类型: {}", request.getEmail(), request.getType());

        } catch (Exception e) {
            log.error("处理邮件验证码请求失败，邮箱: {}, 类型: {}, 错误: {}",
                     request.getEmail(), request.getType(), e.getMessage(), e);
            // 这里可以考虑实现重试机制或者将失败的消息发送到死信队列
        }
    }
}

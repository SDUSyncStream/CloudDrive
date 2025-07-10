package cn.sdu.clouddrive.mailservice.controller;

import cn.sdu.clouddrive.mailservice.config.RabbitMQConfig;
import cn.sdu.clouddrive.mailservice.dto.EmailVerificationRequest;
import cn.sdu.clouddrive.mailservice.service.VerificationCodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;





/**
 * 邮件控制器
 * 处理邮件相关的请求，如发送忘记密码验证码和验证邮箱验证码
 */
@Slf4j
@RestController
@RequestMapping("/api/mail")
public class EmailController {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private VerificationCodeService verificationCodeService;

    /**
     * 发送忘记密码验证码
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, Object>> sendForgotPasswordCode(@RequestParam String email) {
        Map<String, Object> response = new HashMap<>();

        try {
            log.info("收到忘记密码验证码请求，邮箱: {}", email);

            // 创建邮件验证请求
            EmailVerificationRequest request = new EmailVerificationRequest(email, "FORGOT_PASSWORD");

            // 发送到RabbitMQ队列进行异步处理
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.EMAIL_EXCHANGE,
                RabbitMQConfig.EMAIL_ROUTING_KEY,
                request
            );

            response.put("success", true);
            response.put("message", "验证码发送请求已提交，请查收邮件");
            response.put("email", email);

            log.info("忘记密码验证码请求已发送到队列，邮箱: {}", email);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("发送忘记密码验证码失败，邮箱: {}, 错误: {}", email, e.getMessage());
            response.put("success", false);
            response.put("message", "验证码发送失败，请稍后重试");
            return ResponseEntity.status(500).body(response);
        }
    }


    /**
     * 验证邮箱验证码
     */
    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyCode(
            @RequestParam String email,
            @RequestParam String code,
            @RequestParam String type) {

        Map<String, Object> response = new HashMap<>();

        try {
            log.info("收到验证码验证请求，邮箱: {}, 类型: {}", email, type);

            boolean isValid = verificationCodeService.verifyCode(email, code, type);

            if (isValid) {
                response.put("success", true);
                response.put("message", "验证码验证成功");
                log.info("验证码验证成功，邮箱: {}, 类型: {}", email, type);
            } else {
                response.put("success", false);
                response.put("message", "验证码错误或已过期");
                log.warn("验证码验证失败，邮箱: {}, 类型: {}", email, type);
            }

            response.put("email", email);
            response.put("type", type);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("验证码验证异常，邮箱: {}, 类型: {}, 错误: {}", email, type, e.getMessage());
            response.put("success", false);
            response.put("message", "验证失败，请稍后重试");
            return ResponseEntity.status(500).body(response);
        }
    }
}

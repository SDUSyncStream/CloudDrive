package cn.sdu.clouddrive.mailservice.controller;

import cn.sdu.clouddrive.mailservice.service.MailProducerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/mail")
public class MailController {

    @Autowired
    private MailProducerService mailProducerService;

    /**
     * 发送验证码邮件
     */
    @PostMapping("/send-verification")
    public Map<String, Object> sendVerificationMail(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String username = request.get("username");
        String verificationCode = request.get("verificationCode");

        Map<String, Object> response = new HashMap<>();

        try {
            if (email == null || email.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "邮箱地址不能为空");
                return response;
            }

            if (username == null || username.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "用户名不能为空");
                return response;
            }

            if (verificationCode == null || verificationCode.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "验证码不能为空");
                return response;
            }

            // 发送验证码邮件到MQ队列
            mailProducerService.sendVerificationMail(email, username, verificationCode);

            response.put("success", true);
            response.put("message", "验证码邮件已发送");
            response.put("email", email);

            log.info("验证码邮件发送请求成功，邮箱: {}", email);

        } catch (Exception e) {
            log.error("验证码邮件发送请求失败: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "邮件发送失败: " + e.getMessage());
        }

        return response;
    }

    /**
     * 发送密码重置邮件
     */
    @PostMapping("/send-reset")
    public Map<String, Object> sendPasswordResetMail(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String username = request.get("username");
        String resetCode = request.get("resetCode");

        Map<String, Object> response = new HashMap<>();

        try {
            if (email == null || email.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "邮箱地址不能为空");
                return response;
            }

            if (username == null || username.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "用户名不能为空");
                return response;
            }

            if (resetCode == null || resetCode.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "重置码不能为空");
                return response;
            }

            // 发送密码重置邮件到MQ队列
            mailProducerService.sendPasswordResetMail(email, username, resetCode);

            response.put("success", true);
            response.put("message", "密码重置邮件已发送");
            response.put("email", email);

            log.info("密码重置邮件发送请求成功，邮箱: {}", email);

        } catch (Exception e) {
            log.error("密码重置邮件发送请求失败: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "邮件发送失败: " + e.getMessage());
        }

        return response;
    }

    /**
     * 发送通知邮件
     */
    @PostMapping("/send-notification")
    public Map<String, Object> sendNotificationMail(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String username = request.get("username");
        String subject = request.get("subject");
        String content = request.get("content");

        Map<String, Object> response = new HashMap<>();

        try {
            if (email == null || email.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "邮箱地址不能为空");
                return response;
            }

            if (subject == null || subject.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "邮件主题不能为空");
                return response;
            }

            if (content == null || content.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "邮件内容不能为空");
                return response;
            }

            // 发送通知邮件到MQ队列
            mailProducerService.sendNotificationMail(email, username, subject, content);

            response.put("success", true);
            response.put("message", "通知邮件已发送");
            response.put("email", email);

            log.info("通知邮件发送请求成功，邮箱: {}", email);

        } catch (Exception e) {
            log.error("通知邮件发送请求失败: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "邮件发送失败: " + e.getMessage());
        }

        return response;
    }

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "mail-service");
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
}

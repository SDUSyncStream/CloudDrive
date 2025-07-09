package cn.sdu.clouddrive.mailservice.service;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * 发送验证码邮件
     */
    public void sendVerificationCode(String toEmail, String code, String type) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);

            String subject = getSubjectByType(type);
            String content = buildEmailContent(code, type);

            helper.setSubject(subject);
            helper.setText(content, true); // true表示支持HTML

            mailSender.send(message);
            log.info("验证码邮件发送成功，收件人: {}, 类型: {}", toEmail, type);

        } catch (Exception e) {
            log.error("验证码邮件发送失败，收件人: {}, 类型: {}, 错误: {}", toEmail, type, e.getMessage());
            throw new RuntimeException("邮件发送失败: " + e.getMessage());
        }
    }

    /**
     * 根据类型获取邮件主题
     */
    private String getSubjectByType(String type) {
        switch (type) {
            case "FORGOT_PASSWORD":
                return "【云盘系统】忘记密码验证码";
            case "REGISTER":
                return "【云盘系统】注册验证码";
            case "LOGIN":
                return "【云盘系统】登录验证码";
            default:
                return "【云盘系统】邮箱验证码";
        }
    }

    /**
     * 构建邮件内容
     */
    private String buildEmailContent(String code, String type) {
        String purpose = getPurposeByType(type);

        return String.format(
            "<html>" +
            "<body style='font-family: Arial, sans-serif;'>" +
            "<div style='max-width: 600px; margin: 0 auto; padding: 20px;'>" +
            "<h2 style='color: #2c3e50; text-align: center;'>云盘系统</h2>" +
            "<div style='background-color: #f8f9fa; padding: 20px; border-radius: 8px; margin: 20px 0;'>" +
            "<h3 style='color: #495057; margin-top: 0;'>邮箱验证码</h3>" +
            "<p style='color: #6c757d; font-size: 16px;'>您好！</p>" +
            "<p style='color: #6c757d; font-size: 16px;'>您正在进行%s操作，验证码为：</p>" +
            "<div style='text-align: center; margin: 30px 0;'>" +
            "<span style='background-color: #007bff; color: white; padding: 15px 30px; font-size: 24px; font-weight: bold; border-radius: 5px; letter-spacing: 5px;'>%s</span>" +
            "</div>" +
            "<p style='color: #dc3545; font-size: 14px;'><strong>重要提醒：</strong></p>" +
            "<ul style='color: #6c757d; font-size: 14px;'>" +
            "<li>验证码有效期为15分钟，请及时使用</li>" +
            "<li>请勿将验证码告知他人</li>" +
            "<li>如非本人操作，请忽略此邮件</li>" +
            "</ul>" +
            "</div>" +
            "<p style='color: #6c757d; font-size: 12px; text-align: center; margin-top: 30px;'>" +
            "此邮件由系统自动发送，请勿回复" +
            "</p>" +
            "</div>" +
            "</body>" +
            "</html>",
            purpose, code
        );
    }

    /**
     * 根据类型获取操作目的
     */
    private String getPurposeByType(String type) {
        switch (type) {
            case "FORGOT_PASSWORD":
                return "找回密码";
            case "REGISTER":
                return "账户注册";
            case "LOGIN":
                return "安全登录";
            default:
                return "身份验证";
        }
    }
}

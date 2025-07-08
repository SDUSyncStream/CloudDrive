package cn.sdu.clouddrive.mailservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MailMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 收件人邮箱地址
     */
    private String to;

    /**
     * 邮件主题
     */
    private String subject;

    /**
     * 邮件内容
     */
    private String content;

    /**
     * 邮件类型：verification（验证码）、reset（密码重置）、notification（通知）
     */
    private String mailType;

    /**
     * 验证码（如果是验证码邮件）
     */
    private String verificationCode;

    /**
     * 用户名（用于个性化邮件内容）
     */
    private String username;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 重试次数
     */
    private Integer retryCount = 0;

    /**
     * 最大重试次数
     */
    private Integer maxRetryCount = 3;

    /**
     * 邮件优先级：1-高，2-中，3-低
     */
    private Integer priority = 2;

    /**
     * 创建验证码邮件
     */
    public static MailMessage createVerificationMail(String to, String username, String verificationCode) {
        MailMessage message = new MailMessage();
        message.setTo(to);
        message.setUsername(username);
        message.setVerificationCode(verificationCode);
        message.setMailType("verification");
        message.setSubject("【云盘系统】邮箱验证码");
        message.setContent(buildVerificationContent(username, verificationCode));
        message.setCreateTime(LocalDateTime.now());
        message.setPriority(1); // 验证码邮件高优先级
        return message;
    }

    /**
     * 创建密码重置邮件
     */
    public static MailMessage createPasswordResetMail(String to, String username, String resetCode) {
        MailMessage message = new MailMessage();
        message.setTo(to);
        message.setUsername(username);
        message.setVerificationCode(resetCode);
        message.setMailType("reset");
        message.setSubject("【云盘系统】密码重置验证码");
        message.setContent(buildPasswordResetContent(username, resetCode));
        message.setCreateTime(LocalDateTime.now());
        message.setPriority(1); // 密码重置邮件高优先级
        return message;
    }

    /**
     * 创建通知邮件
     */
    public static MailMessage createNotificationMail(String to, String username, String subject, String content) {
        MailMessage message = new MailMessage();
        message.setTo(to);
        message.setUsername(username);
        message.setMailType("notification");
        message.setSubject(subject);
        message.setContent(content);
        message.setCreateTime(LocalDateTime.now());
        message.setPriority(3); // 通知邮件低优先级
        return message;
    }

    /**
     * 构建验证码邮件内容
     */
    private static String buildVerificationContent(String username, String verificationCode) {
        return String.format(
            "亲爱的 %s：\n\n" +
            "您正在进行邮箱验证，验证码为：%s\n\n" +
            "验证码有效期为10分钟，请及时使用。\n" +
            "如果这不是您本人的操作，请忽略此邮件。\n\n" +
            "云盘系统\n" +
            "%s",
            username, verificationCode, LocalDateTime.now()
        );
    }

    /**
     * 构建密码重置邮件内容
     */
    private static String buildPasswordResetContent(String username, String resetCode) {
        return String.format(
            "亲爱的 %s：\n\n" +
            "您正在重置登录密码，验证码为：%s\n\n" +
            "验证码有效期为10分钟，请及时使用。\n" +
            "如果这不是您本人的操作，请立即联系客服。\n\n" +
            "云盘系统\n" +
            "%s",
            username, resetCode, LocalDateTime.now()
        );
    }

    /**
     * 增加重试次数
     */
    public void incrementRetryCount() {
        this.retryCount++;
    }

    /**
     * 检查是否达到最大重试次数
     */
    public boolean isMaxRetryReached() {
        return this.retryCount >= this.maxRetryCount;
    }
}

// src/main/java/cn/sdu/clouddrive/admin/Service/EmailService.java
package cn.sdu.clouddrive.admin.Service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import cn.sdu.clouddrive.admin.pojo.SubscriptionUpdateEmailMessage; // 导入我们定义的消息体

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // 发送简单文本邮件
    public void sendSimpleEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("1661391201@qq.com"); // 你的QQ邮箱
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        try {
            mailSender.send(message);
            System.out.println("邮件发送成功至：" + to);
        } catch (MailException e) {
            System.err.println("发送邮件失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 根据订阅更新消息发送邮件
    public void sendSubscriptionUpdateEmail(SubscriptionUpdateEmailMessage message) {
        String subject = "您的云盘订阅更新通知";
        String emailText = String.format(
                "尊敬的 %s 用户，\n\n您的云盘订阅信息已更新，当前订阅详情如下：\n" +
                        "  - 会员等级ID: %s\n" +
                        "  - 开始日期: %s\n" +
                        "  - 结束日期: %s\n" +
                        "  - 当前状态: %s\n" +
                        "感谢您的使用！\n云盘管理团队",
                message.getUsername(),
                message.getMembershipLevelId(),
                message.getStartDate(),
                message.getEndDate(),
                message.getStatus()
        );

        sendSimpleEmail(message.getUserEmail(), subject, emailText);
    }
}
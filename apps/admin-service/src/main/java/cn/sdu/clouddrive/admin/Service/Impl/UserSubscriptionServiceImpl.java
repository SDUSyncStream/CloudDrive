// src/main/java/cn/sdu/clouddrive/admin/Service/Impl/UserSubscriptionServiceImpl.java
package cn.sdu.clouddrive.admin.Service.Impl;

import cn.sdu.clouddrive.admin.Mapper.UserMapper;
import cn.sdu.clouddrive.admin.Mapper.UserSubscriptionMapper;
import cn.sdu.clouddrive.admin.Service.UserSubscriptionService;
import cn.sdu.clouddrive.admin.config.RabbitMQConfig; // 导入 RabbitMQConfig，获取常量
import cn.sdu.clouddrive.admin.pojo.User;
import cn.sdu.clouddrive.admin.pojo.UserSubscription;
import cn.sdu.clouddrive.admin.pojo.SubscriptionUpdateEmailMessage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserSubscriptionServiceImpl implements UserSubscriptionService {

    @Autowired
    private UserSubscriptionMapper userSubscriptionMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public List<UserSubscription> getAllUserSubscriptions() {
        return userSubscriptionMapper.selectList(null);
    }

    @Override
    public List<UserSubscription> getUserSubscriptionsByUserId(String userId) {
        QueryWrapper<UserSubscription> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        return userSubscriptionMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional
    public int addUserSubscription(UserSubscription subscription) {
        int result = userSubscriptionMapper.insert(subscription);
        if (result > 0) {
            sendSubscriptionUpdateNotification(subscription.getUserId(), subscription);
        }
        return result;
    }

    @Override
    @Transactional
    public int updateUserSubscription(UserSubscription subscription) {
        int result = userSubscriptionMapper.updateById(subscription);
        if (result > 0) {
            sendSubscriptionUpdateNotification(subscription.getUserId(), subscription);
        }
        return result;
    }

    @Override
    public int deleteUserSubscription(String id) {
        return userSubscriptionMapper.deleteById(id);
    }

    private void sendSubscriptionUpdateNotification(String userId, UserSubscription updatedSubscription) {
        User user = userMapper.selectById(userId);
        if (user == null || user.getEmail() == null || user.getEmail().isEmpty()) {
            System.err.println("未找到用户或用户邮箱为空，无法发送邮件通知，用户ID: " + userId);
            return;
        }

        SubscriptionUpdateEmailMessage message = new SubscriptionUpdateEmailMessage(
                userId,
                user.getEmail(),
                updatedSubscription.getMembershipLevelId(),
                updatedSubscription.getStartDate(),
                updatedSubscription.getEndDate(),
                updatedSubscription.getStatus().getValue(),
                updatedSubscription.getPaymentAmount().toPlainString(),
                user.getUsername()
        );

        try {
            // *** 修改点：明确指定交换机名称和路由键 ***
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.SUBSCRIPTION_UPDATE_EMAIL_EXCHANGE, // 交换机名称
                    RabbitMQConfig.SUBSCRIPTION_UPDATE_EMAIL_ROUTING_KEY, // 路由键
                    message
            );
            System.out.println("成功发送订阅更新消息到 RabbitMQ 交换机: " + RabbitMQConfig.SUBSCRIPTION_UPDATE_EMAIL_EXCHANGE + "，路由键: " + RabbitMQConfig.SUBSCRIPTION_UPDATE_EMAIL_ROUTING_KEY + "，消息: " + message);
        } catch (Exception e) {
            System.err.println("发送 RabbitMQ 消息失败，用户ID: " + userId + ", 错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
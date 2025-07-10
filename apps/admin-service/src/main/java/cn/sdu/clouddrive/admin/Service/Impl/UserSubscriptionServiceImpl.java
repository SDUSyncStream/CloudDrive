// src/main/java/cn/sdu/clouddrive/admin/Service/Impl/UserSubscriptionServiceImpl.java
package cn.sdu.clouddrive.admin.Service.Impl;

import cn.sdu.clouddrive.admin.Mapper.UserMapper; // 假设你有 UserMapper
import cn.sdu.clouddrive.admin.Mapper.UserSubscriptionMapper;
import cn.sdu.clouddrive.admin.Service.UserSubscriptionService;
import cn.sdu.clouddrive.admin.pojo.User; // 导入 User POJO
import cn.sdu.clouddrive.admin.pojo.UserSubscription;
import cn.sdu.clouddrive.admin.pojo.SubscriptionUpdateEmailMessage; // 导入我们定义的消息体

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate; // 导入 RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 导入事务注解

import java.util.List;

@Service
public class UserSubscriptionServiceImpl implements UserSubscriptionService {

    @Autowired
    private UserSubscriptionMapper userSubscriptionMapper; // 注入 UserSubscriptionMapper

    @Autowired
    private UserMapper userMapper; // 注入 UserMapper，用于获取用户邮箱和用户名

    @Autowired
    private RabbitTemplate rabbitTemplate; // 注入 RabbitTemplate，用于发送 RabbitMQ 消息

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
    @Transactional // 确保数据库操作和消息发送在同一个事务中
    public int addUserSubscription(UserSubscription subscription) {
        int result = userSubscriptionMapper.insert(subscription);
        if (result > 0) {
            // 新增订阅成功后，发送 RabbitMQ 消息
            sendSubscriptionUpdateNotification(subscription.getUserId(), subscription);
        }
        return result;
    }

    @Override
    @Transactional // 确保数据库操作和消息发送在同一个事务中
    public int updateUserSubscription(UserSubscription subscription) {
        int result = userSubscriptionMapper.updateById(subscription);
        if (result > 0) {
            // 更新成功后，发送 RabbitMQ 消息
            sendSubscriptionUpdateNotification(subscription.getUserId(), subscription);
        }
        return result;
    }

    @Override
    public int deleteUserSubscription(String id) {
        return userSubscriptionMapper.deleteById(id);
    }

    /**
     * 封装发送订阅更新通知消息到 RabbitMQ 的逻辑
     * @param userId 用户的ID
     * @param updatedSubscription 更新或新增后的订阅信息
     */
    private void sendSubscriptionUpdateNotification(String userId, UserSubscription updatedSubscription) {
        // 1. 获取用户的邮箱地址和用户名
        // 假设 UserMapper 中有 selectById 方法
        User user = userMapper.selectById(userId);
        if (user == null) {
            System.err.println("未找到用户，无法发送邮件通知，用户ID: " + userId);
            return;
        }
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            System.err.println("用户邮箱为空，无法发送邮件通知，用户ID: " + userId);
            return;
        }

        // 2. 构建 RabbitMQ 消息体
        SubscriptionUpdateEmailMessage message = new SubscriptionUpdateEmailMessage(
                userId,
                user.getEmail(),
                updatedSubscription.getMembershipLevelId(),
                updatedSubscription.getStartDate(),
                updatedSubscription.getEndDate(),
                updatedSubscription.getStatus().getValue(), // 使用枚举的字符串值
                updatedSubscription.getPaymentAmount().toPlainString(), // 将 BigDecimal 转换为字符串
                user.getUsername() // 添加用户名
        );

        // 3. 将消息发送到 RabbitMQ 队列
        // 定义队列名称，需要与 RabbitMQ 监听器中的队列名称保持一致
        String queueName = "subscription.update.email.queue";
        try {
            rabbitTemplate.convertAndSend(queueName, message);
            System.out.println("成功发送订阅更新消息到 RabbitMQ 队列: " + queueName + "，消息: " + message);
        } catch (Exception e) {
            System.err.println("发送 RabbitMQ 消息失败，用户ID: " + userId + ", 错误: " + e.getMessage());
            e.printStackTrace();
            // 在实际项目中，这里可能需要更复杂的错误处理，例如重试机制或死信队列
        }
    }
}
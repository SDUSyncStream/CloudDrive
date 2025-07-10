package cn.sdu.clouddrive.membership.controller;

import cn.sdu.clouddrive.membership.common.Result;
import cn.sdu.clouddrive.membership.dto.UserSubscriptionDTO;
import cn.sdu.clouddrive.membership.service.UserSubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/subscription")
@CrossOrigin(origins = "*")
public class SubscriptionController {

    @Autowired
    private UserSubscriptionService userSubscriptionService;

    @GetMapping("/user/{userId}")
    public Result<List<UserSubscriptionDTO>> getUserSubscriptions(@PathVariable String userId) {
        List<UserSubscriptionDTO> subscriptions = userSubscriptionService.getUserSubscriptions(userId);
        return Result.success(subscriptions);
    }

    @GetMapping("/user/{userId}/current")
    public Result<UserSubscriptionDTO> getCurrentSubscription(@PathVariable String userId) {
        UserSubscriptionDTO subscription = userSubscriptionService.getCurrentSubscription(userId);
        if (subscription != null) {
            return Result.success(subscription);
        } else {
            return Result.error("用户暂无有效订阅");
        }
    }

    @PostMapping("/{subscriptionId}/cancel")
    public Result<String> cancelSubscription(@PathVariable String subscriptionId) {
        try {
            userSubscriptionService.cancelSubscription(subscriptionId);
            return Result.success("订阅取消成功");
        } catch (Exception e) {
            return Result.error("取消订阅失败: " + e.getMessage());
        }
    }

    @PostMapping("/user/{userId}/default")
    public Result<String> createDefaultSubscription(@PathVariable String userId) {
        try {
            boolean success = userSubscriptionService.createDefaultSubscription(userId);
            if (success) {
                return Result.success("默认订阅创建成功");
            } else {
                return Result.error("默认订阅创建失败");
            }
        } catch (Exception e) {
            return Result.error("创建默认订阅失败: " + e.getMessage());
        }
    }

    @GetMapping("/user/{userId}/can-subscribe/{membershipLevelId}")
    public Result<Boolean> canSubscribeToLevel(@PathVariable String userId, @PathVariable String membershipLevelId) {
        try {
            boolean canSubscribe = userSubscriptionService.canSubscribeToLevel(userId, membershipLevelId);
            return Result.success(canSubscribe);
        } catch (Exception e) {
            return Result.error("检查订阅权限失败: " + e.getMessage());
        }
    }
}
package cn.sdu.clouddrive.membership.controller;

import cn.sdu.clouddrive.membership.common.Result;
import cn.sdu.clouddrive.membership.dto.MembershipLevelDTO;
import cn.sdu.clouddrive.membership.dto.UserSubscriptionDTO;
import cn.sdu.clouddrive.membership.service.MembershipLevelService;
import cn.sdu.clouddrive.membership.service.UserSubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/cache-test")
public class CacheTestController {

    @Autowired
    private MembershipLevelService membershipLevelService;
    
    @Autowired
    private UserSubscriptionService userSubscriptionService;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @GetMapping("/levels")
    public Result<List<MembershipLevelDTO>> testLevelCache() {
        // 第一次调用会查询数据库并缓存
        List<MembershipLevelDTO> levels = membershipLevelService.getAllLevels();
        return Result.success("获取会员等级成功", levels);
    }

    @GetMapping("/user-subscription/{userId}")
    public Result<UserSubscriptionDTO> testUserSubscriptionCache(@PathVariable String userId) {
        // 第一次调用会查询数据库并缓存
        UserSubscriptionDTO subscription = userSubscriptionService.getCurrentSubscription(userId);
        return Result.success("获取用户订阅成功", subscription);
    }

    @GetMapping("/cache-keys")
    public Result<Set<String>> getCacheKeys() {
        // 查看Redis中的缓存键
        Set<String> keys = redisTemplate.keys("*");
        return Result.success("获取缓存键成功", keys);
    }

    @DeleteMapping("/clear-cache")
    public Result<String> clearCache() {
        // 清空所有缓存
        Set<String> keys = redisTemplate.keys("*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
        return Result.success("缓存清空成功", "共清除 " + (keys != null ? keys.size() : 0) + " 个缓存键");
    }

    @PostMapping("/evict-user-cache/{userId}")
    public Result<String> evictUserCache(@PathVariable String userId) {
        // 清除特定用户的缓存
        userSubscriptionService.evictUserSubscriptionCache(userId);
        return Result.success("用户缓存清除成功", "用户ID: " + userId);
    }
}
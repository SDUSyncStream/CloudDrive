package cn.sdu.clouddrive.membership.service;

import cn.sdu.clouddrive.membership.dto.UserSubscriptionDTO;
import cn.sdu.clouddrive.membership.entity.MembershipLevel;
import cn.sdu.clouddrive.membership.entity.UserSubscription;
import cn.sdu.clouddrive.membership.mapper.UserSubscriptionMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserSubscriptionService extends ServiceImpl<UserSubscriptionMapper, UserSubscription> {

    @Autowired
    private MembershipLevelService membershipLevelService;

    public List<UserSubscriptionDTO> getUserSubscriptions(String userId) {
        QueryWrapper<UserSubscription> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).orderByDesc("created_at");
        
        List<UserSubscription> subscriptions = list(wrapper);
        return subscriptions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "userSubscriptions", key = "'current:' + #userId")
    public UserSubscriptionDTO getCurrentSubscription(String userId) {
        // 使用自定义SQL查询，按会员等级价格(优先级)降序排序，然后按结束时间降序排序
        UserSubscription subscription = getBaseMapper().selectCurrentSubscription(userId);
        return subscription != null ? convertToDTO(subscription) : null;
    }

    @CacheEvict(value = "userSubscriptions", key = "'current:' + #userId")
    public UserSubscriptionDTO createSubscription(String userId, String membershipLevelId, 
                                                 String paymentMethod, java.math.BigDecimal amount) {
        MembershipLevel level = membershipLevelService.getById(membershipLevelId);
        if (level == null) {
            throw new RuntimeException("会员等级不存在");
        }

        // 检查用户当前订阅状态
        UserSubscriptionDTO currentSubscription = getCurrentSubscription(userId);
        if (currentSubscription != null && currentSubscription.getIsActive()) {
            // 获取当前订阅的会员等级
            MembershipLevel currentLevel = membershipLevelService.getById(currentSubscription.getMembershipLevelId());
            if (currentLevel != null) {
                // 检查优先级：不能订阅同等级或更低等级的会员
                if (level.getPriority() <= currentLevel.getPriority()) {
                    throw new RuntimeException("您当前的会员等级已包含此权限，无需重复购买");
                }
            }
        }

        UserSubscription subscription = new UserSubscription();
        subscription.setUserId(userId);
        subscription.setMembershipLevelId(membershipLevelId);
        subscription.setStartDate(LocalDateTime.now());
        subscription.setEndDate(LocalDateTime.now().plusDays(level.getDurationDays()));
        subscription.setStatus("active");
        subscription.setPaymentMethod(paymentMethod);
        subscription.setPaymentAmount(amount);
        subscription.setCreatedAt(LocalDateTime.now());
        subscription.setUpdatedAt(LocalDateTime.now());

        save(subscription);
        return convertToDTO(subscription);
    }

    public void cancelSubscription(String subscriptionId) {
        UserSubscription subscription = getById(subscriptionId);
        if (subscription != null) {
            subscription.setStatus("cancelled");
            subscription.setUpdatedAt(LocalDateTime.now());
            updateById(subscription);
            // 清除用户订阅缓存
            evictUserSubscriptionCache(subscription.getUserId());
        }
    }
    
    @CacheEvict(value = "userSubscriptions", key = "'current:' + #userId")
    public void evictUserSubscriptionCache(String userId) {
        // 专门用于清除缓存的方法
    }

    public boolean createDefaultSubscription(String userId) {
        try {
            String subscriptionId = UUID.randomUUID().toString();
            int result = getBaseMapper().insertDefaultSubscription(subscriptionId, userId);
            return result > 0;
        } catch (Exception e) {
            throw new RuntimeException("创建默认订阅失败: " + e.getMessage(), e);
        }
    }

    public boolean canSubscribeToLevel(String userId, String membershipLevelId) {
        MembershipLevel targetLevel = membershipLevelService.getById(membershipLevelId);
        if (targetLevel == null) {
            return false;
        }

        // 检查用户当前订阅状态
        UserSubscriptionDTO currentSubscription = getCurrentSubscription(userId);
        if (currentSubscription != null && currentSubscription.getIsActive()) {
            // 获取当前订阅的会员等级
            MembershipLevel currentLevel = membershipLevelService.getById(currentSubscription.getMembershipLevelId());
            if (currentLevel != null) {
                // 只能订阅更高优先级的等级
                return targetLevel.getPriority() > currentLevel.getPriority();
            }
        }

        // 如果没有活跃订阅，可以订阅任何等级
        return true;
    }

    private UserSubscriptionDTO convertToDTO(UserSubscription subscription) {
        UserSubscriptionDTO dto = new UserSubscriptionDTO();
        BeanUtils.copyProperties(subscription, dto);
        
        // 获取会员等级名称
        String membershipLevelId = subscription.getMembershipLevelId();
        MembershipLevel level = null;
        
        // 先尝试通过ID获取
        if (membershipLevelId != null) {
            level = membershipLevelService.getById(membershipLevelId);
            
            // 如果通过ID找不到，可能传入的是名称，尝试通过名称获取
            if (level == null) {
                level = membershipLevelService.getOne(
                    new QueryWrapper<MembershipLevel>().eq("name", membershipLevelId)
                );
            }
        }
        
        if (level != null) {
            dto.setMembershipLevelName(level.getName());
        } else {
            dto.setMembershipLevelName("未知等级");
        }
        
        // 计算是否活跃和剩余天数
        LocalDateTime now = LocalDateTime.now();
        dto.setIsActive("active".equals(subscription.getStatus()) && 
                       subscription.getEndDate().isAfter(now));
        
        if (dto.getIsActive()) {
            dto.setDaysRemaining(ChronoUnit.DAYS.between(now, subscription.getEndDate()));
        } else {
            dto.setDaysRemaining(0L);
        }
        
        return dto;
    }
}
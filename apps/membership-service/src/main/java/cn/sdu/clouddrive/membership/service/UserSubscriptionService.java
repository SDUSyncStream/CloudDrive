package cn.sdu.clouddrive.membership.service;

import cn.sdu.clouddrive.membership.dto.UserSubscriptionDTO;
import cn.sdu.clouddrive.membership.entity.MembershipLevel;
import cn.sdu.clouddrive.membership.entity.UserSubscription;
import cn.sdu.clouddrive.membership.mapper.UserSubscriptionMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
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

    public UserSubscriptionDTO getCurrentSubscription(String userId) {
        QueryWrapper<UserSubscription> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
                .eq("status", "active")
                .gt("end_date", LocalDateTime.now())
                .orderByDesc("end_date")
                .last("LIMIT 1");
        
        UserSubscription subscription = getOne(wrapper);
        return subscription != null ? convertToDTO(subscription) : null;
    }

    public UserSubscriptionDTO createSubscription(String userId, String membershipLevelId, 
                                                 String paymentMethod, java.math.BigDecimal amount) {
        MembershipLevel level = membershipLevelService.getById(membershipLevelId);
        if (level == null) {
            throw new RuntimeException("会员等级不存在");
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
        }
    }

    private UserSubscriptionDTO convertToDTO(UserSubscription subscription) {
        UserSubscriptionDTO dto = new UserSubscriptionDTO();
        BeanUtils.copyProperties(subscription, dto);
        
        // 获取会员等级名称
        MembershipLevel level = membershipLevelService.getById(subscription.getMembershipLevelId());
        if (level != null) {
            dto.setMembershipLevelName(level.getName());
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
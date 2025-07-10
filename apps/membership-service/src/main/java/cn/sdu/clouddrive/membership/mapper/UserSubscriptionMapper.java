package cn.sdu.clouddrive.membership.mapper;

import cn.sdu.clouddrive.membership.entity.UserSubscription;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserSubscriptionMapper extends BaseMapper<UserSubscription> {
    
    @Insert("INSERT INTO user_subscriptions (id, user_id, membership_level_id, start_date, end_date, status, payment_method, payment_amount, created_at, updated_at) " +
            "VALUES (#{id}, #{userId}, 'level001', NOW(), '2099-12-31 23:59:59', 'active', 'free', 0.00, NOW(), NOW())")
    int insertDefaultSubscription(@Param("id") String id, @Param("userId") String userId);
    
    @Select("SELECT us.* FROM user_subscriptions us " +
            "JOIN membership_levels ml ON us.membership_level_id = ml.id " +
            "WHERE us.user_id = #{userId} AND us.status = 'active' AND us.end_date > NOW() " +
            "ORDER BY ml.price DESC, us.end_date DESC " +
            "LIMIT 1")
    UserSubscription selectCurrentSubscription(@Param("userId") String userId);
}
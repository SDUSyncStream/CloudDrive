package cn.sdu.clouddrive.admin.Mapper;

import cn.sdu.clouddrive.admin.pojo.UserSubscription;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;


@Mapper
public interface UserSubscriptionMapper extends BaseMapper<UserSubscription> {
    // MyBatis-Plus 提供了基础的 CRUD 方法，如果你需要自定义查询，可以在这里添加
    @Select("SELECT * FROM user_subscriptions WHERE user_id = #{userId}")
    List<UserSubscription> selectByUserId(String userId);
}

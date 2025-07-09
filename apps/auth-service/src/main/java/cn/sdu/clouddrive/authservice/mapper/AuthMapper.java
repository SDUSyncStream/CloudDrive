package cn.sdu.clouddrive.authservice.mapper;

import cn.sdu.clouddrive.authservice.pojo.LoginInfo;
import cn.sdu.clouddrive.authservice.pojo.RegisterInfo;
import cn.sdu.clouddrive.authservice.pojo.UserBasicInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Map;

@Mapper
public interface AuthMapper extends BaseMapper<UserBasicInfo>
{
    @Select("select id,username from users where username = #{username} and password_hash = #{passwordHash}")
    public UserBasicInfo getUserBasicInfo(String username, String passwordHash);

    @Insert("INSERT INTO users (id, username, password_hash, email,avatar) VALUES (#{userId}, #{username}, #{passwordHash}, #{email}, #{avatar})")
    Boolean register(RegisterInfo registerInfo);

    @Select("select * from users where username = #{username}")
    Boolean isUsernameExist(String username);

    @Select("select * from users where email = #{email}")
    Boolean isEmailExist(String email);

    @Update("update users set password_hash = #{newPassword} where email = #{email}")
    Boolean refresh(String email,String newPassword);

    @Insert("INSERT INTO user_subscriptions (id, user_id, membership_level_id, start_date, end_date, status, payment_method, payment_amount, created_at, updated_at) " +
            "VALUES (#{subscriptionId}, #{userId}, 'level001', NOW(), '2099-12-31 23:59:59', 'active', 'free', 0.00, NOW(), NOW())")
    Boolean insertUserSubscription(@Param("subscriptionId") String subscriptionId, @Param("userId") String userId);
}

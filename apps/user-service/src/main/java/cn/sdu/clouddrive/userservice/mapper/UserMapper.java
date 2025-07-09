package cn.sdu.clouddrive.userservice.mapper;


import cn.sdu.clouddrive.userservice.pojo.NewPwd;
import cn.sdu.clouddrive.userservice.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper
{
    @Select("select username,email,avatar,storage_quota,storage_used,users.created_at,membership_level_id from users left join user_subscriptions on users.id = user_subscriptions.user_id where users.id = #{userId}")
    public User getUserInfo(String userId);

    @Update("update users set password_hash = #{newPassword} where id = #{userId} and password_hash = #{oldPassword}")
    public Boolean updatePwd(NewPwd newPwd);

    @Update("update users set avatar = #{avatarUrl} where id = #{userId}")
    public Boolean updateAvatar(String userId, String avatarUrl);
}

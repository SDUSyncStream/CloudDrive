package cn.sdu.clouddrive.userservice.mapper;


import cn.sdu.clouddrive.userservice.pojo.NewPwd;
import cn.sdu.clouddrive.userservice.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper
{
    @Select("select username,email,avatar,storage_quota,storage_used,created_at from users where id = #{userId}")
    public User getUserInfo(String userId);

    @Update("update users set password_hash = #{newPassword} where id = #{userId} and password_hash = #{oldPassword}")
    public Boolean updatePwd(NewPwd newPwd);
}

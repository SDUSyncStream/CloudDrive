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

    @Insert("insert into users values (username,password,email)")
    boolean register(RegisterInfo registerInfo);

    @Select("select * from users where username = #{username}")
    boolean isUsernameExist(String username);

    @Select("select * from users where email = #{email}")
    boolean isEmailExist(String email);

    @Update("update user set password_hash = #{newPassword} where username = #{username}")
    boolean refresh(Map<String, String> map);
}

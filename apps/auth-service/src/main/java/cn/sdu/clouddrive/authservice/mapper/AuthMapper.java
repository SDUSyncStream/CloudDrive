package cn.sdu.clouddrive.authservice.mapper;

import cn.sdu.clouddrive.authservice.pojo.LoginInfo;
import cn.sdu.clouddrive.authservice.pojo.UserBasicInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AuthMapper extends BaseMapper<UserBasicInfo>
{
    @Select("select user_id,nick_name from user_info where nick_name = #{nickName} and password = #{password}")
    public UserBasicInfo getUserBasicInfo(LoginInfo loginInfo);
}

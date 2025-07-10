package cn.sdu.clouddrive.admin.Mapper;

import cn.sdu.clouddrive.admin.pojo.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    /**
     * 根据用户名和密码查询管理员用户
     * @param username 用户名
     * @param passwordHash 加密后的密码
     * @return 管理员用户信息，如果不存在则返回 null
     */
    User selectAdminByUsernameAndPassword(@Param("username") String username, @Param("passwordHash") String passwordHash);


}

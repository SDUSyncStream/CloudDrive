package cn.sdu.fileupdownservice.mappers;

import cn.sdu.fileupdownservice.entity.po.UserInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户信息 数据库操作接口
 */
@Mapper
public interface UserInfoMapper<T, P> extends BaseMapper<T, P> {
    /**
     * 根据UserId获取对象
     */
    T selectByUserId(@Param("userId") String userId);

    Integer updateByUserId(@Param("bean") T t, @Param("userId") String userId);

    Integer updateUserSpace(@Param("userId") String userId, @Param("useSpace") Long useSpace, @Param("totalSpace") Long totalSpace);

    UserInfo selectUser(String userId);
}

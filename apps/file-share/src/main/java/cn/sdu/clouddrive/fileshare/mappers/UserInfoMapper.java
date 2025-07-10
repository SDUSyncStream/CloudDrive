package cn.sdu.clouddrive.fileshare.mappers;

import org.apache.ibatis.annotations.Param;

/**
 * 用户信息 数据库操作接口
 */
public interface UserInfoMapper<T, P> {
    /**
     * 根据UserId获取对象
     */
    T selectByUserId(@Param("userId") String userId);

    /**
     * 根据UserId更新
     */
    Integer updateByUserId(@Param("bean") T t, @Param("userId") String userId);

    /**
     * 更新用户空间使用量
     */
    Integer updateUserSpace(@Param("userId") String userId, @Param("useSpace") Long useSpace, @Param("totalSpace") Long totalSpace);
}

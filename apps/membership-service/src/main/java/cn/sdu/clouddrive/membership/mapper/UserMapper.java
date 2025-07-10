package cn.sdu.clouddrive.membership.mapper;

import cn.sdu.clouddrive.membership.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    @Update("UPDATE users SET storage_quota = #{storageQuota}, updated_at = NOW() WHERE id = #{userId}")
    int updateUserStorageQuota(@Param("userId") String userId, @Param("storageQuota") Long storageQuota);
}
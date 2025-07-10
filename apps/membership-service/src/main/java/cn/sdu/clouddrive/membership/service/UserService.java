package cn.sdu.clouddrive.membership.service;

import cn.sdu.clouddrive.membership.entity.User;
import cn.sdu.clouddrive.membership.mapper.UserMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class UserService extends ServiceImpl<UserMapper, User> {
    
    /**
     * 更新用户存储配额
     * @param userId 用户ID
     * @param storageQuota 新的存储配额
     * @return 更新是否成功
     */
    public boolean updateUserStorageQuota(String userId, Long storageQuota) {
        int result = baseMapper.updateUserStorageQuota(userId, storageQuota);
        return result > 0;
    }
    
    /**
     * 根据用户ID获取用户信息
     * @param userId 用户ID
     * @return 用户信息
     */
    public User getUserById(String userId) {
        return getById(userId);
    }
}
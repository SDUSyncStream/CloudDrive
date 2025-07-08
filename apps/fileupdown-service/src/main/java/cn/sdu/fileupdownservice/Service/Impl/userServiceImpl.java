package cn.sdu.fileupdownservice.Service.Impl;

import cn.sdu.fileupdownservice.Entity.User;
import cn.sdu.fileupdownservice.Mapper.userMapper;
import cn.sdu.fileupdownservice.Service.userService;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;

@Resource
public class userServiceImpl implements userService {
    @Resource
    private userMapper userInfoMapper;

    @Override
    @Transactional
    public void updateUserSpace(String userId, Long useSpace) {
        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(User::getUserId, userId)
                .set(User::getUseSpace, useSpace);
        userInfoMapper.update(null, wrapper);
    }
}

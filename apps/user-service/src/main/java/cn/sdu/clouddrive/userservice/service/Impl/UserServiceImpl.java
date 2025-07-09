package cn.sdu.clouddrive.userservice.service.Impl;

import cn.sdu.clouddrive.userservice.mapper.UserMapper;
import cn.sdu.clouddrive.userservice.pojo.NewPwd;
import cn.sdu.clouddrive.userservice.pojo.User;
import cn.sdu.clouddrive.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService
{
    @Autowired
    public UserMapper userMapper;

    @Override
    public User getUserInfo(String userId)
    {
        return userMapper.getUserInfo(userId);
    }

    @Override
    public Boolean updatePwd(NewPwd newPwd)
    {
        return userMapper.updatePwd(newPwd);
    }

    @Override
    public Boolean updateAvatar(String userId, String avatarUrl)
    {
        return userMapper.updateAvatar(userId, avatarUrl);
    }

    @Override
    public User getAvatarAndStorage(String userId)
    {
        return userMapper.getUserInfo(userId);
    }

}

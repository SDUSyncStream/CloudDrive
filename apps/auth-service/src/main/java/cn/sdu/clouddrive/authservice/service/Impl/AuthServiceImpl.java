package cn.sdu.clouddrive.authservice.service.Impl;

import cn.sdu.clouddrive.authservice.mapper.AuthMapper;
import cn.sdu.clouddrive.authservice.pojo.LoginInfo;
import cn.sdu.clouddrive.authservice.pojo.RegisterInfo;
import cn.sdu.clouddrive.authservice.pojo.UserBasicInfo;
import cn.sdu.clouddrive.authservice.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService
{
    @Autowired
    private AuthMapper  authMapper;

    @Override
    public UserBasicInfo isExist(String username, String password)
    {
        return authMapper.getUserBasicInfo(username, password);
    }

    @Override
    public Boolean register(RegisterInfo registerInfo)
    {
        return authMapper.register(registerInfo);
    }

    @Override
    public Boolean isUsernameExist(String username)
    {
        return authMapper.isUsernameExist(username);
    }

    @Override
    public Boolean isEmailExist(String email)
    {
        return authMapper.isEmailExist(email);
    }

    @Override
    public Boolean refresh(String email,String newPassword)
    {
        return authMapper.refresh(email,newPassword);
    }

    @Override
    public Boolean insertUserSubscription(String subscriptionId, String userId)
    {
        return authMapper.insertUserSubscription(subscriptionId, userId);
    }

}

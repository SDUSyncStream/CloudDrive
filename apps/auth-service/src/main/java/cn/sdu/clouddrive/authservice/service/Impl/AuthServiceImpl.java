package cn.sdu.clouddrive.authservice.service.Impl;

import cn.sdu.clouddrive.authservice.mapper.AuthMapper;
import cn.sdu.clouddrive.authservice.pojo.LoginInfo;
import cn.sdu.clouddrive.authservice.pojo.UserBasicInfo;
import cn.sdu.clouddrive.authservice.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService
{
    @Autowired
    private AuthMapper  authMapper;
    @Override
    public UserBasicInfo isExist(LoginInfo loginInfo)
    {
        return authMapper.getUserBasicInfo(loginInfo);
    }
}

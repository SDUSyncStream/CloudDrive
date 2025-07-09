package cn.sdu.clouddrive.authservice.service;

import cn.sdu.clouddrive.authservice.pojo.LoginInfo;
import cn.sdu.clouddrive.authservice.pojo.RegisterInfo;
import cn.sdu.clouddrive.authservice.pojo.UserBasicInfo;

import java.util.Map;

public interface AuthService
{
    UserBasicInfo isExist(String username, String password);

    /**
     * 用户注册
     */
    Boolean register(RegisterInfo registerInfo);

    /**
     * 检查用户名是否已存在
     */
    Boolean isUsernameExist(String username);

    /**
     * 检查邮箱是否已存在
     */
    Boolean isEmailExist(String email);

    Boolean refresh(String email,String newPassword);

    /**
     * 插入用户默认订阅
     */
    int insertUserSubscription(String subscriptionId, String userId);
}

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
    boolean register(RegisterInfo registerInfo);

    /**
     * 检查用户名是否已存在
     */
    boolean isUsernameExist(String username);

    /**
     * 检查邮箱是否已存在
     */
    boolean isEmailExist(String email);

    boolean refresh(Map<String, String> map);
}

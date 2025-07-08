package cn.sdu.clouddrive.authservice.service;

import cn.sdu.clouddrive.authservice.pojo.LoginInfo;
import cn.sdu.clouddrive.authservice.pojo.UserBasicInfo;

public interface AuthService
{
    UserBasicInfo isExist(LoginInfo loginInfo);
}

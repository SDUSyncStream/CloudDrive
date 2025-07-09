package cn.sdu.clouddrive.userservice.service;

import cn.sdu.clouddrive.userservice.pojo.NewPwd;
import cn.sdu.clouddrive.userservice.pojo.User;

public interface UserService
{
    User getUserInfo(String userId);

    Boolean updatePwd(NewPwd newPwd);
}

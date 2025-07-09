package cn.sdu.clouddrive.userservice.service;

import cn.sdu.clouddrive.userservice.pojo.NewPwd;
import cn.sdu.clouddrive.userservice.pojo.User;

public interface UserService
{
    User getUserInfo(String userId);

    Boolean updatePwd(NewPwd newPwd);

    /**
     * 更新用户头像
     */
    Boolean updateAvatar(String userId, String avatarUrl);

    User getAvatarAndStorage(String userId);
}

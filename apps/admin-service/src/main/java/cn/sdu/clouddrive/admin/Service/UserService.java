package cn.sdu.clouddrive.admin.Service;

import cn.sdu.clouddrive.admin.pojo.User;

public interface UserService {
    User adminLogin(String username, String password);
}

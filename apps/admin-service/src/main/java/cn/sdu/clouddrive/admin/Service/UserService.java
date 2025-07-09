package cn.sdu.clouddrive.admin.Service;


import cn.sdu.clouddrive.admin.pojo.SummaryInfo;
import cn.sdu.clouddrive.admin.pojo.User;

import java.util.List;

public interface UserService {
    User adminLogin(String username, String password);
    //统计用户数量，历史已分配总额与使用的总空间量，文件总数量，最近一周的新增用户数量
    SummaryInfo getSummaryInfo();
    //返回所有用户
    List<User> getAllUsers();
    //根据id删除用户
    int deleteUserById(String id);
    //插入用户
    int insertUser(User user);
    //根据id更新用户
    int updateUserById(User user);
}

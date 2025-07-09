package cn.sdu.clouddrive.admin.Service;


import cn.sdu.clouddrive.admin.pojo.SummaryInfo;
import cn.sdu.clouddrive.admin.pojo.User;

public interface UserService {
    User adminLogin(String username, String password);
    //统计用户数量，历史已分配总额与使用的总空间量，文件总数量，最近一周的新增用户数量
    SummaryInfo getSummaryInfo();
    //

}

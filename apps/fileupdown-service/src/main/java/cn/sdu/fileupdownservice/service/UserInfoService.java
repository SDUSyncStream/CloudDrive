package cn.sdu.fileupdownservice.service;


import cn.sdu.fileupdownservice.entity.po.UserInfo;
import cn.sdu.fileupdownservice.entity.query.UserInfoQuery;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户信息 业务接口
 */
public interface UserInfoService {

    /**
     * 根据条件查询列表
     */
    List<UserInfo> findListByParam(UserInfoQuery param);

    void updateUserStatus(String userId, Integer status);

    void changeUserSpace(String userId, Integer changeSpace);
    UserInfo selectByUserId(String userId);
    UserInfo selectUser(String userId);
}
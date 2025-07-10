package cn.sdu.fileupdownservice.service;


import cn.sdu.fileupdownservice.entity.po.Users;
import cn.sdu.fileupdownservice.entity.query.UserInfoQuery;

import java.util.List;

/**
 * 用户信息 业务接口
 */
public interface UserInfoService {

    /**
     * 根据条件查询列表
     */
    List<Users> findListByParam(UserInfoQuery param);

    void updateUserStatus(String userId, Integer status);

    void changeUserSpace(String userId, Integer changeSpace);
    Users selectByUserId(String userId);
    Users selectUser(String userId);
}
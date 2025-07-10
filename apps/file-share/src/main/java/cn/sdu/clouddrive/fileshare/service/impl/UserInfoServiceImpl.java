package cn.sdu.clouddrive.fileshare.service.impl;


import cn.sdu.clouddrive.fileshare.mappers.UserInfoMapper;
import cn.sdu.clouddrive.fileshare.pojo.po.UserInfo;
import cn.sdu.clouddrive.fileshare.pojo.query.UserInfoQuery;
import cn.sdu.clouddrive.fileshare.service.UserInfoService;

import org.springframework.stereotype.Service;

import javax.annotation.Resource;


/**
 * 用户信息 业务接口实现
 */
@Service("userInfoService")
public class UserInfoServiceImpl implements UserInfoService {

    @Resource
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

    /**
     * 根据UserId获取对象
     */
    @Override
    public UserInfo getUserInfoByUserId(String userId) {
        return this.userInfoMapper.selectByUserId(userId);
    }


}
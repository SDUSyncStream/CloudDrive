package cn.sdu.fileupdownservice.service.impl;

import cn.sdu.fileupdownservice.component.RedisComponent;
import cn.sdu.fileupdownservice.entity.constants.Constants;
import cn.sdu.fileupdownservice.entity.enums.UserStatusEnum;
import cn.sdu.fileupdownservice.entity.po.UserInfo;
import cn.sdu.fileupdownservice.entity.query.UserInfoQuery;
import cn.sdu.fileupdownservice.mappers.UserInfoMapper;
import cn.sdu.fileupdownservice.service.FileInfoService;
import cn.sdu.fileupdownservice.service.UserInfoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;


/**
 * 用户信息 业务接口实现
 */
@Service("userInfoService")
public class UserInfoServiceImpl implements UserInfoService {

    @Resource
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;


    @Resource
    private FileInfoService fileInfoService;

    @Resource
    private RedisComponent redisComponent;

    /**
     * 根据条件查询列表
     */
    @Override
    public List<UserInfo> findListByParam(UserInfoQuery param) {
        return this.userInfoMapper.selectList(param);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserStatus(String userId, Integer status) {
        UserInfo userInfo = new UserInfo();
        userInfo.setStatus(status);
        if (UserStatusEnum.DISABLE.getStatus().equals(status)) {
            userInfo.setUseSpace(0L);
            fileInfoService.deleteFileByUserId(userId);
        }
        userInfoMapper.updateByUserId(userInfo, userId);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeUserSpace(String userId, Integer changeSpace) {
        Long space = changeSpace * Constants.MB;
        this.userInfoMapper.updateUserSpace(userId, null, space);
        redisComponent.resetUserSpaceUse(userId);
    }
}
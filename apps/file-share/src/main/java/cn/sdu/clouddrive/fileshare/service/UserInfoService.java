package cn.sdu.clouddrive.fileshare.service;



import cn.sdu.clouddrive.fileshare.pojo.po.UserInfo;



/**
 * 用户信息 业务接口
 */
public interface UserInfoService {


    /**
     * 根据UserId查询对象
     */
    UserInfo getUserInfoByUserId(String userId);


}
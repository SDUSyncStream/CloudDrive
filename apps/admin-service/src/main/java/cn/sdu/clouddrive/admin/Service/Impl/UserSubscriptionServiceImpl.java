package cn.sdu.clouddrive.admin.Service.Impl;

import cn.sdu.clouddrive.admin.Mapper.UserSubscriptionMapper; // 假设你有这个Mapper
import cn.sdu.clouddrive.admin.Service.UserSubscriptionService;
import cn.sdu.clouddrive.admin.pojo.UserSubscription;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper; // 用于构建查询条件
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserSubscriptionServiceImpl implements UserSubscriptionService {

    @Autowired
    private UserSubscriptionMapper userSubscriptionMapper; // 注入 UserSubscriptionMapper

    @Override
    public List<UserSubscription> getAllUserSubscriptions() {
        return userSubscriptionMapper.selectList(null); // selectList(null) 获取所有记录
    }

    @Override
    public List<UserSubscription> getUserSubscriptionsByUserId(String userId) {
        QueryWrapper<UserSubscription> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId); // 根据 user_id 字段查询
        return userSubscriptionMapper.selectList(queryWrapper);
    }

    @Override
    public int addUserSubscription(UserSubscription subscription) {
        return userSubscriptionMapper.insert(subscription);
    }

    @Override
    public int updateUserSubscription(UserSubscription subscription) {
        return userSubscriptionMapper.updateById(subscription);
    }

    @Override
    public int deleteUserSubscription(String id) {
        return userSubscriptionMapper.deleteById(id);
    }
}
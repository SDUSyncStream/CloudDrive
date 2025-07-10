package cn.sdu.clouddrive.admin.Service;

import cn.sdu.clouddrive.admin.pojo.UserSubscription;
import java.util.List;

public interface UserSubscriptionService {
    /**
     * 获取所有用户订阅信息
     * @return 所有用户订阅的列表
     */
    List<UserSubscription> getAllUserSubscriptions();

    /**
     * 根据用户ID获取其所有订阅信息
     * @param userId 用户ID
     * @return 该用户的所有订阅列表
     */
    List<UserSubscription> getUserSubscriptionsByUserId(String userId);

    /**
     * 添加新的用户订阅
     * @param subscription 要添加的用户订阅对象
     * @return 影响的行数，通常为 1 表示成功
     */
    int addUserSubscription(UserSubscription subscription);

    /**
     * 更新用户订阅信息
     * @param subscription 要更新的用户订阅对象
     * @return 影响的行数，通常为 1 表示成功
     */
    int updateUserSubscription(UserSubscription subscription);

    /**
     * 根据ID删除用户订阅
     * @param id 要删除的用户订阅的ID
     * @return 影响的行数，通常为 1 表示成功
     */
    int deleteUserSubscription(String id);
}
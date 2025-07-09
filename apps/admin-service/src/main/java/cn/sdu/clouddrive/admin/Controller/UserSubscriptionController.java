package cn.sdu.clouddrive.admin.Controller;

import cn.sdu.clouddrive.admin.Service.UserSubscriptionService;
import cn.sdu.clouddrive.admin.pojo.UserSubscription;
import cn.sdu.clouddrive.admin.util.ServerResult; // 统一返回结果封装类
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin-api/subscriptions") // 定义订阅相关的API基础路径
public class UserSubscriptionController {

    @Autowired
    private UserSubscriptionService userSubscriptionService; // 注入 UserSubscriptionService

    /**
     * 获取所有用户订阅信息
     * @return ServerResult 封装的用户订阅列表
     */
    @GetMapping("/getAll")
    public ServerResult<List<UserSubscription>> getAllUserSubscriptions() {
        List<UserSubscription> subscriptions = userSubscriptionService.getAllUserSubscriptions();
        return ServerResult.ok(subscriptions, "获取所有用户订阅成功");
    }

    /**
     * 根据用户ID获取其订阅信息
     * @param userId 用户ID
     * @return ServerResult 封装的用户订阅列表
     */
    @GetMapping("/getByUserId/{userId}")
    public ServerResult<List<UserSubscription>> getUserSubscriptionsByUserId(@PathVariable String userId) {
        if (userId == null || userId.isEmpty()) {
            return ServerResult.fail("用户ID不能为空");
        }
        List<UserSubscription> subscriptions = userSubscriptionService.getUserSubscriptionsByUserId(userId);
        return ServerResult.ok(subscriptions, "获取用户订阅成功");
    }


    /**
     * 添加新的用户订阅
     *
     * @param subscription 包含用户订阅信息的JSON请求体
     * @return ServerResult 封装的添加结果
     */
    @PostMapping("/add")
    public ServerResult<String> addUserSubscription(@RequestBody UserSubscription subscription) {
        // 参数校验（简化示例，实际项目中应更完善）
        if (subscription.getUserId() == null || subscription.getUserId().isEmpty() ||
                subscription.getMembershipLevelId() == null || subscription.getMembershipLevelId().isEmpty() ||
                subscription.getStartDate() == null || subscription.getEndDate() == null) {
            return ServerResult.fail("用户ID、会员等级ID、开始日期和结束日期不能为空");
        }

        // 自动生成ID
        if (subscription.getId() == null || subscription.getId().isEmpty()) {
            subscription.setId(UUID.randomUUID().toString());
        }
        // 设置创建时间和更新时间（如果POJO中没有通过@TableField(fill = FieldFill.INSERT_UPDATE)自动填充）
        if (subscription.getCreatedAt() == null) {
            subscription.setCreatedAt(LocalDateTime.now());
        }
        subscription.setUpdatedAt(LocalDateTime.now());

        int result = userSubscriptionService.addUserSubscription(subscription);
        if (result > 0) {
            return ServerResult.ok("用户订阅添加成功");
        } else {
            return ServerResult.fail("用户订阅添加失败");
        }
    }

    /**
     * 根据ID更新用户订阅信息
     *
     * @param subscription 包含更新后用户订阅信息的JSON请求体
     * @return ServerResult 封装的更新结果
     */
    @PutMapping("/update")
    public ServerResult<String> updateUserSubscription(@RequestBody UserSubscription subscription) {
        if (subscription.getId() == null || subscription.getId().isEmpty()) {
            return ServerResult.fail("用户订阅ID不能为空");
        }
        // 设置更新时间
        subscription.setUpdatedAt(LocalDateTime.now());

        int result = userSubscriptionService.updateUserSubscription(subscription);
        if (result > 0) {
            return ServerResult.ok("用户订阅更新成功");
        } else {
            return ServerResult.fail("用户订阅更新失败");
        }
    }

    /**
     * 根据ID删除用户订阅
     *
     * @param id 要删除的用户订阅的ID
     * @return ServerResult 封装的删除结果
     */
    @DeleteMapping("/delete/{id}")
    public ServerResult<String> deleteUserSubscription(@PathVariable String id) {
        if (id == null || id.isEmpty()) {
            return ServerResult.fail("用户订阅ID不能为空");
        }
        int result = userSubscriptionService.deleteUserSubscription(id);
        if (result > 0) {
            return ServerResult.ok("用户订阅删除成功");
        } else {
            return ServerResult.fail("用户订阅删除失败");
        }
    }
}
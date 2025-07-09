package cn.sdu.clouddrive.authservice.controller;

import cn.sdu.clouddrive.authservice.pojo.RegisterInfo;
import cn.sdu.clouddrive.authservice.pojo.ServerResult;
import cn.sdu.clouddrive.authservice.pojo.UserBasicInfo;
import cn.sdu.clouddrive.authservice.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class RegisterController {

    @Autowired
    private AuthService authService;
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ServerResult<String> register(@RequestBody RegisterInfo registerInfo) {
        log.info("用户注册请求: {}", registerInfo.getUsername());

        try {
            // 检查用户名是否已存在
            if (authService.isUsernameExist(registerInfo.getUsername())!=null) {
                return ServerResult.error(409, "用户名已存在");
            }

            // 检查邮箱是否已存在（如果提供了邮箱）
            if (registerInfo.getEmail() != null && !registerInfo.getEmail().isEmpty()) {
                if (authService.isEmailExist(registerInfo.getEmail())!=null) {
                    return ServerResult.error(409, "邮箱已被注册");
                }
            }

            // 检查完邮箱和用户名正确性后，生成UUID
            String userId = UUID.randomUUID().toString();
            registerInfo.setUserId(userId);

            log.info("为用户 {} 生成UUID: {}", registerInfo.getUsername(), userId);

            registerInfo.setAvatar("/user.jpg");

            // 执行注册
            boolean registerSuccess = false;
            try {
                registerSuccess = authService.register(registerInfo);
                log.info("用户 {} 注册结果: {}", registerInfo.getUsername(), registerSuccess);
            } catch (Exception e) {
                log.error("用户 {} 注册失败，错误: {}", registerInfo.getUsername(), e.getMessage(), e);
                return ServerResult.error(500, "注册失败: " + e.getMessage());
            }

            if (registerSuccess) {
                log.info("用户 {} 注册成功，用户ID: {}", registerInfo.getUsername(), userId);
                
                // 创建默认订阅
                try {
                    String subscriptionId = UUID.randomUUID().toString();
                    log.info("开始创建默认订阅，用户ID: {}, 订阅ID: {}", userId, subscriptionId);
                    int subscriptionResult = authService.insertUserSubscription(subscriptionId, userId);
                    if (subscriptionResult > 0) {
                        log.info("用户 {} 默认订阅创建成功，订阅ID: {}, 影响行数: {}", userId, subscriptionId, subscriptionResult);
                    } else {
                        log.error("创建默认订阅失败，用户ID: {}, 影响行数: {}", userId, subscriptionResult);
                    }
                } catch (Exception e) {
                    log.error("创建默认订阅失败，用户ID: {}, 错误: {}", userId, e.getMessage(), e);
                    // 订阅创建失败不影响注册成功
                }
                
                return ServerResult.ok("注册成功");
            } else {
                log.error("用户 {} 注册失败，authService.register返回false", registerInfo.getUsername());
                return ServerResult.error(500, "注册失败，请稍后重试");
            }

        } catch (Exception e) {
            log.error("注册失败: {}", e.getMessage());
            return ServerResult.error(500, "注册失败: " + e.getMessage());
        }
    }
}

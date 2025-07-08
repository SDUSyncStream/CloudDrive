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
            if (authService.isUsernameExist(registerInfo.getUsername())) {
                return ServerResult.error(409, "用户名已存在");
            }

            // 检查邮箱是否已存在（如果提供了邮箱）
            if (registerInfo.getEmail() != null && !registerInfo.getEmail().isEmpty()) {
                if (authService.isEmailExist(registerInfo.getEmail())) {
                    return ServerResult.error(409, "邮箱已被注册");
                }
            }

            // 执行注册
            boolean registerSuccess = authService.register(registerInfo);

            if (registerSuccess) {
                log.info("用户 {} 注册成功", registerInfo.getUsername());
                return ServerResult.ok("注册成功");
            } else {
                return ServerResult.error(500, "注册失败，请稍后重试");
            }

        } catch (Exception e) {
            log.error("注册失败: {}", e.getMessage());
            return ServerResult.error(500, "注册失败: " + e.getMessage());
        }
    }
}

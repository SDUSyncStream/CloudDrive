package cn.sdu.clouddrive.authservice.controller;

import cn.sdu.clouddrive.authservice.pojo.LoginInfo;
import cn.sdu.clouddrive.authservice.pojo.ServerResult;

import cn.sdu.clouddrive.authservice.pojo.UserBasicInfo;
import cn.sdu.clouddrive.authservice.service.AuthService;
import cn.sdu.clouddrive.authservice.service.RedisService;
import cn.sdu.clouddrive.authservice.service.SubscriptionCheckService;
import cn.sdu.clouddrive.authservice.util.JwtUtil;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
//@CrossOrigin
public class AuthController
{
    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisService redisService;

    @Autowired
    private SubscriptionCheckService subscriptionCheckService;

//    @GetMapping("/api/auth/login")
//    public ServerResult<Map<String, String>> login(@RequestParam String username,@RequestParam String password)
//    {
//        UserBasicInfo userBasicInfo = authService.isExist(username,password);
//        log.info("用户登录请求: {},{}", username, password);
//        if (userBasicInfo != null)
//        {
//            // 生成JWT token
//            String token = jwtUtil.generateToken(userBasicInfo.getUsername(), userBasicInfo.getId());
//
//            // 将token存储到Redis，使用用户ID作为主键，过期时间24小时
//            redisService.storeToken(userBasicInfo.getId(), token, 86400000L);
//
//            Map<String, String> map = new HashMap<>();
//            map.put("username", userBasicInfo.getUsername());
//            map.put("token", token);
//            map.put("userId", userBasicInfo.getId());
//
//            return ServerResult.ok(map);
//        } else
//        {
//            return ServerResult.error(401, "用户不存在或密码错误");
//        }
//    }

    @PostMapping("/api/auth/login")
    public ServerResult<Map<String, String>> login(@RequestBody LoginInfo loginInfo)
    {
//        UserBasicInfo userBasicInfo = authService.isExist(username,password);
//        log.info("用户登录请求: {},{}", username, password);
        log.info("用户登录请求: {},{}", loginInfo.getUsername(), loginInfo.getPasswordHash());
        UserBasicInfo userBasicInfo = authService.isExist(loginInfo.getUsername(), loginInfo.getPasswordHash());
        if (userBasicInfo != null)
        {
            // 检查并确保用户有有效订阅
            subscriptionCheckService.ensureUserHasValidSubscription(userBasicInfo.getId());

            // 生成JWT token
            String token = jwtUtil.generateToken(userBasicInfo.getUsername(), userBasicInfo.getId());

            // 将token存储到Redis，使用用户ID作为主键，过期时间24小时
            redisService.storeToken(userBasicInfo.getId(), token, 86400000L);

            Map<String, String> map = new HashMap<>();
            map.put("username", userBasicInfo.getUsername());
            map.put("token", token);
            map.put("userId", userBasicInfo.getId());

            return ServerResult.ok(map);
        } else
        {
            return ServerResult.error(401, "用户不存在或密码错误");
        }
    }

    @PostMapping("/api/auth/logout")
    public ServerResult<String> logout(@RequestHeader("Authorization") String token)
    {
        log.info("用户登出请求，token: {}", token);

        try
        {
            // 去掉Bearer前缀
            if (token != null && token.startsWith("Bearer "))
            {
                token = token.substring(7);
            }

            // 验证token有效性
            if (!jwtUtil.validateToken(token))
            {
                return ServerResult.error(401, "无效的token");
            }

            // 从token中获取用户ID
            String userId = jwtUtil.getUserIdFromToken(token);

            // 从Redis中删除token
            redisService.deleteToken(userId);

            log.info("用户 {} 成功登出", userId);
            return ServerResult.ok("登出成功");

        } catch (Exception e)
        {
            log.error("登出失败: {}", e.getMessage());
            return ServerResult.error(500, "登出失败");
        }
    }

    @PostMapping("/api/auth/refresh")
    public ServerResult<Void> refresh(@RequestBody Map<String, String> map)
    {
        log.info("收到密码重置请求");

        System.out.println(map);
        try
        {
            String email = map.get("email");
            String newPassword = map.get("newPassword");

            if (authService.refresh(email,newPassword))
            {
                return ServerResult.ok();
            }
            return ServerResult.error(500, "重置失败");

        } catch (Exception e)
        {
            return ServerResult.error(500, "重置失败");
        }

    }

    @GetMapping("/test")
    public ServerResult<String> test()
    {
        log.info("test");
        return ServerResult.ok("Auth Service is running");
    }


}

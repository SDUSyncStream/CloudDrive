package cn.sdu.clouddrive.authservice.controller;

import cn.sdu.clouddrive.authservice.pojo.ServerResult;
import cn.sdu.clouddrive.authservice.pojo.UserBasicInfo;
import cn.sdu.clouddrive.authservice.service.AuthService;
import cn.sdu.clouddrive.authservice.service.RedisService;
import cn.sdu.clouddrive.authservice.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class TokenVerifyController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisService redisService;

    @Autowired
    private AuthService authService;

    /**
     * 验证token权限 - 供其他微服务调用
     * 其他微服务可以调用此接口来验证用户token的有效性
     */
    @PostMapping("/verify")
    public ServerResult<Map<String, Object>> verifyToken(@RequestBody Map<String, String> request) {
        String token = request.get("token");

        log.info("收到其他服务的token验证请求");

        if (token == null || token.isEmpty()) {
            return ServerResult.error(401, "token不能为空");
        }

        try {
            // 验证token格式和有效性
            if (!jwtUtil.validateToken(token)) {
                log.warn("无效的token格式");
                return ServerResult.error(401, "无效的token");
            }

            // 从token中获取用户信息
            String userId = jwtUtil.getUserIdFromToken(token);
            String username = jwtUtil.getUsernameFromToken(token);

            // 检查Redis中是否存在该token
            String redisToken = redisService.getToken(userId);
            if (redisToken == null || !redisToken.equals(token)) {
                log.warn("token已失效或不存在于Redis中: userId={}", userId);
                return ServerResult.error(401, "token已失效");
            }

            // 构建返回的用户信息
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("userId", userId);
            userInfo.put("username", username);
            userInfo.put("valid", true);
            userInfo.put("message", "token验证成功");

            log.info("用户 {}({}) token验证成功", username, userId);
            return ServerResult.ok(userInfo);

        } catch (Exception e) {
            log.error("token验证失败: {}", e.getMessage());
            return ServerResult.error(500, "token验证失败: " + e.getMessage());
        }
    }

    /**
     * 健康检查端点 - 测试服务是否正常运行
     */
    @GetMapping("/health")
    public ServerResult<String> health() {
        log.info("Auth service health check requested");
        return ServerResult.ok("Auth Service is running and healthy");
    }
}

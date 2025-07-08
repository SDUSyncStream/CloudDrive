package cn.sdu.clouddrive.authservice.controller;

import cn.sdu.clouddrive.authservice.mapper.AuthMapper;
import cn.sdu.clouddrive.authservice.pojo.LoginInfo;
import cn.sdu.clouddrive.authservice.pojo.ServerResult;
import cn.sdu.clouddrive.authservice.pojo.UserBasicInfo;
import cn.sdu.clouddrive.authservice.service.AuthService;
import cn.sdu.clouddrive.authservice.service.RedisService;
import cn.sdu.clouddrive.authservice.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController
{
    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisService redisService;

    @PostMapping("/login")
    public ServerResult<Map<String,String>> login(@RequestBody LoginInfo loginInfo)
    {
        UserBasicInfo userBasicInfo = authService.isExist(loginInfo);
        log.info("用户登录请求: {}", loginInfo);
        if(userBasicInfo !=null)
        {
            // 生成JWT token
            String token = jwtUtil.generateToken(userBasicInfo.getNickName(), userBasicInfo.getUserId());

            // 将token存储到Redis，使用用户ID作为主键，过期时间24小时
            redisService.storeToken(userBasicInfo.getUserId(), token, 86400000L);

            Map<String,String> map = new HashMap<>();
            map.put("username", userBasicInfo.getNickName());
            map.put("token", token);
            map.put("userId", String.valueOf(userBasicInfo.getUserId()));

            return ServerResult.ok(map);
        }
        else{
            return ServerResult.error(401,"用户不存在或密码错误");
        }
    }
}

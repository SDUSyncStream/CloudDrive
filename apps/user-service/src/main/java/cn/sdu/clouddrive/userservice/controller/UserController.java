package cn.sdu.clouddrive.userservice.controller;

import cn.sdu.clouddrive.userservice.pojo.NewPwd;
import cn.sdu.clouddrive.userservice.pojo.ServerResult;
import cn.sdu.clouddrive.userservice.service.UserService;
import cn.sdu.clouddrive.userservice.pojo.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
public class UserController
{

    @Autowired
    private UserService userService;

    @GetMapping("/api/user/profile/{userId}")
    public ServerResult<User> getUserInfo(@PathVariable String userId)
    {
        User user = userService.getUserInfo(userId);
        if(user != null)
        {
            return ServerResult.ok(user);
        }
        return ServerResult.error(400,"获取用户信息失败");
    }

    @PostMapping("/api/user/newpwd")
    public ServerResult<Void> newPwd(@RequestBody NewPwd newPwd)
    {
        if(userService.updatePwd(newPwd))
            return ServerResult.ok();
        return ServerResult.error(400,"更新失败");
    }
}

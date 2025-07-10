package cn.sdu.clouddrive.userservice.controller;

import cn.sdu.clouddrive.userservice.pojo.NewPwd;
import cn.sdu.clouddrive.userservice.pojo.ServerResult;
import cn.sdu.clouddrive.userservice.service.ImageHostingService;
import cn.sdu.clouddrive.userservice.service.UserService;
import cn.sdu.clouddrive.userservice.pojo.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
public class UserController
{

    @Autowired
    private UserService userService;

    @Autowired
    private ImageHostingService imageHostingService;

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

    /**
     * 上传用户头像
     */
    @PostMapping("/api/user/avatar")
    public ServerResult<Map<String, String>> uploadAvatar(
            @RequestParam("avatar") MultipartFile file,
            @RequestParam("userId") String userId) {

        try {
//            log.info("收到头像上传请求，用户ID: {}, 文件名: {}", userId, file.getOriginalFilename());

            // 验证文件
            if (!imageHostingService.isValidImage(file)) {
                return ServerResult.error(400, "无效的图片文件，请上传JPG、PNG、GIF或WebP格式的图片，且文件大小不超过10MB");
            }

            // 上传图片到托管平台
            String imageUrl = imageHostingService.uploadImage(file);

            // 更新数据库中的头像URL
            Boolean updateSuccess = userService.updateAvatar(userId, imageUrl);

            if (updateSuccess) {
                Map<String, String> result = new HashMap<>();
                result.put("avatarUrl", imageUrl);
                result.put("userId", userId);

//                log.info("头像上传成功，用户ID: {}, 图片URL: {}", userId, imageUrl);
                return ServerResult.ok(result);
            } else {
//                log.error("更新数据库头像失败，用户ID: {}", userId);
                return ServerResult.error(500, "更新头像失败");
            }

        } catch (Exception e) {
//            log.error("头像上传失败，用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            return ServerResult.error(500, "头像上传失败: " + e.getMessage());
        }
    }

    @GetMapping("/api/user/avatarandstorage/{userId}")
    public ServerResult<Map<String,String>> getAvatarAndStorage(@PathVariable String userId) {
        Map<String, String> result = new HashMap<>();
        User user = userService.getAvatarAndStorage(userId);
        if (user != null) {
            result.put("avatar", user.getAvatar());
            result.put("storageQuota", String.valueOf(user.getStorageQuota()));
            result.put("storageUsed", String.valueOf(user.getStorageUsed()));
            return ServerResult.ok(result);
        }
        return ServerResult.error(400, "获取用户信息失败");
    }
}

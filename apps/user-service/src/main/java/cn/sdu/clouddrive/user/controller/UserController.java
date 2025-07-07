package cn.sdu.clouddrive.user.controller;

import cn.sdu.clouddrive.user.dto.ApiResponse;
import cn.sdu.clouddrive.user.dto.LoginRequest;
import cn.sdu.clouddrive.user.dto.RegisterRequest;
import cn.sdu.clouddrive.user.entity.User;
import cn.sdu.clouddrive.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @PostMapping("/register")
    public ApiResponse<User> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = userService.register(request);
            return ApiResponse.success("注册成功", user);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        try {
            String token = userService.login(request);
            User user = userService.getUserByUsername(request.getUsername());
            
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("user", user);
            
            return ApiResponse.success("登录成功", data);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/profile/{id}")
    public ApiResponse<User> getProfile(@PathVariable String id) {
        try {
            User user = userService.getUserById(id);
            if (user == null) {
                return ApiResponse.error("用户不存在");
            }
            return ApiResponse.success(user);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PutMapping("/profile/{id}")
    public ApiResponse<User> updateProfile(@PathVariable String id, @RequestBody User user) {
        try {
            user.setId(id);
            User updatedUser = userService.updateUser(user);
            return ApiResponse.success("更新成功", updatedUser);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/quota/{id}")
    public ApiResponse<Map<String, Object>> getQuota(@PathVariable String id) {
        try {
            User user = userService.getUserById(id);
            if (user == null) {
                return ApiResponse.error("用户不存在");
            }
            
            Map<String, Object> data = new HashMap<>();
            data.put("storageQuota", user.getStorageQuota());
            data.put("storageUsed", user.getStorageUsed());
            data.put("storageAvailable", user.getStorageQuota() - user.getStorageUsed());
            
            return ApiResponse.success(data);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PutMapping("/quota/{id}")
    public ApiResponse<String> updateQuota(@PathVariable String id, @RequestBody Map<String, Long> request) {
        try {
            Long storageQuota = request.get("storageQuota");
            userService.updateStorageQuota(id, storageQuota);
            return ApiResponse.success("配额更新成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping("/validate")
    public ApiResponse<Map<String, Object>> validateToken(@RequestBody Map<String, String> request) {
        try {
            String token = request.get("token");
            boolean isValid = userService.validateToken(token);
            
            Map<String, Object> data = new HashMap<>();
            data.put("valid", isValid);
            
            if (isValid) {
                String userId = userService.getUserIdFromToken(token);
                data.put("userId", userId);
            }
            
            return ApiResponse.success(data);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
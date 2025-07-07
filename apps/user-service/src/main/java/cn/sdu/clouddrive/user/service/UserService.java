package cn.sdu.clouddrive.user.service;

import cn.sdu.clouddrive.user.dto.LoginRequest;
import cn.sdu.clouddrive.user.dto.RegisterRequest;
import cn.sdu.clouddrive.user.entity.User;

public interface UserService {
    User register(RegisterRequest request);
    String login(LoginRequest request);
    User getUserByUsername(String username);
    User getUserById(String id);
    User updateUser(User user);
    void updateStorageUsed(String userId, Long storageUsed);
    void updateStorageQuota(String userId, Long storageQuota);
    boolean validateToken(String token);
    String getUserIdFromToken(String token);
}
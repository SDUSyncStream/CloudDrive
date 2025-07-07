package cn.sdu.clouddrive.user.service.impl;

import cn.sdu.clouddrive.user.dto.LoginRequest;
import cn.sdu.clouddrive.user.dto.RegisterRequest;
import cn.sdu.clouddrive.user.entity.User;
import cn.sdu.clouddrive.user.mapper.UserMapper;
import cn.sdu.clouddrive.user.service.UserService;
import cn.sdu.clouddrive.user.config.JwtUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Override
    public User register(RegisterRequest request) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", request.getUsername())
                   .or()
                   .eq("email", request.getEmail());
        
        User existingUser = userMapper.selectOne(queryWrapper);
        if (existingUser != null) {
            throw new RuntimeException("用户名或邮箱已存在");
        }
        
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        User user = new User(
            UUID.randomUUID().toString(),
            request.getUsername(),
            request.getEmail(),
            hashedPassword
        );
        
        userMapper.insert(user);
        return user;
    }
    
    @Override
    public String login(LoginRequest request) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", request.getUsername())
                   .or()
                   .eq("email", request.getUsername());
        
        User user = userMapper.selectOne(queryWrapper);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("密码错误");
        }
        
        return jwtUtil.generateToken(user.getId());
    }
    
    @Override
    public User getUserByUsername(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return userMapper.selectOne(queryWrapper);
    }
    
    @Override
    public User getUserById(String id) {
        return userMapper.selectById(id);
    }
    
    @Override
    public User updateUser(User user) {
        userMapper.updateById(user);
        return user;
    }
    
    @Override
    public void updateStorageUsed(String userId, Long storageUsed) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setStorageUsed(storageUsed);
            userMapper.updateById(user);
        }
    }
    
    @Override
    public void updateStorageQuota(String userId, Long storageQuota) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setStorageQuota(storageQuota);
            userMapper.updateById(user);
        }
    }
    
    @Override
    public boolean validateToken(String token) {
        return jwtUtil.validateToken(token);
    }
    
    @Override
    public String getUserIdFromToken(String token) {
        return jwtUtil.getUserIdFromToken(token);
    }
}
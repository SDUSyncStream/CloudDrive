package cn.sdu.clouddrive.authservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 存储token到Redis
     */
    public void storeToken(String userId, String token, long expireTime) {
        String key = "user:token:" + userId;
        redisTemplate.opsForValue().set(key, token, expireTime, TimeUnit.MILLISECONDS);
    }

    /**
     * 从Redis获取token
     */
    public String getToken(String userId) {
        String key = "user:token:" + userId;
        return (String) redisTemplate.opsForValue().get(key);
    }

    /**
     * 删除token
     */
    public void deleteToken(String userId) {
        String key = "user:token:" + userId;
        redisTemplate.delete(key);
    }

    /**
     * 检查token是否存在
     */
    public boolean hasToken(String userId) {
        String key = "user:token:" + userId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 设置token过期时间
     */
    public void expireToken(String userId, long timeout, TimeUnit timeUnit) {
        String key = "user:token:" + userId;
        redisTemplate.expire(key, timeout, timeUnit);
    }
}

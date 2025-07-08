package cn.sdu.clouddrive.authservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;


@Service
public class Redis1Service
{
    @Autowired
    @Qualifier("redisTemplate1")
    private RedisTemplate<String, Object> redisTemplate1;

    public String getCode(String addr)
    {
        return (String)redisTemplate1.opsForValue().get(addr);
    }

}

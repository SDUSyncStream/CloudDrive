package cn.sdu.clouddrive.authservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
public class RedisConfig {


    private int database0 = 0;


    private int database1 = 1;


    private int timeout = 10;

    private int database2;
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 设置key的序列化器
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // 设置value的序列化器
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate0()
    {
        RedisTemplate<String, Object> redisTemplate0 = this.getRedisConnectionFactory(database0);
        return redisTemplate0;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate1()
    {
        RedisTemplate<String, Object> redisTemplate1 = this.getRedisConnectionFactory(database1);
        return redisTemplate1;
    }

    public RedisTemplate<String, Object> getRedisConnectionFactory(int database) {
        JedisConnectionFactory jedisFactory = new JedisConnectionFactory();
//        jedisFactory.setHostName(host);
//        jedisFactory.setPort(port);
//        jedisFactory.setPassword(password);
        jedisFactory.setDatabase(database);

        JedisPoolConfig poolConfig = new JedisPoolConfig(); // 进行连接池配置
        jedisFactory.setPoolConfig(poolConfig);
        jedisFactory.afterPropertiesSet(); // 初始化连接池配置

        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.setConnectionFactory(jedisFactory);
        return redisTemplate;
    }
}

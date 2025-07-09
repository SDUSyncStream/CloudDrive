package cn.sdu.clouddrive.mailservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class VerificationCodeService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String VERIFICATION_CODE_PREFIX = "email:verification:";
    private static final int CODE_LENGTH = 6;
    private static final int EXPIRE_MINUTES = 1;
    private static final String CHARACTERS = "0123456789";

    private final SecureRandom random = new SecureRandom();

    /**
     * 生成验证码
     */
    public String generateCode() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return code.toString();
    }

    /**
     * 存储验证码到Redis
     */
    public void storeCode(String email, String code, String type) {
        String key = VERIFICATION_CODE_PREFIX + type + ":" + email;
        stringRedisTemplate.opsForValue().set(key, code, EXPIRE_MINUTES, TimeUnit.MINUTES);
        log.info("验证码已存储到Redis，邮箱: {}, 类型: {}, 过期时间: {}分钟", email, type, EXPIRE_MINUTES);
    }

    /**
     * 验证验证码
     */
    public boolean verifyCode(String email, String code, String type) {
        String key = VERIFICATION_CODE_PREFIX + type + ":" + email;
        String storedCode = stringRedisTemplate.opsForValue().get(key);

        if (storedCode == null) {
            log.warn("验证码不存在或已过期，邮箱: {}, 类型: {}", email, type);
            return false;
        }

        boolean isValid = storedCode.equals(code);
        if (isValid) {
            // 验证成功后删除验证码
            stringRedisTemplate.delete(key);
            log.info("验证码验证成功，邮箱: {}, 类型: {}", email, type);
        } else {
            log.warn("验证码验证失败，邮箱: {}, 类型: {}", email, type);
        }

        return isValid;
    }

    /**
     * 删除验证码
     */
    public void deleteCode(String email, String type) {
        String key = VERIFICATION_CODE_PREFIX + type + ":" + email;
        stringRedisTemplate.delete(key);
        log.info("验证码已删除，邮箱: {}, 类型: {}", email, type);
    }
}

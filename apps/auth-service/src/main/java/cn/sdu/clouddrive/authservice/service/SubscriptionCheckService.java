package cn.sdu.clouddrive.authservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class SubscriptionCheckService {

    @Autowired
    private RestTemplate restTemplate;

    private static final String MEMBERSHIP_SERVICE_URL = "http://membership-service";

    /**
     * 检查用户是否有有效订阅，如果没有则创建默认免费订阅
     */
    public void ensureUserHasValidSubscription(String userId) {
        try {
            // 检查用户当前订阅状态
            String currentSubscriptionUrl = MEMBERSHIP_SERVICE_URL + "/subscription/user/" + userId + "/current";
            
            log.info("检查用户 {} 的订阅状态", userId);
            
            ResponseEntity<String> response = restTemplate.getForEntity(currentSubscriptionUrl, String.class);
            
            // 如果返回错误或没有有效订阅，创建默认订阅
            if (!response.getStatusCode().is2xxSuccessful() || 
                response.getBody() == null || 
                response.getBody().contains("用户暂无有效订阅")) {
                
                log.info("用户 {} 没有有效订阅，创建默认免费订阅", userId);
                createDefaultSubscription(userId);
            } else {
                log.info("用户 {} 已有有效订阅", userId);
            }
            
        } catch (Exception e) {
            log.warn("检查用户 {} 订阅状态失败: {}, 尝试创建默认订阅", userId, e.getMessage());
            // 如果检查失败，也尝试创建默认订阅
            createDefaultSubscription(userId);
        }
    }

    /**
     * 为用户创建默认免费订阅
     */
    private void createDefaultSubscription(String userId) {
        try {
            String createSubscriptionUrl = MEMBERSHIP_SERVICE_URL + "/subscription/user/" + userId + "/default";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            
            HttpEntity<String> request = new HttpEntity<>("", headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                createSubscriptionUrl, 
                HttpMethod.POST, 
                request, 
                String.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("成功为用户 {} 创建默认订阅", userId);
            } else {
                log.error("为用户 {} 创建默认订阅失败: {}", userId, response.getBody());
            }
            
        } catch (Exception e) {
            log.error("为用户 {} 创建默认订阅时发生异常: {}", userId, e.getMessage(), e);
        }
    }
}
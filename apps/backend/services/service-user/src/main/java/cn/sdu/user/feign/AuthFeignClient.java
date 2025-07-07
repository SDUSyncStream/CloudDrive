package cn.sdu.user.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value="service-auth")
public interface AuthFeignClient
{
    @GetMapping("/auth/validate/{userId}")
    Boolean isAuthenticated(@PathVariable("userId") Long userId);
}

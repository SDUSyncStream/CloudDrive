package cn.sdu.user.controller;

import cn.sdu.user.feign.AuthFeignClient;
import cn.sdu.user.pojo.User;
import cn.sdu.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RequestMapping("/user")
@RestController
public class UserController
{
    @Autowired
    private UserService userService;

    @Autowired
    AuthFeignClient authFeignClient;

    @GetMapping("/info/{userId}")
    public User getUserInfoById(@PathVariable Long userId)
    {
        // This method should return user information based on the user ID.
        // For now, we will return a dummy User object.
        if(authFeignClient.isAuthenticated(userId)) {
            User user = new User();
            user.setId(userId);
            user.setUsername("testUser");
            user.setEmail("test@test.com");
            user.setProfilePictureUrl("http://example.com/profile.jpg");
            return user;
        } else {
            throw new RuntimeException("User not authenticated");
        }

    }
}

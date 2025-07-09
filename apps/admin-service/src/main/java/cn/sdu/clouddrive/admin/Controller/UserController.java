package cn.sdu.clouddrive.admin.Controller;

import cn.sdu.clouddrive.admin.Service.UserService;
import cn.sdu.clouddrive.admin.pojo.LoginInfo;
import cn.sdu.clouddrive.admin.pojo.User;
import cn.sdu.clouddrive.admin.util.ServerResult; // 假设你有这样一个统一的返回结果封装类
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*; // 引入 @RequestBody, @PostMapping 等

import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import static sun.security.util.Debug.println;

// @RestController 是 @Controller 和 @ResponseBody 的结合，用于直接返回JSON/XML数据
@RestController
@RequestMapping("/admin-api") // 此Controller下的所有方法都以 /admin-api 开头
public class UserController {

    @Autowired
    private UserService userService; // 注入 UserService

    @GetMapping("/hello") // 处理GET请求到 /admin/hello
    public String hello() {
        System.out.println("Hello from Admin Controller!");
        return "Hello from Admin Controller!";
    }

    //写一个简短的函数检测POST方法的可用性
    @PostMapping("/hello2") // 处理POST请求到 /admin/hello
    public String helloPost() {
        System.out.println("Hello from Admin Controller!");
        return "Hello from Admin Controller!";
    }
    //写一个简短的函数检测PUT方法的可用性
    @PutMapping("/hello3") // 处理POST请求到 /admin/hello
    public String helloPut() {
        System.out.println("Hello from Admin Controller!");
        return "Hello from Admin Controller!";
    }

    /**
     * 管理员登录接口
     *
     * @param requestBody 包含 username 和 password 的JSON请求体
     * @return ServerResult 封装的登录结果
     */
    @PostMapping("/login") // 处理POST请求到 /admin/login
    // @RequestBody 注解用于将请求体（通常是JSON）映射到Java对象或Map
    public ServerResult<Map<String, Object>> adminLogin(@RequestBody LoginInfo requestBody) {
        String username = requestBody.getUsername();
        String password = requestBody.getPassword(); // 注意：这里应该是接收哈希后的密码或在后端进行哈希

        // 1. 参数校验 (简易示例，实际项目中应更完善)
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            return ServerResult.fail("用户名或密码不能为空"); // 假设 ServerResult 有 fail 方法
        }

        // 2. 调用Service层进行登录验证
        // 注意：这里的password应该已经是经过加密/哈希处理的，与数据库中存储的密码哈希值进行比较
        User admin = userService.adminLogin(username, password);
        System.out.println("username, password");

        // 3. 根据登录结果返回响应
        if (admin != null) {
            // 登录成功
            Map<String, Object> data = new HashMap<>();
            // 实际项目中不应该返回密码哈希，这里只返回必要的用户信息
            data.put("userId", admin.getId());   //string
            data.put("username", admin.getUsername());
            data.put("userLevel", admin.getUserlevel());
            data.put("email", admin.getEmail());
            data.put("avatar", admin.getAvatar());
            data.put("storageQuota", admin.getStorageQuota()); //big int
            data.put("storageUsed", admin.getStorageUsed()); //big int



            return ServerResult.ok(data, "登录成功"); // 假设 ServerResult 有 ok 方法
        } else {
            // 登录失败
            return ServerResult.fail("用户名或密码不正确");
        }
    }
    //得到所有用户
    @GetMapping("/getAllUsers")
    public ServerResult<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ServerResult.ok(users, "获取所有用户成功");
    }
    //根据id删除用户
    @DeleteMapping("/deleteUser/{id}")
    public ServerResult<String> deleteUserById(@PathVariable String id) {
        int result = userService.deleteUserById(id);
        if (result > 0) {
            return ServerResult.ok("删除用户成功");
        } else {
            return ServerResult.fail("删除用户失败");
        }
    }
    //插入用户
    @PostMapping("/insertUser")
    public ServerResult<String> insertUser(@RequestBody User user) {
        //生成UUID
        String userId = java.util.UUID.randomUUID().toString();
        user.setId(userId);
        int result = userService.insertUser(user);
        if (result > 0) {
            return ServerResult.ok("插入用户成功");
        } else {
            return ServerResult.fail("插入用户失败");
        }
    }
    //根据id更新用户
    @PutMapping("/updateUser")
    public ServerResult<String> updateUserById(@RequestBody User user) {
        int result = userService.updateUserById(user);
        if (result > 0) {
            return ServerResult.ok("更新用户成功");
        } else {
            return ServerResult.fail("更新用户失败");
        }
    }
}
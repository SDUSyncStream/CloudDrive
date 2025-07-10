package cn.sdu.clouddrive.admin.Controller;

import cn.sdu.clouddrive.admin.Service.UserService;
import cn.sdu.clouddrive.admin.pojo.LoginInfo;
import cn.sdu.clouddrive.admin.pojo.User;
import cn.sdu.clouddrive.admin.util.ServerResult; // 假设你有这样一个统一的返回结果封装类
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*; // 引入 @RequestBody, @PostMapping 等

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

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
     * 将字节数组转换为十六进制字符串
     * @param bytes 要转换的字节数组
     * @return 十六进制字符串
     */
    private String bytesToHex(byte[] bytes) {
        Formatter formatter = new Formatter();
        for (byte b : bytes) {
            formatter.format("%02x", b); // %02x 表示不足两位时前面补0
        }
        return formatter.toString();
    }

    /**
     * 管理员登录接口
     * 使用 SHA-256 哈希用户输入的密码
     *
     * @param requestBody 包含 username 和 password 的JSON请求体
     * @return ServerResult 封装的登录结果
     */
    @PostMapping("/login")
    public ServerResult<Map<String, Object>> adminLogin(@RequestBody LoginInfo requestBody) {
        String username = requestBody.getUsername();
        String plainPassword = requestBody.getPassword(); // 获取用户输入的明文密码

        // 1. 参数校验
        if (username == null || username.isEmpty() || plainPassword == null || plainPassword.isEmpty()) {
            return ServerResult.fail("用户名或密码不能为空");
        }

        // 2. 将用户输入的明文密码进行 SHA-256 哈希
        String hashedInputPassword;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256"); // 改为 SHA-256
            byte[] hashBytes = md.digest(plainPassword.getBytes("UTF-8"));
            hashedInputPassword = bytesToHex(hashBytes); // 使用自定义方法转换为十六进制
        } catch (NoSuchAlgorithmException e) {
            System.err.println("SHA-256 算法不支持: " + e.getMessage());
            return ServerResult.fail("服务器内部错误，无法处理密码哈希。");
        } catch (java.io.UnsupportedEncodingException e) {
            System.err.println("UTF-8 编码不支持: " + e.getMessage());
            return ServerResult.fail("服务器内部错误，无法处理密码编码。");
        }
        System.out.println("Hashed password: " + hashedInputPassword);

        // 3. 调用Service层进行登录验证，Service层此时应该接收哈希后的密码进行比对
        User admin = userService.adminLogin(username, hashedInputPassword); // 将哈希后的密码传给Service

        // 4. 根据登录结果返回响应
        if (admin != null) {
            // 登录成功
            Map<String, Object> data = new HashMap<>();
            data.put("userId", admin.getId());
            data.put("username", admin.getUsername());
            data.put("userLevel", admin.getUserlevel());
            data.put("email", admin.getEmail());
            data.put("avatar", admin.getAvatar());
            data.put("storageQuota", admin.getStorageQuota());
            data.put("storageUsed", admin.getStorageUsed());

            return ServerResult.ok(data, "登录成功");
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
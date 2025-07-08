package cn.sdu.fileupdownservice.Entity;

import lombok.Data;

import java.time.LocalDateTime;
@Data
public class User {
    private String userId;
    private String nickName;
    private String email;
    private String password;
    private LocalDateTime joinTime;
    private LocalDateTime lastLoginTime;
    private Integer status;
    private Long useSpace;
    private Long totalSpace;
}

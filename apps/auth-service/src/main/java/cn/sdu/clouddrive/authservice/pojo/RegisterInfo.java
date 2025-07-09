package cn.sdu.clouddrive.authservice.pojo;

import lombok.Data;

@Data
public class RegisterInfo {
    private String userId;
    private String username;
    private String passwordHash;
    private String email;
    private String avatar;
}

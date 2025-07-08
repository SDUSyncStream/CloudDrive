package cn.sdu.clouddrive.authservice.pojo;

import lombok.Data;

@Data
public class RegisterInfo {
    private String username;
    private String password;
    private String email;
}

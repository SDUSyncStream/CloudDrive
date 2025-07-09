package cn.sdu.clouddrive.userservice.pojo;

import lombok.Data;

@Data
public class NewPwd
{
    private String userId;
    private String oldPassword;
    private String newPassword;
}

package cn.sdu.clouddrive.authservice.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
public class LoginInfo
{
    private String username;
    private String passwordHash;
}

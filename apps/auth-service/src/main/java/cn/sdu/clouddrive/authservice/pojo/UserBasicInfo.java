package cn.sdu.clouddrive.authservice.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("user_info")
public class UserBasicInfo
{
    private Long userId;
    private String nickName;

}


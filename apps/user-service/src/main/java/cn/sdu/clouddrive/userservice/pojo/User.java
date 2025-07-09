package cn.sdu.clouddrive.userservice.pojo;

import lombok.Data;

import java.math.BigInteger;
import java.sql.Date;
import java.sql.Time;

@Data
public class User
{
    private String username;
    private String email;
    private String avatar;
    private BigInteger storageQuota;
    private BigInteger storageUsed;
    private Date createdAt;
}

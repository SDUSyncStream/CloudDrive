package cn.sdu.clouddrive.userservice.pojo;

import lombok.Data;

import java.math.BigInteger;
import java.sql.Date;
import java.sql.Time;

@Data
public class User
{
    private String username;

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getAvatar()
    {
        return avatar;
    }

    public void setAvatar(String avatar)
    {
        this.avatar = avatar;
    }

    public BigInteger getStorageQuota()
    {
        return storageQuota;
    }

    public void setStorageQuota(BigInteger storageQuota)
    {
        this.storageQuota = storageQuota;
    }

    public BigInteger getStorageUsed()
    {
        return storageUsed;
    }

    public void setStorageUsed(BigInteger storageUsed)
    {
        this.storageUsed = storageUsed;
    }

    public Date getCreatedAt()
    {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt)
    {
        this.createdAt = createdAt;
    }

    public String getMembershipLevelId()
    {
        return membershipLevelId;
    }

    public void setMembershipLevelId(String membershipLevelId)
    {
        this.membershipLevelId = membershipLevelId;
    }

    private String email;
    private String avatar;
    private BigInteger storageQuota;
    private BigInteger storageUsed;
    private Date createdAt;
    private String membershipLevelId;
}

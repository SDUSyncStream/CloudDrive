package cn.sdu.fileupdownservice.entity.po;


import java.util.Date;

import cn.sdu.fileupdownservice.entity.enums.DateTimePatternEnum;
import cn.sdu.fileupdownservice.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * 用户信息
 */

public class Users implements Serializable {


    /**
     * 用户ID
     */
    private String id;

    /**
     * 昵称
     */
    private String username;

    /**
     * 邮箱
     */
    private String email;

    /**
     * qq 头像
     */
    private String qqAvatar;

    /**
     * qq openID
     */
    private String qqOpenId;

    /**
     * 密码
     */
    private String password;

    /**
     * 加入时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date joinTime;

    /**
     * 最后登录时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastLoginTime;

    /**
     * 0:禁用 1:正常
     */
    private Integer status;

    /**
     * 使用空间单位byte
     */
    private Long storage_used;

    /**
     * 总空间单位byte
     */
    private Long storage_quota;


    public void setUserId(String userId) {
        this.id = userId;
    }

    public String getUserId() {
        return this.id;
    }

    public void setNickName(String nickName) {
        this.username = nickName;
    }

    public String getNickName() {
        return this.username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return this.email;
    }

    public void setQqAvatar(String qqAvatar) {
        this.qqAvatar = qqAvatar;
    }

    public String getQqAvatar() {
        return this.qqAvatar;
    }

    public void setQqOpenId(String qqOpenId) {
        this.qqOpenId = qqOpenId;
    }

    public String getQqOpenId() {
        return this.qqOpenId;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return this.password;
    }

    public void setJoinTime(Date joinTime) {
        this.joinTime = joinTime;
    }

    public Date getJoinTime() {
        return this.joinTime;
    }

    public void setLastLoginTime(Date lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public Date getLastLoginTime() {
        return this.lastLoginTime;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getStatus() {
        return this.status;
    }

    public void setUseSpace(Long useSpace) {
        this.storage_used = useSpace;
    }

    public Long getUseSpace() {
        return this.storage_used;
    }

    public void setTotalSpace(Long totalSpace) {
        this.storage_quota = totalSpace;
    }

    public Long getTotalSpace() {
        return this.storage_quota;
    }

    @Override
    public String toString() {
        return "用户ID:" + (id == null ? "空" : id) + "，昵称:" + (username == null ? "空" : username) + "，邮箱:" + (email == null ? "空" : email) + "，qqAvatar:" + (qqAvatar == null ? "空" : qqAvatar) + "，qqOpenId:" + (qqOpenId == null ? "空" : qqOpenId) + "，密码:" + (password == null ? "空" : password) + "，加入时间:" + (joinTime == null ? "空" : DateUtil.format(joinTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern())) + "，最后登录时间:" + (lastLoginTime == null ? "空" : DateUtil.format(lastLoginTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern())) + "，0:禁用 1:正常:" + (status == null ? "空" : status) + "，useSpace:" + (storage_used == null ? "空" : storage_used) + "，totalSpace:" + (storage_quota == null ? "空" : storage_quota);
    }
}

package cn.sdu.fileupdownservice.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SysSettingsDto implements Serializable {
    /**
     * 注册发送邮件标题
     */
    private String registerEmailTitle = "邮箱验证码";

    /**
     * 注册发送邮件内容
     */
    private String registerEmailContent = "你好，您的邮箱验证码是：%s，15分钟有效";

    /**
     * 用户初始化空间大小
     */
    private Integer userInitUseSpace = 1024;

    public String getRegisterEmailTitle() {
        return registerEmailTitle;
    }

    public void setRegisterEmailTitle(String registerEmailTitle) {
        this.registerEmailTitle = registerEmailTitle;
    }

    public String getRegisterEmailContent() {
        return registerEmailContent;
    }

    public void setRegisterEmailContent(String registerEmailContent) {
        this.registerEmailContent = registerEmailContent;
    }

    public Integer getUserInitUseSpace() {
        return this.userInitUseSpace;
    }

    public void setUserInitUseSpace(Integer userInitUseSpace) {
        this.userInitUseSpace = userInitUseSpace;
    }
}

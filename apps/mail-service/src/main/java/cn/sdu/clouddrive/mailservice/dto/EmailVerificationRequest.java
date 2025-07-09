package cn.sdu.clouddrive.mailservice.dto;

import lombok.Data;

@Data
public class EmailVerificationRequest {
    private String email;
    private String type; // "FORGOT_PASSWORD", "REGISTER", etc.
    private String userId;

    public EmailVerificationRequest() {}

    public EmailVerificationRequest(String email, String type) {
        this.email = email;
        this.type = type;
    }

    public EmailVerificationRequest(String email, String type, String userId) {
        this.email = email;
        this.type = type;
        this.userId = userId;
    }
}

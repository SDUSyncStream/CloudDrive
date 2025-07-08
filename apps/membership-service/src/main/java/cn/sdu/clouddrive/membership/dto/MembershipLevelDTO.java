package cn.sdu.clouddrive.membership.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class MembershipLevelDTO {
    private String id;
    private String name;
    private Long storageQuota;
    private Long maxFileSize;
    private BigDecimal price;
    private Integer durationDays;
    private String features;
    private String storageQuotaFormatted;
    private String maxFileSizeFormatted;
    private Boolean isRecommended;
}
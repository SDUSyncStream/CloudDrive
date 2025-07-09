package cn.sdu.clouddrive.admin.pojo;

import lombok.Data;

@Data
public class SummaryInfo {
    private int userCount;
    private long totalStorage;
    private long usedStorage;
}

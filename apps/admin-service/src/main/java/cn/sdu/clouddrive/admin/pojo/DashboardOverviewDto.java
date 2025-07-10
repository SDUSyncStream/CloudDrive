package cn.sdu.clouddrive.admin.pojo;

import lombok.Data;

@Data
public class DashboardOverviewDto {
    private Long totalUsers; // 总用户数
    private Long totalStorageUsed; // 总存储已用 (字节)
    private Long totalStorageQuota; // 总存储配额 (字节)
    private Long totalFiles; // 文件总数
    private Long newUsersLast7Days; // 近7日新增用户
}
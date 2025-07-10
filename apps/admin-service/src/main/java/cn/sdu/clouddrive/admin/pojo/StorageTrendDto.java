package cn.sdu.clouddrive.admin.pojo;

import lombok.Data;

@Data
public class StorageTrendDto {
    private String date; // 日期，例如 "2023-07-04"
    private Long storageUsed; // 存储用量 (字节)
}
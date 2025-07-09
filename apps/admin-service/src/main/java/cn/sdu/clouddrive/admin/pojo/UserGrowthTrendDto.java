package cn.sdu.clouddrive.admin.pojo;

import lombok.Data;

@Data
public class UserGrowthTrendDto {
    private String date; // 日期，例如 "2023-07-04"
    private Long userCount; // 用户数
}
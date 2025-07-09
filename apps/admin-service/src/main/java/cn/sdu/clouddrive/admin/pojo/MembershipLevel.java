package cn.sdu.clouddrive.admin.pojo;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 会员等级实体类
 * 对应数据库表 `membership_levels`
 */
@Data // Lombok 注解，自动生成 getter, setter, toString, equals, hashCode 方法
@TableName("membership_levels") // 指定实体类对应的数据库表名
public class MembershipLevel {

    @TableId(value = "id") // 主键映射，value指定字段名
    private String id; // 数据库 id 是 varchar(36)

    @TableField("name") // 映射数据库 name 字段
    private String name; // 数据库是 varchar(50)

    @TableField("storage_quota") // 映射数据库 storage_quota 字段
    private Long storageQuota; // 数据库是 bigint，对应Java的 Long

    @TableField("max_file_size") // 映射数据库 max_file_size 字段
    private Long maxFileSize; // 数据库是 bigint，对应Java的 Long，Java命名规范将下划线转为驼峰

    @TableField("price") // 映射数据库 price 字段
    private BigDecimal price; // 数据库是 decimal(10,2)，对应Java的 BigDecimal，用于精确的货币计算

    @TableField("duration_days") // 映射数据库 duration_days 字段
    private Integer durationDays; // 数据库是 int，对应Java的 Integer

    @TableField("features") // 映射数据库 features 字段
    private String features; // 数据库是 text

    @TableField("created_at") // 映射数据库 created_at 字段
    private LocalDateTime createdAt; // 数据库是 timestamp，对应Java 8的 LocalDateTime

    @TableField("updated_at") // 映射数据库 updated_at 字段
    private LocalDateTime updatedAt; // 数据库是 timestamp，对应Java 8的 LocalDateTime
}
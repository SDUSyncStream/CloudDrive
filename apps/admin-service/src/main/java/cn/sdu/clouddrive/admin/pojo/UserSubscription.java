package cn.sdu.clouddrive.admin.pojo;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.EnumValue; // 用于枚举类型映射
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户订阅实体类
 * 对应数据库表 `user_subscriptions`
 */
@Data // Lombok 注解，自动生成 getter, setter, toString, equals, hashCode 方法
@TableName("user_subscriptions") // 指定实体类对应的数据库表名
public class UserSubscription {

    @TableId(value = "id") // 主键映射，value指定字段名
    private String id; // 数据库 id 是 VARCHAR(36)

    @TableField("user_id") // 映射数据库 user_id 字段
    private String userId; // 数据库 user_id 是 VARCHAR(36)

    @TableField("membership_level_id") // 映射数据库 membership_level_id 字段
    private String membershipLevelId; // 数据库 membership_level_id 是 VARCHAR(36)

    @TableField("start_date") // 映射数据库 start_date 字段
    private LocalDateTime startDate; // 数据库 TIMESTAMP，对应Java 8的 LocalDateTime

    @TableField("end_date") // 映射数据库 end_date 字段
    private LocalDateTime endDate; // 数据库 TIMESTAMP，对应Java 8的 LocalDateTime

    @TableField("status") // 映射数据库 status 字段
    private SubscriptionStatus status; // 使用枚举类型映射 ENUM('active', 'expired', 'cancelled')

    @TableField("payment_method") // 映射数据库 payment_method 字段
    private String paymentMethod; // 数据库 VARCHAR(50)

    @TableField("payment_amount") // 映射数据库 payment_amount 字段
    private BigDecimal paymentAmount; // 数据库 DECIMAL(10,2)，对应Java的 BigDecimal

    @TableField("created_at") // 映射数据库 created_at 字段
    private LocalDateTime createdAt; // 数据库 TIMESTAMP，对应Java 8的 LocalDateTime

    @TableField("updated_at") // 映射数据库 updated_at 字段
    private LocalDateTime updatedAt; // 数据库 TIMESTAMP，对应Java 8的 LocalDateTime

    /**
     * 定义订阅状态的枚举
     * 使用 @EnumValue 注解确保枚举值与数据库字符串值正确映射
     */
    /**
     * 定义订阅状态的枚举
     * 使用 @EnumValue 注解确保枚举值与数据库字符串值正确映射 (MyBatis-Plus)
     * 使用 @JsonValue 注解确保枚举值与JSON字符串值正确映射 (Jackson)
     */
    public enum SubscriptionStatus {
        active("active"),
        expired("expired"),
        cancelled("cancelled");

        @EnumValue // 告诉MyBatis-Plus在存入数据库时使用这个value
        private final String value;

        SubscriptionStatus(String value) {
            this.value = value;
        }

        @JsonValue // 告诉Jackson在序列化(to JSON)和反序列化(from JSON)时使用这个value
        public String getValue() {
            return value;
        }
    }
}
package cn.sdu.clouddrive.admin.Mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map; // 用于趋势图的通用返回

@Mapper
public interface DashboardMapper {

    // 获取总用户数
    @Select("SELECT COUNT(id) FROM users")
    Long getTotalUsers();

    // 获取总存储已用
    @Select("SELECT SUM(storage_used) FROM users")
    Long getTotalStorageUsed();

    // 获取总存储配额
    @Select("SELECT SUM(storage_quota) FROM users")
    Long getTotalStorageQuota();

    // 获取文件总数（假设 file_info 表中的del_flag=2表示正常文件）
    @Select("SELECT COUNT(file_id) FROM file_info WHERE del_flag = 2")
    Long getTotalFiles();

    // 获取近7日新增用户
    // 注意：CURRENT_TIMESTAMP 是当前时间，我们需要减去7天
    @Select("SELECT COUNT(id) FROM users WHERE created_at >= #{sevenDaysAgo}")
    Long getNewUsersLast7Days(LocalDateTime sevenDaysAgo);

    // 获取存储空间使用趋势数据
    // 返回：Map<String, Object> 列表，每个Map包含 'date' (日期字符串) 和 'storageUsed' (该日期的总存储使用量)
    // 假设我们按天统计，并聚合每一天用户的 storage_used
    // 实际实现中，这可能需要更复杂的逻辑，比如统计每天活跃用户或每天存储增量。
    // 这里为了简化，我们假设是获取每天的用户总存储量快照，或者更合理的是统计每天的 storage_used 增量
    // 更准确的趋势图数据应来源于历史快照表或日志，如果直接从users表查询，它反映的是"当天该用户总用量"，而非"每天增长量"
    // 为了模拟图片效果，可以查询在特定日期时，所有用户的存储用量总和（需要额外的逻辑或历史表）
    // 或者，一个更简单的模拟方式是，查询每天 created_at 的用户在当天及之前的所有用户的 storage_used 总和。
    // 这里提供一个简化的，以 created_at 为基准的每日总存储用量。
    // 实际生产环境，趋势图数据往往需要专门的统计表或数据仓库。
    @Select("SELECT DATE_FORMAT(created_at, '%Y-%m-%d') as date, SUM(storage_used) as storageUsed " +
            "FROM users " +
            "WHERE created_at >= #{startDate} AND created_at <= #{endDate} " +
            "GROUP BY date " +
            "ORDER BY date ASC")
    List<Map<String, Object>> getStorageUsageTrend(LocalDateTime startDate, LocalDateTime endDate);


    // 获取用户增长趋势数据
    // 返回：Map<String, Object> 列表，每个Map包含 'date' (日期字符串) 和 'userCount' (该日期新增用户数)
    @Select("SELECT DATE_FORMAT(created_at, '%Y-%m-%d') as date, COUNT(id) as userCount " +
            "FROM users " +
            "WHERE created_at >= #{startDate} AND created_at <= #{endDate} " +
            "GROUP BY date " +
            "ORDER BY date ASC")
    List<Map<String, Object>> getUserGrowthTrend(LocalDateTime startDate, LocalDateTime endDate);
}
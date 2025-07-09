package cn.sdu.clouddrive.admin.Service.Impl;


import cn.sdu.clouddrive.admin.Mapper.DashboardMapper;
import cn.sdu.clouddrive.admin.Service.DashboardService;
import cn.sdu.clouddrive.admin.pojo.DashboardOverviewDto;
import cn.sdu.clouddrive.admin.pojo.StorageTrendDto;
import cn.sdu.clouddrive.admin.pojo.UserGrowthTrendDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal; // 引入 BigDecimal 类
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private DashboardMapper dashboardMapper;

    @Override
    public DashboardOverviewDto getDashboardOverview() {
        DashboardOverviewDto overview = new DashboardOverviewDto();
        overview.setTotalUsers(dashboardMapper.getTotalUsers());
        overview.setTotalStorageUsed(dashboardMapper.getTotalStorageUsed());
        overview.setTotalStorageQuota(dashboardMapper.getTotalStorageQuota());
        overview.setTotalFiles(dashboardMapper.getTotalFiles());

        // 计算近7日
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minus(7, ChronoUnit.DAYS).withHour(0).withMinute(0).withSecond(0).withNano(0);
        overview.setNewUsersLast7Days(dashboardMapper.getNewUsersLast7Days(sevenDaysAgo));

        return overview;
    }

    @Override
    public List<StorageTrendDto> getStorageUsageTrend() {
        LocalDateTime endDate = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(999999999);
        LocalDateTime startDate = endDate.minus(6, ChronoUnit.DAYS).withHour(0).withMinute(0).withSecond(0).withNano(0);

        return dashboardMapper.getStorageUsageTrend(startDate, endDate).stream()
                .map(map -> {
                    StorageTrendDto dto = new StorageTrendDto();
                    dto.setDate((String) map.get("date"));

                    // *** 关键修改：安全地将 BigDecimal 转换为 Long ***
                    Object storageUsedObj = map.get("storageUsed");
                    if (storageUsedObj instanceof BigDecimal) {
                        dto.setStorageUsed(((BigDecimal) storageUsedObj).longValue());
                    } else if (storageUsedObj instanceof Long) {
                        dto.setStorageUsed((Long) storageUsedObj);
                    } else if (storageUsedObj != null) {
                        // 也可以尝试转换为字符串再解析，或抛出异常
                        throw new ClassCastException("Unexpected type for storageUsed: " + storageUsedObj.getClass().getName());
                    } else {
                        dto.setStorageUsed(0L); // 或者根据业务需求设置默认值
                    }
                    // *** 结束关键修改 ***

                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<UserGrowthTrendDto> getUserGrowthTrend() {
        LocalDateTime endDate = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(999999999);
        LocalDateTime startDate = endDate.minus(6, ChronoUnit.DAYS).withHour(0).withMinute(0).withSecond(0).withNano(0);

        return dashboardMapper.getUserGrowthTrend(startDate, endDate).stream()
                .map(map -> {
                    UserGrowthTrendDto dto = new UserGrowthTrendDto();
                    dto.setDate((String) map.get("date"));
                    dto.setUserCount((Long) map.get("userCount")); // userCount 应该仍然是 Long，因为是 COUNT()
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
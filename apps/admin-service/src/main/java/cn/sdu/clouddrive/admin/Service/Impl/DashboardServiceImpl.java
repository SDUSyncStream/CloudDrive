package cn.sdu.clouddrive.admin.Service.Impl;

import cn.sdu.clouddrive.admin.Mapper.DashboardMapper;
import cn.sdu.clouddrive.admin.Service.DashboardService;
import cn.sdu.clouddrive.admin.pojo.DashboardOverviewDto;
import cn.sdu.clouddrive.admin.pojo.StorageTrendDto;
import cn.sdu.clouddrive.admin.pojo.UserGrowthTrendDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.CacheConfig; // <-- 引入
import org.springframework.cache.annotation.Cacheable;  // <-- 引入
import org.springframework.cache.annotation.CacheEvict;  // <-- 引入

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@CacheConfig(cacheNames = "dashboardCache") // <-- 为仪表盘相关缓存定义一个共同的名称空间
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private DashboardMapper dashboardMapper;

    @Override
    @Cacheable(key = "'overview'") // <-- 缓存概览数据，键为 "overview"
    public DashboardOverviewDto getDashboardOverview() {
        System.out.println("从数据库获取仪表盘概览数据..."); // 用于验证是否命中缓存
        DashboardOverviewDto overview = new DashboardOverviewDto();
        overview.setTotalUsers(dashboardMapper.getTotalUsers());
        overview.setTotalStorageUsed(dashboardMapper.getTotalStorageUsed());
        overview.setTotalStorageQuota(dashboardMapper.getTotalStorageQuota());
        overview.setTotalFiles(dashboardMapper.getTotalFiles());

        LocalDateTime sevenDaysAgo = LocalDateTime.now().minus(7, ChronoUnit.DAYS).withHour(0).withMinute(0).withSecond(0).withNano(0);
        overview.setNewUsersLast7Days(dashboardMapper.getNewUsersLast7Days(sevenDaysAgo));

        return overview;
    }

    @Override
    @Cacheable(key = "'storageTrend'") // <-- 缓存存储趋势数据，键为 "storageTrend"
    public List<StorageTrendDto> getStorageUsageTrend() {
        System.out.println("从数据库获取存储使用趋势数据..."); // 用于验证是否命中缓存
        LocalDateTime endDate = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(999999999);
        LocalDateTime startDate = endDate.minus(6, ChronoUnit.DAYS).withHour(0).withMinute(0).withSecond(0).withNano(0);

        return dashboardMapper.getStorageUsageTrend(startDate, endDate).stream()
                .map(map -> {
                    StorageTrendDto dto = new StorageTrendDto();
                    dto.setDate((String) map.get("date"));
                    Object storageUsedObj = map.get("storageUsed");
                    if (storageUsedObj instanceof BigDecimal) {
                        dto.setStorageUsed(((BigDecimal) storageUsedObj).longValue());
                    } else if (storageUsedObj instanceof Long) {
                        dto.setStorageUsed((Long) storageUsedObj);
                    } else if (storageUsedObj != null) {
                        throw new ClassCastException("Unexpected type for storageUsed: " + storageUsedObj.getClass().getName());
                    } else {
                        dto.setStorageUsed(0L);
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(key = "'userGrowthTrend'") // <-- 缓存用户增长趋势数据，键为 "userGrowthTrend"
    public List<UserGrowthTrendDto> getUserGrowthTrend() {
        System.out.println("从数据库获取用户增长趋势数据..."); // 用于验证是否命中缓存
        LocalDateTime endDate = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(999999999);
        LocalDateTime startDate = endDate.minus(6, ChronoUnit.DAYS).withHour(0).withMinute(0).withSecond(0).withNano(0);

        return dashboardMapper.getUserGrowthTrend(startDate, endDate).stream()
                .map(map -> {
                    UserGrowthTrendDto dto = new UserGrowthTrendDto();
                    dto.setDate((String) map.get("date"));
                    dto.setUserCount((Long) map.get("userCount"));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * 由于仪表盘数据通常是聚合数据，当底层数据（如用户表、文件表）发生变化时，
     * 需要清除仪表盘的缓存。你可以在用户管理、文件管理、订阅管理等更新操作成功后调用此方法。
     * 或者，更简单的，可以设置较短的缓存过期时间。
     *
     * 这里提供一个方法来清空所有的仪表盘缓存：
     */
    @CacheEvict(allEntries = true) // <-- 清空 "dashboardCache" 缓存空间中的所有条目
    public void evictDashboardCache() {
        System.out.println("清除仪表盘缓存...");
    }
}
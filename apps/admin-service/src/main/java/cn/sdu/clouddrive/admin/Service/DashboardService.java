package cn.sdu.clouddrive.admin.Service;



import cn.sdu.clouddrive.admin.pojo.DashboardOverviewDto;
import cn.sdu.clouddrive.admin.pojo.StorageTrendDto;
import cn.sdu.clouddrive.admin.pojo.UserGrowthTrendDto;

import java.util.List;

public interface DashboardService {
    DashboardOverviewDto getDashboardOverview();
    List<StorageTrendDto> getStorageUsageTrend();
    List<UserGrowthTrendDto> getUserGrowthTrend();
}
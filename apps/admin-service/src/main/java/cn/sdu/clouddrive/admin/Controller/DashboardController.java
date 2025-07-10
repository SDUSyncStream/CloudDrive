package cn.sdu.clouddrive.admin.Controller;

import cn.sdu.clouddrive.admin.Service.DashboardService;
import cn.sdu.clouddrive.admin.pojo.DashboardOverviewDto;
import cn.sdu.clouddrive.admin.pojo.StorageTrendDto;
import cn.sdu.clouddrive.admin.pojo.UserGrowthTrendDto;
import cn.sdu.clouddrive.admin.util.ServerResult; // 假设你有这样一个统一的返回结果封装类
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin-api/dashboard") // 仪表盘相关的API
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    /**
     * 获取控制台概览数据 (顶部卡片)
     * @return ServerResult 封装的概览数据
     */
    @GetMapping("/overview")
    public ServerResult<DashboardOverviewDto> getDashboardOverview() {
        DashboardOverviewDto overview = dashboardService.getDashboardOverview();
        return ServerResult.ok(overview, "获取概览数据成功");
    }

    /**
     * 获取存储空间使用趋势数据
     * @return ServerResult 封装的趋势数据列表
     */
    @GetMapping("/storage-trend")
    public ServerResult<List<StorageTrendDto>> getStorageUsageTrend() {
        List<StorageTrendDto> trendData = dashboardService.getStorageUsageTrend();
        return ServerResult.ok(trendData, "获取存储趋势数据成功");
    }

    /**
     * 获取用户增长趋势数据
     * @return ServerResult 封装的趋势数据列表
     */
    @GetMapping("/user-growth-trend")
    public ServerResult<List<UserGrowthTrendDto>> getUserGrowthTrend() {
        List<UserGrowthTrendDto> trendData = dashboardService.getUserGrowthTrend();
        return ServerResult.ok(trendData, "获取用户增长趋势数据成功");
    }

    /**
     * 获取所有仪表盘数据（可选：如果前端一次性请求所有数据）
     * @return ServerResult 封装的所有仪表盘数据
     */
    @GetMapping("/all-data")
    public ServerResult<Map<String, Object>> getAllDashboardData() {
        Map<String, Object> data = new HashMap<>();
        data.put("overview", dashboardService.getDashboardOverview());
        data.put("storageTrend", dashboardService.getStorageUsageTrend());
        data.put("userGrowthTrend", dashboardService.getUserGrowthTrend());
        return ServerResult.ok(data, "获取所有仪表盘数据成功");
    }
}
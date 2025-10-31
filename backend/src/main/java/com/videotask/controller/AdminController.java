package com.videotask.controller;

import com.videotask.dto.AdminDto;
import com.videotask.dto.ApiResponse;
import com.videotask.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {
    
    @Autowired
    private AdminService adminService;
    
    /**
     * 获取管理后台总览统计数据
     */
    @GetMapping("/dashboard/stats")
    public ApiResponse<AdminDto.DashboardStatsResponse> getDashboardStats(
            @RequestParam(required = false) String timeFilter,
            @RequestParam(required = false) Integer platformId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        AdminDto.StatsRequest request = new AdminDto.StatsRequest();
        request.setTimeFilter(timeFilter);
        request.setPlatformId(platformId);
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        
        return adminService.getDashboardStats(request);
    }
    
    /**
     * 获取达人排行榜
     */
    @GetMapping("/influencer/ranking")
    public ApiResponse<AdminDto.InfluencerRankingResponse> getInfluencerRanking(
            @RequestParam(defaultValue = "total") String type,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) Integer platformId) {
        
        AdminDto.RankingRequest request = new AdminDto.RankingRequest();
        request.setType(type);
        request.setPage(page);
        request.setSize(size);
        request.setPlatformId(platformId);
        
        return adminService.getInfluencerRanking(request);
    }
    
    /**
     * 导出数据
     */
    @PostMapping("/export")
    public ApiResponse<String> exportData(@RequestBody AdminDto.ExportRequest request) {
        return adminService.exportData(request);
    }
}


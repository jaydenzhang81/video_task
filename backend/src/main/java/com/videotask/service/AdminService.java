package com.videotask.service;

import com.videotask.dto.AdminDto;
import com.videotask.dto.ApiResponse;

public interface AdminService {
    
    /**
     * 获取管理后台总览统计数据
     */
    ApiResponse<AdminDto.DashboardStatsResponse> getDashboardStats(AdminDto.StatsRequest request);
    
    /**
     * 获取达人排行榜
     */
    ApiResponse<AdminDto.InfluencerRankingResponse> getInfluencerRanking(AdminDto.RankingRequest request);
    
    /**
     * 导出数据
     */
    ApiResponse<String> exportData(AdminDto.ExportRequest request);
}


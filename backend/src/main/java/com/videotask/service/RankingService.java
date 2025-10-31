package com.videotask.service;

import com.videotask.dto.ApiResponse;

public interface RankingService {
    
    /**
     * 获取排行榜列表
     */
    ApiResponse<Object> getRankingList(Integer userId, String type, Integer platformId, Integer page, Integer size);
    
    /**
     * 获取我的排名
     */
    ApiResponse<Object> getMyRank(Integer userId, String type, Integer platformId);
    
    /**
     * 获取排行榜统计信息
     */
    ApiResponse<Object> getRankingStats(Integer userId, String type, Integer platformId);
}

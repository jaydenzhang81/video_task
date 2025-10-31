package com.videotask.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class AdminDto {
    
    // 管理后台总览统计
    @Data
    public static class DashboardStatsResponse {
        private AccountStats accountStats;
        private WithdrawStats withdrawStats;
        private PlayStats playStats;
        private PublishStats publishStats;
        private CommentStats commentStats;
        private RevenueStats revenueStats;
        private CollectionStats collectionStats;
    }
    
    // 账号统计
    @Data
    public static class AccountStats {
        private Integer total;
        private Integer online;
        private Integer expired;
        private List<ChartData> chartData;
    }
    
    // 提现统计
    @Data
    public static class WithdrawStats {
        private BigDecimal withdrawn;
        private BigDecimal unwithdrawn;
        private Double monthlyChange;
        private List<ChartData> chartData;
    }
    
    // 播放量统计
    @Data
    public static class PlayStats {
        private Long totalPlays;
        private Double monthlyChange;
        private List<ChartData> chartData;
    }
    
    // 发布统计
    @Data
    public static class PublishStats {
        private Integer successCount;
        private Integer failedCount;
        private Integer totalCount;
        private List<ChartData> chartData;
    }
    
    // 评论统计
    @Data
    public static class CommentStats {
        private Long totalComments;
        private Double monthlyChange;
        private List<ChartData> chartData;
    }
    
    // 收益统计
    @Data
    public static class RevenueStats {
        private BigDecimal totalRevenue;
        private Double monthlyChange;
        private List<ChartData> chartData;
    }
    
    // 收藏统计
    @Data
    public static class CollectionStats {
        private Long totalCollections;
        private Double monthlyChange;
        private List<ChartData> chartData;
    }
    
    // 图表数据
    @Data
    public static class ChartData {
        private String date;
        private Object value;
    }
    
    // 达人排行榜
    @Data
    public static class InfluencerRankingResponse {
        private List<InfluencerRankingItem> rankings;
        private Integer total;
        private String rankingType;
    }
    
    // 达人排行榜项目
    @Data
    public static class InfluencerRankingItem {
        private Integer rank;
        private String influencerName;
        private String avatar;
        private String team;
        private BigDecimal rewardAmount;
        private Integer videosPosted;
        private Integer totalViews;
        private Integer totalLikes;
        private Integer totalComments;
        private String phone;
        private String wechatNickname;
    }
    
    // 统计请求参数
    @Data
    public static class StatsRequest {
        private String timeFilter; // today, week, month, year
        private Integer platformId; // 平台ID，可选
        private String startDate;
        private String endDate;
    }
    
    // 排行榜请求参数
    @Data
    public static class RankingRequest {
        private String type; // total, monthly, weekly
        private Integer page = 1;
        private Integer size = 20;
        private Integer platformId;
    }
    
    // 数据导出请求
    @Data
    public static class ExportRequest {
        private String type; // ranking, stats
        private String timeFilter;
        private Integer platformId;
        private String startDate;
        private String endDate;
    }
}


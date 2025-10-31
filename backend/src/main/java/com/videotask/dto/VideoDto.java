package com.videotask.dto;

import lombok.Data;

@Data
public class VideoDto {
    
    @Data
    public static class VideoInfo {
        private Integer id;
        private String title;
        private String videoDesc;
        private String cover;
        private String url;
        private Integer videoType;
        private Integer status;
        private String hashtags; // 话题字符串，用逗号分割
        private Long ctime;
        private Long utime;
        private Integer likeCount;
        private Integer commentCount;
        private Integer viewCount;
        private Boolean isShared;
        private Boolean isPending;
        private Integer dataStatus; // 0异常，1正常
        private java.math.BigDecimal reward;
    }
    
    @Data
    public static class VideoTypeInfo {
        private Integer id;
        private String name;
    }
    
    @Data
    public static class VideoListRequest {
        private Integer platformId;
        private Integer num = 2;
        private Integer videoType; // 视频分类ID，选填
    }
    
    @Data
    public static class VideoShareRequest {
        private Integer platformId;
        private Integer videoId;
    }
    
    // 视频数据统计相关
    @Data
    public static class VideoStatsRequest {
        private String timeFilter;
        private Integer platformId;
    }
    
    @Data
    public static class VideoStatsResponse {
        private Integer totalVideos;
        private Integer todayViews;
        private Integer totalLikes;
        private Integer totalComments;
        private VideoStatsData playCount;
        private VideoStatsData likeCount;
        private VideoStatsData commentCount;
        private VideoStatsData publishCount;
        private VideoStatsData collectCount;
        private VideoStatsData rewardAmount;
    }
    
    @Data
    public static class VideoStatsData {
        private Integer value;
        private Double trend;
    }
    
    @Data
    public static class VideoPublishListRequest {
        private Integer page = 1;
        private Integer size = 10;
        private String timeFilter;
        private Integer platformId;
    }
    
    @Data
    public static class VideoPublishListResponse {
        private java.util.List<VideoPublishInfo> records;
        private Integer total;
        private Integer current;
        private Integer size;
    }
    
    @Data
    public static class VideoPublishInfo {
        private Integer id;
        private String title;
        private String author;
        private String cover;
        private String duration;
        private String viewCount;
        private String likeCount;
        private String commentCount;
        private String favoriteCount;
        private Integer status;
        private String hashtags; // 话题字符串，用逗号分割
        private Long ctime;
        private Integer dataStatus;
        private Integer videoId;
        private Integer platformId; // 添加平台ID字段
    }
    
    @Data
    public static class VideoChartDataRequest {
        private String timeFilter;
        private Integer platformId;
        private String type;
    }
    
    @Data
    public static class VideoChartDataResponse {
        private java.util.List<VideoChartData> data;
    }
    
    @Data
    public static class VideoChartData {
        private String date;
        private Integer value;
    }
    
    @Data
    public static class VideoShareStatsResponse {
        private Integer totalShares;
        private Integer totalViews;
        private Integer totalLikes;
        private Integer totalComments;
        private java.math.BigDecimal totalReward;
        private ShareStatsData playCount;
        private ShareStatsData likeCount;
        private ShareStatsData commentCount;
        private ShareStatsData shareCount;
        private ShareStatsData rewardAmount;
    }
    
    @Data
    public static class ShareStatsData {
        private Integer value;
        private Double trend;
    }
    
    // 检查发布限制请求
    @Data
    public static class CheckPublishLimitRequest {
        private Integer platformId;
    }
    
    // 检查发布限制响应
    @Data
    public static class CheckPublishLimitResponse {
        private boolean canPublish; // 是否可以发布
        private Integer publishNum; // 允许发布的总数量
        private Integer currentNum; // 当前已发布数量
        private Integer remainingNum; // 剩余可发布数量
        
        // 手动定义getter方法，避免Lombok生成的方法名称问题
        public boolean getCanPublish() {
            return canPublish;
        }
        
        public void setCanPublish(boolean canPublish) {
            this.canPublish = canPublish;
        }
    }
}
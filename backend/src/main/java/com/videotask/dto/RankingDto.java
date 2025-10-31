package com.videotask.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class RankingDto {

    @Data
    public static class RankingItem {
        private Integer rank;           // 排名
        private Integer userId;         // 用户ID
        private String nick;            // 昵称
        private String avatar;          // 头像
        private Integer videoCount;     // 发布视频数
        private BigDecimal rewardAmount; // 奖励金额
        private Integer totalLikes;     // 总点赞数
        private Integer totalComments;  // 总评论数
        private Integer totalViews;     // 总播放量
        private Integer rankChange;     // 排名变化 (正数上升，负数下降，0无变化)
        private String platformName;    // 主要平台名称
    }

    @Data
    public static class RankingListResponse {
        private List<RankingItem> records;  // 排行榜列表
        private Long total;                 // 总记录数
        private Integer current;            // 当前页
        private Integer size;               // 每页大小
        private String type;                // 排行榜类型
        private RankingStats stats;         // 统计信息
    }

    @Data
    public static class MyRankResponse {
        private Integer rank;           // 我的排名
        private Integer totalUsers;     // 总参与人数
        private BigDecimal myReward;    // 我的奖励
        private Integer myVideoCount;   // 我的视频数
        private Integer rankChange;     // 排名变化
        private String type;            // 排行榜类型
    }

    @Data
    public static class RankingStats {
        private Integer totalUsers;         // 总参与人数
        private BigDecimal totalReward;     // 总奖励金额
        private Integer totalVideos;        // 总视频数
        private BigDecimal avgReward;       // 平均奖励
        private Integer avgVideos;          // 平均视频数
    }
}

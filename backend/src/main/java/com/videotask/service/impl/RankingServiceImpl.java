package com.videotask.service.impl;

import cn.hutool.core.date.DateUtil;
import com.videotask.dto.ApiResponse;
import com.videotask.dto.RankingDto;
import com.videotask.entity.User;
import com.videotask.entity.VideoShareUserLog;
import com.videotask.repository.UserAccountRepository;
import com.videotask.repository.UserRepository;
import com.videotask.repository.VideoShareUserLogRepository;
import com.videotask.service.RankingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RankingServiceImpl implements RankingService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private VideoShareUserLogRepository videoShareUserLogRepository;

    @Override
    public ApiResponse<Object> getRankingList(Integer userId, String type, Integer platformId, Integer page, Integer size) {
        try {
            // 计算时间范围
            long startTime = getStartTimeByType(type);
            long endTime = DateUtil.currentSeconds();

            // 获取用户分享数据
            List<VideoShareUserLog> shareLogs = getShareLogsByTimeRange(startTime, endTime, platformId);

            // 按用户分组统计
            Map<Integer, UserStats> userStatsMap = calculateUserStats(shareLogs);

            // 转换为排行榜项目
            List<RankingDto.RankingItem> rankingItems = convertToRankingItems(userStatsMap);

            // 排序（按奖励金额降序，金额相同时按视频数量降序）
            rankingItems.sort((a, b) -> {
                int rewardCompare = b.getRewardAmount().compareTo(a.getRewardAmount());
                if (rewardCompare != 0) {
                    return rewardCompare;
                }
                // 奖励金额相同时，按视频数量降序排序
                return Integer.compare(b.getVideoCount(), a.getVideoCount());
            });

            // 设置排名
            for (int i = 0; i < rankingItems.size(); i++) {
                rankingItems.get(i).setRank(i + 1);
            }

            // 只取前20名
            List<RankingDto.RankingItem> top20Items = rankingItems.subList(0, Math.min(20, rankingItems.size()));

            // 构建响应
            RankingDto.RankingListResponse response = new RankingDto.RankingListResponse();
            response.setRecords(top20Items);
            response.setTotal((long) rankingItems.size());
            response.setCurrent(1);
            response.setSize(20);
            response.setType(type);

            // 添加统计信息
            RankingDto.RankingStats stats = calculateRankingStats(rankingItems);
            response.setStats(stats);

            return ApiResponse.success(response);

        } catch (Exception e) {
            return ApiResponse.error("获取排行榜失败: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<Object> getMyRank(Integer userId, String type, Integer platformId) {
        try {
            // 计算时间范围
            long startTime = getStartTimeByType(type);
            long endTime = DateUtil.currentSeconds();

            // 获取用户分享数据
            List<VideoShareUserLog> shareLogs = getShareLogsByTimeRange(startTime, endTime, platformId);

            // 按用户分组统计
            Map<Integer, UserStats> userStatsMap = calculateUserStats(shareLogs);

            // 转换为排行榜项目
            List<RankingDto.RankingItem> rankingItems = convertToRankingItems(userStatsMap);

            // 排序（按奖励金额降序，金额相同时按视频数量降序）
            rankingItems.sort((a, b) -> {
                int rewardCompare = b.getRewardAmount().compareTo(a.getRewardAmount());
                if (rewardCompare != 0) {
                    return rewardCompare;
                }
                // 奖励金额相同时，按视频数量降序排序
                return Integer.compare(b.getVideoCount(), a.getVideoCount());
            });

            // 设置排名
            for (int i = 0; i < rankingItems.size(); i++) {
                rankingItems.get(i).setRank(i + 1);
            }

            // 查找我的排名
            RankingDto.RankingItem myRank = rankingItems.stream()
                    .filter(item -> item.getUserId().equals(userId))
                    .findFirst()
                    .orElse(null);

            if (myRank == null) {
                return ApiResponse.error("未找到您的排名信息");
            }

            // 构建响应
            RankingDto.MyRankResponse response = new RankingDto.MyRankResponse();
            response.setRank(myRank.getRank());
            response.setTotalUsers(rankingItems.size());
            response.setMyReward(myRank.getRewardAmount());
            response.setMyVideoCount(myRank.getVideoCount());
            response.setRankChange(myRank.getRankChange());
            response.setType(type);

            return ApiResponse.success(response);

        } catch (Exception e) {
            return ApiResponse.error("获取我的排名失败: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<Object> getRankingStats(Integer userId, String type, Integer platformId) {
        try {
            // 计算时间范围
            long startTime = getStartTimeByType(type);
            long endTime = DateUtil.currentSeconds();

            // 获取用户分享数据
            List<VideoShareUserLog> shareLogs = getShareLogsByTimeRange(startTime, endTime, platformId);

            // 按用户分组统计
            Map<Integer, UserStats> userStatsMap = calculateUserStats(shareLogs);

            // 转换为排行榜项目
            List<RankingDto.RankingItem> rankingItems = convertToRankingItems(userStatsMap);

            // 计算统计信息
            RankingDto.RankingStats stats = calculateRankingStats(rankingItems);

            return ApiResponse.success(stats);

        } catch (Exception e) {
            return ApiResponse.error("获取排行榜统计失败: " + e.getMessage());
        }
    }

    /**
     * 根据类型获取开始时间
     */
    private long getStartTimeByType(String type) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime;

        switch (type) {
            case "weekly":
                // 本周开始
                startTime = now.minusDays(now.getDayOfWeek().getValue() - 1).withHour(0).withMinute(0).withSecond(0);
                break;
            case "monthly":
                // 本月开始
                startTime = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
                break;
            case "total":
                // 全部时间
                startTime = LocalDateTime.of(2020, 1, 1, 0, 0, 0);
                break;
            default:
                startTime = now.minusDays(now.getDayOfWeek().getValue() - 1).withHour(0).withMinute(0).withSecond(0);
        }

        return startTime.toInstant(ZoneOffset.of("+8")).toEpochMilli() / 1000L;
    }

    /**
     * 获取指定时间范围内的分享日志
     */
    private List<VideoShareUserLog> getShareLogsByTimeRange(long startTime, long endTime, Integer platformId) {
        if (platformId != null) {
            return videoShareUserLogRepository.findByCtimeBetweenAndPlatformIdAndStatusOrderByCtimeDesc(
                    startTime, endTime, platformId, 1);
        } else {
            return videoShareUserLogRepository.findByCtimeBetweenAndStatusOrderByCtimeDesc(startTime, endTime, 1);
        }
    }

    /**
     * 计算用户统计数据
     */
    private Map<Integer, UserStats> calculateUserStats(List<VideoShareUserLog> shareLogs) {
        return shareLogs.stream()
                .collect(Collectors.groupingBy(
                        VideoShareUserLog::getUserId,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                logs -> {
                                    UserStats stats = new UserStats();
                                    stats.userId = logs.get(0).getUserId();
                                    stats.videoCount = logs.size();
                                    stats.totalLikes = logs.stream().mapToInt(log -> log.getLikeCount() != null ? log.getLikeCount() : 0).sum();
                                    stats.totalComments = logs.stream().mapToInt(log -> log.getCommentCount() != null ? log.getCommentCount() : 0).sum();
                                    stats.totalViews = logs.stream().mapToInt(log -> log.getViewCount() != null ? log.getViewCount() : 0).sum();
                                    // 只计算reward_status为2的记录的奖励金额
                                    stats.rewardAmount = logs.stream()
                                            .filter(log -> log.getRewardStatus() != null && log.getRewardStatus() == 2)
                                            .map(log -> log.getReward() != null ? log.getReward() : BigDecimal.ZERO)
                                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                                    return stats;
                                }
                        )
                ));
    }

    /**
     * 转换为排行榜项目
     */
    private List<RankingDto.RankingItem> convertToRankingItems(Map<Integer, UserStats> userStatsMap) {
        List<RankingDto.RankingItem> items = new ArrayList<>();

        for (Map.Entry<Integer, UserStats> entry : userStatsMap.entrySet()) {
            UserStats stats = entry.getValue();
            User user = userRepository.findById(stats.userId).orElse(null);

            if (user != null) {
                RankingDto.RankingItem item = new RankingDto.RankingItem();
                item.setUserId(stats.userId);
                item.setNick(user.getNick());
                item.setAvatar(user.getAvatar());
                item.setVideoCount(stats.videoCount);
                item.setRewardAmount(stats.rewardAmount);
                item.setTotalLikes(stats.totalLikes);
                item.setTotalComments(stats.totalComments);
                item.setTotalViews(stats.totalViews);
                item.setRankChange(0); // TODO: 计算排名变化
                item.setPlatformName("抖音"); // TODO: 获取主要平台

                items.add(item);
            }
        }

        return items;
    }

    /**
     * 计算排行榜统计信息
     */
    private RankingDto.RankingStats calculateRankingStats(List<RankingDto.RankingItem> rankingItems) {
        RankingDto.RankingStats stats = new RankingDto.RankingStats();

        if (rankingItems.isEmpty()) {
            stats.setTotalUsers(0);
            stats.setTotalReward(BigDecimal.ZERO);
            stats.setTotalVideos(0);
            stats.setAvgReward(BigDecimal.ZERO);
            stats.setAvgVideos(0);
            return stats;
        }

        stats.setTotalUsers(rankingItems.size());
        stats.setTotalReward(rankingItems.stream()
                .map(RankingDto.RankingItem::getRewardAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        stats.setTotalVideos(rankingItems.stream()
                .mapToInt(RankingDto.RankingItem::getVideoCount)
                .sum());
        stats.setAvgReward(stats.getTotalReward().divide(BigDecimal.valueOf(stats.getTotalUsers()), 2, BigDecimal.ROUND_HALF_UP));
        stats.setAvgVideos(stats.getTotalVideos() / stats.getTotalUsers());

        return stats;
    }

    /**
     * 用户统计数据内部类
     */
    private static class UserStats {
        Integer userId;
        Integer videoCount;
        Integer totalLikes;
        Integer totalComments;
        Integer totalViews;
        BigDecimal rewardAmount;
    }
}

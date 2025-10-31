package com.videotask.service.impl;

import cn.hutool.core.date.DateUtil;
import com.videotask.dto.AdminDto;
import com.videotask.dto.ApiResponse;
import com.videotask.entity.*;
import com.videotask.repository.*;
import com.videotask.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserAuthRepository userAuthRepository;
    
    @Autowired
    private VideoShareUserLogRepository shareLogRepository;
    
    @Autowired
    private UserAccountRepository userAccountRepository;
    
    @Autowired
    private VideoRepository videoRepository;
    
    @Autowired
    private UserAccountDrawalRepository drawalRepository;
    
    @Autowired
    private TeamRepository teamRepository;
    
    @Autowired
    private UserWechatRepository userWechatRepository;
    
    @Override
    public ApiResponse<AdminDto.DashboardStatsResponse> getDashboardStats(AdminDto.StatsRequest request) {
        try {
            AdminDto.DashboardStatsResponse response = new AdminDto.DashboardStatsResponse();
            
            // 计算时间范围
            long[] timeRange = calculateTimeRange(request.getTimeFilter(), request.getStartDate(), request.getEndDate());
            long startTime = timeRange[0];
            long endTime = timeRange[1];
            
            // 获取账号统计
            response.setAccountStats(getAccountStats(request.getPlatformId()));
            
            // 获取提现统计
            response.setWithdrawStats(getWithdrawStats(startTime, endTime));
            
            // 获取播放量统计
            response.setPlayStats(getPlayStats(startTime, endTime, request.getPlatformId()));
            
            // 获取发布统计
            response.setPublishStats(getPublishStats(startTime, endTime, request.getPlatformId()));
            
            // 获取评论统计
            response.setCommentStats(getCommentStats(startTime, endTime, request.getPlatformId()));
            
            // 获取收益统计
            response.setRevenueStats(getRevenueStats(startTime, endTime));
            
            // 获取收藏统计
            response.setCollectionStats(getCollectionStats(startTime, endTime, request.getPlatformId()));
            
            return ApiResponse.success(response);
        } catch (Exception e) {
            return ApiResponse.error("获取统计数据失败: " + e.getMessage());
        }
    }
    
    @Override
    public ApiResponse<AdminDto.InfluencerRankingResponse> getInfluencerRanking(AdminDto.RankingRequest request) {
        try {
            // 计算时间范围
            long[] timeRange = calculateTimeRangeByType(request.getType());
            long startTime = timeRange[0];
            long endTime = timeRange[1];
            
            // 获取分享记录
            List<VideoShareUserLog> shareLogs;
            if (request.getPlatformId() != null) {
                shareLogs = shareLogRepository.findByCtimeBetweenAndPlatformIdAndStatusOrderByCtimeDesc(startTime, endTime, request.getPlatformId(), 1);
            } else {
                shareLogs = shareLogRepository.findByCtimeBetweenAndStatusOrderByCtimeDesc(startTime, endTime, 1);
            }
            
            // 按用户分组统计
            Map<Integer, UserStats> userStatsMap = new HashMap<>();
            for (VideoShareUserLog log : shareLogs) {
                UserStats stats = userStatsMap.computeIfAbsent(log.getUserId(), k -> new UserStats());
                stats.userId = log.getUserId();
                stats.rewardAmount = stats.rewardAmount.add(log.getReward() != null ? log.getReward() : BigDecimal.ZERO);
                stats.videosPosted += 1;
                stats.totalViews += log.getViewCount() != null ? log.getViewCount() : 0;
                stats.totalLikes += log.getLikeCount() != null ? log.getLikeCount() : 0;
                stats.totalComments += log.getCommentCount() != null ? log.getCommentCount() : 0;
            }
            
            // 转换为排行榜项目并排序
            List<AdminDto.InfluencerRankingItem> rankings = new ArrayList<>();
            int rank = 1;
            
            List<UserStats> sortedStats = userStatsMap.values().stream()
                .sorted((a, b) -> b.rewardAmount.compareTo(a.rewardAmount))
                .collect(Collectors.toList());
            
            for (UserStats stats : sortedStats) {
                Optional<User> userOpt = userRepository.findById(stats.userId);
                if (!userOpt.isPresent()) continue;
                
                User user = userOpt.get();
                AdminDto.InfluencerRankingItem item = new AdminDto.InfluencerRankingItem();
                item.setRank(rank++);
                item.setInfluencerName(user.getNick() != null ? user.getNick() : user.getPhone());
                item.setAvatar(user.getAvatar());
                item.setPhone(user.getPhone());
                userWechatRepository.findById(user.getId()).ifPresent(wechat -> item.setWechatNickname(wechat.getNickName()));
                
                // 获取团队信息
                if (user.getTeamId() != null) {
                    Optional<Team> teamOpt = teamRepository.findById(user.getTeamId());
                    if (teamOpt.isPresent()) {
                        item.setTeam(teamOpt.get().getName());
                    }
                }
                
                item.setRewardAmount(stats.rewardAmount);
                item.setVideosPosted(stats.videosPosted);
                item.setTotalViews(stats.totalViews);
                item.setTotalLikes(stats.totalLikes);
                item.setTotalComments(stats.totalComments);
                
                rankings.add(item);
            }
            
            // 分页处理
            int start = (request.getPage() - 1) * request.getSize();
            int end = Math.min(start + request.getSize(), rankings.size());
            List<AdminDto.InfluencerRankingItem> pageData = 
                start < rankings.size() ? rankings.subList(start, end) : new ArrayList<>();
            
            AdminDto.InfluencerRankingResponse response = new AdminDto.InfluencerRankingResponse();
            response.setRankings(pageData);
            response.setTotal(rankings.size());
            response.setRankingType(request.getType());
            
            return ApiResponse.success(response);
        } catch (Exception e) {
            return ApiResponse.error("获取排行榜失败: " + e.getMessage());
        }
    }
    
    @Override
    public ApiResponse<String> exportData(AdminDto.ExportRequest request) {
        try {
            // TODO: 实现数据导出功能
            return ApiResponse.success("数据导出功能暂未实现");
        } catch (Exception e) {
            return ApiResponse.error("导出数据失败: " + e.getMessage());
        }
    }
    
    // 私有辅助方法
    
    private AdminDto.AccountStats getAccountStats(Integer platformId) {
        AdminDto.AccountStats stats = new AdminDto.AccountStats();
        
        if (platformId != null) {
            // 按平台统计授权用户数
            List<UserAuth> auths = userAuthRepository.findByPlatformId(platformId);
            stats.setTotal(auths.size());
            
            // 统计在线（有效token）和过期用户数
            long online = auths.stream().filter(auth -> auth.getAccessToken() != null && !auth.getAccessToken().isEmpty()).count();
            stats.setOnline((int) online);
            stats.setExpired(auths.size() - (int) online);
        } else {
            // 统计所有用户
            long total = userRepository.count();
            stats.setTotal((int) total);
            stats.setOnline((int) (total * 0.7)); // 模拟数据
            stats.setExpired((int) (total * 0.3)); // 模拟数据
        }
        
        // 生成图表数据
        stats.setChartData(generateChartData("account", 7));
        
        return stats;
    }
    
    private AdminDto.WithdrawStats getWithdrawStats(long startTime, long endTime) {
        AdminDto.WithdrawStats stats = new AdminDto.WithdrawStats();
        
        // 获取提现统计
        List<UserAccountDrawal> drawals = drawalRepository.findByCtimeBetween(startTime, endTime);
        BigDecimal withdrawn = drawals.stream()
            .filter(d -> d.getStatus() == 1) // 已提现
            .map(UserAccountDrawal::getMoney)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 获取未提现金额
        List<UserAccount> accounts = userAccountRepository.findAll();
        BigDecimal unwithdrawn = accounts.stream()
            .map(UserAccount::getMoney)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        stats.setWithdrawn(withdrawn);
        stats.setUnwithdrawn(unwithdrawn);
        stats.setMonthlyChange(5.2); // 模拟数据
        stats.setChartData(generateChartData("withdraw", 7));
        
        return stats;
    }
    
    private AdminDto.PlayStats getPlayStats(long startTime, long endTime, Integer platformId) {
        AdminDto.PlayStats stats = new AdminDto.PlayStats();
        
        List<VideoShareUserLog> logs;
        if (platformId != null) {
            logs = shareLogRepository.findByCtimeBetweenAndPlatformIdAndStatusOrderByCtimeDesc(startTime, endTime, platformId, 1);
        } else {
            logs = shareLogRepository.findByCtimeBetweenAndStatusOrderByCtimeDesc(startTime, endTime, 1);
        }
        
        long totalPlays = logs.stream()
            .mapToLong(log -> log.getViewCount() != null ? log.getViewCount() : 0)
            .sum();
        
        stats.setTotalPlays(totalPlays);
        stats.setMonthlyChange(-2.1); // 模拟数据
        stats.setChartData(generateChartData("play", 7));
        
        return stats;
    }
    
    private AdminDto.PublishStats getPublishStats(long startTime, long endTime, Integer platformId) {
        AdminDto.PublishStats stats = new AdminDto.PublishStats();
        
        List<VideoShareUserLog> successLogs;
        List<VideoShareUserLog> failedLogs;
        if (platformId != null) {
            successLogs = shareLogRepository.findByCtimeBetweenAndPlatformIdAndStatusOrderByCtimeDesc(startTime, endTime, platformId, 1);
            failedLogs = shareLogRepository.findByCtimeBetweenAndPlatformIdAndStatusOrderByCtimeDesc(startTime, endTime, platformId, -1);
        } else {
            successLogs = shareLogRepository.findByCtimeBetweenAndStatusOrderByCtimeDesc(startTime, endTime, 1);
            failedLogs = shareLogRepository.findByCtimeBetweenAndStatusOrderByCtimeDesc(startTime, endTime, -1);
        }
        
        int successCount = successLogs.size();
        int failedCount = failedLogs.size();
        
        stats.setSuccessCount(successCount);
        stats.setFailedCount(failedCount);
        stats.setTotalCount(successCount + failedCount);
        stats.setChartData(generateChartData("publish", 7));
        
        return stats;
    }
    
    private AdminDto.CommentStats getCommentStats(long startTime, long endTime, Integer platformId) {
        AdminDto.CommentStats stats = new AdminDto.CommentStats();
        
        List<VideoShareUserLog> logs;
        if (platformId != null) {
            logs = shareLogRepository.findByCtimeBetweenAndPlatformIdAndStatusOrderByCtimeDesc(startTime, endTime, platformId, 1);
        } else {
            logs = shareLogRepository.findByCtimeBetweenAndStatusOrderByCtimeDesc(startTime, endTime, 1);
        }
        
        long totalComments = logs.stream()
            .mapToLong(log -> log.getCommentCount() != null ? log.getCommentCount() : 0)
            .sum();
        
        stats.setTotalComments(totalComments);
        stats.setMonthlyChange(3.5); // 模拟数据
        stats.setChartData(generateChartData("comment", 7));
        
        return stats;
    }
    
    private AdminDto.RevenueStats getRevenueStats(long startTime, long endTime) {
        AdminDto.RevenueStats stats = new AdminDto.RevenueStats();
        
        List<VideoShareUserLog> logs = shareLogRepository.findByCtimeBetweenAndStatusOrderByCtimeDesc(startTime, endTime, 1);
        BigDecimal totalRevenue = logs.stream()
            .map(log -> log.getReward() != null ? log.getReward() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        stats.setTotalRevenue(totalRevenue);
        stats.setMonthlyChange(-1.8); // 模拟数据
        stats.setChartData(generateChartData("revenue", 7));
        
        return stats;
    }
    
    private AdminDto.CollectionStats getCollectionStats(long startTime, long endTime, Integer platformId) {
        AdminDto.CollectionStats stats = new AdminDto.CollectionStats();
        
        // 这里使用点赞数作为收藏数的近似值
        List<VideoShareUserLog> logs;
        if (platformId != null) {
            logs = shareLogRepository.findByCtimeBetweenAndPlatformIdAndStatusOrderByCtimeDesc(startTime, endTime, platformId, 1);
        } else {
            logs = shareLogRepository.findByCtimeBetweenAndStatusOrderByCtimeDesc(startTime, endTime, 1);
        }
        
        long totalCollections = logs.stream()
            .mapToLong(log -> log.getLikeCount() != null ? log.getLikeCount() : 0)
            .sum();
        
        stats.setTotalCollections(totalCollections);
        stats.setMonthlyChange(8.3); // 模拟数据
        stats.setChartData(generateChartData("collection", 7));
        
        return stats;
    }
    
    private List<AdminDto.ChartData> generateChartData(String type, int days) {
        List<AdminDto.ChartData> data = new ArrayList<>();
        Random random = new Random();
        
        for (int i = days - 1; i >= 0; i--) {
            AdminDto.ChartData item = new AdminDto.ChartData();
            item.setDate(DateUtil.format(DateUtil.offsetDay(new Date(), -i), "MM-dd"));
            item.setValue(random.nextInt(1000) + 100); // 模拟数据
            data.add(item);
        }
        
        return data;
    }
    
    private long[] calculateTimeRange(String timeFilter, String startDate, String endDate) {
        long endTime = DateUtil.currentSeconds();
        long startTime;
        
        if (startDate != null && endDate != null) {
            startTime = DateUtil.parse(startDate, "yyyy-MM-dd").getTime() / 1000;
            endTime = DateUtil.parse(endDate, "yyyy-MM-dd").getTime() / 1000;
        } else if (timeFilter != null) {
            switch (timeFilter) {
                case "today":
                    startTime = DateUtil.beginOfDay(new Date()).getTime() / 1000;
                    break;
                case "week":
                    startTime = DateUtil.offsetDay(new Date(), -7).getTime() / 1000;
                    break;
                case "month":
                    startTime = DateUtil.offsetDay(new Date(), -30).getTime() / 1000;
                    break;
                case "year":
                    startTime = DateUtil.offsetDay(new Date(), -365).getTime() / 1000;
                    break;
                default:
                    startTime = DateUtil.offsetDay(new Date(), -7).getTime() / 1000;
            }
        } else {
            startTime = DateUtil.offsetDay(new Date(), -7).getTime() / 1000;
        }
        
        return new long[]{startTime, endTime};
    }
    
    private long[] calculateTimeRangeByType(String type) {
        long endTime = DateUtil.currentSeconds();
        long startTime;
        
        switch (type) {
            case "weekly":
                startTime = DateUtil.offsetDay(new Date(), -7).getTime() / 1000;
                break;
            case "monthly":
                startTime = DateUtil.offsetDay(new Date(), -30).getTime() / 1000;
                break;
            case "total":
            default:
                startTime = 0; // 从最早开始
                break;
        }
        
        return new long[]{startTime, endTime};
    }
    
    // 内部类用于统计
    private static class UserStats {
        Integer userId;
        BigDecimal rewardAmount = BigDecimal.ZERO;
        Integer videosPosted = 0;
        Integer totalViews = 0;
        Integer totalLikes = 0;
        Integer totalComments = 0;
    }
}


package com.videotask.service.impl;

import cn.hutool.core.date.DateUtil;
import com.github.kwai.open.response.CreateVideoResponse;
import com.videotask.dto.ApiResponse;
import com.videotask.dto.VideoDto;
import com.videotask.entity.*;
import com.videotask.repository.*;
import com.videotask.service.KuaishouSdkService;
import com.videotask.service.VideoService;
import com.videotask.service.VideoUploadSchedulerService;
import com.videotask.util.VideoDownloadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class VideoServiceImpl implements VideoService {

    @Autowired
    private VideoTypeRepository videoTypeRepository;

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private VideoShareUserLogRepository shareLogRepository;

    @Autowired
    private UserAuthRepository userAuthRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private KuaishouSdkService kuaishouSdkService;

    @Autowired
    private VideoUploadSchedulerService videoUploadSchedulerService;

    @Autowired
    private VideoPlatformRepository videoPlatformRepository;


    @Override
    public ApiResponse<List<VideoDto.VideoTypeInfo>> getVideoTypes() {
        List<VideoType> types = videoTypeRepository.findAll();

        List<VideoDto.VideoTypeInfo> typeInfos = types.stream().map(type -> {
            VideoDto.VideoTypeInfo info = new VideoDto.VideoTypeInfo();
            info.setId(type.getId());
            info.setName(type.getName());
            return info;
        }).collect(Collectors.toList());

        return ApiResponse.success(typeInfos);
    }

    @Override
    public ApiResponse<List<VideoDto.VideoInfo>> getVideoList(Integer userId, VideoDto.VideoListRequest request) {
        // 获取已发布的视频
        List<Video> videos;

        // 如果指定了视频分类，按分类过滤
        if (request.getVideoType() != null) {
            videos = videoRepository.findByStatusAndVideoTypeOrderByCtimeDesc(1, request.getVideoType());
        } else {
            videos = videoRepository.findByStatusOrderByCtimeDesc(1);
        }

        // 限制返回数量
        int num = request.getNum() != null ? request.getNum() : 2;
        videos = videos.stream().limit(num).collect(Collectors.toList());

        // 构建响应
        List<VideoDto.VideoInfo> videoInfos = videos.stream().map(video -> {
            VideoDto.VideoInfo info = new VideoDto.VideoInfo();
            info.setId(video.getId());
            info.setTitle(video.getTitle());
            info.setVideoDesc(video.getVideoDesc());
            info.setCover(video.getCover());
            info.setUrl(video.getUrl());
            info.setVideoType(video.getVideoType());
            info.setStatus(video.getStatus());
            info.setHashtags(video.getHashtags());
            info.setReward(video.getReward());
            info.setCtime(video.getCtime());
            info.setUtime(video.getUtime());

            // 检查用户是否已分享该视频到指定平台
            boolean isShared = false;
            boolean isPending = false;
            if (request.getPlatformId() != null) {
                Optional<VideoShareUserLog> shareLog = shareLogRepository
                        .findByVideoIdAndPlatformIdAndUserIdAndStatusNot(video.getId(), request.getPlatformId(), userId, -1);

                if (shareLog.isPresent()) {
                    VideoShareUserLog log = shareLog.get();
                    // 根据状态判断
                    if (log.getStatus() == 1) {
                        isShared = true;
                        // 如果已分享成功，获取分享数据
                        info.setLikeCount(log.getLikeCount());
                        info.setCommentCount(log.getCommentCount());
                        info.setViewCount(log.getViewCount());
                        info.setDataStatus(log.getDataStatus()); // 设置数据状态
                    } else if (log.getStatus() == 0) {
                        isPending = true;
                        info.setDataStatus(log.getDataStatus()); // 设置数据状态
                    }
                    // status == -1 表示分享失败，不设置任何状态
                }
            }

            info.setIsShared(isShared);
            info.setIsPending(isPending);

            return info;
        }).collect(Collectors.toList());

        return ApiResponse.success(videoInfos);
    }

    @Override
    public ApiResponse<Void> shareVideo(Integer userId, VideoDto.VideoShareRequest request) {
        // 检查视频是否存在
        Optional<Video> videoOpt = videoRepository.findById(request.getVideoId());
        if (!videoOpt.isPresent()) {
            return ApiResponse.error("视频不存在");
        }

        Video video = videoOpt.get();

        // 检查是否已经分享过
        Optional<VideoShareUserLog> existingLog = shareLogRepository
                .findByVideoIdAndPlatformIdAndUserIdAndStatusNot(request.getVideoId(), request.getPlatformId(), userId, -1);
        if (existingLog.isPresent()) {
            return ApiResponse.error("该视频已分享到此平台");
        }

        // 检查用户是否已授权该平台
        Optional<UserAuth> userAuth = userAuthRepository
                .findByUserIdAndPlatformId(userId, request.getPlatformId());
        if (!userAuth.isPresent()) {
            return ApiResponse.error("请先授权该平台");
        }

        // 检查发布限制
        VideoDto.CheckPublishLimitRequest limitRequest = new VideoDto.CheckPublishLimitRequest();
        limitRequest.setPlatformId(request.getPlatformId());
        ApiResponse<VideoDto.CheckPublishLimitResponse> limitResponse = checkPublishLimit(userId, limitRequest);
        if (limitResponse.getCode() != 10000) {
            return ApiResponse.error(limitResponse.getMessage());
        }

        if (!limitResponse.getData().getCanPublish()) {
            return ApiResponse.error("今日已达该平台发布上限（" + limitResponse.getData().getPublishNum() + "个），请明天再试");
        }

        // 保存分享记录，状态为0（分享中）
        try {
            Optional<VideoShareUserLog> shareUserLogOptional = shareLogRepository
                    .findByVideoIdAndPlatformIdAndUserId(request.getVideoId(), request.getPlatformId(), userId);
            VideoShareUserLog shareLog = null;
            if (shareUserLogOptional.isPresent()) {
                shareLog = shareUserLogOptional.get();
            } else {
                shareLog = new VideoShareUserLog();
            }
            shareLog.setVideoId(video.getId());
            shareLog.setPlatformId(request.getPlatformId());
            shareLog.setUserId(userId);
            shareLog.setVideoUrl(video.getUrl());
            shareLog.setStatus(0); // 0分享中
            shareLog.setLikeCount(0);
            shareLog.setCommentCount(0);
            shareLog.setViewCount(0);
            shareLog.setReward(video.getReward());
            shareLog.setRewardStatus(1); // 1进行中
            shareLog.setDataStatus(1); // 1正常（默认）
            shareLog.setCtime(DateUtil.currentSeconds());

            shareLogRepository.save(shareLog);

//            // 触发视频上传
//            videoUploadSchedulerService.uploadSingleVideo(shareLog.getId());

            return ApiResponse.success(null);
        } catch (Exception e) {
            return ApiResponse.error("保存分享记录失败: " + e.getMessage());
        }
    }

    @Autowired
    private VideoDownloadUtil videoDownloadUtil;

    // 快手分享实现
    private VideoShareUserLog shareToKuaishou(Video video, UserAuth userAuth) {
        // 创建分享记录对象（但不保存到数据库）
        VideoShareUserLog shareLog = new VideoShareUserLog();
        shareLog.setVideoId(video.getId());
        shareLog.setPlatformId(2); // 快手平台ID
        shareLog.setUserId(userAuth.getUserId());
        shareLog.setVideoUrl(video.getUrl());
        shareLog.setLikeCount(0);
        shareLog.setCommentCount(0);
        shareLog.setViewCount(0);

        try {
            // 检查access_token是否有效
            if (userAuth.getAccessToken() == null) {
                throw new RuntimeException("快手access_token为空");
            }

            // 检查access_token是否过期，如果过期则刷新
            String accessToken = userAuth.getAccessToken();
            if (!kuaishouSdkService.isAccessTokenValid(userAuth)) {
                ApiResponse<UserAuth> refreshResponse = kuaishouSdkService.refreshAccessToken(userAuth.getUserId());
                if (refreshResponse.getCode() == 10000) {
                    userAuth = refreshResponse.getData();
                    accessToken = userAuth.getAccessToken();
                } else {
                    throw new RuntimeException("刷新access_token失败: " + refreshResponse.getMessage());
                }
            }

            // 下载视频文件
            MultipartFile videoFile = downloadVideoFile(video.getUrl());
            if (videoFile == null) {
                throw new RuntimeException("无法下载视频文件");
            }

            // 准备发布参数
            String title = video.getTitle() != null ? video.getTitle() : "精彩视频分享";
            String description = video.getVideoDesc() != null ? video.getVideoDesc() : "";
            String[] tags = extractTags(video.getVideoDesc());

            // 调用快手SDK发布视频
            CreateVideoResponse res = kuaishouSdkService.createVideo(accessToken, videoFile.getName(), videoFile.getBytes(), downloadVideoFile(video.getCover()).getBytes());

            if (res != null && res.getResult() == 1) {
                // 发布成功，设置分享记录
                shareLog.setPhotoId(res.getVideoInfo().getPhotoId());
                shareLog.setStatus(1); // 成功状态
                return shareLog; // 返回成功的分享记录
            }
            return shareLog;
        } catch (Exception e) {
            throw new RuntimeException("快手分享失败: " + e.getMessage());
        }
    }


    /**
     * 下载视频文件
     */
    private MultipartFile downloadVideoFile(String videoUrl) {
        try {
            // 检查URL是否可访问
            if (!videoDownloadUtil.isUrlAccessible(videoUrl)) {
                throw new RuntimeException("视频URL不可访问: " + videoUrl);
            }

            // 获取文件大小
            long fileSize = videoDownloadUtil.getFileSize(videoUrl);
            if (fileSize > 100 * 1024 * 1024) { // 100MB限制
                throw new RuntimeException("视频文件过大，超过100MB限制");
            }

            // 下载视频文件
            MultipartFile videoFile = videoDownloadUtil.downloadVideoFile(videoUrl);
            if (videoFile == null || videoFile.isEmpty()) {
                throw new RuntimeException("下载视频文件失败");
            }

            return videoFile;

        } catch (Exception e) {
            System.err.println("下载视频文件失败: " + e.getMessage());
            return null;
        }
    }


    /**
     * 从视频描述中提取标签
     */
    private String[] extractTags(String description) {
        if (description == null || description.trim().isEmpty()) {
            return new String[0];
        }

        // 简单的标签提取逻辑，可以根据#号提取标签
        String[] words = description.split("\\s+");
        List<String> tags = new ArrayList<>();

        for (String word : words) {
            if (word.startsWith("#") && word.length() > 1) {
                tags.add(word.substring(1)); // 去掉#号
            }
        }

        return tags.toArray(new String[0]);
    }

    // 抖音分享实现
    private VideoShareUserLog shareToDouyin(Video video, UserAuth userAuth) {
        // 创建分享记录对象（但不保存到数据库）
        VideoShareUserLog shareLog = new VideoShareUserLog();
        shareLog.setVideoId(video.getId());
        shareLog.setPlatformId(1); // 抖音平台ID
        shareLog.setUserId(userAuth.getUserId());
        shareLog.setVideoUrl(video.getUrl());
        shareLog.setLikeCount(0);
        shareLog.setCommentCount(0);
        shareLog.setViewCount(0);

        try {
            // TODO: 实现抖音API调用
            // 这里需要根据抖音的API文档实现具体的分享逻辑

            // 模拟分享成功
            shareLog.setPhotoId("douyin_" + System.currentTimeMillis());
            shareLog.setStatus(1); // 成功状态
            // 使用ctime作为分享时间，会在保存时自动设置

            return shareLog; // 返回成功的分享记录

        } catch (Exception e) {
            // 失败时不保存记录，直接抛出异常
            System.err.println("抖音分享失败: " + e.getMessage());
            throw new RuntimeException("抖音分享失败: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<VideoDto.VideoInfo> getVideoDetail(Integer videoId, Integer userId) {
        // 查找视频
        Optional<Video> videoOpt = videoRepository.findById(videoId);
        if (!videoOpt.isPresent()) {
            return ApiResponse.error("视频不存在");
        }

        Video video = videoOpt.get();

        // 构建视频详情信息
        VideoDto.VideoInfo videoInfo = new VideoDto.VideoInfo();
        videoInfo.setId(video.getId());
        videoInfo.setTitle(video.getTitle());
        videoInfo.setVideoDesc(video.getVideoDesc());
        videoInfo.setCover(video.getCover());
        videoInfo.setUrl(video.getUrl()); // 返回真实的视频URL
        videoInfo.setVideoType(video.getVideoType());
        videoInfo.setStatus(video.getStatus());
        videoInfo.setHashtags(video.getHashtags());
        videoInfo.setCtime(video.getCtime());
        videoInfo.setUtime(video.getUtime());

        // 检查用户是否已分享该视频（检查所有平台）
        List<VideoShareUserLog> shareLogs = shareLogRepository.findByVideoIdAndUserId(videoId, userId);
        boolean isShared = !shareLogs.isEmpty();
        videoInfo.setIsShared(isShared);

        // 如果有分享记录，获取最新的数据
        if (!shareLogs.isEmpty()) {
            VideoShareUserLog latestLog = shareLogs.get(0); // 取第一个作为示例
            videoInfo.setLikeCount(latestLog.getLikeCount());
            videoInfo.setCommentCount(latestLog.getCommentCount());
            videoInfo.setViewCount(latestLog.getViewCount());
            videoInfo.setDataStatus(latestLog.getDataStatus()); // 设置数据状态
        } else {
            // 设置默认值
            videoInfo.setLikeCount(0);
            videoInfo.setCommentCount(0);
            videoInfo.setViewCount(0);
        }

        return ApiResponse.success(videoInfo);
    }

    @Override
    public ApiResponse<VideoDto.VideoStatsResponse> getVideoStats(Integer userId, VideoDto.VideoStatsRequest request) {
        try {
            VideoDto.VideoStatsResponse response = new VideoDto.VideoStatsResponse();

            // 获取基础统计数据
            long totalVideos = videoRepository.count();
            long todayViews = 8902; // 模拟今日播放量
            long totalLikes = 2341; // 模拟总点赞数
            long totalComments = 567; // 模拟总评论数

            response.setTotalVideos((int) totalVideos);
            response.setTodayViews((int) todayViews);
            response.setTotalLikes((int) totalLikes);
            response.setTotalComments((int) totalComments);

            // TODO: 从真实数据源获取详细统计数据
            response.setPlayCount(createStatsData(0, 0.0));
            response.setLikeCount(createStatsData(0, 0.0));
            response.setCommentCount(createStatsData(0, 0.0));
            response.setPublishCount(createStatsData(0, 0.0));
            response.setCollectCount(createStatsData(0, 0.0));
            response.setRewardAmount(createStatsData(0, 0.0));

            return ApiResponse.success(response);
        } catch (Exception e) {
            return ApiResponse.error("获取统计数据失败: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<VideoDto.VideoPublishListResponse> getPublishList(Integer userId, VideoDto.VideoPublishListRequest request) {
        try {
            // 获取用户的发布记录
            List<VideoShareUserLog> shareLogs = shareLogRepository.findByUserId(userId);
            if (request.getPlatformId() != null) {
                shareLogs = shareLogs.stream().filter(log -> log.getPlatformId().equals(request.getPlatformId())).collect(Collectors.toList());
            }

            // 构建发布信息列表
            List<VideoDto.VideoPublishInfo> publishInfos = shareLogs.stream().map(log -> {
                VideoDto.VideoPublishInfo info = new VideoDto.VideoPublishInfo();
                info.setId(log.getId());

                // 获取视频信息
                Optional<Video> videoOpt = videoRepository.findById(log.getVideoId());
                if (videoOpt.isPresent()) {
                    Video video = videoOpt.get();
                    info.setTitle(video.getTitle());
                    info.setCover(video.getCover());
                    info.setHashtags(video.getHashtags());
                    info.setVideoId(log.getVideoId());
                    info.setDuration("00:00"); // TODO: 获取真实视频时长
                } else {
                    info.setTitle("未知视频");
                    info.setCover("/static/images/video-default.jpg");
                    info.setHashtags("");
                    info.setDuration("00:00");
                }

                info.setAuthor("未知作者"); // TODO: 获取真实作者信息
                info.setViewCount(String.valueOf(log.getViewCount() != null ? log.getViewCount() : 0));
                info.setLikeCount(String.valueOf(log.getLikeCount() != null ? log.getLikeCount() : 0));
                info.setCommentCount(String.valueOf(log.getCommentCount() != null ? log.getCommentCount() : 0));
                info.setFavoriteCount("0"); // TODO: 获取真实收藏数
                info.setStatus(log.getStatus());
                info.setCtime(log.getCtime());
                info.setDataStatus(log.getDataStatus());
                info.setPlatformId(log.getPlatformId()); // 设置平台ID

                return info;
            }).collect(Collectors.toList());

            // 分页处理
            int total = publishInfos.size();
            int start = (request.getPage() - 1) * request.getSize();
            int end = Math.min(start + request.getSize(), total);

            List<VideoDto.VideoPublishInfo> pageData;
            // 检查分页参数的有效性
            if (start >= total || start < 0) {
                // 如果起始索引超出范围，返回空列表
                pageData = new ArrayList<>();
            } else {
                pageData = publishInfos.subList(start, end);
            }

            VideoDto.VideoPublishListResponse response = new VideoDto.VideoPublishListResponse();
            response.setRecords(pageData);
            response.setTotal(total);
            response.setCurrent(request.getPage());
            response.setSize(request.getSize());

            return ApiResponse.success(response);
        } catch (Exception e) {
            return ApiResponse.error("获取发布列表失败: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<VideoDto.VideoChartDataResponse> getChartData(Integer userId, VideoDto.VideoChartDataRequest request) {
        try {
            // TODO: 从真实数据源获取图表数据
            List<VideoDto.VideoChartData> chartData = new ArrayList<>();

            VideoDto.VideoChartDataResponse response = new VideoDto.VideoChartDataResponse();
            response.setData(chartData);

            return ApiResponse.success(response);
        } catch (Exception e) {
            return ApiResponse.error("获取图表数据失败: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<VideoDto.VideoShareStatsResponse> getShareStats(Integer userId, Integer platformId) {
        try {
            // 添加调试信息
            System.out.println("=== 调试分享统计数据 ===");
            System.out.println("userId: " + userId);
            System.out.println("platformId: " + platformId);

            // 先查询所有记录，看看数据情况
            List<VideoShareUserLog> allLogs = shareLogRepository.findByUserId(userId);
            System.out.println("用户所有分享记录数量: " + allLogs.size());

            if (!allLogs.isEmpty()) {
                System.out.println("第一条记录: " + allLogs.get(0));
            }

            // 获取用户的分享统计数据
            Map<String, Object> stats = shareLogRepository.getShareStatsByUserId(userId, platformId);

            VideoDto.VideoShareStatsResponse response = new VideoDto.VideoShareStatsResponse();

            if (stats != null) {
                // 解析查询结果
                Long totalViews = (Long) stats.getOrDefault("totalViews", 0L);
                Long totalLikes = (Long) stats.getOrDefault("totalLikes", 0L);
                Long totalComments = (Long) stats.getOrDefault("totalComments", 0L);
                Long totalShares = (Long) stats.getOrDefault("totalShares", 0L);

                response.setTotalViews(totalViews != null ? totalViews.intValue() : 0);
                response.setTotalLikes(totalLikes != null ? totalLikes.intValue() : 0);
                response.setTotalComments(totalComments != null ? totalComments.intValue() : 0);
                response.setTotalShares(totalShares != null ? totalShares.intValue() : 0);

                // 从用户账户表获取历史奖励总额
                java.math.BigDecimal historyReward = java.math.BigDecimal.ZERO;
                Optional<com.videotask.entity.UserAccount> userAccount = userAccountRepository.findByUserId(userId);
                if (userAccount.isPresent()) {
                    historyReward = userAccount.get().getHistoryReward();
                }
                response.setTotalReward(historyReward);

                // 设置详细统计数据
                response.setPlayCount(createShareStatsData(response.getTotalViews(), 0.0));
                response.setLikeCount(createShareStatsData(response.getTotalLikes(), 0.0));
                response.setCommentCount(createShareStatsData(response.getTotalComments(), 0.0));
                response.setShareCount(createShareStatsData(response.getTotalShares(), 0.0));
                response.setRewardAmount(createShareStatsData(historyReward.intValue(), 0.0));
            } else {
                // 如果没有数据，设置默认值
                response.setTotalViews(0);
                response.setTotalLikes(0);
                response.setTotalComments(0);
                response.setTotalShares(0);

                // 从用户账户表获取历史奖励总额
                java.math.BigDecimal historyReward = java.math.BigDecimal.ZERO;
                Optional<com.videotask.entity.UserAccount> userAccount = userAccountRepository.findByUserId(userId);
                if (userAccount.isPresent()) {
                    historyReward = userAccount.get().getHistoryReward();
                }
                response.setTotalReward(historyReward);

                response.setPlayCount(createShareStatsData(0, 0.0));
                response.setLikeCount(createShareStatsData(0, 0.0));
                response.setCommentCount(createShareStatsData(0, 0.0));
                response.setShareCount(createShareStatsData(0, 0.0));
                response.setRewardAmount(createShareStatsData(historyReward.intValue(), 0.0));
            }

            return ApiResponse.success(response);
        } catch (Exception e) {
            return ApiResponse.error("获取分享统计数据失败: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<VideoDto.CheckPublishLimitResponse> checkPublishLimit(Integer userId, VideoDto.CheckPublishLimitRequest request) {
        try {
            // 检查平台是否存在
            Optional<VideoPlatform> platformOpt = videoPlatformRepository.findById(request.getPlatformId());
            if (!platformOpt.isPresent()) {
                return ApiResponse.error("平台不存在");
            }

            VideoPlatform platform = platformOpt.get();

            // 获取平台设置的每日发布上限，默认为5
            Integer publishNum = platform.getPublishNum() != null ? platform.getPublishNum() : 5;

            // 查询用户今天在该平台已发布的视频数量
            Integer currentNum = shareLogRepository.countTodayPublishByUserIdAndPlatformId(userId, request.getPlatformId());
            if (currentNum == null) {
                currentNum = 0;
            }

            // 计算剩余可发布数量
            Integer remainingNum = publishNum - currentNum;

            // 判断是否可以继续发布
            boolean canPublish = remainingNum > 0;

            // 构建响应
            VideoDto.CheckPublishLimitResponse response = new VideoDto.CheckPublishLimitResponse();
            response.setCanPublish(canPublish);
            response.setPublishNum(publishNum);
            response.setCurrentNum(currentNum);
            response.setRemainingNum(remainingNum);

            return ApiResponse.success(response);
        } catch (Exception e) {
            return ApiResponse.error("检查发布限制失败: " + e.getMessage());
        }
    }

    // 辅助方法
    private VideoDto.VideoStatsData createStatsData(Integer value, Double trend) {
        VideoDto.VideoStatsData data = new VideoDto.VideoStatsData();
        data.setValue(value);
        data.setTrend(trend);
        return data;
    }

    private VideoDto.ShareStatsData createShareStatsData(Integer value, Double trend) {
        VideoDto.ShareStatsData data = new VideoDto.ShareStatsData();
        data.setValue(value);
        data.setTrend(trend);
        return data;
    }

    private VideoDto.VideoChartData createChartData(String date, Integer value) {
        VideoDto.VideoChartData data = new VideoDto.VideoChartData();
        data.setDate(date);
        data.setValue(value);
        return data;
    }
}
package com.videotask.service;

import com.github.kwai.open.response.VideoInfoResponse;
import com.videotask.dto.ApiResponse;
import com.videotask.entity.UserAuth;
import com.videotask.entity.VideoShareUserDayLog;
import com.videotask.entity.VideoShareUserLog;
import com.videotask.repository.UserAuthRepository;
import com.videotask.repository.VideoShareUserDayLogRepository;
import com.videotask.repository.VideoShareUserLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class VideoDataSyncService {

    @Autowired
    private VideoShareUserLogRepository shareLogRepository;

    @Autowired
    private VideoShareUserDayLogRepository dayLogRepository;

    @Autowired
    private UserAuthRepository userAuthRepository;

    @Autowired
    private KuaishouSdkService kuaishouSdkService;

    /**
     * 每天凌晨1点执行，获取快手平台视频数据
     */
    public void syncKuaishouVideoData() {
        System.out.println("开始同步快手视频数据...");
        try {
            // 获取所有快手平台的分享记录
            List<VideoShareUserLog> shareLogs = shareLogRepository.findByPlatformIdAndStatus(2, 1);

            for (VideoShareUserLog shareLog : shareLogs) {
                try {
                    syncKuaishouVideoData(shareLog);
                } catch (Exception e) {
                    System.err.println("同步视频数据失败，photoId: " + shareLog.getPhotoId() + ", 错误: " + e.getMessage());
                }
            }

            System.out.println("快手视频数据同步完成");
        } catch (Exception e) {
            System.err.println("快手视频数据同步失败: " + e.getMessage());
        }
    }

    /**
     * 同步单个快手视频的数据
     */
    private void syncKuaishouVideoData(VideoShareUserLog shareLog) {
        if (shareLog.getPhotoId() == null || shareLog.getPhotoId().trim().isEmpty()) {
            System.out.println("跳过无效的photoId: " + shareLog.getPhotoId());
            return;
        }

        // 获取用户授权信息
        Optional<UserAuth> userAuthOpt = userAuthRepository.findByUserIdAndPlatformId(shareLog.getUserId(), 2);
        if (!userAuthOpt.isPresent()) {
            System.out.println("用户未授权快手平台，userId: " + shareLog.getUserId());
            return;
        }

        UserAuth userAuth = userAuthOpt.get();

        // 检查access_token是否有效，如果过期则刷新
        if (!kuaishouSdkService.isAccessTokenValid(userAuth)) {
            try {
                ApiResponse<UserAuth> refreshResponse = kuaishouSdkService.refreshAccessToken(userAuth.getUserId());
                if (refreshResponse.getCode() == 10000) {
                    userAuth = refreshResponse.getData();
                } else {
                    System.err.println("刷新access_token失败，userId: " + userAuth.getUserId());
                    return;
                }
            } catch (Exception e) {
                System.err.println("刷新access_token异常，userId: " + userAuth.getUserId() + ", 错误: " + e.getMessage());
                return;
            }
        }

        // 查询视频信息
        VideoInfoResponse videoInfo = kuaishouSdkService.queryVideoByPhotoId(userAuth.getAccessToken(), shareLog.getPhotoId());
        if (videoInfo == null) {
            System.err.println("查询视频信息失败，photoId: " + shareLog.getPhotoId());
            return;
        }

        // 获取今日日期
        LocalDateTime today = LocalDateTime.now();

        // 查找今日是否已有记录
        Optional<VideoShareUserDayLog> existingLog = dayLogRepository.findByPhotoIdAndToday(shareLog.getPhotoId(), today);

        VideoShareUserDayLog dayLog;
        if (existingLog.isPresent()) {
            // 更新现有记录
            dayLog = existingLog.get();
        } else {
            // 创建新记录
            dayLog = new VideoShareUserDayLog();
            dayLog.setPhotoId(shareLog.getPhotoId());
        }

        // 更新数据 - 从VideoInfoResponse获取真实数据
        try {
            // 从VideoInfoResponse中获取数据
            Integer likeCount = videoInfo.getVideoInfo().getLikeCount().intValue();
            Integer commentCount = videoInfo.getVideoInfo().getCommentCount().intValue();
            Integer viewCount = videoInfo.getVideoInfo().getViewCount().intValue();

            dayLog.setLikeCount(likeCount);
            dayLog.setCommentCount(commentCount);
            dayLog.setViewCount(viewCount);

            // 保存日日志记录
            dayLogRepository.save(dayLog);

            // 同时更新分享记录中的数据
            shareLog.setLikeCount(likeCount);
            shareLog.setCommentCount(commentCount);
            shareLog.setViewCount(viewCount);
            shareLogRepository.save(shareLog);

            System.out.println("同步真实数据成功，photoId: " + shareLog.getPhotoId() + 
                              ", 点赞: " + likeCount + ", 评论: " + commentCount + ", 播放: " + viewCount);

        } catch (Exception e) {
            System.err.println("保存数据失败，photoId: " + shareLog.getPhotoId() + ", 错误: " + e.getMessage());
            // 设置默认值
            dayLog.setLikeCount(0);
            dayLog.setCommentCount(0);
            dayLog.setViewCount(0);
            dayLogRepository.save(dayLog);
        }

        System.out.println("同步成功，photoId: " + shareLog.getPhotoId() +
                ", 点赞: " + dayLog.getLikeCount() +
                ", 评论: " + dayLog.getCommentCount() +
                ", 播放: " + dayLog.getViewCount());
    }

    /**
     * 手动同步指定视频的数据
     */
    public void syncVideoDataByPhotoId(String photoId) {
        System.out.println("手动同步视频数据，photoId: " + photoId);

        Optional<VideoShareUserLog> shareLogOpt = shareLogRepository.findByPhotoId(photoId);
        if (!shareLogOpt.isPresent()) {
            System.err.println("未找到对应的分享记录，photoId: " + photoId);
            return;
        }

        VideoShareUserLog shareLog = shareLogOpt.get();
        syncKuaishouVideoData(shareLog);
    }

    /**
     * 手动同步指定用户的所有视频数据
     */
    public void syncUserVideoData(Integer userId) {
        System.out.println("手动同步用户视频数据，userId: " + userId);

        List<VideoShareUserLog> shareLogs = shareLogRepository.findByUserIdAndPlatformIdAndStatus(userId, 2, 1);

        for (VideoShareUserLog shareLog : shareLogs) {
            try {
                syncKuaishouVideoData(shareLog);
            } catch (Exception e) {
                System.err.println("同步视频数据失败，photoId: " + shareLog.getPhotoId() + ", 错误: " + e.getMessage());
            }
        }
    }


}

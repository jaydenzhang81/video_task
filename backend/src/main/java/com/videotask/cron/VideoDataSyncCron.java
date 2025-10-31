package com.videotask.cron;

import cn.hutool.core.date.DateUtil;
import com.github.kwai.open.response.VideoInfoResponse;
import com.videotask.dto.ApiResponse;
import com.videotask.entity.UserAuth;
import com.videotask.entity.VideoShareUserDayLog;
import com.videotask.entity.VideoShareUserLog;
import com.videotask.repository.UserAuthRepository;
import com.videotask.repository.VideoShareUserDayLogRepository;
import com.videotask.repository.VideoShareUserLogRepository;
import com.videotask.service.KuaishouSdkService;
import com.videotask.service.VideoDataSyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 视频数据同步定时任务
 */
@Slf4j
@Component
public class VideoDataSyncCron {

    @Autowired
    private VideoDataSyncService videoDataSyncService;

    @Autowired
    private VideoShareUserLogRepository shareLogRepository;

    @Autowired
    private VideoShareUserDayLogRepository dayLogRepository;

    @Autowired
    private UserAuthRepository userAuthRepository;

    @Autowired
    private KuaishouSdkService kuaishouSdkService;

    /**
     * 定时任务：每天凌晨1点执行，获取快手平台视频数据
     */
    @Scheduled(fixedDelay = 1000 * 60 * 60)
    public void syncKuaishouVideoData() {
        log.info("开始同步快手视频数据...");
        try {
            // 获取所有快手平台的分享记录
            List<VideoShareUserLog> shareLogs = shareLogRepository.findByPlatformIdAndStatus(2, 1);

            for (VideoShareUserLog shareLog : shareLogs) {
                try {
                    syncKuaishouVideoData(shareLog);
                } catch (Exception e) {
                    log.error("同步视频数据失败，photoId: {}, 错误: {}", shareLog.getPhotoId(), e.getMessage());
                }
            }

            log.info("快手视频数据同步完成");
        } catch (Exception e) {
            log.error("快手视频数据同步失败: {}", e.getMessage());
        }
    }

    /**
     * 同步单个快手视频的数据
     */
    private void syncKuaishouVideoData(VideoShareUserLog shareLog) {
        if (shareLog.getPhotoId() == null || shareLog.getPhotoId().trim().isEmpty()) {
            log.info("跳过无效的photoId: {}", shareLog.getPhotoId());
            return;
        }

        // 获取用户授权信息
        Optional<UserAuth> userAuthOpt = userAuthRepository.findByUserIdAndPlatformId(shareLog.getUserId(), 2);
        if (!userAuthOpt.isPresent()) {
            log.info("用户未授权快手平台，userId: {}", shareLog.getUserId());
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
                    log.error("刷新access_token失败，userId: {}", userAuth.getUserId());
                    return;
                }
            } catch (Exception e) {
                log.error("刷新access_token异常，userId: {}, 错误: {}", userAuth.getUserId(), e.getMessage());
                return;
            }
        }

        // 查询视频信息
        VideoInfoResponse videoInfo = kuaishouSdkService.queryVideoByPhotoId(userAuth.getAccessToken(), shareLog.getPhotoId());
        if (videoInfo == null) {
            log.error("查询视频信息失败，photoId: {}", shareLog.getPhotoId());
            return;
        }

        // 获取今日日期
        Integer today = Integer.valueOf(DateUtil.format(LocalDateTime.now(), "yyyyMMdd"));

        // 查找今日是否已有记录
        Optional<VideoShareUserDayLog> existingLog = dayLogRepository.findByPhotoIdAndDate(shareLog.getPhotoId(), today);

        VideoShareUserDayLog dayLog;
        if (existingLog.isPresent()) {
            // 更新现有记录
            dayLog = existingLog.get();
        } else {
            // 创建新记录
            dayLog = new VideoShareUserDayLog();
            dayLog.setPhotoId(shareLog.getPhotoId());
            dayLog.setCtime(DateUtil.currentSeconds());
            dayLog.setDate(today);
        }

        // 更新数据 - 从VideoInfoResponse获取真实数据
        try {
            // 从VideoInfoResponse中获取数据
            Integer likeCount = videoInfo.getVideoInfo().getLikeCount().intValue();
            Integer commentCount = videoInfo.getVideoInfo().getCommentCount().intValue();
            Integer viewCount = videoInfo.getVideoInfo().getViewCount().intValue();

            // 判断数据状态：如果所有数据都是0，标记为异常，否则标记为正常
            Integer dataStatus = (likeCount == 0 && commentCount == 0 && viewCount == 0) ? 0 : 1;

            dayLog.setLikeCount(likeCount);
            dayLog.setCommentCount(commentCount);
            dayLog.setViewCount(viewCount);

            // 保存日日志记录
            dayLogRepository.save(dayLog);

            // 同时更新分享记录中的数据
            shareLog.setLikeCount(likeCount);
            shareLog.setCommentCount(commentCount);
            shareLog.setViewCount(viewCount);
            shareLog.setDataStatus(dataStatus); // 设置数据状态
            shareLogRepository.save(shareLog);

            log.info("同步视频数据成功，photoId: {}, 点赞: {}, 评论: {}, 播放: {}, 数据状态: {}",
                    shareLog.getPhotoId(), likeCount, commentCount, viewCount,
                    dataStatus == 0 ? "异常" : "正常");

        } catch (Exception e) {
            log.error("更新视频数据失败，photoId: {}, 错误: {}", shareLog.getPhotoId(), e.getMessage());
        }
    }
}

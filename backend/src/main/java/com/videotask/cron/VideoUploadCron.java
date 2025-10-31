package com.videotask.cron;

import com.videotask.entity.VideoShareUserLog;
import com.videotask.repository.VideoShareUserLogRepository;
import com.videotask.service.VideoUploadSchedulerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 视频上传定时任务
 */
@Slf4j
@Component
public class VideoUploadCron {

    @Autowired
    private VideoUploadSchedulerService videoUploadSchedulerService;

    @Autowired
    private VideoShareUserLogRepository shareLogRepository;

    /**
     * 定时任务：每30秒处理一次待上传的视频
     */
    @Scheduled(fixedRate = 1000 * 60)
    public void processPendingVideos() {
        log.info("开始处理待上传的视频...");

        try {
            // 查找状态为0的视频分享记录
            List<VideoShareUserLog> pendingVideos = shareLogRepository.findByStatus(0);

            if (pendingVideos.isEmpty()) {
                log.info("没有待上传的视频");
                return;
            }

            log.info("找到 {} 个待上传的视频", pendingVideos.size());

            // 一条一条处理
            for (VideoShareUserLog shareLog : pendingVideos) {
                try {
                    videoUploadSchedulerService.uploadSingleVideo(shareLog.getId());
                    // 每次上传后等待2秒，避免频率过高
                    Thread.sleep(2000);
                } catch (Exception e) {
                    log.error("处理视频上传失败，shareLogId: {}, 错误: {}", shareLog.getId(), e.getMessage());
                }
            }

        } catch (Exception e) {
            log.error("处理待上传视频时发生错误: {}", e.getMessage(), e);
        }
    }
}

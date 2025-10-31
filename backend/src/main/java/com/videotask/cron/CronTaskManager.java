package com.videotask.cron;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 定时任务管理器
 * 统一管理所有定时任务的执行
 */
@Slf4j
@Component
public class CronTaskManager {

    @Autowired
    private VideoUploadCron videoUploadCron;

    @Autowired
    private RewardCron rewardCron;

    @Autowired
    private TokenRefreshCron tokenRefreshCron;

    @Autowired
    private VideoDataSyncCron videoDataSyncCron;

    /**
     * 手动触发视频上传任务
     */
    public void triggerVideoUpload() {
        log.info("手动触发视频上传任务");
        videoUploadCron.processPendingVideos();
    }

    /**
     * 手动触发奖励检查和发放任务
     */
    public void triggerRewardCheck() {
        log.info("手动触发奖励检查和发放任务");
        rewardCron.checkAndIssueRewards();
    }

    /**
     * 手动触发Token刷新任务
     */
    public void triggerTokenRefresh() {
        log.info("手动触发Token刷新任务");
        tokenRefreshCron.refreshExpiringTokens();
    }

    /**
     * 手动触发Token过期检查任务
     */
    public void triggerTokenExpiredCheck() {
        log.info("手动触发Token过期检查任务");
        tokenRefreshCron.checkExpiredTokens();
    }

    /**
     * 手动触发视频数据同步任务
     */
    public void triggerVideoDataSync() {
        log.info("手动触发视频数据同步任务");
        videoDataSyncCron.syncKuaishouVideoData();
    }

    /**
     * 执行所有定时任务（用于测试）
     */
    public void executeAllTasks() {
        log.info("开始执行所有定时任务");
        
        try {
            // 执行视频上传任务
            triggerVideoUpload();
            
            // 执行奖励检查任务
            triggerRewardCheck();
            
            // 执行Token刷新任务
            triggerTokenRefresh();
            
            // 执行Token过期检查任务
            triggerTokenExpiredCheck();
            
            // 执行视频数据同步任务
            triggerVideoDataSync();
            
            log.info("所有定时任务执行完成");
        } catch (Exception e) {
            log.error("执行定时任务时发生错误: {}", e.getMessage(), e);
        }
    }
}

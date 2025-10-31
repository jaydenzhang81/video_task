package com.videotask.cron;

import com.videotask.entity.VideoShareUserLog;
import com.videotask.repository.VideoShareUserLogRepository;
import com.videotask.service.RewardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 奖励定时任务
 */
@Slf4j
@Component
public class RewardCron {

    @Autowired
    private RewardService rewardService;

    @Autowired
    private VideoShareUserLogRepository videoShareUserLogRepository;

    /**
     * 每日定时任务：检查并发放7天连续数据流的奖励
     */
    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
    public void checkAndIssueRewards() {
        log.info("开始执行奖励检查和发放任务...");
        
        try {
            // 查找所有进行中的任务
            List<VideoShareUserLog> ongoingTasks = videoShareUserLogRepository.findByRewardStatus(1);
            
            if (ongoingTasks.isEmpty()) {
                log.info("没有发现进行中的任务");
                return;
            }
            
            int successCount = 0;
            int failCount = 0;
            
            for (VideoShareUserLog shareLog : ongoingTasks) {
                try {
                    if (rewardService.checkContinuousDataFlow(shareLog)) {
                        // 满足7天连续数据流条件，发放奖励
                        if (rewardService.issueReward(shareLog)) {
                            successCount++;
                            log.info("用户 {} 的视频 {} 奖励发放成功，金额: {}", 
                                    shareLog.getUserId(), shareLog.getVideoId(), shareLog.getReward());
                        } else {
                            failCount++;
                            log.error("用户 {} 的视频 {} 奖励发放失败", shareLog.getUserId(), shareLog.getVideoId());
                        }
                    } else {
                        // 检查是否超过7天但数据流中断，标记为失败
                        if (rewardService.isTaskExpired(shareLog)) {
                            shareLog.setRewardStatus(-1); // 任务失败，无奖励
                            videoShareUserLogRepository.save(shareLog);
                            log.info("用户 {} 的视频 {} 任务已过期，标记为失败", shareLog.getUserId(), shareLog.getVideoId());
                        }
                    }
                } catch (Exception e) {
                    failCount++;
                    log.error("处理用户 {} 的视频 {} 奖励时发生错误: {}", shareLog.getUserId(), shareLog.getVideoId(), e.getMessage());
                }
            }
            
            log.info("奖励检查和发放任务完成，成功: {} 个，失败: {} 个", successCount, failCount);
            
        } catch (Exception e) {
            log.error("奖励检查和发放任务执行失败: {}", e.getMessage(), e);
        }
    }
}

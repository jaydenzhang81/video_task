package com.videotask.service.impl;

import cn.hutool.core.date.DateUtil;
import com.videotask.entity.UserAccount;
import com.videotask.entity.UserAccountLog;
import com.videotask.entity.VideoShareUserDayLog;
import com.videotask.entity.VideoShareUserLog;
import com.videotask.repository.UserAccountLogRepository;
import com.videotask.repository.UserAccountRepository;
import com.videotask.repository.VideoShareUserDayLogRepository;
import com.videotask.repository.VideoShareUserLogRepository;
import com.videotask.service.RewardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class RewardServiceImpl implements RewardService {

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private UserAccountLogRepository userAccountLogRepository;

    @Autowired
    private VideoShareUserLogRepository videoShareUserLogRepository;

    @Autowired
    private VideoShareUserDayLogRepository videoShareUserDayLogRepository;

    /**
     * 检查并发放7天连续数据流的奖励
     */
    @Override
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
                    if (checkContinuousDataFlow(shareLog)) {
                        // 满足7天连续数据流条件，发放奖励
                        if (issueReward(shareLog)) {
                            successCount++;
                            log.info("用户 {} 的视频 {} 奖励发放成功，金额: {}",
                                    shareLog.getUserId(), shareLog.getVideoId(), shareLog.getReward());
                        } else {
                            failCount++;
                            log.error("用户 {} 的视频 {} 奖励发放失败", shareLog.getUserId(), shareLog.getVideoId());
                        }
                    } else {
                        // 检查是否超过7天但数据流中断，标记为失败
                        if (isTaskExpired(shareLog)) {
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

    @Override
    @Transactional
    public boolean addUserBalance(Integer userId, BigDecimal amount, String title, String content, String source) {
        try {
            // 获取或创建用户账户
            UserAccount userAccount = getUserAccount(userId);
            if (userAccount == null) {
                userAccount = new UserAccount();
                userAccount.setUserId(userId);
                userAccount.setMoney(BigDecimal.ZERO);
                userAccount.setHistoryReward(BigDecimal.ZERO);
            }

            // 增加余额和历史奖励
            userAccount.setMoney(userAccount.getMoney().add(amount));
            userAccount.setHistoryReward(userAccount.getHistoryReward().add(amount));
            userAccountRepository.save(userAccount);

            // 记录日志
            UserAccountLog log = new UserAccountLog();
            log.setUserId(userId);
            log.setTitle(title);
            log.setContent(content);
            log.setMoney(amount);
            log.setType(1); // 1视频挣的
            log.setSource(source);
            userAccountLogRepository.save(log);

            return true;
        } catch (Exception e) {
            log.error("为用户 {} 增加余额失败: {}", userId, e.getMessage());
            return false;
        }
    }

    @Override
    public UserAccount getUserAccount(Integer userId) {
        Optional<UserAccount> optional = userAccountRepository.findByUserId(userId);
        return optional.orElse(null);
    }

    @Override
    public List<UserAccountLog> getUserAccountLogs(Integer userId) {
        return userAccountLogRepository.findByUserIdOrderByCtimeDesc(userId);
    }

    @Override
    public boolean checkContinuousDataFlow(VideoShareUserLog shareLog) {
        if (shareLog.getCtime() == null || shareLog.getPhotoId() == null) {
            return false;
        }

        Long startDate = shareLog.getCtime();
        Long endDate = startDate + 8 * 24 * 60 * 60;
        Long now = DateUtil.currentSeconds();

        // 如果还没到7天，不检查
        if (now.compareTo(endDate) < 0) {
            return false;
        }

        // 查找该日期的数据流记录
        List<VideoShareUserDayLog> dayLogs = videoShareUserDayLogRepository.findByPhotoIdAndCtimeBetween(
                shareLog.getPhotoId(), startDate, endDate);
        boolean hasValidData = dayLogs.stream().anyMatch(log ->
                (log.getLikeCount() != null && log.getLikeCount() > 0) ||
                        (log.getCommentCount() != null && log.getCommentCount() > 0) ||
                        (log.getViewCount() != null && log.getViewCount() > 0)
        );
        if (!hasValidData) {
            return false;
        }
        return true;
    }

    @Override
    @Transactional
    public boolean issueReward(VideoShareUserLog shareLog) {
        if (shareLog.getReward() == null || shareLog.getReward().compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("视频 {} 没有设置奖励金额", shareLog.getVideoId());
            return false;
        }

        try {
            // 发放奖励到用户账户
            String title = "视频分享奖励";
            String content = String.format("视频ID: %d 的7天连续数据流奖励", shareLog.getVideoId());
            String source = String.valueOf(shareLog.getVideoId());

            boolean success = addUserBalance(shareLog.getUserId(), shareLog.getReward(), title, content, source);

            if (success) {
                // 更新分享记录状态为已发放
                shareLog.setRewardStatus(2); // 2已发放
                videoShareUserLogRepository.save(shareLog);
                return true;
            }

            return false;
        } catch (Exception e) {
            log.error("发放奖励失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 检查任务是否已过期（超过7天但数据流中断）
     */
    @Override
    public boolean isTaskExpired(VideoShareUserLog shareLog) {
        if (shareLog.getCtime() == null) {
            return false;
        }

        Long taskStartDate = shareLog.getCtime();
        Long expireDate = taskStartDate + 8 * 24 * 60 * 60;
        Long now = DateUtil.currentSeconds();

        return now > expireDate;
    }
}

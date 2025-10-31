package com.videotask.service;

import com.videotask.entity.UserAccount;
import com.videotask.entity.UserAccountLog;
import com.videotask.entity.VideoShareUserLog;

import java.math.BigDecimal;
import java.util.List;

public interface RewardService {
    
    /**
     * 检查并发放7天连续数据流的奖励
     */
    void checkAndIssueRewards();
    
    /**
     * 为用户账户增加余额
     */
    boolean addUserBalance(Integer userId, BigDecimal amount, String title, String content, String source);
    
    /**
     * 获取用户账户信息
     */
    UserAccount getUserAccount(Integer userId);
    
    /**
     * 获取用户账户日志
     */
    List<UserAccountLog> getUserAccountLogs(Integer userId);
    
    /**
     * 检查任务是否满足7天连续数据流条件
     */
    boolean checkContinuousDataFlow(VideoShareUserLog shareLog);
    
    /**
     * 发放奖励到用户账户
     */
    boolean issueReward(VideoShareUserLog shareLog);
    
    /**
     * 检查任务是否已过期
     */
    boolean isTaskExpired(VideoShareUserLog shareLog);
}

package com.videotask.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.videotask.dto.ApiResponse;
import com.videotask.dto.UserAccountDto;
import com.videotask.entity.UserAccount;
import com.videotask.entity.UserAccountLog;
import com.videotask.service.RewardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@SaCheckLogin
@RestController
@RequestMapping("/api/reward")
public class RewardController {

    @Autowired
    private RewardService rewardService;

    /**
     * 手动触发奖励检查和发放
     */
    @PostMapping("/check-and-issue")
    public ApiResponse<String> checkAndIssueRewards() {
        try {
            rewardService.checkAndIssueRewards();
            return ApiResponse.success("奖励检查和发放任务已启动");
        } catch (Exception e) {
            log.error("手动触发奖励检查失败: {}", e.getMessage());
            return ApiResponse.error("奖励检查失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户账户信息
     */
    @GetMapping("/account")
    public ApiResponse<UserAccountDto.UserAccountInfo> getUserAccount() {
        Integer userId = Integer.valueOf(StpUtil.getLoginId().toString());
        try {
            UserAccount account = rewardService.getUserAccount(userId);
            if (account == null) {
                return ApiResponse.error("用户账户不存在");
            }
            
            // 转换为DTO
            UserAccountDto.UserAccountInfo accountInfo = new UserAccountDto.UserAccountInfo();
            accountInfo.setUserId(account.getUserId());
            accountInfo.setMoney(account.getMoney());
            accountInfo.setHistoryReward(account.getHistoryReward());
            accountInfo.setUtime(account.getUtime());
            
            return ApiResponse.success(accountInfo);
        } catch (Exception e) {
            log.error("获取用户账户失败: {}", e.getMessage());
            return ApiResponse.error("获取用户账户失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户账户日志
     */
    @GetMapping("/account/logs")
    public ApiResponse<List<UserAccountDto.UserAccountLogInfo>> getUserAccountLogs() {
        Integer userId = Integer.valueOf(StpUtil.getLoginId().toString());
        try {
            List<UserAccountLog> logs = rewardService.getUserAccountLogs(userId);
            
            // 转换为DTO列表
            List<UserAccountDto.UserAccountLogInfo> logInfos = logs.stream().map(log -> {
                UserAccountDto.UserAccountLogInfo logInfo = new UserAccountDto.UserAccountLogInfo();
                logInfo.setId(log.getId());
                logInfo.setUserId(log.getUserId());
                logInfo.setTitle(log.getTitle());
                logInfo.setContent(log.getContent());
                logInfo.setMoney(log.getMoney());
                logInfo.setType(log.getType());
                logInfo.setSource(log.getSource());
                logInfo.setCtime(log.getCtime());
                return logInfo;
            }).collect(java.util.stream.Collectors.toList());
            
            return ApiResponse.success(logInfos);
        } catch (Exception e) {
            log.error("获取用户账户日志失败: {}", e.getMessage());
            return ApiResponse.error("获取用户账户日志失败: " + e.getMessage());
        }
    }

    /**
     * 手动为用户增加余额（管理员功能）
     */
    @PostMapping("/add-balance")
    public ApiResponse<Boolean> addUserBalance(
            @RequestParam BigDecimal amount,
            @RequestParam String title,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) String source) {
        Integer userId = Integer.valueOf(StpUtil.getLoginId().toString());
        try {
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                return ApiResponse.error("金额必须大于0");
            }
            
            boolean success = rewardService.addUserBalance(userId, amount, title, content, source);
            if (success) {
                return ApiResponse.success(true);
            } else {
                return ApiResponse.error("增加余额失败");
            }
        } catch (Exception e) {
            log.error("手动增加用户余额失败: {}", e.getMessage());
            return ApiResponse.error("增加余额失败: " + e.getMessage());
        }
    }
}

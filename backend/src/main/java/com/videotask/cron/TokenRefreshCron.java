package com.videotask.cron;

import com.videotask.entity.UserAuth;
import com.videotask.repository.UserAuthRepository;
import com.videotask.service.TokenRefreshService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Token刷新定时任务
 */
@Slf4j
@Component
public class TokenRefreshCron {

    @Autowired
    private TokenRefreshService tokenRefreshService;

    @Autowired
    private UserAuthRepository userAuthRepository;

    /**
     * 定时任务：每小时检查一次即将过期的token并刷新
     * 提前1小时刷新token，确保token不会过期
     */
    @Scheduled(fixedRate = 60 * 60 * 1000L) // 每小时执行一次
    public void refreshExpiringTokens() {
        log.info("=== 开始执行Token刷新定时任务 ===");
        log.info("执行时间: {}", LocalDateTime.now());

        try {
            // 获取所有用户授权记录，然后在内存中过滤即将过期的token
            List<UserAuth> allUserAuths = userAuthRepository.findAll();
            List<UserAuth> expiringTokens = new java.util.ArrayList<>();

            for (UserAuth userAuth : allUserAuths) {
                if (tokenRefreshService.shouldRefreshToken(userAuth)) {
                    expiringTokens.add(userAuth);
                }
            }

            if (expiringTokens.isEmpty()) {
                log.info("✓ 没有发现即将过期的token");
                return;
            }

            log.info("发现 {} 个即将过期的token", expiringTokens.size());

            int refreshCount = 0;
            int errorCount = 0;

            for (UserAuth userAuth : expiringTokens) {
                try {
                    log.info("开始刷新token，用户ID: {}, 平台ID: {}", userAuth.getUserId(), userAuth.getPlatformId());

                    boolean refreshSuccess = tokenRefreshService.refreshToken(userAuth);
                    if (refreshSuccess) {
                        refreshCount++;
                        log.info("✓ 成功刷新token，用户ID: {}", userAuth.getUserId());
                    } else {
                        errorCount++;
                        log.error("✗ 刷新token失败，用户ID: {}", userAuth.getUserId());
                    }
                } catch (Exception e) {
                    errorCount++;
                    log.error("✗ 刷新token异常，用户ID: {}, 错误: {}", userAuth.getUserId(), e.getMessage());
                }
            }

            log.info("=== Token刷新任务完成 ===");
            log.info("成功刷新: {} 个", refreshCount);
            log.info("失败: {} 个", errorCount);
            log.info("总计处理: {} 个", expiringTokens.size());

        } catch (Exception e) {
            log.error("✗ Token刷新任务执行失败: {}", e.getMessage());
        }
    }

    /**
     * 定时任务：每天凌晨3点检查已过期的token
     * 用于监控和统计
     */
    @Scheduled(cron = "0 0 3 * * ?") // 每天凌晨3点执行
    public void checkExpiredTokens() {
        log.info("=== 开始检查已过期的token ===");
        log.info("执行时间: {}", LocalDateTime.now());

        try {
            // 获取所有用户授权记录，然后在内存中过滤已过期的token
            List<UserAuth> allUserAuths = userAuthRepository.findAll();
            List<UserAuth> expiredTokens = new java.util.ArrayList<>();

            for (UserAuth userAuth : allUserAuths) {
                if (tokenRefreshService.isTokenExpired(userAuth)) {
                    expiredTokens.add(userAuth);
                }
            }

            if (expiredTokens.isEmpty()) {
                log.info("✓ 没有发现已过期的token");
                return;
            }

            log.info("发现 {} 个已过期的token", expiredTokens.size());

            for (UserAuth userAuth : expiredTokens) {
                log.info("已过期token - 用户ID: {}, 平台ID: {}, 过期时间: {}",
                        userAuth.getUserId(), userAuth.getPlatformId(),
                        userAuth.getUtime() + userAuth.getExpiresIn());
            }

            log.info("=== 过期token检查完成 ===");

        } catch (Exception e) {
            log.error("✗ 检查过期token失败: {}", e.getMessage());
        }
    }
}

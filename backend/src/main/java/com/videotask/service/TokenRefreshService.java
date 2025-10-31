package com.videotask.service;

import cn.hutool.core.date.DateUtil;
import com.videotask.entity.UserAuth;
import com.videotask.repository.UserAuthRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Token刷新定时任务服务
 * 用于自动刷新即将过期的access_token
 */
@Service
public class TokenRefreshService {

    @Autowired
    private UserAuthRepository userAuthRepository;

    @Autowired
    private KuaishouSdkService kuaishouSdkService;

    @Autowired
    private DouyinAuthService douyinAuthService;

    /**
     * 每小时检查一次即将过期的token并刷新
     * 提前1小时刷新token，确保token不会过期
     */
    public void refreshExpiringTokens() {
        System.out.println("=== 开始执行Token刷新定时任务 ===");
        System.out.println("执行时间: " + LocalDateTime.now());

        try {
            // 获取所有用户授权记录，然后在内存中过滤即将过期的token
            List<UserAuth> allUserAuths = userAuthRepository.findAll();
            List<UserAuth> expiringTokens = new java.util.ArrayList<>();

            for (UserAuth userAuth : allUserAuths) {
                if (shouldRefreshToken(userAuth)) {
                    expiringTokens.add(userAuth);
                }
            }

            if (expiringTokens.isEmpty()) {
                System.out.println("✓ 没有发现即将过期的token");
                return;
            }

            System.out.println("发现 " + expiringTokens.size() + " 个即将过期的token");

            int refreshCount = 0;
            int errorCount = 0;

            for (UserAuth userAuth : expiringTokens) {
                try {
                    System.out.println("开始刷新token，用户ID: " + userAuth.getUserId() +
                            ", 平台ID: " + userAuth.getPlatformId());

                    boolean refreshSuccess = refreshToken(userAuth);
                    if (refreshSuccess) {
                        refreshCount++;
                        System.out.println("✓ 成功刷新token，用户ID: " + userAuth.getUserId());
                    } else {
                        errorCount++;
                        System.err.println("✗ 刷新token失败，用户ID: " + userAuth.getUserId());
                    }
                } catch (Exception e) {
                    errorCount++;
                    System.err.println("✗ 刷新token异常，用户ID: " + userAuth.getUserId() +
                            ", 错误: " + e.getMessage());
                }
            }

            System.out.println("=== Token刷新任务完成 ===");
            System.out.println("成功刷新: " + refreshCount + " 个");
            System.out.println("失败: " + errorCount + " 个");
            System.out.println("总计处理: " + expiringTokens.size() + " 个");

        } catch (Exception e) {
            System.err.println("✗ Token刷新任务执行失败: " + e.getMessage());
        }
    }

    /**
     * 每天凌晨3点检查已过期的token
     * 用于监控和统计
     */
    public void checkExpiredTokens() {
        System.out.println("=== 开始检查已过期的token ===");
        System.out.println("执行时间: " + LocalDateTime.now());

        try {
            // 获取所有用户授权记录，然后在内存中过滤已过期的token
            List<UserAuth> allUserAuths = userAuthRepository.findAll();
            List<UserAuth> expiredTokens = new java.util.ArrayList<>();

            for (UserAuth userAuth : allUserAuths) {
                if (isTokenExpired(userAuth)) {
                    expiredTokens.add(userAuth);
                }
            }

            if (expiredTokens.isEmpty()) {
                System.out.println("✓ 没有发现已过期的token");
                return;
            }

            System.out.println("发现 " + expiredTokens.size() + " 个已过期的token");

            for (UserAuth userAuth : expiredTokens) {
                System.out.println("已过期token - 用户ID: " + userAuth.getUserId() +
                        ", 平台ID: " + userAuth.getPlatformId() +
                        ", 过期时间: " + userAuth.getUtime() + userAuth.getExpiresIn());
            }

            System.out.println("=== 过期token检查完成 ===");

        } catch (Exception e) {
            System.err.println("✗ 检查过期token失败: " + e.getMessage());
        }
    }

    /**
     * 判断是否需要刷新token
     * 提前10分钟刷新，确保token不会过期
     */
    public boolean shouldRefreshToken(UserAuth userAuth) {
        if (userAuth == null || userAuth.getAccessToken() == null ||
                userAuth.getExpiresIn() == null || userAuth.getUtime() == null) {
            return false;
        }
        // 计算token过期时间
        Long expireTime = userAuth.getUtime() + userAuth.getExpiresIn();
        Long refreshTime = expireTime - 60L * 60L;
        Long now = DateUtil.currentSeconds();
        return now > refreshTime;
    }

    /**
     * 判断token是否已过期
     */
    public boolean isTokenExpired(UserAuth userAuth) {
        if (userAuth == null || userAuth.getAccessToken() == null ||
                userAuth.getExpiresIn() == null || userAuth.getUtime() == null) {
            return false;
        }

        // 计算token过期时间
        Long expireTime = userAuth.getUtime() + userAuth.getExpiresIn();
        Long now = DateUtil.currentSeconds();

        return now > expireTime;
    }

    /**
     * 刷新单个用户的token
     */
    public boolean refreshToken(UserAuth userAuth) {
        try {
            // 根据平台类型调用不同的刷新方法
            if (userAuth.getPlatformId() == 2) { // 快手平台
                return refreshKuaishouToken(userAuth);
            } else if (userAuth.getPlatformId() == 1) { // 抖音平台
                return refreshDouyinToken(userAuth);
            }
            // 可以在这里添加其他平台的刷新逻辑

            return false;
        } catch (Exception e) {
            System.err.println("刷新token失败，用户ID: " + userAuth.getUserId() +
                    ", 平台ID: " + userAuth.getPlatformId() +
                    ", 错误: " + e.getMessage());
            return false;
        }
    }

    /**
     * 刷新快手平台的token
     */
    private boolean refreshKuaishouToken(UserAuth userAuth) {
        try {
            if (userAuth.getRefreshToken() == null) {
                System.err.println("缺少刷新令牌，用户ID: " + userAuth.getUserId());
                return false;
            }

            // 调用快手SDK服务刷新token
            com.videotask.dto.ApiResponse<com.videotask.entity.UserAuth> response = kuaishouSdkService.refreshAccessToken(userAuth.getUserId());

            if (response.getCode() == 10000) {
                System.out.println("快手token刷新成功，用户ID: " + userAuth.getUserId());
                return true;
            } else {
                System.err.println("快手token刷新失败，用户ID: " + userAuth.getUserId() +
                        ", 错误: " + response.getMessage());
                return false;
            }
        } catch (Exception e) {
            System.err.println("快手token刷新异常，用户ID: " + userAuth.getUserId() +
                    ", 错误: " + e.getMessage());
            return false;
        }
    }

    /**
     * 刷新抖音平台的token
     */
    private boolean refreshDouyinToken(UserAuth userAuth) {
        try {
            if (userAuth.getRefreshToken() == null) {
                System.err.println("缺少刷新令牌，用户ID: " + userAuth.getUserId());
                return false;
            }

            // 调用抖音SDK服务刷新token
            com.videotask.dto.ApiResponse<com.videotask.entity.UserAuth> response = douyinAuthService.refreshAccessToken(userAuth.getUserId());

            if (response.getCode() == 10000) {
                System.out.println("抖音token刷新成功，用户ID: " + userAuth.getUserId());
                return true;
            } else {
                System.err.println("抖音token刷新失败，用户ID: " + userAuth.getUserId() +
                        ", 错误: " + response.getMessage());
                return false;
            }
        } catch (Exception e) {
            System.err.println("抖音token刷新异常，用户ID: " + userAuth.getUserId() +
                    ", 错误: " + e.getMessage());
            return false;
        }
    }

    /**
     * 手动刷新指定用户的token
     */
    public boolean refreshUserToken(Integer userId, Integer platformId) {
        try {
            java.util.Optional<com.videotask.entity.UserAuth> userAuthOpt = userAuthRepository.findByUserIdAndPlatformId(userId, platformId);
            if (!userAuthOpt.isPresent()) {
                System.err.println("用户未授权该平台，用户ID: " + userId + ", 平台ID: " + platformId);
                return false;
            }

            UserAuth userAuth = userAuthOpt.get();
            return refreshToken(userAuth);
        } catch (Exception e) {
            System.err.println("手动刷新token失败，用户ID: " + userId +
                    ", 平台ID: " + platformId + ", 错误: " + e.getMessage());
            return false;
        }
    }

    /**
     * 检查token是否即将过期（用于外部调用）
     */
    public boolean isTokenExpiringSoon(Integer userId, Integer platformId) {
        try {
            java.util.Optional<com.videotask.entity.UserAuth> userAuthOpt = userAuthRepository.findByUserIdAndPlatformId(userId, platformId);
            if (!userAuthOpt.isPresent()) {
                return false;
            }

            return shouldRefreshToken(userAuthOpt.get());
        } catch (Exception e) {
            System.err.println("检查token过期状态失败，用户ID: " + userId +
                    ", 平台ID: " + platformId + ", 错误: " + e.getMessage());
            return false;
        }
    }
}

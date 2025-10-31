package com.videotask.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.videotask.dto.ApiResponse;
import com.videotask.service.TokenRefreshService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Token刷新控制器
 * 提供手动刷新token的API接口
 */
@SaCheckLogin
@RestController
@RequestMapping("/api/token-refresh")
@CrossOrigin(origins = "*")
public class TokenRefreshController {

    @Autowired
    private TokenRefreshService tokenRefreshService;

    /**
     * 手动刷新指定用户的token
     */
    @PostMapping("/refresh")
    public ApiResponse<Boolean> refreshUserToken(
            @RequestParam Integer userId,
            @RequestParam Integer platformId) {
        try {
            boolean success = tokenRefreshService.refreshUserToken(userId, platformId);
            if (success) {
                return ApiResponse.success(true);
            } else {
                return ApiResponse.error("Token刷新失败");
            }
        } catch (Exception e) {
            return ApiResponse.error("Token刷新异常: " + e.getMessage());
        }
    }

    /**
     * 检查指定用户的token是否即将过期
     */
    @GetMapping("/check")
    public ApiResponse<Boolean> checkTokenExpiring(
            @RequestParam Integer userId,
            @RequestParam Integer platformId) {
        try {
            boolean isExpiring = tokenRefreshService.isTokenExpiringSoon(userId, platformId);
            return ApiResponse.success(isExpiring);
        } catch (Exception e) {
            return ApiResponse.error("检查Token状态异常: " + e.getMessage());
        }
    }

    /**
     * 手动触发定时任务刷新所有即将过期的token
     */
    @PostMapping("/refresh-all")
    public ApiResponse<String> refreshAllExpiringTokens() {
        try {
            // 直接调用定时任务方法
            tokenRefreshService.refreshExpiringTokens();
            return ApiResponse.success("已触发所有token刷新任务");
        } catch (Exception e) {
            return ApiResponse.error("触发token刷新任务失败: " + e.getMessage());
        }
    }
}

package com.videotask.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.videotask.dto.ApiResponse;
import com.videotask.entity.UserAuth;
import com.videotask.service.KuaishouSdkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 快手授权控制器
 */
@SaCheckLogin
@RestController
@RequestMapping("/api/kuaishou/auth")
public class KuaishouAuthController {

    @Autowired
    private KuaishouSdkService kuaishouSdkService;

    /**
     * 使用授权码换取access_token
     */
    @PostMapping("/token")
    public ApiResponse<UserAuth> exchangeAccessToken(
            @RequestParam Integer userId,
            @RequestParam String code,
            @RequestParam(required = false) String state) {
        return kuaishouSdkService.exchangeAccessToken(userId, code, state);
    }

    /**
     * 刷新access_token
     */
    @PostMapping("/refresh")
    public ApiResponse<UserAuth> refreshAccessToken(@RequestParam Integer userId) {
        return kuaishouSdkService.refreshAccessToken(userId);
    }
}

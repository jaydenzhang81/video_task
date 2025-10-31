package com.videotask.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.videotask.dto.ApiResponse;
import com.videotask.dto.PlatformDto;
import com.videotask.entity.UserAuth;
import com.videotask.service.KuaishouSdkService;
import com.videotask.service.DouyinAuthService;
import com.videotask.service.PlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@SaCheckLogin
@RestController
@RequestMapping("/api/platform")
@CrossOrigin(origins = "*")
public class PlatformController {

    @Autowired
    private PlatformService platformService;

    @Autowired
    private KuaishouSdkService kuaishouSdkService;

    @Autowired
    private DouyinAuthService douyinAuthService;

    @GetMapping("/list")
    public ApiResponse<List<PlatformDto.PlatformInfo>> getPlatformList(HttpServletRequest request) {
        Integer userId = Integer.valueOf(StpUtil.getLoginId().toString());
        return platformService.getPlatformList(userId);
    }

    @PostMapping("/auth")
    public ApiResponse<Void> savePlatformAuth(@RequestBody PlatformDto.PlatformAuthRequest request, HttpServletRequest httpRequest) {
        Integer userId = Integer.valueOf(StpUtil.getLoginId().toString());
        return platformService.savePlatformAuth(userId, request);
    }

    @PostMapping("/kuaishou/auth")
    public ApiResponse<UserAuth> kuaishouAuth(@RequestBody PlatformDto.KuaishouAuthRequest request, HttpServletRequest httpRequest) {
        Integer userId = Integer.valueOf(StpUtil.getLoginId().toString());
        return kuaishouSdkService.exchangeAccessToken(userId, request.getAuthCode(), request.getState());
    }

    @PostMapping("/kuaishou/refresh")
    public ApiResponse<UserAuth> refreshKuaishouToken(HttpServletRequest httpRequest) {
        Integer userId = Integer.valueOf(StpUtil.getLoginId().toString());

        return kuaishouSdkService.refreshAccessToken(userId);
    }

    @PostMapping("/douyin/auth")
    public ApiResponse<UserAuth> douyinAuth(@RequestBody PlatformDto.DouyinAuthRequest request, HttpServletRequest httpRequest) {
        Integer userId = Integer.valueOf(StpUtil.getLoginId().toString());
        return douyinAuthService.exchangeAccessToken(userId, request.getAuthCode(), request.getState());
    }

    @PostMapping("/douyin/refresh")
    public ApiResponse<UserAuth> refreshDouyinToken(HttpServletRequest httpRequest) {
        Integer userId = Integer.valueOf(StpUtil.getLoginId().toString());
        return douyinAuthService.refreshAccessToken(userId);
    }
} 
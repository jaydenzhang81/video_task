package com.videotask.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.videotask.dto.ApiResponse;
import com.videotask.service.VideoDataSyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@SaCheckLogin
@RestController
@RequestMapping("/api/video-sync")
@CrossOrigin(origins = "*")
public class VideoDataSyncController {

    @Autowired
    private VideoDataSyncService videoDataSyncService;

    /**
     * 手动同步指定视频的数据
     */
    @PostMapping("/sync-by-photo-id")
    public ApiResponse<Void> syncVideoDataByPhotoId(@RequestParam String photoId) {
        try {
            videoDataSyncService.syncVideoDataByPhotoId(photoId);
            return ApiResponse.success();
        } catch (Exception e) {
            return ApiResponse.error("同步失败: " + e.getMessage());
        }
    }

    /**
     * 手动同步指定用户的所有视频数据
     */
    @PostMapping("/sync-by-user-id")
    public ApiResponse<Void> syncUserVideoData() {
        Integer userId = Integer.valueOf(StpUtil.getLoginId().toString());
        try {
            videoDataSyncService.syncUserVideoData(userId);
            return ApiResponse.success();
        } catch (Exception e) {
            return ApiResponse.error("同步失败: " + e.getMessage());
        }
    }

    /**
     * 手动触发快手平台数据同步
     */
    @PostMapping("/sync-kuaishou")
    public ApiResponse<Void> syncKuaishouVideoData() {
        try {
            videoDataSyncService.syncKuaishouVideoData();
            return ApiResponse.success();
        } catch (Exception e) {
            return ApiResponse.error("同步失败: " + e.getMessage());
        }
    }
    

    
}

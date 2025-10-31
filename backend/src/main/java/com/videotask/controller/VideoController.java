package com.videotask.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.videotask.dto.ApiResponse;
import com.videotask.dto.VideoDto;
import com.videotask.service.VideoService;
import com.videotask.cron.CronTaskManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@SaCheckLogin
@RestController
@RequestMapping("/api/video")
@CrossOrigin(origins = "*")
public class VideoController {

    @Autowired
    private VideoService videoService;

    @Autowired
    private CronTaskManager cronTaskManager;


    @GetMapping("/types")
    public ApiResponse<List<VideoDto.VideoTypeInfo>> getVideoTypes() {
        return videoService.getVideoTypes();
    }

    @GetMapping("/list")
    public ApiResponse<List<VideoDto.VideoInfo>> getVideoList(
            @RequestParam(required = false) Integer platformId,
            @RequestParam(defaultValue = "2") Integer num,
            @RequestParam(required = false) Integer videoType,
            HttpServletRequest request) {
        Integer userId = Integer.valueOf(StpUtil.getLoginId().toString());

        VideoDto.VideoListRequest listRequest = new VideoDto.VideoListRequest();
        listRequest.setPlatformId(platformId);
        listRequest.setNum(num);
        listRequest.setVideoType(videoType);

        return videoService.getVideoList(userId, listRequest);
    }

    @PostMapping("/share")
    public ApiResponse<Void> shareVideo(@RequestBody VideoDto.VideoShareRequest request, HttpServletRequest httpRequest) {
        Integer userId = Integer.valueOf(StpUtil.getLoginId().toString());
        return videoService.shareVideo(userId, request);
    }

    @GetMapping("/detail/{videoId}")
    public ApiResponse<VideoDto.VideoInfo> getVideoDetail(@PathVariable Integer videoId, HttpServletRequest request) {
        Integer userId = Integer.valueOf(StpUtil.getLoginId().toString());

        return videoService.getVideoDetail(videoId, userId);
    }

    @GetMapping("/stats")
    public ApiResponse<VideoDto.VideoStatsResponse> getVideoStats(
            @RequestParam(required = false) String timeFilter,
            @RequestParam(required = false) Integer platformId,
            HttpServletRequest request) {

        
        Integer userId = Integer.valueOf(StpUtil.getLoginId().toString());

        VideoDto.VideoStatsRequest statsRequest = new VideoDto.VideoStatsRequest();
        statsRequest.setTimeFilter(timeFilter);
        statsRequest.setPlatformId(platformId);

        return videoService.getVideoStats(userId, statsRequest);
    }

    @GetMapping("/publish-list")
    public ApiResponse<VideoDto.VideoPublishListResponse> getPublishList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String timeFilter,
            @RequestParam(required = false) Integer platformId,
            HttpServletRequest request) {

        
        Integer userId = Integer.valueOf(StpUtil.getLoginId().toString());

        VideoDto.VideoPublishListRequest listRequest = new VideoDto.VideoPublishListRequest();
        listRequest.setPage(page);
        listRequest.setSize(size);
        listRequest.setTimeFilter(timeFilter);
        listRequest.setPlatformId(platformId);

        return videoService.getPublishList(userId, listRequest);
    }

    @GetMapping("/chart-data")
    public ApiResponse<VideoDto.VideoChartDataResponse> getChartData(
            @RequestParam(required = false) String timeFilter,
            @RequestParam(required = false) Integer platformId,
            @RequestParam(defaultValue = "line") String type,
            HttpServletRequest request) {

        
        Integer userId = Integer.valueOf(StpUtil.getLoginId().toString());

        VideoDto.VideoChartDataRequest chartRequest = new VideoDto.VideoChartDataRequest();
        chartRequest.setTimeFilter(timeFilter);
        chartRequest.setPlatformId(platformId);
        chartRequest.setType(type);

        return videoService.getChartData(userId, chartRequest);
    }

    @GetMapping("/share-stats")
    public ApiResponse<VideoDto.VideoShareStatsResponse> getShareStats(
            @RequestParam(required = false) Integer platformId,
            HttpServletRequest request) {

        
        Integer userId = Integer.valueOf(StpUtil.getLoginId().toString());

        return videoService.getShareStats(userId, platformId);
    }

    @PostMapping("/trigger-upload")
    public ApiResponse<Void> triggerVideoUpload() {
        try {
            // 手动触发视频上传定时任务
            cronTaskManager.triggerVideoUpload();
            return ApiResponse.success(null);
        } catch (Exception e) {
            return ApiResponse.error("触发视频上传任务失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查用户在指定平台的视频发布数量是否已达上限
     * @param request 请求参数，包含平台ID
     * @return 检查结果，包含是否可以发布、每日上限、当前发布数量和剩余可发布数量
     */
    @PostMapping("/check-publish-limit")
    public ApiResponse<VideoDto.CheckPublishLimitResponse> checkPublishLimit(@RequestBody VideoDto.CheckPublishLimitRequest request) {
        Integer userId = Integer.valueOf(StpUtil.getLoginId().toString());
        return videoService.checkPublishLimit(userId, request);
    }
}
package com.videotask.service;

/**
 * 视频上传定时任务服务
 */
public interface VideoUploadSchedulerService {
    
    /**
     * 处理待上传的视频
     * 查找状态为0的视频，一条一条上传
     */
    void processPendingVideos();
    
    /**
     * 上传单个视频
     * @param shareLogId 分享记录ID
     */
    void uploadSingleVideo(Integer shareLogId);
}

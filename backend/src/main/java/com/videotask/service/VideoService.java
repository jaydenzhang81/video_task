package com.videotask.service;

import com.videotask.dto.ApiResponse;
import com.videotask.dto.VideoDto;

import java.util.List;

public interface VideoService {
    ApiResponse<List<VideoDto.VideoTypeInfo>> getVideoTypes();
    ApiResponse<List<VideoDto.VideoInfo>> getVideoList(Integer userId, VideoDto.VideoListRequest request);
    ApiResponse<Void> shareVideo(Integer userId, VideoDto.VideoShareRequest request);
    ApiResponse<VideoDto.VideoInfo> getVideoDetail(Integer videoId, Integer userId);
    ApiResponse<VideoDto.VideoStatsResponse> getVideoStats(Integer userId, VideoDto.VideoStatsRequest request);
    ApiResponse<VideoDto.VideoPublishListResponse> getPublishList(Integer userId, VideoDto.VideoPublishListRequest request);
    ApiResponse<VideoDto.VideoChartDataResponse> getChartData(Integer userId, VideoDto.VideoChartDataRequest request);
    ApiResponse<VideoDto.VideoShareStatsResponse> getShareStats(Integer userId, Integer platformId);
    ApiResponse<VideoDto.CheckPublishLimitResponse> checkPublishLimit(Integer userId, VideoDto.CheckPublishLimitRequest request);

}
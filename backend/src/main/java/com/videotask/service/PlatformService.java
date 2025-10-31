package com.videotask.service;

import com.videotask.dto.ApiResponse;
import com.videotask.dto.PlatformDto;

import java.util.List;

public interface PlatformService {
    ApiResponse<List<PlatformDto.PlatformInfo>> getPlatformList(Integer userId);
    ApiResponse<Void> savePlatformAuth(Integer userId, PlatformDto.PlatformAuthRequest request);
} 
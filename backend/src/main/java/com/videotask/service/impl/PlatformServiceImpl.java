package com.videotask.service.impl;

import com.videotask.dto.ApiResponse;
import com.videotask.dto.PlatformDto;
import com.videotask.entity.UserAuth;
import com.videotask.entity.VideoPlatform;
import com.videotask.repository.UserAuthRepository;
import com.videotask.repository.VideoPlatformRepository;
import com.videotask.service.PlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PlatformServiceImpl implements PlatformService {
    
    @Autowired
    private VideoPlatformRepository platformRepository;
    
    @Autowired
    private UserAuthRepository userAuthRepository;
    
    @Override
    public ApiResponse<List<PlatformDto.PlatformInfo>> getPlatformList(Integer userId) {
        // 获取所有启用的平台
        List<VideoPlatform> platforms = platformRepository.findByStatusNot(-1);

        // 获取用户已授权的平台
        List<UserAuth> userAuths = userAuthRepository.findByUserId(userId);
        
        // 构建响应
        List<PlatformDto.PlatformInfo> platformInfos = platforms.stream().map(platform -> {
            PlatformDto.PlatformInfo info = new PlatformDto.PlatformInfo();
            info.setId(platform.getId());
            info.setName(platform.getName());
            info.setImageUrl(platform.getImageUrl());
            
            // 检查用户是否已授权该平台
            boolean isAuth = userAuths.stream()
                    .anyMatch(auth -> auth.getPlatformId().equals(platform.getId()));
            info.setIsAuth(isAuth);
            
            return info;
        }).collect(Collectors.toList());
        
        return ApiResponse.success(platformInfos);
    }
    
    @Override
    public ApiResponse<Void> savePlatformAuth(Integer userId, PlatformDto.PlatformAuthRequest request) {
        try {
            // 检查是否已存在授权记录
            Optional<UserAuth> existingAuth = userAuthRepository
                    .findByUserIdAndPlatformId(userId, request.getPlatformId());
            
            UserAuth userAuth;
            if (existingAuth.isPresent()) {
                // 更新现有授权
                userAuth = existingAuth.get();
            } else {
                // 创建新授权
                userAuth = new UserAuth();
                userAuth.setUserId(userId);
                userAuth.setPlatformId(request.getPlatformId());
            }
            
            // 更新授权信息
            userAuth.setAccessToken(request.getAuthCode()); // 暂时使用authCode作为accessToken
            userAuth.setOpenId(request.getState()); // 暂时使用state作为openId
            
            userAuthRepository.save(userAuth);
            
            return ApiResponse.success();
        } catch (Exception e) {
            return ApiResponse.error("保存授权信息失败: " + e.getMessage());
        }
    }
} 
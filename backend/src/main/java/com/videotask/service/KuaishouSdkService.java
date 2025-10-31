package com.videotask.service;

import cn.hutool.core.date.DateUtil;
import com.github.kwai.open.KwaiOpenException;
import com.github.kwai.open.api.KwaiOpenOauthApi;
import com.github.kwai.open.api.KwaiOpenVideoApi;
import com.github.kwai.open.request.*;
import com.github.kwai.open.response.*;
import com.videotask.config.KuaishouConfig;
import com.videotask.dto.ApiResponse;
import com.videotask.entity.UserAuth;
import com.videotask.repository.UserAuthRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class KuaishouSdkService {

    @Autowired
    private KuaishouConfig kuaishouConfig;

    @Autowired
    private UserAuthRepository userAuthRepository;

    private KwaiOpenOauthApi kwaiOpenOauthApi;
    private KwaiOpenVideoApi kwaiOpenVideoApi;

    private KwaiOpenOauthApi getKwaiOpenOauthApi() {
        if (kwaiOpenOauthApi == null) {
            kwaiOpenOauthApi = KwaiOpenOauthApi.init(kuaishouConfig.getAppId());
        }
        return kwaiOpenOauthApi;
    }

    private KwaiOpenVideoApi getKwaiOpenVideoApi() {
        if (kwaiOpenVideoApi == null) {
            kwaiOpenVideoApi = KwaiOpenVideoApi.init(kuaishouConfig.getAppId());
        }
        return kwaiOpenVideoApi;
    }

    public ApiResponse<UserAuth> exchangeAccessToken(Integer userId, String authCode, String state) {
        try {
            AccessTokenRequest request = new AccessTokenRequest(authCode, kuaishouConfig.getAppSecret());
            AccessTokenResponse response = getKwaiOpenOauthApi().getAccessToken(request);
            UserAuth userAuth = saveOrUpdateUserAuth(userId, response, state);
            return ApiResponse.success(userAuth);
        } catch (KwaiOpenException e) {
            return ApiResponse.error("快手授权失败: " + e.getMessage());
        }
    }

    public ApiResponse<UserAuth> refreshAccessToken(Integer userId) {
        try {
            Optional<UserAuth> userAuthOpt = userAuthRepository.findByUserIdAndPlatformId(userId, 2);
            if (!userAuthOpt.isPresent()) {
                return ApiResponse.error("用户未授权快手平台");
            }

            UserAuth userAuth = userAuthOpt.get();
            if (userAuth.getRefreshToken() == null) {
                return ApiResponse.error("缺少刷新令牌");
            }

            RefreshTokenRequest request = new RefreshTokenRequest(userAuth.getRefreshToken(), kuaishouConfig.getAppSecret());
            RefreshTokenResponse response = getKwaiOpenOauthApi().refreshToken(request);

            userAuth.setAccessToken(response.getAccessToken());
            userAuth.setRefreshToken(response.getRefreshToken());
            userAuth.setExpiresIn(response.getExpiresIn().intValue());
            userAuth.setRefreshTokenExpiresIn(response.getRefreshTokenExpiresIn().intValue());
            userAuth.setUtime(DateUtil.currentSeconds());

            userAuthRepository.save(userAuth);
            return ApiResponse.success(userAuth);
        } catch (KwaiOpenException e) {
            return ApiResponse.error("刷新令牌失败: " + e.getMessage());
        }
    }

    public boolean isAccessTokenValid(UserAuth userAuth) {
        if (userAuth == null || userAuth.getAccessToken() == null) {
            return false;
        }
        if (userAuth.getExpiresIn() != null && userAuth.getUtime() != null) {
            Long expireTime = userAuth.getUtime() + userAuth.getExpiresIn();
            return DateUtil.currentSeconds() < expireTime;
        }
        return false;
    }

    public Map<String, Object> publishVideoComplete(String accessToken, MultipartFile videoFile,
                                                    String title, String description, String[] tags) {
        try {
            // 1. 开始上传
            StartUploadResponse startUploadResponse = startVideoUpload(accessToken, videoFile);
            if (startUploadResponse == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("error", "开始上传失败");
                return result;
            }

            // 2. 上传视频文件
            Map<String, Object> uploadResult = uploadVideoFile(startUploadResponse, videoFile);
            if (!(Boolean) uploadResult.get("success")) {
                return uploadResult;
            }

            // 3. 发布视频
            Map<String, Object> publishResult = publishVideo(accessToken, startUploadResponse.getUploadToken(), title, description, tags);
            return publishResult;

        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", "视频发布流程失败: " + e.getMessage());
            return result;
        }
    }

    private StartUploadResponse startVideoUpload(String accessToken, MultipartFile videoFile) {
        try {
            // 根据官方文档，简化实现，直接调用startUpload
            StartUploadRequest request = new StartUploadRequest(accessToken);

            StartUploadResponse response = getKwaiOpenVideoApi().startUpload(request);
            return response;
        } catch (KwaiOpenException e) {
            System.err.println("开始上传失败: " + e.getMessage());
            return null;
        }
    }

    private Map<String, Object> uploadVideoFile(StartUploadResponse startUploadResponse, MultipartFile videoFile) {
        try {
            // 根据错误信息，使用正确的构造函数
            // UploadFileRequest(StartUploadResponse, byte[])
            UploadFileRequest uploadFileRequest = new UploadFileRequest(startUploadResponse, videoFile.getBytes());

            UploadFileResponse response = getKwaiOpenVideoApi().uploadFile(uploadFileRequest);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "视频上传成功");
            return result;
        } catch (KwaiOpenException e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", "视频上传失败: " + e.getMessage());
            return result;
        } catch (IOException e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", "文件读取失败: " + e.getMessage());
            return result;
        }
    }

    private Map<String, Object> publishVideo(String accessToken, String uploadToken, String title,
                                             String description, String[] tags) {
        try {
            VideoPublishRequest request = new VideoPublishRequest(accessToken);
            request.setCaption(title);
            request.setUploadToken(uploadToken);

            if (description != null && !description.trim().isEmpty()) {
                String fullTitle = title + " - " + description;
                request.setCaption(fullTitle);
            }

            VideoPublishResponse response = getKwaiOpenVideoApi().videoPublish(request);

            Map<String, Object> result = new HashMap<>();
            result.put("photo_id", response.getVideoInfo().getPhotoId());
            result.put("success", true);
            result.put("message", "视频发布成功");
            return result;
        } catch (KwaiOpenException e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", "视频发布失败: " + e.getMessage());
            return result;
        }
    }

    private UserAuth saveOrUpdateUserAuth(Integer userId, AccessTokenResponse response, String state) {
        Optional<UserAuth> existingAuth = userAuthRepository.findByUserIdAndPlatformId(userId, 2);

        UserAuth userAuth;
        if (existingAuth.isPresent()) {
            userAuth = existingAuth.get();
        } else {
            userAuth = new UserAuth();
            userAuth.setUserId(userId);
            userAuth.setPlatformId(2);
        }

        userAuth.setAccessToken(response.getAccessToken());
        userAuth.setRefreshToken(response.getRefreshToken());
        userAuth.setExpiresIn(response.getExpiresIn().intValue());
        userAuth.setRefreshTokenExpiresIn(response.getRefreshTokenExpiresIn().intValue());
        userAuth.setOpenId(response.getOpenId());
        userAuth.setUtime(DateUtil.currentSeconds());
        userAuth.setState(state);

        return userAuthRepository.save(userAuth);
    }

    public CreateVideoResponse createVideo(String accessToken, String title, byte[] videoFileData, byte[] coverFileData) {
        CreateVideoRequest request = new CreateVideoRequest(accessToken);
        request.setCaption(title);
        request.setCover(coverFileData);
        request.setVideoFileData(videoFileData);
        request.setMerchantProductId(25125127807337L);
        try {
            CreateVideoResponse response = getKwaiOpenVideoApi().createVideo(request);
            return response;
        } catch (KwaiOpenException e) {
            e.printStackTrace();
            return null;
        }
    }

    public VideoInfoResponse queryVideoByPhotoId(String accessToken, String photoId) {
        try {
            VideoInfoRequest request = new VideoInfoRequest(accessToken, photoId);
            VideoInfoResponse response = getKwaiOpenVideoApi().queryVideoInfo(request);
            return response;
        } catch (KwaiOpenException e) {
            System.err.println("查询视频信息失败: " + e.getMessage());
            return null;
        }
    }
}

package com.videotask.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.videotask.config.DouyinConfig;
import com.videotask.dto.ApiResponse;
import com.videotask.entity.UserAuth;
import com.videotask.repository.UserAuthRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class DouyinAuthService {

    @Autowired
    private DouyinConfig douyinConfig;

    @Autowired
    private UserAuthRepository userAuthRepository;

    private static final String DOUYIN_ACCESS_TOKEN_URL = "https://open.douyin.com/oauth/access_token/";

    /**
     * 通过抖音 authCode 换取并保存 access_token
     * 参考文档：https://developer.open-douyin.com/docs/resource/zh-CN/dop/develop/openapi/account-permission/get-access-token
     */
    public ApiResponse<UserAuth> exchangeAccessToken(Integer userId, String authCode, String state) {
        try {
            // 调用抖音开放平台API获取access_token
            Map<String, Object> tokenResponse = getDouyinAccessToken(authCode);
            
            if (tokenResponse == null || !tokenResponse.containsKey("access_token")) {
                return ApiResponse.error("获取抖音access_token失败");
            }

            Optional<UserAuth> existingAuth = userAuthRepository.findByUserIdAndPlatformId(userId, 1);

            UserAuth userAuth;
            if (existingAuth.isPresent()) {
                userAuth = existingAuth.get();
            } else {
                userAuth = new UserAuth();
                userAuth.setUserId(userId);
                userAuth.setPlatformId(1);
            }

            // 保存抖音返回的token信息
            userAuth.setAccessToken((String) tokenResponse.get("access_token"));
            userAuth.setRefreshToken((String) tokenResponse.get("refresh_token"));
            userAuth.setExpiresIn((Integer) tokenResponse.get("expires_in"));
            userAuth.setRefreshTokenExpiresIn((Integer) tokenResponse.get("refresh_expires_in"));
            userAuth.setOpenId((String) tokenResponse.get("open_id"));
            userAuth.setState(state);
            userAuth.setUtime(DateUtil.currentSeconds());

            userAuthRepository.save(userAuth);
            return ApiResponse.success(userAuth);
        } catch (Exception e) {
            return ApiResponse.error("抖音授权失败: " + e.getMessage());
        }
    }

    /**
     * 刷新抖音access_token
     * 参考文档：https://developer.open-douyin.com/docs/resource/zh-CN/dop/develop/openapi/account-permission/refresh-access-token
     */
    public ApiResponse<UserAuth> refreshAccessToken(Integer userId) {
        try {
            Optional<UserAuth> userAuthOpt = userAuthRepository.findByUserIdAndPlatformId(userId, 1);
            if (!userAuthOpt.isPresent()) {
                return ApiResponse.error("用户未授权抖音平台");
            }

            UserAuth userAuth = userAuthOpt.get();
            if (userAuth.getRefreshToken() == null) {
                return ApiResponse.error("缺少刷新令牌");
            }

            // 调用抖音开放平台API刷新access_token
            Map<String, Object> tokenResponse = refreshDouyinAccessToken(userAuth.getRefreshToken());
            
            if (tokenResponse == null || !tokenResponse.containsKey("access_token")) {
                return ApiResponse.error("刷新抖音access_token失败");
            }

            // 更新token信息
            userAuth.setAccessToken((String) tokenResponse.get("access_token"));
            userAuth.setRefreshToken((String) tokenResponse.get("refresh_token"));
            userAuth.setExpiresIn((Integer) tokenResponse.get("expires_in"));
            userAuth.setRefreshTokenExpiresIn((Integer) tokenResponse.get("refresh_expires_in"));
            userAuth.setUtime(DateUtil.currentSeconds());

            userAuthRepository.save(userAuth);
            return ApiResponse.success(userAuth);
        } catch (Exception e) {
            return ApiResponse.error("刷新抖音token失败: " + e.getMessage());
        }
    }

    /**
     * 调用抖音开放平台API获取access_token
     * 
     * @param authCode 用户授权码
     * @return 包含token信息的Map，失败返回null
     */
    private Map<String, Object> getDouyinAccessToken(String authCode) {
        try {
            // 构建请求参数
            Map<String, Object> params = new HashMap<>();
            params.put("client_key", douyinConfig.getClientKey());
            params.put("client_secret", douyinConfig.getClientSecret());
            params.put("code", authCode);
            params.put("grant_type", "authorization_code");

            // 发送POST请求到抖音开放平台
            HttpResponse response = HttpRequest.post(DOUYIN_ACCESS_TOKEN_URL)
                    .header("content-type", "application/x-www-form-urlencoded")
                    .form(params)
                    .timeout(10000) // 10秒超时
                    .execute();

            if (response.isOk()) {
                String responseBody = response.body();
                JSONObject jsonResponse = JSONUtil.parseObj(responseBody);
                
                // 检查响应状态
                if (jsonResponse.containsKey("data")) {
                    JSONObject data = jsonResponse.getJSONObject("data");
                    
                    // 检查是否有错误码
                    Integer errorCode = data.getInt("error_code", 0);
                    if (errorCode != 0) {
                        String description = data.getStr("description", "未知错误");
                        System.err.println("抖音API错误: " + errorCode + " - " + description);
                        return null;
                    }
                    
                    // 提取token信息
                    Map<String, Object> tokenInfo = new HashMap<>();
                    tokenInfo.put("access_token", data.getStr("access_token"));
                    tokenInfo.put("refresh_token", data.getStr("refresh_token"));
                    tokenInfo.put("expires_in", data.getInt("expires_in"));
                    tokenInfo.put("refresh_expires_in", data.getInt("refresh_expires_in"));
                    tokenInfo.put("open_id", data.getStr("open_id"));
                    tokenInfo.put("scope", data.getStr("scope"));
                    
                    return tokenInfo;
                } else {
                    System.err.println("抖音API响应格式错误: " + responseBody);
                    return null;
                }
            } else {
                System.err.println("抖音API请求失败，HTTP状态码: " + response.getStatus());
                return null;
            }
        } catch (Exception e) {
            System.err.println("调用抖音API异常: " + e.getMessage());
            return null;
        }
    }

    /**
     * 调用抖音开放平台API刷新access_token
     * 
     * @param refreshToken 刷新令牌
     * @return 包含token信息的Map，失败返回null
     */
    private Map<String, Object> refreshDouyinAccessToken(String refreshToken) {
        try {
            // 构建请求参数
            Map<String, Object> params = new HashMap<>();
            params.put("client_key", douyinConfig.getClientKey());
            params.put("client_secret", douyinConfig.getClientSecret());
            params.put("refresh_token", refreshToken);
            params.put("grant_type", "refresh_token");

            // 发送POST请求到抖音开放平台
            HttpResponse response = HttpRequest.post(DOUYIN_ACCESS_TOKEN_URL)
                    .header("content-type", "application/x-www-form-urlencoded")
                    .form(params)
                    .timeout(10000) // 10秒超时
                    .execute();

            if (response.isOk()) {
                String responseBody = response.body();
                JSONObject jsonResponse = JSONUtil.parseObj(responseBody);
                
                // 检查响应状态
                if (jsonResponse.containsKey("data")) {
                    JSONObject data = jsonResponse.getJSONObject("data");
                    
                    // 检查是否有错误码
                    Integer errorCode = data.getInt("error_code", 0);
                    if (errorCode != 0) {
                        String description = data.getStr("description", "未知错误");
                        System.err.println("抖音刷新API错误: " + errorCode + " - " + description);
                        return null;
                    }
                    
                    // 提取token信息
                    Map<String, Object> tokenInfo = new HashMap<>();
                    tokenInfo.put("access_token", data.getStr("access_token"));
                    tokenInfo.put("refresh_token", data.getStr("refresh_token"));
                    tokenInfo.put("expires_in", data.getInt("expires_in"));
                    tokenInfo.put("refresh_expires_in", data.getInt("refresh_expires_in"));
                    tokenInfo.put("open_id", data.getStr("open_id"));
                    tokenInfo.put("scope", data.getStr("scope"));
                    
                    return tokenInfo;
                } else {
                    System.err.println("抖音刷新API响应格式错误: " + responseBody);
                    return null;
                }
            } else {
                System.err.println("抖音刷新API请求失败，HTTP状态码: " + response.getStatus());
                return null;
            }
        } catch (Exception e) {
            System.err.println("调用抖音刷新API异常: " + e.getMessage());
            return null;
        }
    }

    /**
     * 检查抖音access_token是否有效
     */
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
}



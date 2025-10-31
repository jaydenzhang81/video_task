package com.videotask.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.videotask.dto.ApiResponse;
import com.videotask.dto.WechatDto;
import com.videotask.entity.User;
import com.videotask.entity.UserWechat;
import com.videotask.repository.UserRepository;
import com.videotask.repository.UserWechatRepository;
import com.videotask.service.UserService;
import com.videotask.service.WechatAuthService;
import com.videotask.util.PasswordUtil;
import com.videotask.util.WechatApiUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class WechatAuthServiceImpl implements WechatAuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserWechatRepository userWechatRepository;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private WechatApiUtil wechatApiUtil;
    
    @Value("${wechat.app-id:}")
    private String wechatAppId;
    
    @Value("${wechat.app-secret:}")
    private String wechatAppSecret;
    
    @Override
    public ApiResponse<WechatDto.WechatLoginResponse> wechatLogin(WechatDto.WechatLoginRequest request) {
        try {
            // 检查unionid是否存在
            Optional<UserWechat> existingWechatUser = userWechatRepository.findByUnionid(request.getUnionid());
            
            if (existingWechatUser.isPresent()) {
                // 用户已存在，直接登录
                UserWechat wechatUser = existingWechatUser.get();
                User user = userService.getUserById(wechatUser.getId());
                
                if (user == null) {
                    return ApiResponse.error("用户不存在");
                }
                
                if (user.getStatus() != 1) {
                    return ApiResponse.error("用户已被禁用");
                }
                
                // 更新微信用户信息
                updateWechatUserInfo(wechatUser, request);
                
                // 生成token
                StpUtil.login(user.getId());
                String token = StpUtil.getTokenValue();
                
                // 构建响应
                WechatDto.WechatLoginResponse response = buildLoginResponse(user, wechatUser, token, false, false);
                
                return ApiResponse.success(response);
            } else {
                // 新用户，需要绑定手机号
                WechatDto.WechatLoginResponse response = new WechatDto.WechatLoginResponse();
                response.setNewUser(true);
                response.setNeedBindPhone(true);
                
                return ApiResponse.success(response);
            }
        } catch (Exception e) {
            return ApiResponse.error("微信登录失败");
        }
    }
    
    @Override
    public ApiResponse<WechatDto.WechatLoginResponse> bindPhone(WechatDto.WechatBindPhoneRequest request) {
        try {
            // 检查手机号是否已被绑定
            Optional<UserWechat> existingWechatUser = userWechatRepository.findByPhoneNumber(request.getPhone());
            if (existingWechatUser.isPresent()) {
                return ApiResponse.error("该手机号已被绑定");
            }
            
            // 检查手机号是否已注册
            Optional<User> existingUser = userRepository.findByPhone(request.getPhone());
            User user;
            
            if (existingUser.isPresent()) {
                // 手机号已注册，直接绑定微信
                user = existingUser.get();
                
                // 创建微信用户记录
                UserWechat wechatUser = new UserWechat();
                wechatUser.setId(user.getId());
                wechatUser.setUnionid(request.getUnionid());
                wechatUser.setPhoneNumber(request.getPhone());
                wechatUser.setCtime(System.currentTimeMillis());
                wechatUser.setUtime(System.currentTimeMillis());
                
                userWechatRepository.save(wechatUser);
            } else {
                // 手机号未注册，创建新用户
                user = new User();
                user.setPhone(request.getPhone());
                user.setNick("微信用户" + request.getPhone().substring(7));
                user.setStatus(1);
                user.setCtime(System.currentTimeMillis());
                user.setUtime(System.currentTimeMillis());
                
                // 生成默认密码
                String salt = PasswordUtil.generateSalt();
                String defaultPassword = "123456"; // 默认密码
                String hashedPassword = PasswordUtil.hashPassword(defaultPassword, salt);
                user.setSalt(salt);
                user.setPassword(hashedPassword);
                
                user = userRepository.save(user);
                
                // 创建微信用户记录
                UserWechat wechatUser = new UserWechat();
                wechatUser.setId(user.getId());
                wechatUser.setUnionid(request.getUnionid());
                wechatUser.setPhoneNumber(request.getPhone());
                wechatUser.setCtime(System.currentTimeMillis());
                wechatUser.setUtime(System.currentTimeMillis());
                
                userWechatRepository.save(wechatUser);
            }
            
            // 生成token
            StpUtil.login(user.getId());
            String token = StpUtil.getTokenValue();
            
            // 获取微信用户信息
            UserWechat wechatUser = userWechatRepository.findByUnionid(request.getUnionid()).orElse(null);
            
            // 构建响应
            WechatDto.WechatLoginResponse response = buildLoginResponse(user, wechatUser, token, true, false);
            
            return ApiResponse.success(response);
        } catch (Exception e) {
            return ApiResponse.error("绑定手机号失败");
        }
    }
    
    @Override
    public ApiResponse<WechatDto.WechatLoginResponse> loginByCode(String code) {
        try {
            // 检查微信配置
            if (wechatAppId == null || wechatAppId.isEmpty() || 
                wechatAppSecret == null || wechatAppSecret.isEmpty()) {
                return ApiResponse.error("微信登录配置不完整");
            }
            
            // 1. 通过code获取access_token
            WechatApiUtil.WechatAccessToken accessToken = wechatApiUtil.getAccessToken(code, wechatAppId, wechatAppSecret);
            if (accessToken == null || accessToken.getAccessToken() == null) {
                return ApiResponse.error("获取微信访问令牌失败");
            }
            
            // 2. 通过access_token获取用户信息
            WechatApiUtil.WechatUserInfo wechatUserInfo = wechatApiUtil.getUserInfo(accessToken.getAccessToken(), accessToken.getOpenId());
            if (wechatUserInfo == null) {
                return ApiResponse.error("获取微信用户信息失败");
            }
            
            // 3. 根据unionid判断用户是否存在
            String unionid = wechatUserInfo.getUnionId();
            String openId = wechatUserInfo.getOpenId();
            
            if (unionid == null || unionid.isEmpty()) {
                return ApiResponse.error("获取微信unionid失败");
            }
            
            Optional<UserWechat> existingWechatUser = userWechatRepository.findByUnionid(unionid);
            
            if (existingWechatUser.isPresent()) {
                // 用户已存在，直接登录
                UserWechat wechatUser = existingWechatUser.get();
                User user = userService.getUserById(wechatUser.getId());
                
                if (user == null) {
                    return ApiResponse.error("用户不存在");
                }
                
                if (user.getStatus() != 1) {
                    return ApiResponse.error("用户已被禁用");
                }
                
                // 更新微信用户信息
                updateWechatUserInfoFromApi(wechatUser, wechatUserInfo);
                
                // 生成token
                StpUtil.login(user.getId());
                String token = StpUtil.getTokenValue();
                
                // 构建响应
                WechatDto.WechatLoginResponse response = buildLoginResponse(user, wechatUser, token, false, false);
                
                return ApiResponse.success(response);
            } else {
                // 新用户，需要绑定手机号
                WechatDto.WechatLoginResponse response = new WechatDto.WechatLoginResponse();
                response.setNewUser(true);
                response.setNeedBindPhone(true);
                response.setUnionid(unionid);
                response.setOpenId(openId);
                
                return ApiResponse.success(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("微信登录失败");
        }
    }

    @Override
    public boolean checkUnionidExists(String unionid) {
        return userWechatRepository.existsByUnionid(unionid);
    }
    
    /**
     * 从微信API更新用户信息
     */
    private void updateWechatUserInfoFromApi(UserWechat wechatUser, WechatApiUtil.WechatUserInfo wechatUserInfo) {
        boolean needUpdate = false;
        
        if (wechatUserInfo.getNickname() != null && !wechatUserInfo.getNickname().equals(wechatUser.getNickName())) {
            wechatUser.setNickName(wechatUserInfo.getNickname());
            needUpdate = true;
        }
        
        if (wechatUserInfo.getHeadImgUrl() != null && !wechatUserInfo.getHeadImgUrl().equals(wechatUser.getHeadImgUrl())) {
            wechatUser.setHeadImgUrl(wechatUserInfo.getHeadImgUrl());
            needUpdate = true;
        }
        
        if (wechatUserInfo.getSex() != null && !wechatUserInfo.getSex().equals(wechatUser.getSex())) {
            wechatUser.setSex(wechatUserInfo.getSex());
            needUpdate = true;
        }
        
        if (wechatUserInfo.getCountry() != null && !wechatUserInfo.getCountry().equals(wechatUser.getCountry())) {
            wechatUser.setCountry(wechatUserInfo.getCountry());
            needUpdate = true;
        }
        
        if (wechatUserInfo.getProvince() != null && !wechatUserInfo.getProvince().equals(wechatUser.getProvince())) {
            wechatUser.setProvince(wechatUserInfo.getProvince());
            needUpdate = true;
        }
        
        if (wechatUserInfo.getCity() != null && !wechatUserInfo.getCity().equals(wechatUser.getCity())) {
            wechatUser.setCity(wechatUserInfo.getCity());
            needUpdate = true;
        }
        
        if (needUpdate) {
            wechatUser.setUtime(System.currentTimeMillis());
            userWechatRepository.save(wechatUser);
        }
    }

    /**
     * 更新微信用户信息
     */
    private void updateWechatUserInfo(UserWechat wechatUser, WechatDto.WechatLoginRequest request) {
        boolean needUpdate = false;
        
        if (request.getNickName() != null && !request.getNickName().equals(wechatUser.getNickName())) {
            wechatUser.setNickName(request.getNickName());
            needUpdate = true;
        }
        
        if (request.getHeadImgUrl() != null && !request.getHeadImgUrl().equals(wechatUser.getHeadImgUrl())) {
            wechatUser.setHeadImgUrl(request.getHeadImgUrl());
            needUpdate = true;
        }
        
        if (request.getSex() != null && !request.getSex().equals(wechatUser.getSex())) {
            wechatUser.setSex(request.getSex());
            needUpdate = true;
        }
        
        if (request.getCountry() != null && !request.getCountry().equals(wechatUser.getCountry())) {
            wechatUser.setCountry(request.getCountry());
            needUpdate = true;
        }
        
        if (request.getProvince() != null && !request.getProvince().equals(wechatUser.getProvince())) {
            wechatUser.setProvince(request.getProvince());
            needUpdate = true;
        }
        
        if (request.getCity() != null && !request.getCity().equals(wechatUser.getCity())) {
            wechatUser.setCity(request.getCity());
            needUpdate = true;
        }
        
        if (needUpdate) {
            wechatUser.setUtime(System.currentTimeMillis());
            userWechatRepository.save(wechatUser);
        }
    }
    
    /**
     * 构建登录响应
     */
    private WechatDto.WechatLoginResponse buildLoginResponse(User user, UserWechat wechatUser, String token, boolean isNewUser, boolean needBindPhone) {
        WechatDto.WechatLoginResponse response = new WechatDto.WechatLoginResponse();
        response.setToken(token);
        response.setNewUser(isNewUser);
        response.setNeedBindPhone(needBindPhone);
        
        WechatDto.UserInfo userInfo = new WechatDto.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setPhone(user.getPhone());
        userInfo.setNick(user.getNick());
        userInfo.setAvatar(user.getAvatar());
        userInfo.setStatus(user.getStatus());
        
        if (wechatUser != null) {
            userInfo.setWechatNickName(wechatUser.getNickName());
            userInfo.setWechatAvatar(wechatUser.getHeadImgUrl());
        }
        
        response.setUser(userInfo);
        
        return response;
    }
}

package com.videotask.service;

import com.videotask.dto.ApiResponse;
import com.videotask.dto.UserDto;
import com.videotask.entity.User;

import java.util.Optional;

public interface UserService {
    ApiResponse<Void> register(UserDto.RegisterRequest request);
    ApiResponse<UserDto.LoginResponse> login(UserDto.LoginRequest request);
    User getUserById(Integer userId);
    void updateUser(User user);
    Optional<User> getUserByPhone(String phone);
    // 设置验证码验证器
    void setSmsCodeVerifier(SmsCodeVerifier verifier);
    
    // 短信验证码验证接口
    interface SmsCodeVerifier {
        boolean verify(String phone, String code);
    }
} 
package com.videotask.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.videotask.dto.ApiResponse;
import com.videotask.dto.UserDto;
import com.videotask.entity.User;
import com.videotask.repository.UserRepository;
import com.videotask.service.UserService;
import com.videotask.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    private SmsCodeVerifier smsCodeVerifier;
    
    @Override
    public ApiResponse<Void> register(UserDto.RegisterRequest request) {
        // 验证短信验证码
        if (smsCodeVerifier != null && request.getCode() != null) {
            if (!smsCodeVerifier.verify(request.getPhone(), request.getCode())) {
                return ApiResponse.error("验证码错误或已过期");
            }
        }
        
        // 检查手机号是否已存在
        Optional<User> existingUser = userRepository.findByPhone(request.getPhone());
        if (existingUser.isPresent()) {
            return ApiResponse.error("手机号已存在");
        }
        
        // 创建新用户
        User user = new User();
        user.setPhone(request.getPhone());
        // 如果提供了昵称就使用，否则使用默认昵称
        if (request.getNick() != null && !request.getNick().trim().isEmpty()) {
            user.setNick(request.getNick().trim());
        } else {
            user.setNick("用户" + request.getPhone().substring(7)); // 默认昵称
        }
        user.setStatus(1); // 可登录状态
        user.setCtime(System.currentTimeMillis());
        user.setUtime(System.currentTimeMillis());
        
        // 生成盐值和密码哈希
        String salt = PasswordUtil.generateSalt();
        String hashedPassword = PasswordUtil.hashPassword(request.getPassword(), salt);
        user.setSalt(salt);
        user.setPassword(hashedPassword);
        
        userRepository.save(user);
        
        return ApiResponse.success();
    }
    
    @Override
    public ApiResponse<UserDto.LoginResponse> login(UserDto.LoginRequest request) {
        // 查找用户
        Optional<User> userOpt = userRepository.findByPhoneAndStatus(request.getPhone(), 1);
        if (!userOpt.isPresent()) {
            return ApiResponse.error("用户不存在或已被禁用");
        }
        
        User user = userOpt.get();
        
        // 验证密码
        if (!PasswordUtil.verifyPassword(request.getPassword(), user.getSalt(), user.getPassword())) {
            return ApiResponse.error("密码错误");
        }
        
        // 生成 token
        StpUtil.login(user.getId());
        String token = StpUtil.getTokenValue();
        
        // 构建响应
        UserDto.LoginResponse response = new UserDto.LoginResponse();
        UserDto.UserInfo userInfo = new UserDto.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setPhone(user.getPhone());
        userInfo.setNick(user.getNick());
        userInfo.setStatus(user.getStatus());
        response.setUser(userInfo);
        response.setToken(token);
        
        return ApiResponse.success(response);
    }
    
    @Override
    public User getUserById(Integer userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        return userOpt.orElse(null);
    }
    
    @Override
    public void updateUser(User user) {
        userRepository.save(user);
    }
    
    @Override
    public Optional<User> getUserByPhone(String phone) {
        return userRepository.findByPhone(phone);
    }
    
    @Override
    public void setSmsCodeVerifier(SmsCodeVerifier verifier) {
        this.smsCodeVerifier = verifier;
    }
} 
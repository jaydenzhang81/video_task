package com.videotask.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class UserDto {
    
    @Data
    public static class RegisterRequest {
        @NotBlank(message = "手机号不能为空")
        private String phone;
        
        @NotBlank(message = "密码不能为空")
        private String password;
        
        private String code; // 验证码
        private String nick; // 昵称（可选）
    }
    
    @Data
    public static class LoginRequest {
        @NotBlank(message = "手机号不能为空")
        private String phone;
        
        @NotBlank(message = "密码不能为空")
        private String password;
    }
    
    @Data
    public static class LoginResponse {
        private UserInfo user;
        private String token;
    }
    
    @Data
    public static class UserInfo {
        private Integer id;
        private String phone;
        private String nick;
        private Integer status;
    }
} 
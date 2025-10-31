package com.videotask.dto;

import lombok.Data;

public class WechatDto {
    
    @Data
    public static class WechatLoginRequest {
        private String code; // 微信授权码
        private String unionid; // 微信unionid
        private String openId; // 微信openId
        private String nickName; // 昵称
        private String headImgUrl; // 头像
        private Integer sex; // 性别
        private String country; // 国家
        private String province; // 省份
        private String city; // 城市
    }
    
    @Data
    public static class WechatBindPhoneRequest {
        private String unionid; // 微信unionid
        private String phone; // 手机号
        private String code; // 验证码
    }
    
    @Data
    public static class WechatLoginResponse {
        private String token;
        private UserInfo user;
        private boolean isNewUser; // 是否新用户
        private boolean needBindPhone; // 是否需要绑定手机号
        private String unionid; // 微信unionid
        private String openId; // 微信openId
    }
    
    @Data
    public static class UserInfo {
        private Integer id;
        private String phone;
        private String nick;
        private String avatar;
        private Integer status;
        private String wechatNickName;
        private String wechatAvatar;
    }
}

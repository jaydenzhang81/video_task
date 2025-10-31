package com.videotask.service;

import com.videotask.dto.ApiResponse;
import com.videotask.dto.WechatDto;

public interface WechatAuthService {
    /**
     * 微信登录
     */
    ApiResponse<WechatDto.WechatLoginResponse> wechatLogin(WechatDto.WechatLoginRequest request);
    
    /**
     * 绑定手机号
     */
    ApiResponse<WechatDto.WechatLoginResponse> bindPhone(WechatDto.WechatBindPhoneRequest request);
    
    /**
     * 通过code进行微信登录（App端）
     */
    ApiResponse<WechatDto.WechatLoginResponse> loginByCode(String code);
    
    /**
     * 检查unionid是否存在
     */
    boolean checkUnionidExists(String unionid);
}

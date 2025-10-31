package com.videotask.controller;

import com.videotask.dto.ApiResponse;
import com.videotask.dto.WechatDto;
import com.videotask.service.WechatAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/wechat")
@CrossOrigin(origins = "*")
public class WechatAuthController {
    
    @Autowired
    private WechatAuthService wechatAuthService;
    
    /**
     * 微信登录
     */
    @PostMapping("/login")
    public ApiResponse<WechatDto.WechatLoginResponse> wechatLogin(@Valid @RequestBody WechatDto.WechatLoginRequest request) {
        return wechatAuthService.wechatLogin(request);
    }
    
    /**
     * 绑定手机号
     */
    @PostMapping("/bindPhone")
    public ApiResponse<WechatDto.WechatLoginResponse> bindPhone(@Valid @RequestBody WechatDto.WechatBindPhoneRequest request) {
        return wechatAuthService.bindPhone(request);
    }
    
    /**
     * 通过code进行微信登录（App端）
     */
    @PostMapping("/loginByCode")
    public ApiResponse<WechatDto.WechatLoginResponse> loginByCode(@RequestBody Map<String, String> request) {
        String code = request.get("code");
        if (code == null || code.isEmpty()) {
            return ApiResponse.error("code不能为空");
        }
        
        // 这里需要调用微信API获取access_token和用户信息
        // 由于需要微信AppSecret，建议在后端实现
        return wechatAuthService.loginByCode(code);
    }

    /**
     * 检查unionid是否存在
     */
    @GetMapping("/checkUnionid")
    public ApiResponse<Boolean> checkUnionid(@RequestParam String unionid) {
        boolean exists = wechatAuthService.checkUnionidExists(unionid);
        return ApiResponse.success(exists);
    }
}

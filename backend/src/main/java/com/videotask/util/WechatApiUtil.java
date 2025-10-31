package com.videotask.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * 微信API工具类
 * 用于调用微信开放平台接口
 */
@Component
public class WechatApiUtil {
    
    private static final String WECHAT_ACCESS_TOKEN_URL = "https://api.weixin.qq.com/sns/oauth2/access_token";
    private static final String WECHAT_USER_INFO_URL = "https://api.weixin.qq.com/sns/userinfo";
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 通过code获取access_token
     * @param code 微信授权码
     * @param appId 微信AppID
     * @param appSecret 微信AppSecret
     * @return access_token信息
     */
    public WechatAccessToken getAccessToken(String code, String appId, String appSecret) {
        try {
            String url = WECHAT_ACCESS_TOKEN_URL + 
                "?appid=" + appId + 
                "&secret=" + appSecret + 
                "&code=" + code + 
                "&grant_type=authorization_code";
            
            String response = sendGetRequest(url);
            Map<String, Object> result = objectMapper.readValue(response, Map.class);
            
            WechatAccessToken accessToken = new WechatAccessToken();
            accessToken.setAccessToken((String) result.get("access_token"));
            accessToken.setExpiresIn((Integer) result.get("expires_in"));
            accessToken.setRefreshToken((String) result.get("refresh_token"));
            accessToken.setOpenId((String) result.get("openid"));
            accessToken.setScope((String) result.get("scope"));
            accessToken.setUnionId((String) result.get("unionid"));
            
            return accessToken;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 通过access_token获取用户信息
     * @param accessToken 访问令牌
     * @param openId 用户openId
     * @return 用户信息
     */
    public WechatUserInfo getUserInfo(String accessToken, String openId) {
        try {
            String url = WECHAT_USER_INFO_URL + 
                "?access_token=" + accessToken + 
                "&openid=" + openId;
            
            String response = sendGetRequest(url);
            Map<String, Object> result = objectMapper.readValue(response, Map.class);
            
            WechatUserInfo userInfo = new WechatUserInfo();
            userInfo.setOpenId((String) result.get("openid"));
            userInfo.setNickname((String) result.get("nickname"));
            userInfo.setSex((Integer) result.get("sex"));
            userInfo.setProvince((String) result.get("province"));
            userInfo.setCity((String) result.get("city"));
            userInfo.setCountry((String) result.get("country"));
            userInfo.setHeadImgUrl((String) result.get("headimgurl"));
            userInfo.setUnionId((String) result.get("unionid"));
            
            return userInfo;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 发送GET请求
     */
    private String sendGetRequest(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        
        reader.close();
        connection.disconnect();
        
        return response.toString();
    }
    
    /**
     * 微信访问令牌
     */
    public static class WechatAccessToken {
        private String accessToken;
        private Integer expiresIn;
        private String refreshToken;
        private String openId;
        private String scope;
        private String unionId;
        
        // getters and setters
        public String getAccessToken() { return accessToken; }
        public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
        
        public Integer getExpiresIn() { return expiresIn; }
        public void setExpiresIn(Integer expiresIn) { this.expiresIn = expiresIn; }
        
        public String getRefreshToken() { return refreshToken; }
        public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
        
        public String getOpenId() { return openId; }
        public void setOpenId(String openId) { this.openId = openId; }
        
        public String getScope() { return scope; }
        public void setScope(String scope) { this.scope = scope; }
        
        public String getUnionId() { return unionId; }
        public void setUnionId(String unionId) { this.unionId = unionId; }
    }
    
    /**
     * 微信用户信息
     */
    public static class WechatUserInfo {
        private String openId;
        private String nickname;
        private Integer sex;
        private String province;
        private String city;
        private String country;
        private String headImgUrl;
        private String unionId;
        
        // getters and setters
        public String getOpenId() { return openId; }
        public void setOpenId(String openId) { this.openId = openId; }
        
        public String getNickname() { return nickname; }
        public void setNickname(String nickname) { this.nickname = nickname; }
        
        public Integer getSex() { return sex; }
        public void setSex(Integer sex) { this.sex = sex; }
        
        public String getProvince() { return province; }
        public void setProvince(String province) { this.province = province; }
        
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
        
        public String getHeadImgUrl() { return headImgUrl; }
        public void setHeadImgUrl(String headImgUrl) { this.headImgUrl = headImgUrl; }
        
        public String getUnionId() { return unionId; }
        public void setUnionId(String unionId) { this.unionId = unionId; }
    }
}

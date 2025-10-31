package com.videotask.dto;

import lombok.Data;

@Data
public class PlatformDto {
    
    @Data
    public static class PlatformInfo {
        private Integer id;
        private String name;
        private String imageUrl;
        private Boolean isAuth;
        private Integer status;
    }
    
    @Data
    public static class PlatformAuthRequest {
        private Integer platformId;
        private String authCode;
        private String state;
        private String grantedPermissions;
    }
    
    @Data
    public static class KuaishouAuthRequest {
        private String authCode;
        private String state;
    }

    @Data
    public static class DouyinAuthRequest {
        private String authCode;
        private String state;
    }
} 
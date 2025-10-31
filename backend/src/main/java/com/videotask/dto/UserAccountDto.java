package com.videotask.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserAccountDto {

    @Data
    public static class UserAccountInfo {
        private Integer userId;
        private BigDecimal money;           // 当前余额
        private BigDecimal historyReward;  // 历史奖励汇总
        private Long utime;       // 更新时间
    }

    @Data
    public static class UserAccountLogInfo {
        private Integer id;
        private Integer userId;
        private String title;
        private String content;
        private BigDecimal money;
        private Integer type;              // 1视频挣的，2提现了
        private String source;             // 视频ID
        private Long ctime;
    }
}

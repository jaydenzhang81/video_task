package com.videotask.entity;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "tb_video_share_user_log")
public class VideoShareUserLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "video_id", nullable = false)
    private Integer videoId;

    @Column(name = "platform_id", nullable = false)
    private Integer platformId;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "video_url")
    private String videoUrl;

    @Column(name = "upload_token")
    private String uploadToken;

    @Column(name = "photo_id")
    private String photoId;

    @Column(name = "status")
    private Integer status; // 0分享中，1分享成功，-1分享失败

    @Column(name = "like_count")
    private Integer likeCount;

    @Column(name = "comment_count")
    private Integer commentCount;

    @Column(name = "view_count")
    private Integer viewCount;

    @Column(name = "reward", precision = 10, scale = 2)
    private BigDecimal reward;

    @Column(name = "reward_status")
    private Integer rewardStatus; // 1进行中，2已发放，-1任务失败，无奖励

    @Column(name = "data_status")
    private Integer dataStatus; // 0异常，1正常

    @Column(name = "ctime")
    private Long ctime;

}
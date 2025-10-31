package com.videotask.entity;

import lombok.Data;
import javax.persistence.*;

@Data
@Entity
@Table(name = "tb_video_platform")
public class VideoPlatform {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "image_url")
    private String imageUrl;
    
    @Column(name = "app_id")
    private String appId;
    
    @Column(name = "app_secret")
    private String appSecret;
    
    @Column(name = "status")
    private Integer status; // 0未启用，1启用
    
    @Column(name = "publish_num")
    private Integer publishNum; // 每天允许发布的视频数量上限
}
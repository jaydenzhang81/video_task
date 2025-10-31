package com.videotask.entity;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "tb_video")
public class Video {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "video_desc")
    private String videoDesc;

    @Column(name = "cover")
    private String cover;

    @Column(name = "url", nullable = false)
    private String url;

    @Column(name = "video_type")
    private Integer videoType;

    @Column(name = "status")
    private Integer status; // 0未发布，1已发布

    @Column(name = "reward", precision = 10, scale = 2)
    private BigDecimal reward = BigDecimal.ZERO;

    @Column(name = "hashtags")
    private String hashtags; // 话题字符串，用逗号分割

    @Column(name = "ctime")
    private Long ctime;

    @Column(name = "utime")
    private Long utime;
}
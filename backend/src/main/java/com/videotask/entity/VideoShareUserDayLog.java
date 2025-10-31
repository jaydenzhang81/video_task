package com.videotask.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "tb_video_share_user_day_log")
public class VideoShareUserDayLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "photo_id")
    private String photoId;

    @Column(name = "like_count")
    private Integer likeCount = 0;

    @Column(name = "comment_count")
    private Integer commentCount = 0;

    @Column(name = "view_count")
    private Integer viewCount = 0;

    @Column(name = "ctime")
    private Long ctime;

    @Column(name = "date")
    private Integer date;
}

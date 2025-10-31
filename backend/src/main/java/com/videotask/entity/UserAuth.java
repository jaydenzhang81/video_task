package com.videotask.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "tb_user_auth")
public class UserAuth {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "platform_id", nullable = false)
    private Integer platformId;

    @Column(name = "access_token")
    private String accessToken;

    @Column(name = "refresh_token")
    private String refreshToken;

    @Column(name = "expires_in")
    private Integer expiresIn;

    @Column(name = "refresh_token_expires_in")
    private Integer refreshTokenExpiresIn;

    @Column(name = "utime")
    private Long utime;

    @Column(name = "open_id")
    private String openId;

    @Column(name = "state")
    private String state;

} 
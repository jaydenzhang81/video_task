package com.videotask.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "tb_user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "phone", nullable = false, unique = true)
    private String phone;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "salt")
    private String salt;

    @Column(name = "nick")
    private String nick;

    @Column(name = "status")
    private Integer status; // -1删除，0禁用，1可登录

    @Column(name = "ctime")
    private Long ctime;

    @Column(name = "utime")
    private Long utime;

    @Column(name = "avatar")
    private String avatar;

    @Column(name = "user_type")
    private Integer userType; // 0非带货，1带货用户

    @Column(name = "team_id")
    private Integer teamId;

    @Column(name = "gender")
    private Integer gender; // 0保密，1男，2女

    @Column(name = "region")
    private String region; // 地区
}
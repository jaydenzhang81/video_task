package com.videotask.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "tb_user_wechat")
public class UserWechat {
    @Id
    @Column(name = "id")
    private Integer id; // 用户ID，与用户表一对一

    @Column(name = "nick_name")
    private String nickName; // 昵称

    @Column(name = "country")
    private String country; // 地区

    @Column(name = "province")
    private String province; // 省份

    @Column(name = "city")
    private String city; // 城市

    @Column(name = "open_id")
    private String openId; // 微信 openId

    @Column(name = "sex")
    private Integer sex; // 性别

    @Column(name = "head_img_url")
    private String headImgUrl; // 头像地址

    @Column(name = "phone_number")
    private String phoneNumber; // 手机号

    @Column(name = "ctime")
    private Long ctime; // 创建时间

    @Column(name = "utime")
    private Long utime; // 更新时间

    @Column(name = "unionid")
    private String unionid; // unionid
}

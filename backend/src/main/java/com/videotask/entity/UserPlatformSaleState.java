package com.videotask.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "tb_user_platform_sale_state")
public class UserPlatformSaleState {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id")
    private Integer userId;

    /**
     * 平台ID，1抖音，2快手，等
     */
    @Column(name = "platform")
    private Integer platform;

    /**
     * 状态：1可发布带货视频，其他不可
     */
    @Column(name = "status")
    private Integer status;
}



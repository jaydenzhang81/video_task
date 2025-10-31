package com.videotask.entity;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "tb_user_account_log")
public class UserAccountLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "title")
    private String title;

    @Column(name = "content")
    private String content;

    @Column(name = "money", precision = 10, scale = 2)
    private BigDecimal money;

    @Column(name = "type")
    private Integer type; // 1视频挣的，2提现了

    @Column(name = "source")
    private String source; // 视频ID

    @Column(name = "ctime")
    private Long ctime;

}

package com.videotask.entity;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "tb_user_account_drawal")
public class UserAccountDrawal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "money", precision = 10, scale = 2)
    private BigDecimal money;

    @Column(name = "ctime")
    private Long ctime;

    @Column(name = "status")
    private Integer status; // 0申请提现，1通过申请，2支付成功

    @Column(name = "account_type")
    private String accountType; // 账户类型：wechat微信，alipay支付宝，bank银行卡
}

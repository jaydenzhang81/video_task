package com.videotask.entity;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "tb_finance_flow")
public class FinanceFlow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "type")
    private String type; // income收入, expense支出, withdraw提现, reward奖励, refund退款

    @Column(name = "title")
    private String title; // 流水标题

    @Column(name = "description")
    private String description; // 流水描述

    @Column(name = "amount")
    private BigDecimal amount; // 金额

    @Column(name = "status")
    private Integer status; // 0处理中，1已完成，2失败

    @Column(name = "related_id")
    private Integer relatedId; // 关联ID（如提现记录ID）

    @Column(name = "related_type")
    private String relatedType; // 关联类型

    @Column(name = "create_time")
    private Long createTime;

    @Column(name = "update_time")
    private Long updateTime;
}

package com.videotask.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "tb_user_account")
public class UserAccount {
    @Id
    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "money", precision = 10, scale = 2)
    private BigDecimal money = BigDecimal.ZERO;

    @Column(name = "history_reward", precision = 10, scale = 2)
    private BigDecimal historyReward = BigDecimal.ZERO;

    @Column(name = "utime")
    private Long utime;

}

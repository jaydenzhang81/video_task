package com.videotask.entity;

import lombok.Data;
import javax.persistence.*;

/**
 * 团队实体类
 * 映射tb_team表
 */
@Data
@Entity
@Table(name = "tb_team")
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "name")
    private String name;
}
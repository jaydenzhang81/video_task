package com.videotask.repository;

import com.videotask.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 团队数据访问接口
 * 提供团队相关的CRUD操作
 */
@Repository
public interface TeamRepository extends JpaRepository<Team, Integer> {
    // 可以添加自定义查询方法
}
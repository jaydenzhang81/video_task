package com.videotask.repository;

import com.videotask.entity.UserAccountLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserAccountLogRepository extends JpaRepository<UserAccountLog, Integer> {
    List<UserAccountLog> findByUserIdOrderByCtimeDesc(Integer userId);
    List<UserAccountLog> findByUserIdAndCtimeBetweenOrderByCtimeDesc(Integer userId, Long startTime, Long endTime);
    List<UserAccountLog> findByUserIdAndTypeOrderByCtimeDesc(Integer userId, Integer type);
}

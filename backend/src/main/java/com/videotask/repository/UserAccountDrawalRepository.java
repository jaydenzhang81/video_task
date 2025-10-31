package com.videotask.repository;

import com.videotask.entity.UserAccountDrawal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserAccountDrawalRepository extends JpaRepository<UserAccountDrawal, Integer> {
    List<UserAccountDrawal> findByUserIdAndStatusOrderByCtimeDesc(Integer userId, Integer status);
    List<UserAccountDrawal> findByUserIdOrderByCtimeDesc(Integer userId);
    List<UserAccountDrawal> findByCtimeBetween(Long startTime, Long endTime);
}

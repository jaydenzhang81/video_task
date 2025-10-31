package com.videotask.repository;

import com.videotask.entity.UserAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAuthRepository extends JpaRepository<UserAuth, Integer> {
    List<UserAuth> findByUserId(Integer userId);
    Optional<UserAuth> findByUserIdAndPlatformId(Integer userId, Integer platformId);
    List<UserAuth> findByPlatformId(Integer platformId);
} 
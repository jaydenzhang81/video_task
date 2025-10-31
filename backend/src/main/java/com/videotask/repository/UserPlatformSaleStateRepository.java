package com.videotask.repository;

import com.videotask.entity.UserPlatformSaleState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserPlatformSaleStateRepository extends JpaRepository<UserPlatformSaleState, Integer> {
    List<UserPlatformSaleState> findByUserId(Integer userId);
    Optional<UserPlatformSaleState> findByUserIdAndPlatform(Integer userId, Integer platform);
}



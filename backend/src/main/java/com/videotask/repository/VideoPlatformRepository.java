package com.videotask.repository;

import com.videotask.entity.VideoPlatform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoPlatformRepository extends JpaRepository<VideoPlatform, Integer> {
    List<VideoPlatform> findByStatus(Integer status);

    List<VideoPlatform> findByStatusNot(int i);
}
package com.videotask.repository;

import com.videotask.entity.VideoType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoTypeRepository extends JpaRepository<VideoType, Integer> {
} 
package com.videotask.repository;

import com.videotask.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoRepository extends JpaRepository<Video, Integer> {
    List<Video> findByVideoType(Integer videoType);
    
    @Query("SELECT v FROM Video v WHERE v.status = :status ORDER BY v.ctime DESC")
    List<Video> findByStatusOrderByCtimeDesc(@Param("status") Integer status);
    
    @Query("SELECT v FROM Video v WHERE v.status = :status AND v.videoType = :videoType ORDER BY v.ctime DESC")
    List<Video> findByStatusAndVideoTypeOrderByCtimeDesc(@Param("status") Integer status, @Param("videoType") Integer videoType);
} 
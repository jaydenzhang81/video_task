package com.videotask.repository;

import com.videotask.entity.VideoShareUserDayLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VideoShareUserDayLogRepository extends JpaRepository<VideoShareUserDayLog, Integer> {
    
    /**
     * 根据photoId查找今日的记录
     */
    @Query("SELECT v FROM VideoShareUserDayLog v WHERE v.photoId = :photoId AND DATE(v.ctime) = DATE(:today)")
    Optional<VideoShareUserDayLog> findByPhotoIdAndToday(@Param("photoId") String photoId, @Param("today") LocalDateTime today);
    
    /**
     * 根据photoId查找所有记录
     */
    List<VideoShareUserDayLog> findByPhotoIdOrderByCtimeDesc(String photoId);
    
    /**
     * 查找指定日期范围内的记录
     */
    @Query("SELECT v FROM VideoShareUserDayLog v WHERE v.photoId = :photoId AND v.ctime BETWEEN :startDate AND :endDate ORDER BY v.ctime DESC")
    List<VideoShareUserDayLog> findByPhotoIdAndDateRange(@Param("photoId") String photoId, 
                                                        @Param("startDate") LocalDateTime startDate, 
                                                        @Param("endDate") LocalDateTime endDate);
    
    /**
     * 查找指定日期范围内的记录（用于奖励检查）
     */
    List<VideoShareUserDayLog> findByPhotoIdAndCtimeBetween(String photoId, Long startDate, Long endDate);

    Optional<VideoShareUserDayLog> findByPhotoIdAndDate(String photoId, Integer today);
}

package com.videotask.repository;

import com.videotask.entity.VideoShareUserLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface VideoShareUserLogRepository extends JpaRepository<VideoShareUserLog, Integer> {
    List<VideoShareUserLog> findByUserId(Integer userId);
    Optional<VideoShareUserLog> findByVideoIdAndPlatformIdAndUserId(Integer videoId, Integer platformId, Integer userId);
    List<VideoShareUserLog> findByVideoIdAndUserId(Integer videoId, Integer userId);
    
    @Query("SELECT v FROM VideoShareUserLog v WHERE v.userId = :userId AND v.platformId = :platformId")
    List<VideoShareUserLog> findByUserIdAndPlatformId(@Param("userId") Integer userId, @Param("platformId") Integer platformId);
    
    /**
     * 根据平台ID和状态查找分享记录
     */
    List<VideoShareUserLog> findByPlatformIdAndStatus(Integer platformId, Integer status);
    
    /**
     * 根据状态查找分享记录
     */
    List<VideoShareUserLog> findByStatus(Integer status);
    
    /**
     * 查询用户今天在指定平台的发布数量
     */
    @Query("SELECT COUNT(v) FROM VideoShareUserLog v WHERE v.userId = :userId AND v.platformId = :platformId AND DATE(FROM_UNIXTIME(v.ctime)) = CURRENT_DATE")
    Integer countTodayPublishByUserIdAndPlatformId(@Param("userId") Integer userId, @Param("platformId") Integer platformId);
    
    /**
     * 根据用户ID、平台ID和状态查找分享记录
     */
    List<VideoShareUserLog> findByUserIdAndPlatformIdAndStatus(Integer userId, Integer platformId, Integer status);
    
    /**
     * 根据photoId查找分享记录
     */
    Optional<VideoShareUserLog> findByPhotoId(String photoId);
    
    /**
     * 查找需要检查奖励状态的记录（进行中的任务）
     */
    List<VideoShareUserLog> findByRewardStatus(Integer rewardStatus);
    
    /**
     * 根据用户ID查找分享记录
     */
    List<VideoShareUserLog> findByUserIdAndRewardStatus(Integer userId, Integer rewardStatus);
    
    /**
     * 根据用户ID和平台ID汇总播放数据
     */
    @Query("SELECT SUM(v.viewCount) as totalViews, SUM(v.likeCount) as totalLikes, " +
           "SUM(v.commentCount) as totalComments, COUNT(v) as totalShares " +
           "FROM VideoShareUserLog v " +
           "WHERE v.userId = :userId " +
           "AND (:platformId IS NULL OR v.platformId = :platformId)")
    Map getShareStatsByUserId(@Param("userId") Integer userId, @Param("platformId") Integer platformId);
    
    /**
     * 根据时间范围和平台ID查找分享记录
     */
    List<VideoShareUserLog> findByCtimeBetweenAndPlatformIdAndStatusOrderByCtimeDesc(Long startTime, Long endTime, Integer platformId, Integer status);
    
    /**
     * 根据时间范围查找分享记录
     */
    List<VideoShareUserLog> findByCtimeBetweenAndStatusOrderByCtimeDesc(Long startTime, Long endTime, Integer status);

    Optional<VideoShareUserLog> findByVideoIdAndPlatformIdAndUserIdAndStatusNot(Integer videoId, Integer platformId, Integer userId, int i);
}
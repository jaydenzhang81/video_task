package com.videotask.repository;

import com.videotask.entity.FinanceFlow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface FinanceFlowRepository extends JpaRepository<FinanceFlow, Integer> {
    
    // 分页查询用户财务流水
    Page<FinanceFlow> findByUserIdOrderByCreateTimeDesc(Integer userId, Pageable pageable);
    
    // 根据类型查询用户财务流水
    Page<FinanceFlow> findByUserIdAndTypeOrderByCreateTimeDesc(Integer userId, String type, Pageable pageable);
    
    // 根据时间范围查询用户财务流水
    @Query("SELECT f FROM FinanceFlow f WHERE f.userId = :userId AND f.createTime >= :startTime AND f.createTime <= :endTime ORDER BY f.createTime DESC")
    Page<FinanceFlow> findByUserIdAndTimeRange(@Param("userId") Integer userId, 
                                               @Param("startTime") Long startTime, 
                                               @Param("endTime") Long endTime, 
                                               Pageable pageable);
    
    // 根据类型和时间范围查询用户财务流水
    @Query("SELECT f FROM FinanceFlow f WHERE f.userId = :userId AND f.type = :type AND f.createTime >= :startTime AND f.createTime <= :endTime ORDER BY f.createTime DESC")
    Page<FinanceFlow> findByUserIdAndTypeAndTimeRange(@Param("userId") Integer userId, 
                                                      @Param("type") String type, 
                                                      @Param("startTime") Long startTime, 
                                                      @Param("endTime") Long endTime, 
                                                      Pageable pageable);
    
    // 计算用户总收入
    @Query("SELECT COALESCE(SUM(f.amount), 0) FROM FinanceFlow f WHERE f.userId = :userId AND f.type = 'income' AND f.status = 1")
    BigDecimal getTotalIncomeByUserId(@Param("userId") Integer userId);
    
    // 计算用户总支出
    @Query("SELECT COALESCE(SUM(f.amount), 0) FROM FinanceFlow f WHERE f.userId = :userId AND f.type IN ('expense', 'withdraw') AND f.status = 1")
    BigDecimal getTotalExpenseByUserId(@Param("userId") Integer userId);
    
    // 根据时间范围计算总收入
    @Query("SELECT COALESCE(SUM(f.amount), 0) FROM FinanceFlow f WHERE f.userId = :userId AND f.type = 'income' AND f.status = 1 AND f.createTime >= :startTime AND f.createTime <= :endTime")
    BigDecimal getTotalIncomeByUserIdAndTimeRange(@Param("userId") Integer userId, 
                                                  @Param("startTime") Long startTime, 
                                                  @Param("endTime") Long endTime);
    
    // 根据时间范围计算总支出
    @Query("SELECT COALESCE(SUM(f.amount), 0) FROM FinanceFlow f WHERE f.userId = :userId AND f.type IN ('expense', 'withdraw') AND f.status = 1 AND f.createTime >= :startTime AND f.createTime <= :endTime")
    BigDecimal getTotalExpenseByUserIdAndTimeRange(@Param("userId") Integer userId, 
                                                   @Param("startTime") Long startTime, 
                                                   @Param("endTime") Long endTime);
}

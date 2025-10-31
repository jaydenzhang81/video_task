package com.videotask.service;

import com.videotask.dto.ApiResponse;
import com.videotask.entity.Team;
import java.util.List;

/**
 * 团队服务接口
 * 定义团队相关的业务逻辑方法
 */
public interface TeamService {
    /**
     * 获取当前登录用户的团队信息
     * @param userId 用户ID
     * @return 团队信息
     */
    ApiResponse<Team> getCurrentUserTeam(Integer userId);
    
    /**
     * 获取所有团队列表
     * @return 团队列表
     */
    ApiResponse<List<Team>> getAllTeams();
    
    /**
     * 修改用户所属团队
     * @param userId 用户ID
     * @param teamId 新的团队ID
     * @return 操作结果
     */
    ApiResponse<Void> changeUserTeam(Integer userId, Integer teamId);
}
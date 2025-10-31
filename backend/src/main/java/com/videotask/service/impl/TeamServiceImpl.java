package com.videotask.service.impl;

import com.videotask.dto.ApiResponse;
import com.videotask.entity.Team;
import com.videotask.entity.User;
import com.videotask.repository.TeamRepository;
import com.videotask.repository.UserRepository;
import com.videotask.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

/**
 * 团队服务实现类
 * 实现团队相关的业务逻辑
 */
@Service
public class TeamServiceImpl implements TeamService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TeamRepository teamRepository;
    
    @Override
    public ApiResponse<Team> getCurrentUserTeam(Integer userId) {
        try {
            // 查询用户信息
            Optional<User> userOpt = userRepository.findById(userId);
            if (!userOpt.isPresent()) {
                return ApiResponse.error("用户不存在");
            }
            
            User user = userOpt.get();
            Integer teamId = user.getTeamId();
            
            // 如果用户没有所属团队（teamId为0或null），返回无团队信息
            if (teamId == null || teamId == 0) {
                Team noTeam = new Team();
                noTeam.setId(0);
                noTeam.setName("无团队");
                return ApiResponse.success(noTeam);
            }
            
            // 查询用户所属团队
            Optional<Team> teamOpt = teamRepository.findById(teamId);
            return teamOpt.map(ApiResponse::success)
                    .orElseGet(() -> {
                        // 如果团队不存在，返回无团队信息
                        Team noTeam = new Team();
                        noTeam.setId(0);
                        noTeam.setName("无团队");
                        return ApiResponse.success(noTeam);
                    });
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("获取团队信息失败");
        }
    }
    
    @Override
    public ApiResponse<List<Team>> getAllTeams() {
        try {
            // 获取所有团队列表
            List<Team> teams = teamRepository.findAll();
            return ApiResponse.success(teams);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("获取团队列表失败");
        }
    }
    
    @Override
    @Transactional
    public ApiResponse<Void> changeUserTeam(Integer userId, Integer teamId) {
        try {
            // 验证用户存在性
            Optional<User> userOpt = userRepository.findById(userId);
            if (!userOpt.isPresent()) {
                return ApiResponse.error("用户不存在");
            }
            
            // 验证团队存在性（teamId为0表示无团队）
            if (teamId != 0) {
                Optional<Team> teamOpt = teamRepository.findById(teamId);
                if (!teamOpt.isPresent()) {
                    return ApiResponse.error("团队不存在");
                }
            }
            
            // 更新用户所属团队
            User user = userOpt.get();
            user.setTeamId(teamId);
            userRepository.save(user);
            
            return ApiResponse.success();
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("修改团队失败");
        }
    }
}
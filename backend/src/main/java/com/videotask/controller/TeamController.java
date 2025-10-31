package com.videotask.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.videotask.dto.ApiResponse;
import com.videotask.entity.Team;
import com.videotask.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 团队控制器
 * 处理团队相关的HTTP请求
 */
@RestController
@RequestMapping("/api/team")
@CrossOrigin(origins = "*")
public class TeamController {
    
    @Autowired
    private TeamService teamService;
    
    /**
     * 获取当前登录用户的团队信息
     * @param request HTTP请求对象
     * @return 用户团队信息
     */
    @SaCheckLogin
    @GetMapping("/my-team")
    public ApiResponse<Team> getMyTeam(HttpServletRequest request) {
        try {
            // 获取登录用户ID
            Integer userId = Integer.valueOf(StpUtil.getLoginId().toString());
            return teamService.getCurrentUserTeam(userId);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("获取团队信息失败");
        }
    }
    
    /**
     * 获取所有团队列表
     * @return 团队列表
     */
    @SaCheckLogin
    @GetMapping("/list")
    public ApiResponse<List<Team>> getAllTeams() {
        return teamService.getAllTeams();
    }
    
    /**
     * 修改用户所属团队
     * @param request 请求体包含新的团队ID
     * @return 操作结果
     */
    @SaCheckLogin
    @PostMapping("/change")
    public ApiResponse<Void> changeTeam(@RequestBody Map<String, Object> request) {
        try {
            // 获取登录用户ID
            Integer userId = Integer.valueOf(StpUtil.getLoginId().toString());
            
            // 获取请求中的团队ID
            Integer teamId = Integer.parseInt(request.get("teamId").toString());
            
            return teamService.changeUserTeam(userId, teamId);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("修改团队失败");
        }
    }
}
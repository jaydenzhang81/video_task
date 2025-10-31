package com.videotask.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.videotask.dto.ApiResponse;
import com.videotask.service.RankingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@SaCheckLogin
@RestController
@RequestMapping("/api/ranking")
@CrossOrigin(origins = "*")
public class RankingController {

    @Autowired
    private RankingService rankingService;

    /**
     * 获取排行榜数据
     */
    @GetMapping("/list")
    public ApiResponse<Object> getRankingList(
            @RequestParam(defaultValue = "weekly") String type, // weekly, monthly, total
            @RequestParam(required = false) Integer platformId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            HttpServletRequest request) {
        
        Integer userId = Integer.valueOf(StpUtil.getLoginId().toString());
        return rankingService.getRankingList(userId, type, platformId, page, size);
    }

    /**
     * 获取我的排名
     */
    @GetMapping("/my-rank")
    public ApiResponse<Object> getMyRank(
            @RequestParam(defaultValue = "weekly") String type,
            @RequestParam(required = false) Integer platformId,
            HttpServletRequest request) {
        
        Integer userId = Integer.valueOf(StpUtil.getLoginId().toString());
        return rankingService.getMyRank(userId, type, platformId);
    }

    /**
     * 获取排行榜统计信息
     */
    @GetMapping("/stats")
    public ApiResponse<Object> getRankingStats(
            @RequestParam(defaultValue = "weekly") String type,
            @RequestParam(required = false) Integer platformId,
            HttpServletRequest request) {
        
        Integer userId = Integer.valueOf(StpUtil.getLoginId().toString());
        return rankingService.getRankingStats(userId, type, platformId);
    }
}

package com.videotask.config;


import cn.dev33.satoken.exception.NotLoginException;
import com.videotask.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 全局异常处理
 */
@ControllerAdvice
public class GlobalException {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalException.class);

    // 全局异常拦截（拦截项目中的所有异常）
    @ResponseBody
    @ExceptionHandler
    public ApiResponse handlerException(Exception e, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        // 不同异常返回不同状态码
        ApiResponse aj = new ApiResponse();
        if (e instanceof NotLoginException) {    // 如果是未登录异常
            NotLoginException ee = (NotLoginException) e;
            aj.setCode(10401);
            aj.setMessage(e.getMessage());
        } else {    // 普通异常, 输出：500 + 异常信息
            aj.setMessage(e.getMessage());
            aj.setCode(10001);
        }
        LOGGER.error("error", e);
        // 返回给前端
        return aj;
    }
}


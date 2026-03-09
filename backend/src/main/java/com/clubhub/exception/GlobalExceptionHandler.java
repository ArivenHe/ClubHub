package com.clubhub.exception;

import com.clubhub.dto.ApiResponse;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BizException.class)
    public ApiResponse<Void> handleBizException(BizException ex) {
        return ApiResponse.fail(ex.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class, ConstraintViolationException.class,
        HttpMessageNotReadableException.class})
    public ApiResponse<Void> handleValidationException(Exception ex) {
        return ApiResponse.fail("请求参数不合法");
    }

    @ExceptionHandler(NotLoginException.class)
    public ApiResponse<Void> handleNotLogin(NotLoginException ex) {
        return ApiResponse.fail("未登录或登录已过期");
    }

    @ExceptionHandler(NotPermissionException.class)
    public ApiResponse<Void> handleNoPermission(NotPermissionException ex) {
        return ApiResponse.fail("无权限访问");
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception ex) {
        log.error("Unhandled exception", ex);
        String exceptionType = ex.getClass().getSimpleName();
        String message = ex.getMessage();
        if (message == null || message.isBlank() || "null".equalsIgnoreCase(message.trim())) {
            Throwable root = ex.getCause();
            while (root != null && root.getCause() != null) {
                root = root.getCause();
            }
            if (root != null && root.getMessage() != null && !root.getMessage().isBlank()) {
                message = root.getMessage();
            } else {
                message = "无详细异常信息";
            }
        }
        return ApiResponse.fail("系统异常(" + exceptionType + "): " + message);
    }
}

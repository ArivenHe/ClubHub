package com.clubhub.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.clubhub.dto.ApiResponse;
import com.clubhub.dto.ChangePasswordRequest;
import com.clubhub.dto.LoginRequest;
import com.clubhub.dto.LoginResponse;
import com.clubhub.entity.User;
import com.clubhub.exception.BizException;
import com.clubhub.repository.UserRepository;
import com.clubhub.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        authService.logout();
        return ApiResponse.ok("退出成功", null);
    }

    @PostMapping("/password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request.getOldPassword(), request.getNewPassword());
        return ApiResponse.ok("密码修改成功", null);
    }

    @GetMapping("/me")
    public ApiResponse<Map<String, Object>> me() {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = userRepository.findById(userId).orElseThrow(() -> new BizException("用户不存在"));
        return ApiResponse.ok(Map.of(
            "id", user.getId(),
            "username", user.getUsername(),
            "realName", user.getRealName()
        ));
    }
}

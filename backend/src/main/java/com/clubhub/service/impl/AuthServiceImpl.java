package com.clubhub.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.clubhub.dto.LoginRequest;
import com.clubhub.dto.LoginResponse;
import com.clubhub.dto.LoginUserInfo;
import com.clubhub.entity.User;
import com.clubhub.exception.BizException;
import com.clubhub.repository.UserRepository;
import com.clubhub.service.AuthService;
import com.clubhub.service.RbacService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RbacService rbacService;

    @Override
    public LoginResponse login(LoginRequest request) {
        String loginId = request.getUsername().trim();
        User user = userRepository.findByUsername(loginId)
            .or(() -> userRepository.findByStudentNo(loginId))
            .orElseThrow(() -> new BizException("账号或密码错误"));

        if (!Boolean.TRUE.equals(user.getEnabled())) {
            throw new BizException("账号已被禁用");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BizException("账号或密码错误");
        }

        StpUtil.login(user.getId());
        String token = StpUtil.getTokenValue();

        List<String> roles = rbacService.getRoleCodeListByUserId(user.getId());
        List<String> permissions = rbacService.getPermissionCodeListByUserId(user.getId());

        LoginUserInfo info = LoginUserInfo.builder()
            .id(user.getId())
            .username(user.getUsername())
            .realName(user.getRealName())
            .roles(roles)
            .permissions(permissions)
            .build();

        return LoginResponse.builder()
            .token(token)
            .userInfo(info)
            .build();
    }

    @Override
    public void logout() {
        StpUtil.logout();
    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BizException("用户不存在"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BizException("旧密码错误");
        }
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new BizException("新密码不能与旧密码一致");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}

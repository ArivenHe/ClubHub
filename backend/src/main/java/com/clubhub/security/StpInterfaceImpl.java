package com.clubhub.security;

import cn.dev33.satoken.stp.StpInterface;
import com.clubhub.service.RbacService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final RbacService rbacService;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        Long userId = parseUserId(loginId);
        if (userId == null) {
            return Collections.emptyList();
        }
        return rbacService.getPermissionCodeListByUserId(userId);
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        Long userId = parseUserId(loginId);
        if (userId == null) {
            return Collections.emptyList();
        }
        return rbacService.getRoleCodeListByUserId(userId);
    }

    private Long parseUserId(Object loginId) {
        try {
            return Long.parseLong(String.valueOf(loginId));
        } catch (Exception e) {
            return null;
        }
    }
}

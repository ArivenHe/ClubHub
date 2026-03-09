package com.clubhub.service.impl;

import com.clubhub.entity.Permission;
import com.clubhub.entity.Role;
import com.clubhub.repository.PermissionRepository;
import com.clubhub.repository.RolePermissionRepository;
import com.clubhub.repository.RoleRepository;
import com.clubhub.repository.UserRoleRepository;
import com.clubhub.service.RbacService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RbacServiceImpl implements RbacService {

    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PermissionRepository permissionRepository;

    @Override
    public List<String> getRoleCodeListByUserId(Long userId) {
        List<Long> roleIds = userRoleRepository.findRoleIdsByUserId(userId);
        if (roleIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<Role> roles = roleRepository.findByIdIn(roleIds);
        return roles.stream()
            .map(Role::getCode)
            .distinct()
            .sorted()
            .collect(Collectors.toList());
    }

    @Override
    public List<String> getPermissionCodeListByUserId(Long userId) {
        List<Long> roleIds = userRoleRepository.findRoleIdsByUserId(userId);
        if (roleIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> permissionIds = rolePermissionRepository.findPermissionIdsByRoleIds(roleIds);
        if (permissionIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<Permission> permissions = permissionRepository.findByIdIn(permissionIds);
        return permissions.stream()
            .map(Permission::getCode)
            .distinct()
            .sorted()
            .collect(Collectors.toList());
    }
}

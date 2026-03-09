package com.clubhub.service.impl;

import com.clubhub.dto.CreatePermissionRequest;
import com.clubhub.dto.CreateRoleRequest;
import com.clubhub.dto.RbacPermissionResponse;
import com.clubhub.dto.RbacRoleResponse;
import com.clubhub.dto.UpdatePermissionRequest;
import com.clubhub.dto.UpdateRoleRequest;
import com.clubhub.entity.Permission;
import com.clubhub.entity.Role;
import com.clubhub.entity.RolePermission;
import com.clubhub.exception.BizException;
import com.clubhub.repository.PermissionRepository;
import com.clubhub.repository.RolePermissionRepository;
import com.clubhub.repository.RoleRepository;
import com.clubhub.repository.UserRoleRepository;
import com.clubhub.service.RbacAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RbacAdminServiceImpl implements RbacAdminService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final UserRoleRepository userRoleRepository;

    @Override
    public List<RbacRoleResponse> listRoles(String keyword) {
        List<Role> roles = roleRepository.findAllByOrderByNameAsc();
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim().toLowerCase();
            roles = roles.stream().filter(role -> {
                String code = role.getCode() == null ? "" : role.getCode().toLowerCase();
                String name = role.getName() == null ? "" : role.getName().toLowerCase();
                String description = role.getDescription() == null ? "" : role.getDescription().toLowerCase();
                return code.contains(kw) || name.contains(kw) || description.contains(kw);
            }).toList();
        }
        return buildRoleResponses(roles);
    }

    @Override
    @Transactional
    public RbacRoleResponse createRole(CreateRoleRequest request) {
        String code = normalizeCode(request.getCode());
        String name = normalizeName(request.getName());
        String description = normalizeDescription(request.getDescription());
        if (roleRepository.existsByCode(code)) {
            throw new BizException("角色编码已存在");
        }

        Role role = new Role();
        role.setCode(code);
        role.setName(name);
        role.setDescription(description);
        role = roleRepository.save(role);

        saveRolePermissions(role.getId(), request.getPermissionIds());
        return getRoleResponse(role.getId());
    }

    @Override
    @Transactional
    public RbacRoleResponse updateRole(Long roleId, UpdateRoleRequest request) {
        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new BizException("角色不存在"));

        String code = normalizeCode(request.getCode());
        String name = normalizeName(request.getName());
        String description = normalizeDescription(request.getDescription());

        roleRepository.findByCode(code).ifPresent(existing -> {
            if (!existing.getId().equals(roleId)) {
                throw new BizException("角色编码已存在");
            }
        });

        role.setCode(code);
        role.setName(name);
        role.setDescription(description);
        roleRepository.save(role);

        saveRolePermissions(roleId, request.getPermissionIds());
        return getRoleResponse(roleId);
    }

    @Override
    @Transactional
    public void deleteRole(Long roleId) {
        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new BizException("角色不存在"));
        if ("ADMIN".equals(role.getCode())) {
            throw new BizException("默认 ADMIN 角色不可删除");
        }
        if (userRoleRepository.existsByRoleId(roleId)) {
            throw new BizException("该角色已分配给用户，无法删除");
        }

        rolePermissionRepository.deleteByRoleId(roleId);
        roleRepository.deleteById(roleId);
    }

    @Override
    public List<RbacPermissionResponse> listPermissions(String keyword) {
        List<Permission> permissions = permissionRepository.findAllByOrderByCodeAsc();
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim().toLowerCase();
            permissions = permissions.stream().filter(permission -> {
                String code = permission.getCode() == null ? "" : permission.getCode().toLowerCase();
                String name = permission.getName() == null ? "" : permission.getName().toLowerCase();
                return code.contains(kw) || name.contains(kw);
            }).toList();
        }
        return permissions.stream().map(this::toPermissionResponse).toList();
    }

    @Override
    @Transactional
    public RbacPermissionResponse createPermission(CreatePermissionRequest request) {
        String code = normalizePermissionCode(request.getCode());
        String name = normalizePermissionName(request.getName());
        if (permissionRepository.existsByCode(code)) {
            throw new BizException("权限编码已存在");
        }

        Permission permission = new Permission();
        permission.setCode(code);
        permission.setName(name);
        permission = permissionRepository.save(permission);
        return toPermissionResponse(permission);
    }

    @Override
    @Transactional
    public RbacPermissionResponse updatePermission(Long permissionId, UpdatePermissionRequest request) {
        Permission permission = permissionRepository.findById(permissionId)
            .orElseThrow(() -> new BizException("权限不存在"));
        String code = normalizePermissionCode(request.getCode());
        String name = normalizePermissionName(request.getName());

        permissionRepository.findByCode(code).ifPresent(existing -> {
            if (!existing.getId().equals(permissionId)) {
                throw new BizException("权限编码已存在");
            }
        });

        permission.setCode(code);
        permission.setName(name);
        permission = permissionRepository.save(permission);
        return toPermissionResponse(permission);
    }

    @Override
    @Transactional
    public void deletePermission(Long permissionId) {
        Permission permission = permissionRepository.findById(permissionId)
            .orElseThrow(() -> new BizException("权限不存在"));
        if (rolePermissionRepository.existsByPermissionId(permissionId)) {
            throw new BizException("该权限已被角色使用，无法删除");
        }
        permissionRepository.delete(permission);
    }

    private List<RbacRoleResponse> buildRoleResponses(Collection<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return List.of();
        }

        List<Long> roleIds = roles.stream().map(Role::getId).distinct().toList();
        List<RolePermission> rolePermissions = rolePermissionRepository.findByRoleIdIn(roleIds);

        Map<Long, List<Long>> rolePermissionIdsMap = new HashMap<>();
        for (RolePermission rp : rolePermissions) {
            rolePermissionIdsMap.computeIfAbsent(rp.getRoleId(), key -> new ArrayList<>()).add(rp.getPermissionId());
        }

        List<Long> permissionIds = rolePermissions.stream().map(RolePermission::getPermissionId).distinct().toList();
        Map<Long, String> permissionCodeMap = permissionIds.isEmpty()
            ? Map.of()
            : permissionRepository.findByIdIn(permissionIds).stream()
                .collect(Collectors.toMap(Permission::getId, Permission::getCode, (a, b) -> a));

        return roles.stream().map(role -> {
            List<Long> ids = rolePermissionIdsMap.getOrDefault(role.getId(), Collections.emptyList());
            List<Long> sortedIds = ids.stream().distinct().sorted().toList();
            List<String> codes = sortedIds.stream().map(permissionCodeMap::get).filter(Objects::nonNull).sorted().toList();
            return RbacRoleResponse.builder()
                .id(role.getId())
                .code(role.getCode())
                .name(role.getName())
                .description(role.getDescription())
                .permissionIds(sortedIds)
                .permissionCodes(codes)
                .build();
        }).toList();
    }

    private RbacRoleResponse getRoleResponse(Long roleId) {
        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new BizException("角色不存在"));
        List<RbacRoleResponse> responses = buildRoleResponses(List.of(role));
        if (responses.isEmpty()) {
            throw new BizException("角色不存在");
        }
        return responses.get(0);
    }

    private void saveRolePermissions(Long roleId, List<Long> permissionIds) {
        List<Long> ids = permissionIds == null ? List.of() : permissionIds.stream()
            .filter(Objects::nonNull)
            .distinct()
            .toList();

        if (!ids.isEmpty()) {
            Set<Long> validPermissionIds = permissionRepository.findByIdIn(ids).stream()
                .map(Permission::getId)
                .collect(Collectors.toSet());
            for (Long id : ids) {
                if (!validPermissionIds.contains(id)) {
                    throw new BizException("权限不存在: " + id);
                }
            }
        }

        rolePermissionRepository.deleteByRoleId(roleId);
        for (Long permissionId : ids) {
            RolePermission rp = new RolePermission();
            rp.setRoleId(roleId);
            rp.setPermissionId(permissionId);
            rolePermissionRepository.save(rp);
        }
    }

    private RbacPermissionResponse toPermissionResponse(Permission permission) {
        return RbacPermissionResponse.builder()
            .id(permission.getId())
            .code(permission.getCode())
            .name(permission.getName())
            .build();
    }

    private String normalizeCode(String code) {
        if (code == null || code.isBlank()) {
            throw new BizException("角色编码不能为空");
        }
        return code.trim().toUpperCase();
    }

    private String normalizeName(String name) {
        if (name == null || name.isBlank()) {
            throw new BizException("角色名称不能为空");
        }
        return name.trim();
    }

    private String normalizeDescription(String description) {
        if (description == null) {
            return null;
        }
        String val = description.trim();
        return val.isEmpty() ? null : val;
    }

    private String normalizePermissionCode(String code) {
        if (code == null || code.isBlank()) {
            throw new BizException("权限编码不能为空");
        }
        return code.trim();
    }

    private String normalizePermissionName(String name) {
        if (name == null || name.isBlank()) {
            throw new BizException("权限名称不能为空");
        }
        return name.trim();
    }
}

package com.clubhub.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.clubhub.dto.ApiResponse;
import com.clubhub.dto.CreatePermissionRequest;
import com.clubhub.dto.CreateRoleRequest;
import com.clubhub.dto.RbacPermissionResponse;
import com.clubhub.dto.RbacRoleResponse;
import com.clubhub.dto.UpdatePermissionRequest;
import com.clubhub.dto.UpdateRoleRequest;
import com.clubhub.service.RbacAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/rbac")
@RequiredArgsConstructor
@SaCheckPermission("rbac:manage")
public class AdminRbacController {

    private final RbacAdminService rbacAdminService;

    @GetMapping("/roles")
    public ApiResponse<List<RbacRoleResponse>> listRoles(@RequestParam(required = false) String keyword) {
        return ApiResponse.ok(rbacAdminService.listRoles(keyword));
    }

    @PostMapping("/roles")
    public ApiResponse<RbacRoleResponse> createRole(@Valid @RequestBody CreateRoleRequest request) {
        return ApiResponse.ok("角色创建成功", rbacAdminService.createRole(request));
    }

    @PutMapping("/roles/{id}")
    public ApiResponse<RbacRoleResponse> updateRole(@PathVariable Long id, @Valid @RequestBody UpdateRoleRequest request) {
        return ApiResponse.ok("角色更新成功", rbacAdminService.updateRole(id, request));
    }

    @DeleteMapping("/roles/{id}")
    public ApiResponse<Void> deleteRole(@PathVariable Long id) {
        rbacAdminService.deleteRole(id);
        return ApiResponse.ok("角色删除成功", null);
    }

    @GetMapping("/permissions")
    public ApiResponse<List<RbacPermissionResponse>> listPermissions(@RequestParam(required = false) String keyword) {
        return ApiResponse.ok(rbacAdminService.listPermissions(keyword));
    }

    @PostMapping("/permissions")
    public ApiResponse<RbacPermissionResponse> createPermission(@Valid @RequestBody CreatePermissionRequest request) {
        return ApiResponse.ok("权限创建成功", rbacAdminService.createPermission(request));
    }

    @PutMapping("/permissions/{id}")
    public ApiResponse<RbacPermissionResponse> updatePermission(@PathVariable Long id,
                                                                @Valid @RequestBody UpdatePermissionRequest request) {
        return ApiResponse.ok("权限更新成功", rbacAdminService.updatePermission(id, request));
    }

    @DeleteMapping("/permissions/{id}")
    public ApiResponse<Void> deletePermission(@PathVariable Long id) {
        rbacAdminService.deletePermission(id);
        return ApiResponse.ok("权限删除成功", null);
    }
}

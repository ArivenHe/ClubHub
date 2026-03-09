package com.clubhub.service;

import com.clubhub.dto.CreatePermissionRequest;
import com.clubhub.dto.CreateRoleRequest;
import com.clubhub.dto.RbacPermissionResponse;
import com.clubhub.dto.RbacRoleResponse;
import com.clubhub.dto.UpdatePermissionRequest;
import com.clubhub.dto.UpdateRoleRequest;

import java.util.List;

public interface RbacAdminService {

    List<RbacRoleResponse> listRoles(String keyword);

    RbacRoleResponse createRole(CreateRoleRequest request);

    RbacRoleResponse updateRole(Long roleId, UpdateRoleRequest request);

    void deleteRole(Long roleId);

    List<RbacPermissionResponse> listPermissions(String keyword);

    RbacPermissionResponse createPermission(CreatePermissionRequest request);

    RbacPermissionResponse updatePermission(Long permissionId, UpdatePermissionRequest request);

    void deletePermission(Long permissionId);
}

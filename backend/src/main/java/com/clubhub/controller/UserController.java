package com.clubhub.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.clubhub.dto.ApiResponse;
import com.clubhub.dto.ImportUsersResult;
import com.clubhub.dto.RegisterRequest;
import com.clubhub.dto.ResetPasswordRequest;
import com.clubhub.dto.RoleResponse;
import com.clubhub.dto.UpdateUserRoleRequest;
import com.clubhub.dto.UpdateUserStatusRequest;
import com.clubhub.dto.UserResponse;
import com.clubhub.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @SaCheckPermission("user:manage")
    public ApiResponse<List<UserResponse>> listUsers(@RequestParam(required = false) String keyword,
                                                     @RequestParam(required = false) Boolean enabled,
                                                     @RequestParam(required = false) String roleCode) {
        return ApiResponse.ok(userService.listUsers(keyword, enabled, roleCode));
    }

    @PostMapping("/register")
    @SaCheckPermission("user:manage")
    public ApiResponse<UserResponse> registerByAdmin(@Valid @RequestBody RegisterRequest request,
                                                     @RequestParam(required = false) String roleCode) {
        return ApiResponse.ok("创建成功", userService.createUserByAdmin(request, roleCode));
    }

    @PostMapping("/import")
    @SaCheckPermission("user:import")
    public ApiResponse<ImportUsersResult> importUsers(@RequestPart("file") MultipartFile file,
                                                       @RequestParam(required = false) String roleCode) {
        return ApiResponse.ok("导入完成", userService.importUsersFromExcel(file, roleCode));
    }

    @GetMapping("/roles")
    @SaCheckPermission("user:manage")
    public ApiResponse<List<RoleResponse>> listRoles() {
        return ApiResponse.ok(userService.listRoles());
    }

    @PutMapping("/{id}/status")
    @SaCheckPermission("user:manage")
    public ApiResponse<UserResponse> updateStatus(@PathVariable Long id,
                                                  @Valid @RequestBody UpdateUserStatusRequest request) {
        return ApiResponse.ok("状态更新成功", userService.updateUserStatus(id, request.getEnabled()));
    }

    @PutMapping("/{id}/role")
    @SaCheckPermission("user:manage")
    public ApiResponse<UserResponse> updateRole(@PathVariable Long id,
                                                @Valid @RequestBody UpdateUserRoleRequest request) {
        return ApiResponse.ok("角色更新成功", userService.updateUserRole(id, request.getRoleCode()));
    }

    @PutMapping("/{id}/password")
    @SaCheckPermission("user:manage")
    public ApiResponse<Void> resetPassword(@PathVariable Long id,
                                           @Valid @RequestBody ResetPasswordRequest request) {
        userService.resetUserPassword(id, request.getNewPassword());
        return ApiResponse.ok("密码重置成功", null);
    }
}

package com.clubhub.service;

import com.clubhub.dto.ImportUsersResult;
import com.clubhub.dto.RegisterRequest;
import com.clubhub.dto.RoleResponse;
import com.clubhub.dto.UserResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {

    UserResponse createUserByAdmin(RegisterRequest request, String roleCode);

    List<UserResponse> listUsers(String keyword, Boolean enabled, String roleCode);

    ImportUsersResult importUsersFromExcel(MultipartFile file, String defaultRoleCode);

    UserResponse updateUserStatus(Long userId, boolean enabled);

    UserResponse updateUserRole(Long userId, String roleCode);

    void resetUserPassword(Long userId, String newPassword);

    List<RoleResponse> listRoles();
}

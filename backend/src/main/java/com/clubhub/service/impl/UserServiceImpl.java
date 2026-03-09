package com.clubhub.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.clubhub.dto.ImportUsersResult;
import com.clubhub.dto.RegisterRequest;
import com.clubhub.dto.RoleResponse;
import com.clubhub.dto.UserResponse;
import com.clubhub.entity.Role;
import com.clubhub.entity.User;
import com.clubhub.entity.UserRole;
import com.clubhub.exception.BizException;
import com.clubhub.repository.RoleRepository;
import com.clubhub.repository.UserRepository;
import com.clubhub.repository.UserRoleRepository;
import com.clubhub.service.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.default-password:123456}")
    private String defaultPassword;

    @Override
    @Transactional
    public UserResponse createUserByAdmin(RegisterRequest request, String roleCode) {
        String username = request.getUsername().trim();
        String realName = request.getRealName().trim();
        String studentNo = request.getStudentNo() == null ? null : request.getStudentNo().trim();
        String finalRoleCode = roleCode == null || roleCode.isBlank() ? "MEMBER" : roleCode.trim();

        if (userRepository.existsByUsername(username)) {
            throw new BizException("用户名已存在");
        }
        if (studentNo != null && !studentNo.isBlank() && userRepository.existsByStudentNo(studentNo)) {
            throw new BizException("学号已存在");
        }

        Role targetRole = roleRepository.findByCode(finalRoleCode)
            .orElseThrow(() -> new BizException("角色不存在: " + finalRoleCode));

        User user = createUserAndBindRole(username, realName, blankToNull(studentNo), request.getPassword(), targetRole);
        return toUserResponse(user, List.of(targetRole.getCode()));
    }

    @Override
    public List<UserResponse> listUsers(String keyword, Boolean enabled, String roleCode) {
        List<User> users = userRepository.findAll();
        Map<Long, List<String>> roleMap = buildRoleCodeMap(users.stream().map(User::getId).toList());
        String kw = keyword == null ? null : keyword.trim().toLowerCase();
        String roleCodeFilter = roleCode == null ? null : roleCode.trim().toUpperCase();

        return users.stream()
            .filter(user -> {
                if (kw == null || kw.isEmpty()) {
                    return true;
                }
                String username = user.getUsername() == null ? "" : user.getUsername().toLowerCase();
                String realName = user.getRealName() == null ? "" : user.getRealName().toLowerCase();
                String studentNo = user.getStudentNo() == null ? "" : user.getStudentNo().toLowerCase();
                return username.contains(kw) || realName.contains(kw) || studentNo.contains(kw);
            })
            .filter(user -> enabled == null || Objects.equals(Boolean.TRUE.equals(user.getEnabled()), enabled))
            .map(user -> {
                List<String> roles = roleMap.getOrDefault(user.getId(), Collections.emptyList());
                return Map.entry(user, roles);
            })
            .filter(entry -> roleCodeFilter == null || roleCodeFilter.isEmpty()
                || entry.getValue().stream().anyMatch(code -> roleCodeFilter.equalsIgnoreCase(code)))
            .map(entry -> toUserResponse(entry.getKey(), entry.getValue()))
            .toList();
    }

    @Override
    @Transactional
    public ImportUsersResult importUsersFromExcel(MultipartFile file, String defaultRoleCode) {
        if (file == null || file.isEmpty()) {
            throw new BizException("请上传 Excel 文件");
        }

        String roleCode = (defaultRoleCode == null || defaultRoleCode.isBlank()) ? "MEMBER" : defaultRoleCode.trim();
        Role defaultRole = roleRepository.findByCode(roleCode)
            .orElseThrow(() -> new BizException("默认角色不存在: " + roleCode));

        int success = 0;
        int skip = 0;
        List<String> reasons = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();

        try (InputStream inputStream = file.getInputStream(); Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null || sheet.getPhysicalNumberOfRows() <= 1) {
                throw new BizException("Excel 内容为空，至少需要一行数据");
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }

                String username = formatter.formatCellValue(row.getCell(0)).trim();
                String realName = formatter.formatCellValue(row.getCell(1)).trim();
                String studentNo = formatter.formatCellValue(row.getCell(2)).trim();

                if (username.isBlank() || realName.isBlank()) {
                    skip++;
                    reasons.add("第 " + (i + 1) + " 行: username/realName 不能为空");
                    continue;
                }

                if (userRepository.existsByUsername(username)) {
                    skip++;
                    reasons.add("第 " + (i + 1) + " 行: 用户名已存在 " + username);
                    continue;
                }

                if (!studentNo.isBlank() && userRepository.existsByStudentNo(studentNo)) {
                    skip++;
                    reasons.add("第 " + (i + 1) + " 行: 学号已存在 " + studentNo);
                    continue;
                }

                createUserAndBindRole(username, realName, blankToNull(studentNo), defaultPassword, defaultRole);
                success++;
            }
        } catch (IOException e) {
            throw new BizException("读取 Excel 失败: " + e.getMessage());
        }

        return ImportUsersResult.builder()
            .successCount(success)
            .skipCount(skip)
            .skipReasons(reasons)
            .build();
    }

    @Override
    @Transactional
    public UserResponse updateUserStatus(Long userId, boolean enabled) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BizException("用户不存在"));

        Long currentUserId = StpUtil.getLoginIdAsLong();
        if (!enabled && Objects.equals(currentUserId, userId)) {
            throw new BizException("不能禁用当前登录账号");
        }

        user.setEnabled(enabled);
        User saved = userRepository.save(user);
        return toUserResponse(saved, getRoleCodesByUserId(saved.getId()));
    }

    @Override
    @Transactional
    public UserResponse updateUserRole(Long userId, String roleCode) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BizException("用户不存在"));
        Role role = roleRepository.findByCode(roleCode)
            .orElseThrow(() -> new BizException("角色不存在: " + roleCode));

        Long currentUserId = StpUtil.getLoginIdAsLong();
        if (Objects.equals(currentUserId, userId) && !"ADMIN".equals(role.getCode())) {
            throw new BizException("当前登录管理员角色不能降级");
        }

        userRoleRepository.deleteByUserId(userId);
        UserRole userRole = new UserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(role.getId());
        userRoleRepository.save(userRole);

        return toUserResponse(user, List.of(role.getCode()));
    }

    @Override
    @Transactional
    public void resetUserPassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BizException("用户不存在"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public List<RoleResponse> listRoles() {
        return roleRepository.findAllByOrderByNameAsc().stream()
            .map(role -> RoleResponse.builder()
                .id(role.getId())
                .code(role.getCode())
                .name(role.getName())
                .description(role.getDescription())
                .build())
            .toList();
    }

    private User createUserAndBindRole(String username, String realName, String studentNo, String rawPassword, Role role) {
        User user = new User();
        user.setUsername(username);
        user.setRealName(realName);
        user.setStudentNo(studentNo);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setEnabled(true);
        user = userRepository.save(user);

        UserRole userRole = new UserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(role.getId());
        userRoleRepository.save(userRole);
        return user;
    }

    private Map<Long, List<String>> buildRoleCodeMap(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }

        List<UserRole> userRoles = userRoleRepository.findByUserIdIn(userIds);
        if (userRoles.isEmpty()) {
            return Map.of();
        }

        List<Long> roleIds = userRoles.stream().map(UserRole::getRoleId).distinct().toList();
        Map<Long, String> roleCodeMap = roleRepository.findByIdIn(roleIds).stream()
            .collect(Collectors.toMap(Role::getId, Role::getCode, (a, b) -> a));

        Map<Long, List<String>> result = new HashMap<>();
        for (UserRole userRole : userRoles) {
            String roleCode = roleCodeMap.get(userRole.getRoleId());
            if (roleCode == null) {
                continue;
            }
            result.computeIfAbsent(userRole.getUserId(), k -> new ArrayList<>()).add(roleCode);
        }

        return result;
    }

    private List<String> getRoleCodesByUserId(Long userId) {
        return buildRoleCodeMap(List.of(userId)).getOrDefault(userId, Collections.emptyList());
    }

    private UserResponse toUserResponse(User user, List<String> roleCodes) {
        return UserResponse.builder()
            .id(user.getId())
            .username(user.getUsername())
            .realName(user.getRealName())
            .studentNo(user.getStudentNo())
            .enabled(Boolean.TRUE.equals(user.getEnabled()))
            .roles(roleCodes)
            .build();
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value;
    }
}

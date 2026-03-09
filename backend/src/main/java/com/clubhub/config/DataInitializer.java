package com.clubhub.config;

import com.clubhub.entity.Permission;
import com.clubhub.entity.Role;
import com.clubhub.entity.RolePermission;
import com.clubhub.entity.User;
import com.clubhub.entity.UserRole;
import com.clubhub.repository.PermissionRepository;
import com.clubhub.repository.RolePermissionRepository;
import com.clubhub.repository.RoleRepository;
import com.clubhub.repository.UserRepository;
import com.clubhub.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.default-password:123456}")
    private String defaultPassword;

    @Override
    @Transactional
    public void run(String... args) {
        Role adminRole = ensureRole("ADMIN", "管理员", "系统管理员角色");
        Role memberRole = ensureRole("MEMBER", "成员", "社团成员角色");

        Map<String, String> permissionMap = new LinkedHashMap<>();
        permissionMap.put("user:import", "批量导入用户");
        permissionMap.put("user:manage", "管理用户");
        permissionMap.put("rbac:manage", "管理角色与权限");
        permissionMap.put("doc:upload", "上传文档");
        permissionMap.put("doc:read", "查看文档");
        permissionMap.put("doc:manage", "后台管理文章");
        permissionMap.put("tag:read", "查看标签");
        permissionMap.put("tag:create", "创建标签");
        permissionMap.put("tag:manage", "管理标签");
        permissionMap.put("software:create", "新增软件推荐");
        permissionMap.put("software:read", "查看软件推荐");

        Map<String, Permission> permissions = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : permissionMap.entrySet()) {
            permissions.put(entry.getKey(), ensurePermission(entry.getKey(), entry.getValue()));
        }

        for (Permission permission : permissions.values()) {
            bindRolePermission(adminRole.getId(), permission.getId());
        }
        bindRolePermission(memberRole.getId(), permissions.get("doc:upload").getId());
        bindRolePermission(memberRole.getId(), permissions.get("doc:read").getId());
        bindRolePermission(memberRole.getId(), permissions.get("tag:read").getId());
        bindRolePermission(memberRole.getId(), permissions.get("tag:create").getId());
        bindRolePermission(memberRole.getId(), permissions.get("software:read").getId());

        User admin = userRepository.findByUsername("admin").orElseGet(() -> {
            User user = new User();
            user.setUsername("admin");
            user.setRealName("系统管理员");
            user.setStudentNo(null);
            user.setPassword(passwordEncoder.encode(defaultPassword));
            user.setEnabled(true);
            return userRepository.save(user);
        });

        if (!userRoleRepository.existsByUserIdAndRoleId(admin.getId(), adminRole.getId())) {
            UserRole userRole = new UserRole();
            userRole.setUserId(admin.getId());
            userRole.setRoleId(adminRole.getId());
            userRoleRepository.save(userRole);
        }
    }

    private Role ensureRole(String code, String name, String description) {
        return roleRepository.findByCode(code).orElseGet(() -> {
            Role role = new Role();
            role.setCode(code);
            role.setName(name);
            role.setDescription(description);
            return roleRepository.save(role);
        });
    }

    private Permission ensurePermission(String code, String name) {
        return permissionRepository.findByCode(code).orElseGet(() -> {
            Permission permission = new Permission();
            permission.setCode(code);
            permission.setName(name);
            return permissionRepository.save(permission);
        });
    }

    private void bindRolePermission(Long roleId, Long permissionId) {
        if (!rolePermissionRepository.existsByRoleIdAndPermissionId(roleId, permissionId)) {
            RolePermission rp = new RolePermission();
            rp.setRoleId(roleId);
            rp.setPermissionId(permissionId);
            rolePermissionRepository.save(rp);
        }
    }
}

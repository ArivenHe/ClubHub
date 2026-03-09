package com.clubhub.repository;

import com.clubhub.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {

    boolean existsByRoleIdAndPermissionId(Long roleId, Long permissionId);

    boolean existsByPermissionId(Long permissionId);

    List<RolePermission> findByRoleId(Long roleId);

    List<RolePermission> findByRoleIdIn(Collection<Long> roleIds);

    void deleteByRoleId(Long roleId);

    void deleteByPermissionId(Long permissionId);

    @Query("select rp.permissionId from RolePermission rp where rp.roleId in :roleIds")
    List<Long> findPermissionIdsByRoleIds(List<Long> roleIds);
}

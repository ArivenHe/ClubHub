package com.clubhub.repository;

import com.clubhub.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PermissionRepository extends JpaRepository<Permission, Long> {

    Optional<Permission> findByCode(String code);

    boolean existsByCode(String code);

    List<Permission> findByIdIn(Collection<Long> ids);

    List<Permission> findAllByOrderByCodeAsc();
}

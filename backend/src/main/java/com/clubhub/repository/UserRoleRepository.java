package com.clubhub.repository;

import com.clubhub.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    List<UserRole> findByUserId(Long userId);

    List<UserRole> findByUserIdIn(Collection<Long> userIds);

    boolean existsByRoleId(Long roleId);

    boolean existsByUserIdAndRoleId(Long userId, Long roleId);

    void deleteByUserId(Long userId);

    @Query("select ur.roleId from UserRole ur where ur.userId = :userId")
    List<Long> findRoleIdsByUserId(Long userId);
}

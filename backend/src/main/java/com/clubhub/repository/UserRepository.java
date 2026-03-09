package com.clubhub.repository;

import com.clubhub.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByStudentNo(String studentNo);

    boolean existsByUsername(String username);

    boolean existsByStudentNo(String studentNo);

    List<User> findByIdIn(Collection<Long> ids);
}

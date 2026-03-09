package com.clubhub.repository;

import com.clubhub.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {

    boolean existsByNameIgnoreCase(String name);

    Optional<Tag> findByNameIgnoreCase(String name);

    List<Tag> findByIdIn(Collection<Long> ids);

    List<Tag> findAllByOrderByCreatedAtDesc();
}

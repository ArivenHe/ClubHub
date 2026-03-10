package com.clubhub.repository;

import com.clubhub.entity.SoftwareTool;
import com.clubhub.enums.SoftwareToolStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SoftwareToolRepository extends JpaRepository<SoftwareTool, Long> {

    List<SoftwareTool> findAllByOrderByCreatedAtDesc();

    List<SoftwareTool> findByStatusOrderByCreatedAtDesc(SoftwareToolStatus status);
}

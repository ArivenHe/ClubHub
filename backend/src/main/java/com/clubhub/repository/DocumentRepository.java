package com.clubhub.repository;

import com.clubhub.entity.Document;
import com.clubhub.enums.DocumentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long>, JpaSpecificationExecutor<Document> {

    List<Document> findByAuthorIdOrderByCreatedAtDesc(Long authorId);

    List<Document> findAllByOrderByCreatedAtDesc();

    List<Document> findByStatusOrderByCreatedAtDesc(DocumentStatus status);

    List<Document> findByStatusAndRecommendedTrueOrderByUpdatedAtDesc(DocumentStatus status, Pageable pageable);

    List<Document> findByIdIn(Collection<Long> ids);

    long countByTagId(Long tagId);

    @Modifying
    @Query("update Document d set d.viewCount = coalesce(d.viewCount, 0) + 1 where d.id = :id")
    int incrementViewCountById(@Param("id") Long id);
}

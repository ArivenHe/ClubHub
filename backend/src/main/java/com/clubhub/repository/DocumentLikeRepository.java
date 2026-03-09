package com.clubhub.repository;

import com.clubhub.entity.DocumentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DocumentLikeRepository extends JpaRepository<DocumentLike, Long> {

    boolean existsByDocumentIdAndUserId(Long documentId, Long userId);

    long countByDocumentId(Long documentId);

    void deleteByDocumentId(Long documentId);

    void deleteByDocumentIdAndUserId(Long documentId, Long userId);

    @Query("select dl.documentId, count(dl.id) from DocumentLike dl where dl.documentId in :documentIds group by dl.documentId")
    List<Object[]> countGroupedByDocumentIds(List<Long> documentIds);

    @Query("select dl.documentId from DocumentLike dl where dl.userId = :userId and dl.documentId in :documentIds")
    List<Long> findLikedDocumentIdsByUserIdAndDocumentIds(Long userId, List<Long> documentIds);
}

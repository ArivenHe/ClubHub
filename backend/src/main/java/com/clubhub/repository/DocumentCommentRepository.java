package com.clubhub.repository;

import com.clubhub.entity.DocumentComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentCommentRepository extends JpaRepository<DocumentComment, Long> {

    List<DocumentComment> findByDocumentIdOrderByCreatedAtAsc(Long documentId);

    List<DocumentComment> findAllByOrderByCreatedAtDesc();

    void deleteByDocumentId(Long documentId);
}

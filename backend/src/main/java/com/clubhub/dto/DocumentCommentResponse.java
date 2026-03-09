package com.clubhub.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class DocumentCommentResponse {

    private Long id;
    private Long documentId;
    private Long authorId;
    private String authorName;
    private String content;
    private boolean canDelete;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

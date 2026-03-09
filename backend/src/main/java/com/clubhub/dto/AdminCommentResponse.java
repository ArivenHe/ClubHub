package com.clubhub.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminCommentResponse {

    private Long id;
    private Long documentId;
    private String documentTitle;
    private String documentAuthorName;
    private Long authorId;
    private String authorName;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

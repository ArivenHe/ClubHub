package com.clubhub.dto;

import com.clubhub.enums.DocumentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class DocumentResponse {

    private Long id;
    private String title;
    private String summary;
    private String content;
    private String attachmentPath;
    private Long authorId;
    private String authorName;
    private String semesterTag;
    private Long tagId;
    private String tagName;
    private DocumentStatus status;
    private Boolean recommended;
    private Long viewCount;
    private Long likeCount;
    private Boolean liked;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

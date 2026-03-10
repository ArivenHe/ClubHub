package com.clubhub.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {

    private Long id;
    private String type;
    private String title;
    private String content;
    private Long relatedId;
    private Boolean read;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
}

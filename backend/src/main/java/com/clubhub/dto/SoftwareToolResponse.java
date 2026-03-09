package com.clubhub.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SoftwareToolResponse {

    private Long id;
    private String name;
    private String category;
    private String downloadUrl;
    private String description;
    private String recommendedBy;
    private LocalDateTime createdAt;
}

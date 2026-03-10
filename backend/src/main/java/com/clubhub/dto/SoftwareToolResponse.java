package com.clubhub.dto;

import com.clubhub.enums.SoftwareToolStatus;
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
    private Long applicantId;
    private SoftwareToolStatus status;
    private String reviewRemark;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
}

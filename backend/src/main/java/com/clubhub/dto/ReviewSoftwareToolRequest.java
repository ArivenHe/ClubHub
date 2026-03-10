package com.clubhub.dto;

import com.clubhub.enums.SoftwareToolStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReviewSoftwareToolRequest {

    @NotNull
    private SoftwareToolStatus status;

    @Size(max = 255)
    private String reviewRemark;
}

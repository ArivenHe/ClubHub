package com.clubhub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateSoftwareToolRequest {

    @NotBlank
    @Size(max = 80)
    private String name;

    @Size(max = 80)
    private String category;

    @Size(max = 255)
    private String downloadUrl;

    @Size(max = 500)
    private String description;

    @Size(max = 80)
    private String recommendedBy;
}

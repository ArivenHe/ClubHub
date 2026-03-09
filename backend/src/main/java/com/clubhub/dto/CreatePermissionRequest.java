package com.clubhub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreatePermissionRequest {

    @NotBlank
    @Size(max = 80)
    private String code;

    @NotBlank
    @Size(max = 80)
    private String name;
}

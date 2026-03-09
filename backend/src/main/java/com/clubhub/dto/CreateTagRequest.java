package com.clubhub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateTagRequest {

    @NotBlank
    @Size(max = 50)
    private String name;

    @Size(max = 255)
    private String description;
}

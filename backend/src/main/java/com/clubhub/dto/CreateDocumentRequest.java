package com.clubhub.dto;

import com.clubhub.enums.DocumentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateDocumentRequest {

    @NotBlank
    @Size(max = 120)
    private String title;

    @Size(max = 500)
    private String summary;

    private String content;

    @Size(max = 50)
    private String semesterTag;

    private Long tagId;

    private DocumentStatus status;
}

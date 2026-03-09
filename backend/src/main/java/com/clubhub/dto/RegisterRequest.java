package com.clubhub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank
    @Size(min = 4, max = 50)
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字、下划线")
    private String username;

    @NotBlank
    @Size(min = 6, max = 30)
    private String password;

    @NotBlank
    @Size(max = 50)
    private String realName;

    @Size(max = 30)
    private String studentNo;
}

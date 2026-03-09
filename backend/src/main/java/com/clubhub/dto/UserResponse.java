package com.clubhub.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UserResponse {

    private Long id;
    private String username;
    private String realName;
    private String studentNo;
    private boolean enabled;
    private List<String> roles;
}

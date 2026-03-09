package com.clubhub.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class LoginUserInfo {

    private Long id;
    private String username;
    private String realName;
    private List<String> roles;
    private List<String> permissions;
}

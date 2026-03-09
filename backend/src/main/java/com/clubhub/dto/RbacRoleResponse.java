package com.clubhub.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RbacRoleResponse {

    private Long id;
    private String code;
    private String name;
    private String description;
    private List<Long> permissionIds;
    private List<String> permissionCodes;
}

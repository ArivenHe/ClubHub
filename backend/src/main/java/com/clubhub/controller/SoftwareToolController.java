package com.clubhub.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.clubhub.dto.ApiResponse;
import com.clubhub.dto.CreateSoftwareToolRequest;
import com.clubhub.dto.SoftwareToolResponse;
import com.clubhub.service.SoftwareToolService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/software-tools")
@RequiredArgsConstructor
public class SoftwareToolController {

    private final SoftwareToolService softwareToolService;

    @GetMapping
    @SaCheckPermission("software:read")
    public ApiResponse<List<SoftwareToolResponse>> list() {
        return ApiResponse.ok(softwareToolService.listTools());
    }

    @PostMapping
    @SaCheckPermission("software:create")
    public ApiResponse<SoftwareToolResponse> create(@Valid @RequestBody CreateSoftwareToolRequest request) {
        return ApiResponse.ok("新增成功", softwareToolService.createTool(request));
    }

    @PostMapping("/applications")
    @SaCheckPermission("software:read")
    public ApiResponse<SoftwareToolResponse> submitApplication(@Valid @RequestBody CreateSoftwareToolRequest request) {
        return ApiResponse.ok("申请已提交，等待审核", softwareToolService.submitApplication(request));
    }
}

package com.clubhub.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.clubhub.dto.ApiResponse;
import com.clubhub.dto.ReviewSoftwareToolRequest;
import com.clubhub.dto.SoftwareToolResponse;
import com.clubhub.service.SoftwareToolService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/software-tools")
@RequiredArgsConstructor
public class AdminSoftwareToolController {

    private final SoftwareToolService softwareToolService;

    @GetMapping
    @SaCheckPermission("software:create")
    public ApiResponse<List<SoftwareToolResponse>> list() {
        return ApiResponse.ok(softwareToolService.adminListTools());
    }

    @PutMapping("/{id}/status")
    @SaCheckPermission("software:create")
    public ApiResponse<SoftwareToolResponse> review(@PathVariable Long id,
                                                    @Valid @RequestBody ReviewSoftwareToolRequest request) {
        return ApiResponse.ok(
            "审核状态更新成功",
            softwareToolService.reviewTool(id, request.getStatus(), request.getReviewRemark())
        );
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("software:create")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        softwareToolService.deleteTool(id);
        return ApiResponse.ok("删除成功", null);
    }
}

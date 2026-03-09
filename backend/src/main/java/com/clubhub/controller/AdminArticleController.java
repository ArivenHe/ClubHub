package com.clubhub.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.clubhub.dto.ApiResponse;
import com.clubhub.dto.DocumentResponse;
import com.clubhub.enums.DocumentStatus;
import com.clubhub.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/articles")
@RequiredArgsConstructor
@SaCheckPermission("doc:manage")
public class AdminArticleController {

    private final DocumentService documentService;

    @GetMapping
    public ApiResponse<List<DocumentResponse>> list(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) DocumentStatus status,
        @RequestParam(required = false) Boolean recommended
    ) {
        return ApiResponse.ok(documentService.adminListDocuments(keyword, status, recommended));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<DocumentResponse> updateStatus(@PathVariable Long id, @RequestParam DocumentStatus status) {
        return ApiResponse.ok("状态更新成功", documentService.adminUpdateStatus(id, status));
    }

    @PutMapping("/{id}/recommended")
    public ApiResponse<DocumentResponse> updateRecommended(@PathVariable Long id, @RequestParam boolean recommended) {
        return ApiResponse.ok("推荐状态更新成功", documentService.adminUpdateRecommended(id, recommended));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        documentService.adminDeleteDocument(id);
        return ApiResponse.ok("删除成功", null);
    }
}

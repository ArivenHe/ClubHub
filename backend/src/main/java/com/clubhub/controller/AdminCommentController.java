package com.clubhub.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.clubhub.dto.AdminCommentResponse;
import com.clubhub.dto.ApiResponse;
import com.clubhub.service.AdminCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/comments")
@RequiredArgsConstructor
@SaCheckPermission("doc:manage")
public class AdminCommentController {

    private final AdminCommentService adminCommentService;

    @GetMapping
    public ApiResponse<List<AdminCommentResponse>> list(@RequestParam(required = false) String keyword,
                                                        @RequestParam(required = false) Long documentId) {
        return ApiResponse.ok(adminCommentService.listComments(keyword, documentId));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        adminCommentService.deleteComment(id);
        return ApiResponse.ok("删除成功", null);
    }
}

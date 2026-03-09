package com.clubhub.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.clubhub.dto.ApiResponse;
import com.clubhub.dto.CreateDocumentCommentRequest;
import com.clubhub.dto.CreateDocumentRequest;
import com.clubhub.dto.DocumentCommentResponse;
import com.clubhub.dto.DocumentResponse;
import com.clubhub.service.DocumentCommentService;
import com.clubhub.service.DocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;
    private final DocumentCommentService documentCommentService;

    @PostMapping
    @SaCheckPermission("doc:upload")
    public ApiResponse<DocumentResponse> create(@Valid @RequestBody CreateDocumentRequest request) {
        return ApiResponse.ok("保存成功", documentService.createDocument(request));
    }

    @PutMapping("/{id}")
    @SaCheckPermission("doc:upload")
    public ApiResponse<DocumentResponse> updateMine(@PathVariable Long id, @Valid @RequestBody CreateDocumentRequest request) {
        return ApiResponse.ok("更新成功", documentService.updateMyDocument(id, request));
    }

    @GetMapping("/mine")
    @SaCheckPermission("doc:read")
    public ApiResponse<List<DocumentResponse>> mine() {
        return ApiResponse.ok(documentService.listMyDocuments());
    }

    @GetMapping
    @SaCheckPermission("doc:read")
    public ApiResponse<List<DocumentResponse>> listAll() {
        return ApiResponse.ok(documentService.listAllDocuments());
    }

    @GetMapping("/{id}")
    @SaCheckPermission("doc:read")
    public ApiResponse<DocumentResponse> detail(@PathVariable Long id) {
        return ApiResponse.ok(documentService.getDocumentDetail(id));
    }

    @PostMapping("/{id}/likes")
    @SaCheckPermission("doc:read")
    public ApiResponse<DocumentResponse> like(@PathVariable Long id) {
        return ApiResponse.ok("点赞成功", documentService.likeDocument(id));
    }

    @DeleteMapping("/{id}/likes")
    @SaCheckPermission("doc:read")
    public ApiResponse<DocumentResponse> unlike(@PathVariable Long id) {
        return ApiResponse.ok("取消点赞成功", documentService.unlikeDocument(id));
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("doc:upload")
    public ApiResponse<Void> deleteMine(@PathVariable Long id) {
        documentService.deleteMyDocument(id);
        return ApiResponse.ok("删除成功", null);
    }

    @GetMapping("/{id}/comments")
    @SaCheckPermission("doc:read")
    public ApiResponse<List<DocumentCommentResponse>> listComments(@PathVariable Long id) {
        return ApiResponse.ok(documentCommentService.listComments(id));
    }

    @PostMapping("/{id}/comments")
    @SaCheckPermission("doc:read")
    public ApiResponse<DocumentCommentResponse> createComment(@PathVariable Long id,
                                                              @Valid @RequestBody CreateDocumentCommentRequest request) {
        return ApiResponse.ok("评论成功", documentCommentService.createComment(id, request));
    }

    @DeleteMapping("/comments/{commentId}")
    @SaCheckPermission("doc:read")
    public ApiResponse<Void> deleteComment(@PathVariable Long commentId) {
        documentCommentService.deleteComment(commentId);
        return ApiResponse.ok("评论删除成功", null);
    }

    @GetMapping("/recommended")
    @SaCheckPermission("doc:read")
    public ApiResponse<List<DocumentResponse>> listRecommended(@RequestParam(defaultValue = "6") int limit) {
        return ApiResponse.ok(documentService.listRecommendedDocuments(limit));
    }
}

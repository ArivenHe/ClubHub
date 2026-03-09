package com.clubhub.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.clubhub.dto.ApiResponse;
import com.clubhub.dto.CreateTagRequest;
import com.clubhub.dto.TagResponse;
import com.clubhub.dto.UpdateTagRequest;
import com.clubhub.service.TagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/tags")
@RequiredArgsConstructor
@SaCheckPermission("tag:manage")
public class AdminTagController {

    private final TagService tagService;

    @GetMapping
    public ApiResponse<List<TagResponse>> list() {
        return ApiResponse.ok(tagService.listAdminTags());
    }

    @PostMapping
    public ApiResponse<TagResponse> create(@Valid @RequestBody CreateTagRequest request) {
        return ApiResponse.ok("新增成功", tagService.createTag(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<TagResponse> update(@PathVariable Long id, @Valid @RequestBody UpdateTagRequest request) {
        return ApiResponse.ok("更新成功", tagService.updateTag(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        tagService.deleteTag(id);
        return ApiResponse.ok("删除成功", null);
    }
}

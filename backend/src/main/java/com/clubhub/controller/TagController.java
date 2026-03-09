package com.clubhub.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.clubhub.dto.ApiResponse;
import com.clubhub.dto.CreateTagRequest;
import com.clubhub.dto.TagResponse;
import com.clubhub.service.TagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @GetMapping
    @SaCheckPermission("tag:read")
    public ApiResponse<List<TagResponse>> list() {
        return ApiResponse.ok(tagService.listPublicTags());
    }

    @PostMapping
    @SaCheckPermission("tag:create")
    public ApiResponse<TagResponse> create(@Valid @RequestBody CreateTagRequest request) {
        return ApiResponse.ok("创建成功", tagService.createTag(request));
    }
}

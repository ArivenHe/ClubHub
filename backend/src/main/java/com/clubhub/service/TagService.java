package com.clubhub.service;

import com.clubhub.dto.CreateTagRequest;
import com.clubhub.dto.TagResponse;
import com.clubhub.dto.UpdateTagRequest;

import java.util.List;

public interface TagService {

    TagResponse createTag(CreateTagRequest request);

    TagResponse updateTag(Long tagId, UpdateTagRequest request);

    void deleteTag(Long tagId);

    List<TagResponse> listAdminTags();

    List<TagResponse> listPublicTags();
}

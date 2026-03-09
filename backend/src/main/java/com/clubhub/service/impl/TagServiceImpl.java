package com.clubhub.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.clubhub.dto.CreateTagRequest;
import com.clubhub.dto.TagResponse;
import com.clubhub.dto.UpdateTagRequest;
import com.clubhub.entity.Tag;
import com.clubhub.entity.User;
import com.clubhub.exception.BizException;
import com.clubhub.repository.DocumentRepository;
import com.clubhub.repository.TagRepository;
import com.clubhub.repository.UserRepository;
import com.clubhub.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;

    @Override
    @Transactional
    public TagResponse createTag(CreateTagRequest request) {
        String name = normalizeTagName(request.getName());
        if (tagRepository.existsByNameIgnoreCase(name)) {
            throw new BizException("标签已存在");
        }
        Long creatorId = StpUtil.getLoginIdAsLong();
        if (creatorId == null) {
            throw new BizException("未登录或登录已过期");
        }

        Tag tag = new Tag();
        tag.setName(name);
        tag.setDescription(normalizeDescription(request.getDescription()));
        tag.setCreatorId(creatorId);
        tag = tagRepository.save(tag);
        return toResponse(tag, findCreatorName(tag.getCreatorId()));
    }

    @Override
    @Transactional
    public TagResponse updateTag(Long tagId, UpdateTagRequest request) {
        Tag tag = tagRepository.findById(tagId)
            .orElseThrow(() -> new BizException("标签不存在"));
        String name = normalizeTagName(request.getName());

        tagRepository.findByNameIgnoreCase(name).ifPresent(existing -> {
            if (!existing.getId().equals(tagId)) {
                throw new BizException("标签名已存在");
            }
        });

        tag.setName(name);
        tag.setDescription(normalizeDescription(request.getDescription()));
        tag = tagRepository.save(tag);
        return toResponse(tag, findCreatorName(tag.getCreatorId()));
    }

    @Override
    @Transactional
    public void deleteTag(Long tagId) {
        Tag tag = tagRepository.findById(tagId)
            .orElseThrow(() -> new BizException("标签不存在"));
        long usageCount = documentRepository.countByTagId(tagId);
        if (usageCount > 0) {
            throw new BizException("该标签已被文章使用，无法删除");
        }
        tagRepository.delete(tag);
    }

    @Override
    public List<TagResponse> listAdminTags() {
        return toResponses(tagRepository.findAllByOrderByCreatedAtDesc());
    }

    @Override
    public List<TagResponse> listPublicTags() {
        return toResponses(tagRepository.findAllByOrderByCreatedAtDesc());
    }

    private List<TagResponse> toResponses(List<Tag> tags) {
        if (tags == null || tags.isEmpty()) {
            return List.of();
        }
        List<Long> creatorIds = tags.stream()
            .map(Tag::getCreatorId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        Map<Long, String> creatorMap = creatorIds.isEmpty()
            ? Map.of()
            : userRepository.findByIdIn(creatorIds).stream()
                .collect(Collectors.toMap(User::getId, User::getRealName, (a, b) -> a));
        return tags.stream()
            .map(tag -> {
                Long creatorId = tag.getCreatorId();
                String creatorName = creatorId == null ? "未知用户" : creatorMap.getOrDefault(creatorId, "未知用户");
                return toResponse(tag, creatorName);
            })
            .toList();
    }

    private TagResponse toResponse(Tag tag, String creatorName) {
        return TagResponse.builder()
            .id(tag.getId())
            .name(tag.getName())
            .description(tag.getDescription())
            .creatorId(tag.getCreatorId())
            .creatorName(creatorName)
            .createdAt(tag.getCreatedAt())
            .updatedAt(tag.getUpdatedAt())
            .build();
    }

    private String findCreatorName(Long creatorId) {
        return userRepository.findById(creatorId).map(User::getRealName).orElse("未知用户");
    }

    private String normalizeTagName(String rawName) {
        if (rawName == null || rawName.isBlank()) {
            throw new BizException("标签名不能为空");
        }
        return rawName.trim();
    }

    private String normalizeDescription(String rawDescription) {
        if (rawDescription == null) {
            return null;
        }
        String value = rawDescription.trim();
        return value.isEmpty() ? null : value;
    }
}

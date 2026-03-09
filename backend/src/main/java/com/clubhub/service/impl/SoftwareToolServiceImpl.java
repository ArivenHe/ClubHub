package com.clubhub.service.impl;

import com.clubhub.dto.CreateSoftwareToolRequest;
import com.clubhub.dto.SoftwareToolResponse;
import com.clubhub.entity.SoftwareTool;
import com.clubhub.repository.SoftwareToolRepository;
import com.clubhub.service.SoftwareToolService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SoftwareToolServiceImpl implements SoftwareToolService {

    private final SoftwareToolRepository softwareToolRepository;

    @Override
    @Transactional
    public SoftwareToolResponse createTool(CreateSoftwareToolRequest request) {
        SoftwareTool tool = new SoftwareTool();
        tool.setName(request.getName());
        tool.setCategory(request.getCategory());
        tool.setDownloadUrl(request.getDownloadUrl());
        tool.setDescription(request.getDescription());
        tool.setRecommendedBy(request.getRecommendedBy());
        tool = softwareToolRepository.save(tool);
        return toResponse(tool);
    }

    @Override
    public List<SoftwareToolResponse> listTools() {
        return softwareToolRepository.findAllByOrderByCreatedAtDesc().stream()
            .map(this::toResponse)
            .toList();
    }

    private SoftwareToolResponse toResponse(SoftwareTool tool) {
        return SoftwareToolResponse.builder()
            .id(tool.getId())
            .name(tool.getName())
            .category(tool.getCategory())
            .downloadUrl(tool.getDownloadUrl())
            .description(tool.getDescription())
            .recommendedBy(tool.getRecommendedBy())
            .createdAt(tool.getCreatedAt())
            .build();
    }
}

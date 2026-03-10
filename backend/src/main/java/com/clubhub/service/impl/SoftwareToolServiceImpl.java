package com.clubhub.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.clubhub.dto.CreateSoftwareToolRequest;
import com.clubhub.dto.SoftwareToolResponse;
import com.clubhub.entity.SoftwareTool;
import com.clubhub.entity.User;
import com.clubhub.enums.SoftwareToolStatus;
import com.clubhub.exception.BizException;
import com.clubhub.repository.SoftwareToolRepository;
import com.clubhub.repository.UserRepository;
import com.clubhub.service.NotificationService;
import com.clubhub.service.SoftwareToolService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SoftwareToolServiceImpl implements SoftwareToolService {

    private final SoftwareToolRepository softwareToolRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public SoftwareToolResponse createTool(CreateSoftwareToolRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = userRepository.findById(userId).orElse(null);
        SoftwareTool tool = new SoftwareTool();
        tool.setName(request.getName());
        tool.setCategory(request.getCategory());
        tool.setDownloadUrl(request.getDownloadUrl());
        tool.setDescription(request.getDescription());
        tool.setRecommendedBy(normalizeRecommendedBy(request.getRecommendedBy(), user));
        tool.setApplicantId(userId);
        tool.setStatus(SoftwareToolStatus.APPROVED);
        tool.setReviewedAt(LocalDateTime.now());
        tool.setReviewRemark("后台直接发布");
        tool = softwareToolRepository.save(tool);
        return toResponse(tool);
    }

    @Override
    public List<SoftwareToolResponse> listTools() {
        return softwareToolRepository.findAllByOrderByCreatedAtDesc().stream()
            .filter((tool) -> normalizeStatus(tool.getStatus()) == SoftwareToolStatus.APPROVED)
            .map(this::toResponse)
            .toList();
    }

    @Override
    @Transactional
    public SoftwareToolResponse submitApplication(CreateSoftwareToolRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = userRepository.findById(userId).orElseThrow(() -> new BizException("用户不存在"));

        SoftwareTool tool = new SoftwareTool();
        tool.setName(request.getName());
        tool.setCategory(request.getCategory());
        tool.setDownloadUrl(request.getDownloadUrl());
        tool.setDescription(request.getDescription());
        tool.setRecommendedBy(buildApplicantDisplayName(user));
        tool.setApplicantId(userId);
        tool.setStatus(SoftwareToolStatus.PENDING);
        tool.setReviewRemark(null);
        tool.setReviewedAt(null);
        tool = softwareToolRepository.save(tool);
        return toResponse(tool);
    }

    @Override
    public List<SoftwareToolResponse> adminListTools() {
        return softwareToolRepository.findAllByOrderByCreatedAtDesc().stream()
            .map(this::toResponse)
            .toList();
    }

    @Override
    @Transactional
    public SoftwareToolResponse reviewTool(Long toolId, SoftwareToolStatus status, String reviewRemark) {
        if (status == null) {
            throw new BizException("审核状态不能为空");
        }

        SoftwareTool tool = softwareToolRepository.findById(toolId).orElseThrow(() -> new BizException("推荐记录不存在"));
        tool.setStatus(status);
        tool.setReviewRemark(reviewRemark == null ? null : reviewRemark.trim());
        tool.setReviewedAt(LocalDateTime.now());
        SoftwareTool saved = softwareToolRepository.save(tool);

        if (saved.getApplicantId() != null) {
            if (status == SoftwareToolStatus.REJECTED) {
                notificationService.createSystemNotice(
                    saved.getApplicantId(),
                    "SOFTWARE_REJECTED",
                    "软件推荐申请未通过",
                    "你提交的软件推荐《" + saved.getName() + "》未通过审核。"
                        + (saved.getReviewRemark() == null || saved.getReviewRemark().isBlank() ? "" : (" 原因：" + saved.getReviewRemark())),
                    saved.getId()
                );
            } else if (status == SoftwareToolStatus.APPROVED) {
                notificationService.createSystemNotice(
                    saved.getApplicantId(),
                    "SOFTWARE_APPROVED",
                    "软件推荐申请已通过",
                    "你提交的软件推荐《" + saved.getName() + "》已通过审核并展示到前台。",
                    saved.getId()
                );
            }
        }
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteTool(Long toolId) {
        SoftwareTool tool = softwareToolRepository.findById(toolId).orElseThrow(() -> new BizException("推荐记录不存在"));
        if (tool.getApplicantId() != null) {
            notificationService.createSystemNotice(
                tool.getApplicantId(),
                "SOFTWARE_DELETED",
                "软件推荐已被删除",
                "你提交的软件推荐《" + tool.getName() + "》已被管理员删除。",
                null
            );
        }
        softwareToolRepository.deleteById(tool.getId());
    }

    private SoftwareToolResponse toResponse(SoftwareTool tool) {
        return SoftwareToolResponse.builder()
            .id(tool.getId())
            .name(tool.getName())
            .category(tool.getCategory())
            .downloadUrl(tool.getDownloadUrl())
            .description(tool.getDescription())
            .recommendedBy(tool.getRecommendedBy())
            .applicantId(tool.getApplicantId())
            .status(normalizeStatus(tool.getStatus()))
            .reviewRemark(tool.getReviewRemark())
            .reviewedAt(tool.getReviewedAt())
            .createdAt(tool.getCreatedAt())
            .build();
    }

    private String normalizeRecommendedBy(String inputValue, User fallbackUser) {
        String provided = inputValue == null ? "" : inputValue.trim();
        if (!provided.isEmpty()) {
            return provided;
        }
        if (fallbackUser == null) {
            return null;
        }
        return buildApplicantDisplayName(fallbackUser);
    }

    private String buildApplicantDisplayName(User user) {
        String realName = user.getRealName() == null ? "" : user.getRealName().trim();
        String studentNo = user.getStudentNo() == null ? "" : user.getStudentNo().trim();
        if (!realName.isEmpty() && !studentNo.isEmpty()) {
            return realName + "+" + studentNo;
        }
        if (!realName.isEmpty()) {
            return realName;
        }
        if (!studentNo.isEmpty()) {
            return studentNo;
        }
        return user.getUsername();
    }

    private SoftwareToolStatus normalizeStatus(SoftwareToolStatus status) {
        return status == null ? SoftwareToolStatus.APPROVED : status;
    }
}

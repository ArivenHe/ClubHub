package com.clubhub.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.clubhub.dto.NotificationResponse;
import com.clubhub.entity.Notification;
import com.clubhub.exception.BizException;
import com.clubhub.repository.NotificationRepository;
import com.clubhub.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    @Transactional
    public void createSystemNotice(Long userId, String type, String title, String content, Long relatedId) {
        if (userId == null || type == null || type.isBlank() || title == null || title.isBlank() || content == null || content.isBlank()) {
            return;
        }
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type.trim());
        notification.setTitle(title.trim());
        notification.setContent(content.trim());
        notification.setRelatedId(relatedId);
        notification.setReadFlag(false);
        notificationRepository.save(notification);
    }

    @Override
    public List<NotificationResponse> listMine(int limit) {
        Long userId = StpUtil.getLoginIdAsLong();
        int finalLimit = limit <= 0 ? 20 : Math.min(limit, 100);
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
            .limit(finalLimit)
            .map(this::toResponse)
            .toList();
    }

    @Override
    public long countUnreadMine() {
        Long userId = StpUtil.getLoginIdAsLong();
        return notificationRepository.countByUserIdAndReadFlagFalse(userId);
    }

    @Override
    @Transactional
    public void markRead(Long notificationId) {
        Long userId = StpUtil.getLoginIdAsLong();
        Notification notification = notificationRepository.findById(notificationId).orElseThrow(() -> new BizException("提醒不存在"));
        if (!userId.equals(notification.getUserId())) {
            throw new BizException("无权限操作该提醒");
        }
        if (!Boolean.TRUE.equals(notification.getReadFlag())) {
            notification.setReadFlag(true);
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
        }
    }

    @Override
    @Transactional
    public void markAllRead() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        LocalDateTime now = LocalDateTime.now();
        boolean changed = false;
        for (Notification notification : notifications) {
            if (!Boolean.TRUE.equals(notification.getReadFlag())) {
                notification.setReadFlag(true);
                notification.setReadAt(now);
                changed = true;
            }
        }
        if (changed) {
            notificationRepository.saveAll(notifications);
        }
    }

    private NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
            .id(notification.getId())
            .type(notification.getType())
            .title(notification.getTitle())
            .content(notification.getContent())
            .relatedId(notification.getRelatedId())
            .read(Boolean.TRUE.equals(notification.getReadFlag()))
            .readAt(notification.getReadAt())
            .createdAt(notification.getCreatedAt())
            .build();
    }
}

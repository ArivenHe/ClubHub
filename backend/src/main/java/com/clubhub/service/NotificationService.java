package com.clubhub.service;

import com.clubhub.dto.NotificationResponse;

import java.util.List;

public interface NotificationService {

    void createSystemNotice(Long userId, String type, String title, String content, Long relatedId);

    List<NotificationResponse> listMine(int limit);

    long countUnreadMine();

    void markRead(Long notificationId);

    void markAllRead();
}

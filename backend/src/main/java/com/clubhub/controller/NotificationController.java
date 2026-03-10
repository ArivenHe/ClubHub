package com.clubhub.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.clubhub.dto.ApiResponse;
import com.clubhub.dto.NotificationResponse;
import com.clubhub.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @SaCheckPermission("doc:read")
    public ApiResponse<List<NotificationResponse>> list(@RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.ok(notificationService.listMine(limit));
    }

    @GetMapping("/unread-count")
    @SaCheckPermission("doc:read")
    public ApiResponse<Long> unreadCount() {
        return ApiResponse.ok(notificationService.countUnreadMine());
    }

    @PutMapping("/{id}/read")
    @SaCheckPermission("doc:read")
    public ApiResponse<Void> markRead(@PathVariable Long id) {
        notificationService.markRead(id);
        return ApiResponse.ok("已标记为已读", null);
    }

    @PutMapping("/read-all")
    @SaCheckPermission("doc:read")
    public ApiResponse<Void> markAllRead() {
        notificationService.markAllRead();
        return ApiResponse.ok("已全部标记为已读", null);
    }
}

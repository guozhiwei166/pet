package com.petcare.notification.controller;

import com.petcare.common.Result;
import com.petcare.notification.NotificationService;
import com.petcare.notification.entity.Notification;
import com.petcare.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public Result<List<Notification>> list(@RequestParam(defaultValue = "false") boolean unreadOnly) {
        return Result.ok(notificationService.list(SecurityUtils.currentId(), unreadOnly));
    }

    @GetMapping("/unread-count")
    public Result<Long> unreadCount() {
        return Result.ok(notificationService.unreadCount(SecurityUtils.currentId()));
    }

    @PostMapping("/{id}/read")
    public Result<Void> read(@PathVariable Long id) {
        notificationService.read(SecurityUtils.currentId(), id);
        return Result.ok();
    }

    @PostMapping("/read-all")
    public Result<Void> readAll() {
        notificationService.readAll(SecurityUtils.currentId());
        return Result.ok();
    }
}

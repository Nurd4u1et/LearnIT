package com.learnly.controller;

import com.learnly.dto.MiscDtos.NotificationDto;
import com.learnly.security.CurrentUser;
import com.learnly.service.NotificationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notifications;
    private final CurrentUser         me;

    public NotificationController(NotificationService notifications, CurrentUser me) {
        this.notifications = notifications;
        this.me = me;
    }

    @GetMapping
    public List<NotificationDto> list() {
        return notifications.list(me.id()).stream().map(NotificationDto::from).toList();
    }

    @GetMapping("/unread-count")
    public Map<String, Long> unread() {
        return Map.of("count", notifications.unreadCount(me.id()));
    }

    @PostMapping("/{id}/read")
    public void read(@PathVariable Long id) { notifications.markRead(id); }

    @PostMapping("/read-all")
    public void readAll() { notifications.markAllRead(me.id()); }
}

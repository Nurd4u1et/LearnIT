package com.learnly.service;

import com.learnly.entity.Notification;
import com.learnly.repository.NotificationRepository;
import com.learnly.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class NotificationService {

    private final NotificationRepository notifications;
    private final UserRepository         users;

    public NotificationService(NotificationRepository notifications, UserRepository users) {
        this.notifications = notifications;
        this.users = users;
    }

    public Notification create(Long userId, String type, String title, String body, String link) {
        var n = Notification.builder()
                .user(users.getReferenceById(userId))
                .type(type).title(title).body(body).link(link)
                .build();
        return notifications.save(n);
    }

    public List<Notification> list(Long userId) {
        return notifications.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public long unreadCount(Long userId) {
        return notifications.countByUserIdAndReadFalse(userId);
    }

    public void markRead(Long id) {
        notifications.findById(id).ifPresent(n -> n.setRead(true));
    }

    public void markAllRead(Long userId) {
        notifications.findByUserIdOrderByCreatedAtDesc(userId)
                .forEach(n -> n.setRead(true));
    }
}

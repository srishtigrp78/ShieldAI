package com.shieldai.dashboard.service;

import com.shieldai.dashboard.entity.NotificationEntity;
import com.shieldai.dashboard.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public NotificationEntity createNotification(String message) {
        NotificationEntity notification = new NotificationEntity();
        notification.setMessage(message);
        return notificationRepository.save(notification);
    }

    public List<NotificationEntity> getAllNotifications() {
        return notificationRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<NotificationEntity> getUnreadNotifications() {
        return notificationRepository.findByIsReadFalseOrderByCreatedAtDesc();
    }

    public long getUnreadCount() {
        return notificationRepository.countByIsReadFalse();
    }

    public boolean markAsRead(Long id) {
        Optional<NotificationEntity> notificationOpt = notificationRepository.findById(id);
        if (notificationOpt.isPresent()) {
            NotificationEntity notification = notificationOpt.get();
            notification.setIsRead(true);
            notificationRepository.save(notification);
            return true;
        }
        return false;
    }

    public void markAllAsRead() {
        List<NotificationEntity> unreadNotifications = notificationRepository.findByIsReadFalseOrderByCreatedAtDesc();
        unreadNotifications.forEach(notification -> notification.setIsRead(true));
        notificationRepository.saveAll(unreadNotifications);
    }
}
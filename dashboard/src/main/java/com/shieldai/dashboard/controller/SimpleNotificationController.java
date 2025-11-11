package com.shieldai.dashboard.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/notifications")
public class SimpleNotificationController {

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllNotifications() {
        List<Map<String, Object>> notifications = new ArrayList<>();
        
        Map<String, Object> notification1 = new HashMap<>();
        notification1.put("id", 1L);
        notification1.put("message", "High confidence detection: ChatGPT detected");
        notification1.put("isRead", false);
        notification1.put("createdAt", LocalDateTime.now().minusMinutes(5));
        notifications.add(notification1);
        
        Map<String, Object> notification2 = new HashMap<>();
        notification2.put("id", 2L);
        notification2.put("message", "New candidate assessment started");
        notification2.put("isRead", true);
        notification2.put("createdAt", LocalDateTime.now().minusHours(1));
        notifications.add(notification2);
        
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread")
    public ResponseEntity<List<Map<String, Object>>> getUnreadNotifications() {
        List<Map<String, Object>> notifications = new ArrayList<>();
        
        Map<String, Object> notification = new HashMap<>();
        notification.put("id", 1L);
        notification.put("message", "High confidence detection: ChatGPT detected");
        notification.put("isRead", false);
        notification.put("createdAt", LocalDateTime.now().minusMinutes(5));
        notifications.add(notification);
        
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        return ResponseEntity.ok(Map.of("unreadCount", 1L));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createNotification(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        
        Map<String, Object> notification = new HashMap<>();
        notification.put("id", System.currentTimeMillis());
        notification.put("message", message);
        notification.put("isRead", false);
        notification.put("createdAt", LocalDateTime.now());
        
        System.out.println("📢 Notification created: " + message);
        
        return ResponseEntity.ok(notification);
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<Map<String, String>> markAsRead(@PathVariable Long id) {
        System.out.println("📖 Marked notification " + id + " as read");
        return ResponseEntity.ok(Map.of("status", "success"));
    }

    @PostMapping("/read-all")
    public ResponseEntity<Map<String, String>> markAllAsRead() {
        System.out.println("📖 Marked all notifications as read");
        return ResponseEntity.ok(Map.of("status", "success"));
    }
}
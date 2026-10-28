package com.hiresmart.api;

import com.hiresmart.model.Notification;
import com.hiresmart.service.EmailNotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationRestController {
    private final EmailNotificationService notificationService;

    public NotificationRestController(EmailNotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<List<Notification>> getNotifications(@RequestParam(required = false) String email) {
        if (email != null && !email.isBlank()) {
            return ResponseEntity.ok(notificationService.getNotificationsForUser(email));
        }
        return ResponseEntity.ok(notificationService.getAllNotifications());
    }
}

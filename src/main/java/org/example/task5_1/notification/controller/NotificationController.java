package org.example.task5_1.notification.controller;

import org.example.task5_1.notification.dto.UserEvent;
import org.example.task5_1.notification.service.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final EmailService emailService;

    public NotificationController(EmailService emailService) {
        this.emailService = emailService;
    }

    /**
     * Ручная отправка уведомления (POST).
     * Тело: {"operation":"CREATE","email":"user@example.com"}
     */
    @PostMapping("/send")
    public ResponseEntity<Void> sendNotification(@RequestBody UserEvent event) {
        if (event == null || event.getEmail() == null) {
            return ResponseEntity.badRequest().build();
        }
        if (event.getOperation() == UserEvent.Operation.CREATE) {
            emailService.sendSimpleMessage(event.getEmail(), "Регистрация",
                    "Здравствуйте! Ваш аккаунт на сайте был успешно создан.");
        } else if (event.getOperation() == UserEvent.Operation.DELETE) {
            emailService.sendSimpleMessage(event.getEmail(), "Удаление аккаунта",
                    "Здравствуйте! Ваш аккаунт был удалён.");
        }
        return ResponseEntity.accepted().build();
    }
}
package org.example.task5_1.notification.listener;

import org.example.task5_1.notification.dto.UserEvent;
import org.example.task5_1.notification.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class UserEventListener {

    private final EmailService emailService;
    private final String siteName;

    public UserEventListener(EmailService emailService,
                             @Value("${notification.site-name:localhost}") String siteName) {
        this.emailService = emailService;
        this.siteName = siteName;
    }

    /**
     * Обработка сообщений из Kafka.
     * Слушает топик, извлекает операцию и email и отправляет соответствующее письмо.
     */
    @KafkaListener(topics = "${notification.topic:users.events}", groupId = "notification-service-group")
    public void onUserEvent(UserEvent event) {
        if (event == null || event.getEmail() == null) return;
        String to = event.getEmail();
        if (event.getOperation() == UserEvent.Operation.CREATE) {
            String subject = "Регистрация";
            String text = "Здравствуйте! Ваш аккаунт на сайте " + siteName + " был успешно создан.";
            emailService.sendSimpleMessage(to, subject, text);
        } else if (event.getOperation() == UserEvent.Operation.DELETE) {
            String subject = "Удаление аккаунта";
            String text = "Здравствуйте! Ваш аккаунт был удалён.";
            emailService.sendSimpleMessage(to, subject, text);
        }
    }
}
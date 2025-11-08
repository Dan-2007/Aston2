package org.example.task5_1.notification.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender sender;
    private final String from = "no-reply@example.com";

    public EmailService(JavaMailSender sender) {
        this.sender = sender;
    }

    /**
     * Отправляет простое текстовое письмо.
     * Используется и в listener'е, и в REST контроллере.
     */
    public void sendSimpleMessage(String to, String subject, String text) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setFrom(from);
        msg.setSubject(subject);
        msg.setText(text);
        sender.send(msg);
    }
}
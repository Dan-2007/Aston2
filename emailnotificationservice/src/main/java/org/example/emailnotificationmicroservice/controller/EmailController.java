package org.example.emailnotificationmicroservice.controller;

import org.example.emailnotificationmicroservice.dto.EmailRequest;
import org.example.emailnotificationmicroservice.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/email")
@AllArgsConstructor
public class EmailController {

    private UserService userService;

    @PostMapping
    public ResponseEntity<String> sendEmail(@RequestBody EmailRequest emailRequest) {
        String email = emailRequest.getEmail();

        log.info("Asking user service if email {} exists", email);

        userService.checkEmail(email);

        log.info("Message to email {} successfully sent", email);
        log.info("The message itself: {}", emailRequest.getMessage());

        return ResponseEntity.ok(String.format("Message to %s successfully send", email));
    }
}

package org.example.aston_spring_boot.controller;

import org.example.aston_spring_boot.dto.*;
import org.example.aston_spring_boot.service.EmailService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@Tag(name = "Send emails to users", description = "API for sending email to users")
@Slf4j
@Validated
@AllArgsConstructor
@RestController
@RequestMapping("/api/email")
public class EmailController {
    private final EmailService emailService;

    @Operation(summary = "Send email to user", description = "Sends email to user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email sent successfully"),
            @ApiResponse(responseCode = "404", description = "No such email")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    void sendEmail(@Parameter(description = "Users email and message", required = true) @RequestBody @Valid EmailMessageDTO dto){
        log.info("POST /api/email");
        emailService.send(dto.getEmail(), dto.getMessage());
    }


}
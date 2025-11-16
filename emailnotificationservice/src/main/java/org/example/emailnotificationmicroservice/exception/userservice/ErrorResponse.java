package org.example.emailnotificationmicroservice.exception.userservice;

public record ErrorResponse(int status, String code, String message) {}
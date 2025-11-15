package org.example.aston_spring_boot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "Data for sending email to user")
@AllArgsConstructor
@Builder
@Getter
@Setter
public class EmailMessageDTO {
    @Schema(description = "User's email address", example = "john.doe@example.com", maxLength = 30)
    @Email
    @Size(max = 30)
    private String email;

    @Schema(description = "Message text", example = "Hello, mr John Doe")
    @NotBlank
    private String message;
    
}
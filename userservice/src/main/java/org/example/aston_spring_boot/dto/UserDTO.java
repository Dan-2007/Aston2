package org.example.aston_spring_boot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Schema(description = "User response data")
@AllArgsConstructor
@Builder
@Getter
@Setter
public class UserDTO {
    @Schema(description = "Unique identifier of the user", example = "1")
    private Long id;

    @Schema(description = "Full name of the user", example = "John Doe")
    private String name;

    @Schema(description = "Email address of the user", example = "john.doe@example.com")
    private String email;

    @Schema(description = "Age of the user", example = "25", minimum = "1", maximum = "150")
    private Integer age;

    @Schema(description = "Timestamp when user was created", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;
}

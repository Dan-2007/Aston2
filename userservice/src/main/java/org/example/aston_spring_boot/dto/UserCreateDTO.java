package org.example.aston_spring_boot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "Data for creating a new user")
@AllArgsConstructor
@Builder
@Getter
@Setter
public class UserCreateDTO {
    @Schema(description = "User's full name", example = "John Doe", maxLength = 15)
    @NotBlank
    @Size(max = 15)
    private String name;

    @Schema(description = "User's email address", example = "john.doe@example.com", maxLength = 30)
    @Email
    @Size(max = 30)
    private String email;

    @Schema(description = "User's age", example = "25", minimum = "1", maximum = "150")
    @NotNull
    @Min(1)
    @Max(150)
    private Integer age;
}

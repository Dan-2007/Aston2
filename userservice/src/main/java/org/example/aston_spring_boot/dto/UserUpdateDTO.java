package org.example.aston_spring_boot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;

@Schema(description = "User update request data")
@AllArgsConstructor
@Builder
@Getter
@Setter
public class UserUpdateDTO {
    @Schema(
            description = "Full name of the user",
            example = "John Doe",
            requiredMode = Schema.RequiredMode.REQUIRED,
            maxLength = 15
    )
    @NotBlank
    @Size(max = 15)
    private JsonNullable<String> name;

    @Schema(
            description = "Email address of the user",
            example = "john.doe@example.com",
            requiredMode = Schema.RequiredMode.REQUIRED,
            maxLength = 30
    )
    @Email
    @Size(max = 30)
    private JsonNullable<String> email;

    @Schema(
            description = "Age of the user",
            example = "25",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minimum = "1",
            maximum = "150"
    )
    @NotNull
    @Min(1)
    @Max(150)
    private JsonNullable<Integer> age;
}

package org.example.aston_spring_boot.dto;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;

@ParameterObject
@Schema(description = "Parameters for filtering, sorting and pagination users")
@Getter
@Setter
public class UserParamsDTO {
    @Parameter(description = "Filter by name (case-insensitive contains)", example = "John")
    private String name;

    @Parameter(description = "Filter by age greater than", example = "18")
    @Positive
    private Integer ageGt;

    @Parameter(description = "Filter by age less than", example = "65")
    @Positive
    private Integer ageLt;

    @Parameter(description = "Filter by creation date after", example = "2025-01-01T00:00:00")
    private LocalDateTime createdAtGt;

    @Parameter(description = "Filter by creation date before", example = "2025-12-31T00:00:00")
    private LocalDateTime createdAtLt;


    @Parameter(description = "Page number (0-based)", example = "0")
    private Integer page = 0;

    @Parameter(description = "Page size", example = "20")
    private Integer size = 20;

    @Parameter(
            description = "Field to sort by",
            example = "createdAt",
            schema = @Schema(allowableValues = {"id", "createdAt", "name", "age", "email"})
    )
    @Pattern(regexp = "id|createdAt|name|age|email",
            message = "SortBy must be one of: id, createdAt, name, age, email")
    private String sortBy = "createdAt";

    @Parameter(
            description = "Sort direction",
            example = "desc",
            schema = @Schema(allowableValues = {"asc", "desc"})
    )
    @Pattern(regexp = "asc|desc", flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "SortDirection must be 'asc' or 'desc'")
    private String sortDirection = "desc";

    public Pageable toPageable() {
        return PageRequest.of(page, size,
                Sort.by(Sort.Direction.fromString(sortDirection), sortBy));
    }
}

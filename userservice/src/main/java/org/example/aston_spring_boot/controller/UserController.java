package org.example.aston_spring_boot.controller;

import org.example.aston_spring_boot.dto.UserCreateDTO;
import org.example.aston_spring_boot.dto.UserDTO;
import org.example.aston_spring_boot.dto.UserParamsDTO;
import org.example.aston_spring_boot.dto.UserUpdateDTO;
import org.example.aston_spring_boot.hateoas.UserDTOModelAssembler;
import org.example.aston_spring_boot.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@Tag(name = "User management", description = "API for managing users")
@Slf4j
@Validated
@AllArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final UserDTOModelAssembler userDTOModelAssembler;

    @Operation(
            summary = "Get user by ID",
            description = "Returns a single user by their unique identifier"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User found successfully",
                    content = @Content(schema = @Schema(implementation = UserDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content
            ),
            @ApiResponse(responseCode = "400", description = "Invalid ID format")
    })
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public EntityModel<UserDTO> getUser(
            @Parameter(description = "User ID", example = "1", required = true)
            @PathVariable Long id){
        log.info("GET /api/users/{}", id);
        var userDTO = userService.findUser(id);
        return userDTOModelAssembler.toModel(userDTO);
    }

    @Operation(
            summary = "Get all users",
            description = """
        Returns paginated and filtered list of users.
        
        **Filtering:**
        - `name`: Filter by name (contains, case-insensitive)
        - `ageGt`: Age greater than
        - `ageLt`: Age less than
        - `createdAtGt`: Created after date
        - `createdAtLt`: Created before date
        
        **Pagination:**
        - `page`: Page number (0-based)
        - `size`: Page size
        
        **Sorting:**
        - `sortBy`: Field to sort by (id, createdAt, name, age, email)
        - `sortDirection`: Sort direction (asc, desc)
        """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Users retrieved successfully",
                    content = @Content(schema = @Schema(implementation = UserDTO[].class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid query parameters")
    })
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public CollectionModel<EntityModel<UserDTO>> getAllUses(@Parameter(description = "Search and pagination parameters")
                             @Valid UserParamsDTO params){
        log.info("GET /api/users");
        var userDTOs = userService.findAllUsers(params);
        return CollectionModel.of(userDTOs.stream().map(userDTOModelAssembler::toModel).toList());
    }

    @Operation(summary = "Create new user", description = "Creates a new user in the db")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully",
                    content = @Content(schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid user data"),
            @ApiResponse(responseCode = "409", description = "Email already exists")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    EntityModel<UserDTO> createUser(@Parameter(description = "User data for creation", required = true)
                       @RequestBody @Valid UserCreateDTO dto){
        log.info("POST /api/users");
        var userDTO = userService.createUser(dto);
        return userDTOModelAssembler.toModel(userDTO);
    }

    @Operation(summary = "Delete user", description = "Deletes a user by their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteUser(@Parameter(description = "User ID to delete", example = "1")
                    @PathVariable Long id){
        log.info("DELETE /api/users/{}", id);
        userService.deleteUser(id);
    }

    @Operation(summary = "Update user", description = "Partially updates an existing user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully",
                    content = @Content(schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid update data"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    EntityModel<UserDTO> updateUser(@Parameter(description = "User data for update", required = true)
                       @RequestBody @Valid UserUpdateDTO dto,
                       @Parameter(description = "User ID to update", example = "1")
                       @PathVariable Long id){
        log.info("PUT /api/users/{}", id);
        var userDTO = userService.updateUser(id, dto);
        return userDTOModelAssembler.toModel(userDTO);
    }

}

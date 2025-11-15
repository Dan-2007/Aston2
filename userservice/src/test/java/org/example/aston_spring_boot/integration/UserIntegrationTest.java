package org.example.aston_spring_boot.integration;

import org.example.aston_spring_boot.dto.UserCreateDTO;
import org.example.aston_spring_boot.dto.UserUpdateDTO;
import org.example.aston_spring_boot.model.User;
import org.example.aston_spring_boot.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Transactional
@DisplayName("User Integration Tests")
@ActiveProfiles("test")
class UserIntegrationTest {

    @MockitoBean
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testuser");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        CompletableFuture<SendResult<String, Object>> successFuture =
                CompletableFuture.completedFuture(mock(org.springframework.kafka.support.SendResult.class));

        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(successFuture);

        userRepository.deleteAll();

        testUser = User.builder()
                .name("John Doe")
                .email("john.doe@example.com")
                .age(25)
                .createdAt(LocalDateTime.now())
                .build();

        testUser = userRepository.save(testUser);
    }

    @Test
    @DisplayName("Full flow: create user -> get -> update -> delete")
    void shouldPerformFullUserLifecycle() throws Exception {

        UserCreateDTO createDTO = UserCreateDTO.builder()
                .name("Jane Smith")
                .email("jane.smith@example.com")
                .age(30)
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Jane Smith"))
                .andExpect(jsonPath("$.email").value("jane.smith@example.com"))
                .andExpect(jsonPath("$.age").value(30))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andReturn();

        String responseBody = createResult.getResponse().getContentAsString();
        Long userId = objectMapper.readTree(responseBody).get("id").asLong();

        mockMvc.perform(get("/api/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.name").value("Jane Smith"))
                .andExpect(jsonPath("$.email").value("jane.smith@example.com"))
                .andExpect(jsonPath("$.age").value(30));

        UserUpdateDTO updateDTO = UserUpdateDTO.builder()
                .name(org.openapitools.jackson.nullable.JsonNullable.of("Jane Updated"))
                .age(org.openapitools.jackson.nullable.JsonNullable.of(31))
                .build();

        mockMvc.perform(put("/api/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.name").value("Jane Updated"))
                .andExpect(jsonPath("$.age").value(31))
                .andExpect(jsonPath("$.email").value("jane.smith@example.com"));

        User updatedUser = userRepository.findById(userId).orElseThrow();
        assertThat(updatedUser.getName()).isEqualTo("Jane Updated");
        assertThat(updatedUser.getAge()).isEqualTo(31);
        assertThat(updatedUser.getEmail()).isEqualTo("jane.smith@example.com");

        mockMvc.perform(delete("/api/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));

        assertThat(userRepository.existsById(userId)).isFalse();
    }

    @Test
    @DisplayName("Should get all users with pagination and filters")
    void shouldGetAllUsersWithPaginationAndFiltering() throws Exception {

        User user2 = User.builder()
                .name("Alice Johnson")
                .email("alice.johnson@example.com")
                .age(28)
                .createdAt(LocalDateTime.now().minusHours(1))
                .build();

        User user3 = User.builder()
                .name("Bob Wilson")
                .email("bob.wilson@example.com")
                .age(35)
                .createdAt(LocalDateTime.now().minusHours(2))
                .build();

        userRepository.save(user2);
        userRepository.save(user3);

        mockMvc.perform(get("/api/users")
                        .param("page", "0")
                        .param("size", "2")
                        .param("sortBy", "name")
                        .param("sortDirection", "asc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.userDTOList").isArray())
                .andExpect(jsonPath("$._embedded.userDTOList.length()").value(2));

        mockMvc.perform(get("/api/users")
                        .param("name", "john doe")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.userDTOList").isArray())
                .andExpect(jsonPath("$._embedded.userDTOList.length()").value(1))
                .andExpect(jsonPath("$._embedded.userDTOList[0].name").value("John Doe"));

        mockMvc.perform(get("/api/users")
                        .param("ageGt", "26")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.userDTOList").isArray())
                .andExpect(jsonPath("$._embedded.userDTOList.length()").value(2));
    }

    @Test
    @DisplayName("Should process validation errors")
    void shouldHandleValidationErrors() throws Exception {

        UserCreateDTO invalidDTO = UserCreateDTO.builder()
                .name("")
                .email("invalid-email")
                .age(200)
                .build();

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        UserUpdateDTO invalidUpdateDTO = UserUpdateDTO.builder()
                .name(org.openapitools.jackson.nullable.JsonNullable.of(""))
                .age(org.openapitools.jackson.nullable.JsonNullable.of(200))
                .build();

        mockMvc.perform(put("/api/users/{id}", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUpdateDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("Should process operations with non exist users")
    void shouldHandleNonExistentUserOperations() throws Exception {
        Long nonExistentId = 999L;

        mockMvc.perform(get("/api/users/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("User with id 999 not found"));

        UserUpdateDTO updateDTO = UserUpdateDTO.builder()
                .name(org.openapitools.jackson.nullable.JsonNullable.of("Updated Name"))
                .build();

        mockMvc.perform(put("/api/users/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));

        mockMvc.perform(delete("/api/users/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    @DisplayName("Should process unique email conflict")
    void shouldHandleEmailUniquenessConflict() throws Exception {

        UserCreateDTO duplicateEmailDTO = UserCreateDTO.builder()
                .name("Duplicate User")
                .email("john.doe@example.com")
                .age(30)
                .build();

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateEmailDTO)))
                .andExpect(status().isConflict()) // Исправлено на 409 Conflict
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.code").value("CONFLICT"));
    }

    @Test
    @DisplayName("Should process partial update correctly")
    void shouldHandlePartialUpdate() throws Exception {

        UserUpdateDTO partialUpdateDTO = UserUpdateDTO.builder()
                .name(org.openapitools.jackson.nullable.JsonNullable.of("Updated Name"))
                .build();

        mockMvc.perform(put("/api/users/{id}", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(partialUpdateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUser.getId()))
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.age").value(25));

        UserUpdateDTO ageUpdateDTO = UserUpdateDTO.builder()
                .age(org.openapitools.jackson.nullable.JsonNullable.of(30))
                .build();

        mockMvc.perform(put("/api/users/{id}", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ageUpdateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUser.getId()))
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.age").value(30));
    }

}
package org.example.aston_spring_boot.web;

import org.example.aston_spring_boot.dto.UserCreateDTO;
import org.example.aston_spring_boot.dto.UserDTO;
import org.example.aston_spring_boot.dto.UserUpdateDTO;
import org.example.aston_spring_boot.model.User;
import org.example.aston_spring_boot.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.hateoas.EntityModel;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@DisplayName("UserController REST API Tests")
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class UserControllerTest {

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
    private WebApplicationContext wac;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private User testUser2;


    @BeforeEach
    void setUp() {
        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(CompletableFuture.completedFuture(null));

        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
                .build();
        testUser = User.builder()
                .name("Artem")
                .email("artem@yakkonen.ru")
                .age(26)
                .build();
        testUser2 = User.builder()
                .name("Nail")
                .email("nail@alishev.ru")
                .age(33)
                .build();

        userRepository.deleteAll();
        userRepository.save(testUser);
        userRepository.save(testUser2);
    }

    @Test
    @DisplayName("POST /api/users/ - Should create user and return DTO")
    void shouldCreateUser() throws Exception {
        var userCreateDTO = UserCreateDTO.builder()
                .name("Zaur")
                .email("zaur@tregulov.ru")
                .age(35)
                .build();

        var result = mockMvc.perform(post("/api/users")
                        .content(om.writeValueAsString(userCreateDTO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().contentType("application/hal+json"))
                .andReturn();

        var body = result.getResponse().getContentAsString();

        var collectionModel =
                om.readValue(body, new TypeReference<EntityModel<UserDTO>>() {});

        UserDTO responseUser = collectionModel.getContent();

        assertThat(responseUser.getId()).isNotNull();
        assertThat(responseUser.getName()).isEqualTo("Zaur");
        assertThat(responseUser.getEmail()).isEqualTo("zaur@tregulov.ru");
        assertThat(responseUser.getAge()).isEqualTo(35);
        assertThat(responseUser.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("GET /api/users/{id} - Should return user by ID")
    void shouldGetUser() throws Exception {
        var result = mockMvc.perform(get("/api/users/{id}", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/hal+json"))
                .andReturn();

        var body = result.getResponse().getContentAsString();

        var collectionModel =
                om.readValue(body, new TypeReference<EntityModel<UserDTO>>() {});

        UserDTO responseUser = collectionModel.getContent();

        assertThat(responseUser.getId()).isEqualTo(testUser.getId());
        assertThat(responseUser.getName()).isEqualTo("Artem");
        assertThat(responseUser.getEmail()).isEqualTo("artem@yakkonen.ru");
        assertThat(responseUser.getAge()).isEqualTo(26);
        assertThat(responseUser.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("PUT /api/users/{id} - Should update user by ID and return DTO")
    void shouldUpdateUser() throws Exception {
        var userUpdateDTO = UserUpdateDTO.builder()
                .name(JsonNullable.of("ArtemUpdated"))
                .build();

        var result = mockMvc.perform(put("/api/users/{id}", testUser.getId())
                        .content(om.writeValueAsString(userUpdateDTO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/hal+json"))
                .andReturn();

        var body = result.getResponse().getContentAsString();

        var collectionModel =
                om.readValue(body, new TypeReference<EntityModel<UserDTO>>() {});

        UserDTO responseUser = collectionModel.getContent();

        assertThat(responseUser.getId()).isEqualTo(testUser.getId());
        assertThat(responseUser.getName()).isEqualTo("ArtemUpdated");
        assertThat(responseUser.getEmail()).isEqualTo("artem@yakkonen.ru");
        assertThat(responseUser.getAge()).isEqualTo(26);
        assertThat(responseUser.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("DELETE /api/users/{id} - Should delete user by ID")
    void shouldDeleteUser() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        var user = userRepository.findById(testUser.getId());

        assertThat(user).isEmpty();
    }

    @Test
    @DisplayName("GET /api/users - Should return all users")
    void shouldGetAllUsers() throws Exception {
        var result = mockMvc.perform(get("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "id")
                        .param("sortDirection", "asc")
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/hal+json"))
                .andReturn();

        var body = result.getResponse().getContentAsString();

        var jsonNode = om.readTree(body);
        var usersArray = jsonNode.get("_embedded").get("userDTOList");
        UserDTO[] responseUsers = om.convertValue(usersArray, UserDTO[].class);

        assertThat(responseUsers).hasSize(2);
        assertThat(responseUsers[0].getId()).isEqualTo(testUser.getId());
        assertThat(responseUsers[0].getName()).isEqualTo("Artem");
        assertThat(responseUsers[1].getName()).isEqualTo("Nail");
    }

}

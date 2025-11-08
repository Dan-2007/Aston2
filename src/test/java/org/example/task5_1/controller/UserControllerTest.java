package org.example.task5_1.controller;

import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.ObjectMapper;
import org.example.task5_1.dto.UserDTO;
import org.example.task5_1.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(UserController.class)
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("GET /api/users - returns list")
    void testGetAll() throws Exception {
        UserDTO u1 = new UserDTO(1L, "Alice", "a@example.com", 30, LocalDateTime.now());
        UserDTO u2 = new UserDTO(2L, "Bob", "b@example.com", 25, LocalDateTime.now());
        Mockito.when(userService.findAllUsers()).thenReturn(List.of(u1, u2));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("GET /api/users/{id} - found")
    void testGetByIdFound() throws Exception {
        UserDTO u = new UserDTO(1L, "Alice", "a@example.com", 30, LocalDateTime.now());
        Mockito.when(userService.findUser(1L)).thenReturn(Optional.of(u));

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("GET /api/users/{id} - not found")
    void testGetByIdNotFound() throws Exception {
        Mockito.when(userService.findUser(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/users - create")
    void testCreate() throws Exception {
        UserDTO input = new UserDTO(null, "New", "new@example.com", 20, null);
        UserDTO saved = new UserDTO(10L, "New", "new@example.com", 20, LocalDateTime.now());
        Mockito.when(userService.createUser(any(UserDTO.class))).thenReturn(saved);

        mockMvc.perform(post("/api/users/create_user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    @DisplayName("PUT /api/users/{id} - update")
    void testUpdate() throws Exception {
        UserDTO update = new UserDTO(null, "Updated", null, null, null);
        UserDTO result = new UserDTO(1L, "Updated", "a@example.com", 30, LocalDateTime.now());
        Mockito.when(userService.updateUser(eq(1L), any(UserDTO.class))).thenReturn(Optional.of(result));

        mockMvc.perform(put("/api/users/update_user/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    @DisplayName("DELETE /api/users/{id} - delete")
    void testDelete() throws Exception {
        Mockito.when(userService.deleteUser(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/users/delete_user/1"))
                .andExpect(status().isNoContent());
    }
}
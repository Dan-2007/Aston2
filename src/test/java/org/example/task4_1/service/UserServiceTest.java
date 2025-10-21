package org.example.task4_1.service;

import org.example.task4_1.dto.UserDTO;
import org.example.task4_1.model.User;
import org.example.task4_1.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Profile;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Profile("test")
public class UserServiceTest {

    @Mock
    private UserRepository userRepository; // <-- мок репозитория, не DAO

    @InjectMocks
    private UserService userService; // будет создан через @InjectMocks

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @BeforeEach
    void setUp() {
        // не нужно вручную создавать userService, @InjectMocks делает это автоматически,
        // но если хотите явный конструктор, можно:
        // userService = new UserService(userRepository);
    }

    @Test
    void testFindUser_Found() {
        User u = new User();
        u.setId(1L);
        u.setName("Ivan");
        u.setEmail("ivan@example.com");
        u.setCreatedAt(LocalDateTime.now());

        when(userRepository.findById(1L)).thenReturn(Optional.of(u));

        Optional<UserDTO> resultOpt = userService.findUser(1L);

        assertTrue(resultOpt.isPresent());
        UserDTO result = resultOpt.get();
        assertEquals(1L, result.getId());
        assertEquals("Ivan", result.getName());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void testFindUser_NotFound_ReturnsEmpty() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<UserDTO> resultOpt = userService.findUser(999L);

        assertTrue(resultOpt.isEmpty());
        verify(userRepository, times(1)).findById(999L);
    }

    @Test
    void testCreateUser() {
        UserDTO inputDto = new UserDTO(null, "Petr", "petr@example.com", null, LocalDateTime.now());

        User savedEntity = new User();
        savedEntity.setId(5L);
        savedEntity.setName(inputDto.getName());
        savedEntity.setEmail(inputDto.getEmail());
        savedEntity.setCreatedAt(inputDto.getCreatedAt());

        when(userRepository.save(any(User.class))).thenReturn(savedEntity);

        UserDTO result = userService.createUser(inputDto);

        assertNotNull(result);
        assertEquals(5L, result.getId());
        assertEquals("Petr", result.getName());

        verify(userRepository, times(1)).save(userCaptor.capture());
        User passedToRepo = userCaptor.getValue();
        assertEquals("petr@example.com", passedToRepo.getEmail());
        assertEquals("Petr", passedToRepo.getName());
    }

    @Test
    void testFindAllUsers() {
        User u1 = new User();
        u1.setId(1L);
        u1.setName("A");
        u1.setEmail("a@example.com");

        User u2 = new User();
        u2.setId(2L);
        u2.setName("B");
        u2.setEmail("b@example.com");

        when(userRepository.findAll()).thenReturn(Arrays.asList(u1, u2));

        List<UserDTO> all = userService.findAllUsers();

        assertNotNull(all);
        assertEquals(2, all.size());
        assertEquals(1L, all.get(0).getId());
        assertEquals("A", all.get(0).getName());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void testUpdateUser() {
        Long id = 10L;

        User existing = new User();
        existing.setId(id);
        existing.setName("Old");
        existing.setEmail("old@example.com");

        User saved = new User();
        saved.setId(id);
        saved.setName("New");
        saved.setEmail("old@example.com");

        // при обновлении сервис сначала делает findById(id), затем save(existing)
        when(userRepository.findById(id)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenReturn(saved);

        UserDTO updateDto = new UserDTO(null, "New", null, null, null);
        Optional<UserDTO> resultOpt = userService.updateUser(id, updateDto);

        assertTrue(resultOpt.isPresent());
        UserDTO result = resultOpt.get();
        assertEquals("New", result.getName());

        verify(userRepository, times(1)).findById(id);
        verify(userRepository, times(1)).save(userCaptor.capture());
        User passedToSave = userCaptor.getValue();
        assertEquals("New", passedToSave.getName());
    }

    @Test
    void testDeleteUser() {
        Long id = 7L;
        when(userRepository.existsById(id)).thenReturn(true);
        // deleteById - void, по умолчанию ничего делать не нужно

        boolean res = userService.deleteUser(id);

        assertTrue(res);
        verify(userRepository, times(1)).existsById(id);
        verify(userRepository, times(1)).deleteById(id);
    }
}
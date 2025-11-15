package org.example.aston_spring_boot.unit;

import org.example.aston_spring_boot.dto.UserCreateDTO;
import org.example.aston_spring_boot.dto.UserDTO;
import org.example.aston_spring_boot.dto.UserParamsDTO;
import org.example.aston_spring_boot.dto.UserUpdateDTO;
import org.example.aston_spring_boot.exception.UserNotFoundException;
import org.example.aston_spring_boot.mapper.UserMapper;
import org.example.aston_spring_boot.model.User;
import org.example.aston_spring_boot.repository.UserRepository;
import org.example.aston_spring_boot.service.UserService;
import org.example.aston_spring_boot.specification.UserSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
@ActiveProfiles("test")
class UserServiceTest {
    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserSpecification userSpecification;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserDTO testUserDTO;
    private UserCreateDTO testUserCreateDTO;
    private UserUpdateDTO testUserUpdateDTO;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john.doe@example.com")
                .age(25)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testUserDTO = UserDTO.builder()
                .id(1L)
                .name("John Doe")
                .email("john.doe@example.com")
                .age(25)
                .createdAt(LocalDateTime.now())
                .build();

        testUserCreateDTO = UserCreateDTO.builder()
                .name("John Doe")
                .email("john.doe@example.com")
                .age(25)
                .build();

        testUserUpdateDTO = UserUpdateDTO.builder()
                .name(JsonNullable.of("Jane Doe"))
                .email(JsonNullable.of("jane.doe@example.com"))
                .age(JsonNullable.of(30))
                .build();
    }

    @Test
    @DisplayName("Should find user by ID")
    void shouldFindUserById() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userMapper.map(testUser)).thenReturn(testUserDTO);

        UserDTO result = userService.findUser(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("John Doe");
        assertThat(result.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(result.getAge()).isEqualTo(25);

        verify(userRepository).findById(1L);
        verify(userMapper).map(testUser);
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void shouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findUser(999L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User with id 999 not found");

        verify(userRepository).findById(999L);
        verify(userMapper, never()).map(any(User.class));
    }

    @Test
    @DisplayName("Should create new user")
    void shouldCreateUser() {
        CompletableFuture<SendResult<String, Object>> successFuture =
                CompletableFuture.completedFuture(mock(SendResult.class));

        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(successFuture);

        when(userMapper.map(testUserCreateDTO)).thenReturn(testUser);
        when(userRepository.save(testUser)).thenReturn(testUser);
        when(userMapper.map(testUser)).thenReturn(testUserDTO);

        UserDTO result = userService.createUser(testUserCreateDTO);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("John Doe");

        verify(userMapper).map(testUserCreateDTO);
        verify(userRepository).save(testUser);
        verify(userMapper).map(testUser);
    }

    @Test
    @DisplayName("Should delete user")
    void shouldDeleteUser() {
        CompletableFuture<SendResult<String, Object>> successFuture =
                CompletableFuture.completedFuture(mock(SendResult.class));

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(successFuture);

        userService.deleteUser(1L);

        verify(userRepository).findById(1L);
        verify(userRepository).deleteById(1L);
        verify(kafkaTemplate).send(eq("user-deleted-events-topic"), eq("1"), any());
    }

    @Test
    @DisplayName("Should throw exception when user not found when deleting")
    void shouldThrowExceptionWhenDeletingNonExistentUser() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser(999L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User with id 999 not found");

        verify(userRepository).findById(999L);
        verify(userRepository, never()).deleteById(anyLong());
        verify(kafkaTemplate, never()).send(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("Should update user")
    void shouldUpdateUser() {
        User updatedUser = User.builder()
                .id(1L)
                .name("Jane Doe")
                .email("jane.doe@example.com")
                .age(30)
                .build();

        UserDTO updatedUserDTO = UserDTO.builder()
                .id(1L)
                .name("Jane Doe")
                .email("jane.doe@example.com")
                .age(30)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(testUser)).thenReturn(updatedUser);
        when(userMapper.map(updatedUser)).thenReturn(updatedUserDTO);

        UserDTO result = userService.updateUser(1L, testUserUpdateDTO);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Jane Doe");
        assertThat(result.getEmail()).isEqualTo("jane.doe@example.com");
        assertThat(result.getAge()).isEqualTo(30);

        verify(userRepository).findById(1L);
        verify(userMapper).update(eq(testUserUpdateDTO), eq(testUser));
        verify(userRepository).save(testUser);
        verify(userMapper).map(updatedUser);
    }

    @Test
    @DisplayName("Should throw exception when user not found when updating")
    void shouldThrowExceptionWhenUpdatingNonExistentUser() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(999L, testUserUpdateDTO))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User with id 999 not found");

        verify(userRepository).findById(999L);
        verify(userMapper, never()).update(any(), any());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should find all users with parameters")
    void shouldFindAllUsersWithParams() {
        UserParamsDTO params = new UserParamsDTO();
        params.setName("John");
        params.setAgeGt(20);
        params.setPage(0);
        params.setSize(10);

        List<User> users = List.of(testUser);
        List<UserDTO> userDTOs = List.of(testUserDTO);
        Page<User> userPage = new PageImpl<>(users);

        when(userSpecification.build(params)).thenReturn(mock(Specification.class));
        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(userPage);
        when(userMapper.fromUsers(users)).thenReturn(userDTOs);

        List<UserDTO> result = userService.findAllUsers(params);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testUserDTO);

        verify(userSpecification).build(params);
        verify(userRepository).findAll(any(Specification.class), any(Pageable.class));
        verify(userMapper).fromUsers(users);
    }
}
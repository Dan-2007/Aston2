package org.example.task5_1.service;

import org.example.task5_1.dto.UserDTO;
import org.example.task5_1.model.User;
import org.example.task5_1.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = Objects.requireNonNull(userRepository);
    }

    // Map User -> UserDTO
    private UserDTO toDto(User user) {
        if (user == null) return null;
        return new UserDTO(user.getId(), user.getName(), user.getEmail(), user.getAge(), user.getCreatedAt());
    }

    // Map UserDTO -> User (for create/update)
    private User fromDto(UserDTO dto) {
        User user = new User();
        user.setId(dto.getId());
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setAge(dto.getAge());
        user.setCreatedAt(dto.getCreatedAt());
        return user;
    }

    public List<UserDTO> findAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public Optional<UserDTO> findUser(@PathVariable Long id) {
        return userRepository.findById(id).map(this::toDto);
    }

    @Transactional
    public UserDTO createUser(UserDTO dto) {
        User user = fromDto(dto);
        if (user.getCreatedAt() == null) {
            user.setCreatedAt(java.time.LocalDateTime.now());
        }
        User saved = userRepository.save(user);
        return toDto(saved);
    }

    @Transactional
    public Optional<UserDTO> updateUser(@PathVariable Long id, UserDTO dto) {
        return userRepository.findById(id).map(existing -> {
            if (dto.getName() != null) existing.setName(dto.getName());
            if (dto.getEmail() != null) existing.setEmail(dto.getEmail());
            if (dto.getAge() != null) existing.setAge(dto.getAge());
            if (dto.getCreatedAt() != null) existing.setCreatedAt(dto.getCreatedAt());
            User saved = userRepository.save(existing);
            return toDto(saved);
        });
    }

    @Transactional
    public boolean deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) return false;
        userRepository.deleteById(id);
        return true;
    }
}
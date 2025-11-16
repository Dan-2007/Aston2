package org.example.aston_spring_boot.service;

import org.example.aston_spring_boot.dto.UserCreateDTO;
import org.example.aston_spring_boot.dto.UserDTO;
import org.example.aston_spring_boot.dto.UserParamsDTO;
import org.example.aston_spring_boot.dto.UserUpdateDTO;
import org.example.aston_spring_boot.exception.UserNotFoundException;
import org.example.aston_spring_boot.mapper.UserMapper;
import org.example.aston_spring_boot.repository.UserRepository;
import org.example.aston_spring_boot.specification.UserSpecification;
import org.example.events.UserCreatedEvent;
import org.example.events.UserDeletedEvent;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@AllArgsConstructor
@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserSpecification userSpecification;
    private KafkaTemplate<String, Object> kafkaTemplate;

    public UserDTO findUser(Long id){
        try {
            var user = userRepository.findById(id)
                    .orElseThrow(() ->
                            new UserNotFoundException(String.format("User with id %d not found", id)));
            return userMapper.map(user);
        }
        catch (UserNotFoundException e){
            log.info("User with id {} not found", id);
            throw e;
        }
    }

    public UserDTO createUser(UserCreateDTO userDTO){
        log.debug("Creating user with email: {}", userDTO.getEmail());
        try {
            var user = userMapper.map(userDTO);
            var savedUser = userRepository.save(user);

            log.debug("User created successfully: ID={}, email={}", savedUser.getId(), savedUser.getEmail());

            var result = userMapper.map(savedUser);

            UserCreatedEvent userCreatedEvent = UserCreatedEvent.builder()
                    .id(result.getId())
                    .name(result.getName())
                    .email(result.getEmail())
                    .age(result.getAge())
                    .createdAt(result.getCreatedAt())
                    .build();

            kafkaTemplate.send("user-created-events-topic", result.getId().toString(), userCreatedEvent)
                    .whenComplete((sendResult, throwable) -> {
                        if (throwable != null) {
                            log.error("Failed to send event for user {}: {}",
                                    result.getId(), throwable.getMessage());
                        } else {
                            log.info("User {} event sent - Topic: {}, Partition: {}, Offset: {}",
                                    result.getId(),
                                    sendResult.getRecordMetadata().topic(),
                                    sendResult.getRecordMetadata().partition(),
                                    sendResult.getRecordMetadata().offset());
                        }
                    });

            return result;
        }
        catch (Exception e){
            log.error("User creation failed for email: {}", userDTO.getEmail(), e);
            throw e;
        }
    }

    public void deleteUser(Long id){
        log.debug("Deleting user with id: {}", id);
        try {
            var user = userRepository.findById(id)
                    .orElseThrow(() -> new UserNotFoundException(String.format("User with id %d not found", id)));

            UserDeletedEvent userDeletedEvent = UserDeletedEvent.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .build();

            userRepository.deleteById(id);
            log.debug("User deleted successfully: ID={}", id);

            kafkaTemplate.send("user-deleted-events-topic", user.getId().toString(), userDeletedEvent)
                    .whenComplete((sendResult, throwable) -> {
                        if (throwable != null) {
                            log.error("Failed to send deletion event for user {}: {}",
                                    user.getId(), throwable.getMessage());
                        } else {
                            log.info("User {} deletion event sent - Topic: {}, Partition: {}, Offset: {}",
                                    user.getId(),
                                    sendResult.getRecordMetadata().topic(),
                                    sendResult.getRecordMetadata().partition(),
                                    sendResult.getRecordMetadata().offset());
                        }
                    });

        } catch (UserNotFoundException e) {
            log.warn("User with id {} not found", id);
            throw e;
        } catch (Exception e) {
            log.error("User deletion failed for id: {}", id, e);
            throw e;
        }
    }

    public UserDTO updateUser(Long id, UserUpdateDTO dto){
        log.debug("Updating user with id: {}", id);
        try {
            var user = userRepository.findById(id)
                    .orElseThrow(() -> new UserNotFoundException(String.format("User with id %d not found", id)));
            userMapper.update(dto, user);
            var updatedUser = userRepository.save(user);
            return userMapper.map(updatedUser);
        }
        catch (Exception e){
            throw e;
        }
    }

    public List<UserDTO> findAllUsers(UserParamsDTO params){
        log.debug("Finding users with params: {}", params);

        try {
            var spec = userSpecification.build(params);
            var pageable = params.toPageable();
            var usersPage = userRepository.findAll(spec, pageable);
            var users = usersPage.getContent();

            if (users.isEmpty()) {
                log.debug("No users found with params: {}", params);
            } else {
                log.debug("Found {} users. Total pages: {}",
                        users.size(),
                        usersPage.getTotalPages());
            }

            return userMapper.fromUsers(users);

        } catch (Exception e) {
            log.error("Failed to find users with params: {}", params, e);
            throw e;
        }
    }

    public void existsByEmailOrThrowException(String email){
        log.debug("Checking if user with email {} exists", email);
        var userExists = userRepository.existsByEmail(email);
        if(!userExists){
            log.error("User with email {} does not exist", email);
            throw new UserNotFoundException("User with email " + email + " does not exist");
        }
    }
}

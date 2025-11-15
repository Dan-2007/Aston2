package org.example.aston_spring_boot.repository;

import org.example.aston_spring_boot.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@DataJpaTest
@Testcontainers
@DisplayName("UserRepository Integration Tests")
@ActiveProfiles("test")
class UserRepositoryTest {

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
    private TestEntityManager entityManager;


    @Autowired
    private UserRepository userRepository;


    private User testUser1;
    private User testUser2;
    private User testUser3;


    @BeforeEach
    void setUp() {
        entityManager.clear();


        testUser1 = User.builder()
                .name("John Doe")
                .email("john.doe@example.com")
                .age(25)
                .build();


        testUser2 = User.builder()
                .name("Jane Smith")
                .email("jane.smith@example.com")
                .age(30)
                .build();


        testUser3 = User.builder()
                .name("Bob Johnson")
                .email("bob.johnson@example.com")
                .age(35)
                .build();


        entityManager.persistAndFlush(testUser1);
        entityManager.persistAndFlush(testUser2);
        entityManager.persistAndFlush(testUser3);
        entityManager.clear();
    }


    @Test
    @DisplayName("Should find user by ID")
    void shouldFindUserById() {
        Optional<User> foundUser = userRepository.findById(testUser1.getId());

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getName()).isEqualTo("John Doe");
        assertThat(foundUser.get().getEmail()).isEqualTo("john.doe@example.com");
        assertThat(foundUser.get().getAge()).isEqualTo(25);
    }


    @Test
    @DisplayName("Should return empty Optional for ID that does not exist")
    void shouldReturnEmptyOptionalForNonExistentId() {
        Optional<User> foundUser = userRepository.findById(999L);

        assertThat(foundUser).isEmpty();
    }


    @Test
    @DisplayName("Should save user")
    void shouldSaveUser() {
        User newUser = User.builder()
                .name("New User")
                .email("new.user@example.com")
                .age(28)
                .build();

        User savedUser = userRepository.save(newUser);
        entityManager.flush();
        entityManager.clear();

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getName()).isEqualTo("New User");
        assertThat(savedUser.getEmail()).isEqualTo("new.user@example.com");
        assertThat(savedUser.getAge()).isEqualTo(28);
        assertThat(savedUser.getCreatedAt()).isNotNull();

        Optional<User> foundUser = userRepository.findById(savedUser.getId());
        assertThat(foundUser).isPresent();
    }

    @Test
    @DisplayName("Should partially update user")
    void shouldPartiallyUpdateUser() {
        User originalUser = userRepository.findById(testUser1.getId()).orElseThrow();
        String originalEmail = originalUser.getEmail();
        LocalDateTime originalCreatedAt = originalUser.getCreatedAt();

        originalUser.setName("John Updated");
        originalUser.setAge(33);
        userRepository.save(originalUser);
        entityManager.flush();
        entityManager.clear();

        User foundUser = userRepository.findById(testUser1.getId()).orElseThrow();

        assertThat(foundUser.getName()).isEqualTo("John Updated");
        assertThat(foundUser.getAge()).isEqualTo(33);

        assertThat(foundUser.getEmail()).isEqualTo(originalEmail);
        assertThat(foundUser.getCreatedAt()).isEqualTo(originalCreatedAt);

        assertThat(foundUser.getUpdatedAt()).isNotNull();
        assertThat(foundUser.getUpdatedAt()).isAfter(originalCreatedAt);
    }


    @Test
    @DisplayName("Should delete user")
    void shouldDeleteUser() {
        Long userId = testUser1.getId();

        userRepository.deleteById(userId);
        entityManager.flush();
        entityManager.clear();

        Optional<User> foundUser = userRepository.findById(userId);
        assertThat(foundUser).isEmpty();
    }


    @Test
    @DisplayName("Should check if user exists")
    void shouldCheckUserExistence() {
        assertThat(userRepository.existsById(testUser1.getId())).isTrue();
        assertThat(userRepository.existsById(999L)).isFalse();
    }


    @Test
    @DisplayName("Should find all users")
    void shouldFindAllUsers() {
        List<User> users = userRepository.findAll();

        assertThat(users).hasSize(3);
        assertThat(users).extracting(User::getName)
                .containsExactlyInAnyOrder("John Doe", "Jane Smith", "Bob Johnson");
    }


    @Test
    @DisplayName("Should find user with pagination")
    void shouldFindUsersWithPagination() {
        Pageable pageable = PageRequest.of(0, 2, Sort.by("name"));

        Page<User> userPage = userRepository.findAll(pageable);

        assertThat(userPage.getContent()).hasSize(2);
        assertThat(userPage.getTotalElements()).isEqualTo(3);
        assertThat(userPage.getTotalPages()).isEqualTo(2);
        assertThat(userPage.getContent()).extracting(User::getName)
                .containsExactly("Bob Johnson", "Jane Smith");
    }


    @Test
    @DisplayName("Should find users by specification (by age)")
    void shouldFindUsersBySpecification() {
        Specification<User> spec = (root, query, cb) ->
                cb.greaterThan(root.get("age"), 25);

        List<User> users = userRepository.findAll(spec);

        assertThat(users).hasSize(2);
        assertThat(users).extracting(User::getName)
                .containsExactlyInAnyOrder("Jane Smith", "Bob Johnson");
    }


    @Test
    @DisplayName("Should find users with specification and pagination")
    void shouldFindUsersBySpecificationWithPagination() {
        Specification<User> spec = (root, query, cb) ->
                cb.like(cb.lower(root.get("name")), "%j%");

        Pageable pageable = PageRequest.of(0, 2);

        Page<User> userPage = userRepository.findAll(spec, pageable);

        assertThat(userPage.getContent()).hasSize(2);
        assertThat(userPage.getContent()).extracting(User::getName)
                .containsExactlyInAnyOrder("John Doe", "Jane Smith");
    }


    @Test
    @DisplayName("Should find users by name (case insensitive)")
    void shouldFindUsersByNameCaseInsensitive() {
        Specification<User> spec = (root, query, cb) ->
                cb.like(cb.lower(root.get("name")), "%john%");

        List<User> users = userRepository.findAll(spec);

        assertThat(users).hasSize(2);
        assertThat(users).extracting(User::getName)
                .containsExactlyInAnyOrder("John Doe", "Bob Johnson");
    }


    @Test
    @DisplayName("Should find users by range of age")
    void shouldFindUsersByAgeRange() {
        Specification<User> spec = (root, query, cb) ->
                cb.and(
                        cb.greaterThanOrEqualTo(root.get("age"), 25),
                        cb.lessThanOrEqualTo(root.get("age"), 30)
                );

        List<User> users = userRepository.findAll(spec);

        assertThat(users).hasSize(2);
        assertThat(users).extracting(User::getName)
                .containsExactlyInAnyOrder("John Doe", "Jane Smith");
    }


    @Test
    @DisplayName("Should check unique of email")
    void shouldCheckEmailUniqueness() {
        User duplicateEmailUser = User.builder()
                .name("Duplicate Email")
                .email("john.doe@example.com")
                .age(40)
                .build();

        assertThatThrownBy(() -> {
            entityManager.persistAndFlush(duplicateEmailUser);
        }).isInstanceOf(Exception.class);
    }
}



package org.example.userservice.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.userservice.dto.UserRequest;
import org.example.userservice.dto.UserResponse;
import org.example.userservice.entity.User;
import org.example.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
@RefreshScope
public class UserService {

    private final UserRepository userRepository;

    // Значения из внешней конфигурации
    @Value("${app.max-users-per-page:100}")
    private int maxUsersPerPage;

    @Value("${app.circuit-breaker.enabled:true}")
    private boolean circuitBreakerEnabled;

    @Value("${app.default-user-age:18}")
    private int defaultUserAge;

    @CircuitBreaker(name = "userService", fallbackMethod = "getAllUsersFallback")
    @Retry(name = "userService", fallbackMethod = "getAllUsersFallback")
    public UserResponse createUser(UserRequest userRequest) {
        log.info("Creating new user: {}", userRequest.getEmail());

        if (userRepository.existsByEmail(userRequest.getEmail())) {
            log.warn("User with email {} already exists", userRequest.getEmail());
            throw new IllegalArgumentException("User with email " + userRequest.getEmail() + " already exists");
        }

        User user = User.builder()
                .name(userRequest.getName())
                .email(userRequest.getEmail())
                .age(userRequest.getAge() != null ? userRequest.getAge() : defaultUserAge)
                .createdAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(user);
        log.info("User created with ID: {}", savedUser.getId());

        // Логируем настройки из конфигурации
        log.debug("Circuit Breaker enabled: {}, Max users per page: {}",
                circuitBreakerEnabled, maxUsersPerPage);

        return mapToResponse(savedUser);
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "getUserByIdFallback")
    @Retry(name = "userService", fallbackMethod = "getUserByIdFallback")
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        log.info("Fetching user by ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", id);
                    return new IllegalArgumentException("User not found with ID: " + id);
                });

        log.debug("Successfully retrieved user with ID: {}", id);
        return mapToResponse(user);
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "getAllUsersFallback")
    @Retry(name = "userService", fallbackMethod = "getAllUsersFallback")
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        log.info("Fetching all users");

        List<UserResponse> users = userRepository.findAll().stream()
                .limit(maxUsersPerPage) // Ограничение из конфигурации
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        log.info("Retrieved {} users (limited to {} by config)",
                users.size(), maxUsersPerPage);
        return users;
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "updateUserFallback")
    public UserResponse updateUser(Long id, UserRequest userRequest) {
        log.info("Updating user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));

        boolean emailChanged = !user.getEmail().equals(userRequest.getEmail());

        if (emailChanged && userRepository.existsByEmail(userRequest.getEmail())) {
            log.warn("Email conflict: {} already exists", userRequest.getEmail());
            throw new IllegalArgumentException("User with email " + userRequest.getEmail() + " already exists");
        }

        user.setName(userRequest.getName());
        user.setEmail(userRequest.getEmail());
        user.setAge(userRequest.getAge() != null ? userRequest.getAge() : user.getAge());

        User updatedUser = userRepository.save(user);
        log.info("User updated with ID: {}", id);

        return mapToResponse(updatedUser);
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "deleteUserFallback")
    public void deleteUser(Long id) {
        log.info("Deleting user with ID: {}", id);

        if (!userRepository.existsById(id)) {
            log.error("Attempt to delete non-existent user with ID: {}", id);
            throw new IllegalArgumentException("User not found with ID: " + id);
        }

        userRepository.deleteById(id);
        log.info("User deleted with ID: {}", id);
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "findUserByEmailFallback")
    @Transactional(readOnly = true)
    public Optional<UserResponse> findUserByEmail(String email) {
        log.info("Finding user by email: {}", email);

        return userRepository.findByEmail(email)
                .map(user -> {
                    log.debug("Found user by email: {}", email);
                    return mapToResponse(user);
                });
    }


    private UserResponse createUserFallback(UserRequest userRequest, Throwable t) {
        log.error("Fallback: Failed to create user {}. Error: {}",
                userRequest.getEmail(), t.getMessage());

        return UserResponse.builder()
                .id(-1L)
                .name("Fallback User")
                .email("fallback@example.com")
                .age(defaultUserAge)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private UserResponse getUserByIdFallback(Long id, Throwable t) {
        log.error("Fallback: Failed to get user by ID {}. Error: {}", id, t.getMessage());

        return UserResponse.builder()
                .id(id)
                .name("Service Temporarily Unavailable")
                .email("fallback@example.com")
                .age(defaultUserAge)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private List<UserResponse> getAllUsersFallback(Throwable t) {
        log.error("Fallback: Failed to get all users. Error: {}", t.getMessage());

        if (circuitBreakerEnabled) {
            return Collections.singletonList(
                    UserResponse.builder()
                            .id(-1L)
                            .name("Service Unavailable - Circuit Breaker Active")
                            .email("circuit-breaker@example.com")
                            .age(defaultUserAge)
                            .createdAt(LocalDateTime.now())
                            .build()
            );
        }
        return Collections.emptyList();
    }

    private UserResponse updateUserFallback(Long id, UserRequest userRequest, Throwable t) {
        log.error("Fallback: Failed to update user {}. Error: {}", id, t.getMessage());

        return UserResponse.builder()
                .id(id)
                .name("Update Failed - " + userRequest.getName())
                .email(userRequest.getEmail())
                .age(userRequest.getAge() != null ? userRequest.getAge() : defaultUserAge)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private void deleteUserFallback(Long id, Throwable t) {
        log.error("Fallback: Failed to delete user {}. Error: {}", id, t.getMessage());
        throw new RuntimeException("Delete operation failed. Please try again later.");
    }

    private Optional<UserResponse> findUserByEmailFallback(String email, Throwable t) {
        log.error("Fallback: Failed to find user by email {}. Error: {}", email, t.getMessage());
        return Optional.empty();
    }


    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .age(user.getAge())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public HealthStatus checkHealth() {
        try {
            long userCount = userRepository.count();
            return HealthStatus.builder()
                    .status("UP")
                    .details("User count: " + userCount)
                    .timestamp(LocalDateTime.now())
                    .build();
        } catch (Exception e) {
            return HealthStatus.builder()
                    .status("DOWN")
                    .details("Database connection failed: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }

    @lombok.Builder
    @lombok.Data
    public static class HealthStatus {
        private String status;
        private String details;
        private LocalDateTime timestamp;
    }
}

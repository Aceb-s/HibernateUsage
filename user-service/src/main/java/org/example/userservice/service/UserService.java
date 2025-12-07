package org.example.userservice.service;

import org.example.userservice.dto.UserRequest;
import org.example.userservice.dto.UserResponse;
import org.example.userservice.entity.User;
import org.example.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final KafkaProducerService kafkaProducerService;

    public UserResponse createUser(UserRequest userRequest) {
        log.info("Creating new user: {}", userRequest.getEmail());

        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new IllegalArgumentException("User with email " + userRequest.getEmail() + " already exists");
        }

        User user = User.builder()
                .name(userRequest.getName())
                .email(userRequest.getEmail())
                .age(userRequest.getAge())
                .build();

        User savedUser = userRepository.save(user);
        log.info("User created with ID: {}", savedUser.getId());

        kafkaProducerService.sendUserEvent("CREATE", savedUser.getEmail(), savedUser.getName());

        return mapToResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        log.info("Fetching user by ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));

        return mapToResponse(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        log.info("Fetching all users");

        return userRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public UserResponse updateUser(Long id, UserRequest userRequest) {
        log.info("Updating user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));

        if (!user.getEmail().equals(userRequest.getEmail()) &&
                userRepository.existsByEmail(userRequest.getEmail())) {
            throw new IllegalArgumentException("User with email " + userRequest.getEmail() + " already exists");
        }

        user.setName(userRequest.getName());
        user.setEmail(userRequest.getEmail());
        user.setAge(userRequest.getAge());

        User updatedUser = userRepository.save(user);
        log.info("User updated with ID: {}", id);

        return mapToResponse(updatedUser);
    }

    public void deleteUser(Long id) {
        log.info("Deleting user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));

        userRepository.deleteById(id);
        log.info("User deleted with ID: {}", id);

        kafkaProducerService.sendUserEvent("DELETE", user.getEmail(), user.getName());
    }

    @Transactional(readOnly = true)
    public Optional<UserResponse> findUserByEmail(String email) {
        log.info("Finding user by email: {}", email);

        return userRepository.findByEmail(email)
                .map(this::mapToResponse);
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
}

package org.example.userservice.service;

import org.example.userservice.dto.UserRequest;
import org.example.userservice.dto.UserResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
class UserServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private UserService userService;

    @Test
    void createAndGetUser_ShouldWorkCorrectly() {
        // Given
        UserRequest userRequest = new UserRequest("John Doe", "john@example.com", 30);

        // When
        UserResponse createdUser = userService.createUser(userRequest);
        UserResponse foundUser = userService.getUserById(createdUser.getId());

        // Then
        assertNotNull(createdUser.getId());
        assertEquals("John Doe", foundUser.getName());
        assertEquals("john@example.com", foundUser.getEmail());
        assertEquals(30, foundUser.getAge());
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        // Given
        UserRequest user1 = new UserRequest("User1", "user1@example.com", 25);
        UserRequest user2 = new UserRequest("User2", "user2@example.com", 30);

        userService.createUser(user1);
        userService.createUser(user2);

        // When
        List<UserResponse> users = userService.getAllUsers();

        // Then
        assertTrue(users.size() >= 2);
    }

    @Test
    void updateUser_ShouldUpdateUserData() {
        // Given
        UserRequest userRequest = new UserRequest("Original Name", "original@example.com", 25);
        UserResponse createdUser = userService.createUser(userRequest);

        // When
        UserRequest updateRequest = new UserRequest("Updated Name", "updated@example.com", 30);
        UserResponse updatedUser = userService.updateUser(createdUser.getId(), updateRequest);

        // Then
        assertEquals("Updated Name", updatedUser.getName());
        assertEquals("updated@example.com", updatedUser.getEmail());
        assertEquals(30, updatedUser.getAge());
    }

    @Test
    void deleteUser_ShouldRemoveUser() {
        // Given
        UserRequest userRequest = new UserRequest("To Delete", "delete@example.com", 40);
        UserResponse createdUser = userService.createUser(userRequest);

        // When
        userService.deleteUser(createdUser.getId());

        // Then
        assertThrows(IllegalArgumentException.class,
                () -> userService.getUserById(createdUser.getId()));
    }
}

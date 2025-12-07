package org.example.userservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.userservice.dto.UserRequest;
import org.example.userservice.dto.UserResponse;
import org.example.userservice.service.UserService;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "API для управления пользователями")
public class UserController {

    private final UserService userService;

    @PostMapping
    @Operation(summary = "Создать пользователя", description = "Создает нового пользователя")
    @ApiResponse(responseCode = "201", description = "Пользователь создан")
    @ApiResponse(responseCode = "400", description = "Неверные данные")
    public ResponseEntity<UserResponse> createUser(
            @Parameter(description = "Данные пользователя", required = true)
            @Valid @RequestBody UserRequest userRequest) {

        log.info("Creating user: {}", userRequest.getEmail());
        UserResponse userResponse = userService.createUser(userRequest);

        // HATEOAS ссылки
        addUserLinks(userResponse);

        URI location = linkTo(methodOn(UserController.class)
                .getUser(userResponse.getId())).toUri();

        return ResponseEntity.created(location).body(userResponse);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить пользователя по ID", description = "Возвращает пользователя")
    @ApiResponse(responseCode = "200", description = "Пользователь найден")
    @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    public ResponseEntity<UserResponse> getUser(
            @Parameter(description = "ID пользователя", required = true, example = "1")
            @PathVariable Long id) {

        log.info("Getting user by ID: {}", id);
        UserResponse userResponse = userService.getUserById(id);
        addUserLinks(userResponse);
        return ResponseEntity.ok(userResponse);
    }

    @GetMapping
    @Operation(summary = "Получить всех пользователей", description = "Возвращает список пользователей")
    public ResponseEntity<CollectionModel<EntityModel<UserResponse>>> getAllUsers() {
        log.info("Getting all users");

        List<EntityModel<UserResponse>> users = userService.getAllUsers().stream()
                .map(user -> {
                    addUserLinks(user);
                    return EntityModel.of(user,
                            linkTo(methodOn(UserController.class).getUser(user.getId())).withSelfRel(),
                            linkTo(methodOn(UserController.class).getAllUsers()).withRel("users"));
                })
                .collect(Collectors.toList());

        CollectionModel<EntityModel<UserResponse>> collectionModel = CollectionModel.of(users);
        collectionModel.add(linkTo(methodOn(UserController.class).getAllUsers()).withSelfRel());

        return ResponseEntity.ok(collectionModel);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить пользователя", description = "Обновляет данные пользователя")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserRequest userRequest) {

        log.info("Updating user with ID: {}", id);
        UserResponse userResponse = userService.updateUser(id, userRequest);
        addUserLinks(userResponse);
        return ResponseEntity.ok(userResponse);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить пользователя", description = "Удаляет пользователя")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("Deleting user with ID: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Найти пользователя по email", description = "Ищет пользователя по email")
    public ResponseEntity<UserResponse> getUserByEmail(@PathVariable String email) {
        log.info("Getting user by email: {}", email);
        Optional<UserResponse> userResponse = userService.findUserByEmail(email);

        return userResponse
                .map(this::addUserLinks)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private UserResponse addUserLinks(UserResponse userResponse) {
        Long userId = userResponse.getId();

        userResponse.add(linkTo(methodOn(UserController.class).getUser(userId)).withSelfRel());
        userResponse.add(linkTo(methodOn(UserController.class).getAllUsers()).withRel("users"));
        userResponse.add(linkTo(methodOn(UserController.class).updateUser(userId, null)).withRel("update"));
        userResponse.add(linkTo(methodOn(UserController.class).deleteUser(userId)).withRel("delete"));

        return userResponse;
    }
}

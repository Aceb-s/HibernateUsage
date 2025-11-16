package org.example.service;

import org.example.dao.UserDao;
import org.example.entity.User;
import lombok.extern.java.Log;

import java.util.List;
import java.util.Optional;

@Log
public class UserService {
    private final UserDao userDao;

    public UserService() {
        this.userDao = new UserDao();
    }

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public User createUser(String name, String hometown, Integer age) {
        validateName(name);
        validateHometown(hometown);
        validateAge(age);

        User user = new User(name, hometown, age);
        Long id = userDao.save(user);
        log.info("User created with ID: " + id);
        return user;
    }

    public Optional<User> getUserById(Long id) {
        validateId(id);
        return userDao.findById(id);
    }

    public List<User> getAllUsers() {
        return userDao.findAll();
    }

    public void updateUser(Long id, String name, String hometown, Integer age) {
        validateId(id);

        User user = userDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));

        if (name != null && !name.trim().isEmpty()) {
            user.setName(name);
        }
        if (hometown != null && !hometown.trim().isEmpty()) {
            user.setHometown(hometown);
        }
        if (age != null) {
            validateAge(age);
            user.setAge(age);
        }

        userDao.update(user);
        log.info("User updated with ID: " + id);
    }

    public void deleteUser(Long id) {
        validateId(id);

        if (!userDao.findById(id).isPresent()) {
            throw new IllegalArgumentException("User not found with ID: " + id);
        }

        userDao.delete(id);
        log.info("User deleted with ID: " + id);
    }

    public Optional<User> findUserByHometown(String hometown) {
        validateHometown(hometown);
        return userDao.findByHometown(hometown);
    }

    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
    }

    private void validateHometown(String hometown) {
        if (hometown == null || hometown.trim().isEmpty()) {
            throw new IllegalArgumentException("Hometown cannot be empty");
        }
    }

    private void validateAge(Integer age) {
        if (age == null || age < 0 || age > 150) {
            throw new IllegalArgumentException("Age must be between 0 and 150");
        }
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID must be positive");
        }
    }
}

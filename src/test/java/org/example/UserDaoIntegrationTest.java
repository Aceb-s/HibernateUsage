package org.example;

import org.example.entity.User;
import org.example.dao.UserDao;
import org.example.service.UserService;
import org.example.config.HibernateUtil;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserDaoIntegrationTest {

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    private static UserDao userDao;

    @BeforeAll
    static void setUp() {
        System.setProperty("hibernate.connection.url", postgres.getJdbcUrl());
        System.setProperty("hibernate.connection.username", postgres.getUsername());
        System.setProperty("hibernate.connection.password", postgres.getPassword());
        System.setProperty("hibernate.hbm2ddl.auto", "create");

        userDao = new UserDao();
    }

    @AfterAll
    static void tearDown() {
        HibernateUtil.shutdown();
    }

    @Test
    @Order(1)
    void testSaveUser() {
        User user = new User("John Doe", "New York", 30);
        Long id = userDao.save(user);

        assertNotNull(id);
        assertTrue(id > 0);
    }

    @Test
    @Order(2)
    void testFindById() {
        User user = new User("Jane Smith", "Boston", 25);
        Long id = userDao.save(user);

        Optional<User> foundUser = userDao.findById(id);

        assertTrue(foundUser.isPresent());
        assertEquals("Jane Smith", foundUser.get().getName());
        assertEquals("Boston", foundUser.get().getHometown());
        assertEquals(25, foundUser.get().getAge());
    }

    @Test
    @Order(3)
    void testFindAll() {
        List<User> users = userDao.findAll();

        assertFalse(users.isEmpty());
        assertTrue(users.size() >= 2); // Учитываем предыдущие тесты
    }

    @Test
    @Order(4)
    void testUpdateUser() {
        User user = new User("Old Name", "Old City", 40);
        Long id = userDao.save(user);

        user.setName("New Name");
        user.setHometown("New City");
        user.setAge(45);
        userDao.update(user);

        Optional<User> updatedUser = userDao.findById(id);
        assertTrue(updatedUser.isPresent());
        assertEquals("New Name", updatedUser.get().getName());
        assertEquals("New City", updatedUser.get().getHometown());
        assertEquals(45, updatedUser.get().getAge());
    }

    @Test
    @Order(5)
    void testDeleteUser() {
        User user = new User("To Delete", "Some City", 50);
        Long id = userDao.save(user);

        assertTrue(userDao.findById(id).isPresent());

        userDao.delete(id);

        assertFalse(userDao.findById(id).isPresent());
    }

    @Test
    @Order(6)
    void testFindByHometown() {
        User user = new User("Hometown Test", "Chicago", 35);
        userDao.save(user);

        Optional<User> foundUser = userDao.findByHometown("Chicago");

        assertTrue(foundUser.isPresent());
        assertEquals("Hometown Test", foundUser.get().getName());
        assertEquals("Chicago", foundUser.get().getHometown());
    }

    @Test
    @Order(7)
    void testFindByIdNotFound() {
        Optional<User> user = userDao.findById(999999L);
        assertFalse(user.isPresent());
    }

    @Test
    @Order(8)
    void testFindByHometownNotFound() {
        Optional<User> user = userDao.findByHometown("NonExistentCity");
        assertFalse(user.isPresent());
    }
}
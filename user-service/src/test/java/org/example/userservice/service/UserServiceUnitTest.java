package org.example.userservice.sercice;

import org.example.entity.User;
import org.example.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceUnitTest {

    @Mock
    private UserDao userDao;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userDao);
    }

    @Test
    void testCreateUser_Success() {
        User user = new User("John Doe", "New York", 30);
        when(userDao.save(any(User.class))).thenReturn(1L);

        User createdUser = userService.createUser("John Doe", "New York", 30);

        assertNotNull(createdUser);
        assertEquals("John Doe", createdUser.getName());
        assertEquals("New York", createdUser.getHometown());
        assertEquals(30, createdUser.getAge());
        verify(userDao, times(1)).save(any(User.class));
    }

    @Test
    void testCreateUser_InvalidName() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.createUser("", "New York", 30));
        assertThrows(IllegalArgumentException.class,
                () -> userService.createUser(null, "New York", 30));

        verify(userDao, never()).save(any(User.class));
    }

    @Test
    void testCreateUser_InvalidHometown() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.createUser("John Doe", "", 30));
        assertThrows(IllegalArgumentException.class,
                () -> userService.createUser("John Doe", null, 30));

        verify(userDao, never()).save(any(User.class));
    }

    @Test
    void testCreateUser_InvalidAge() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.createUser("John Doe", "New York", -1));
        assertThrows(IllegalArgumentException.class,
                () -> userService.createUser("John Doe", "New York", 151));
        assertThrows(IllegalArgumentException.class,
                () -> userService.createUser("John Doe", "New York", null));

        verify(userDao, never()).save(any(User.class));
    }

    @Test
    void testGetUserById_Success() {
        User user = new User("John Doe", "New York", 30);
        when(userDao.findById(1L)).thenReturn(Optional.of(user));

        Optional<User> result = userService.getUserById(1L);

        assertTrue(result.isPresent());
        assertEquals("John Doe", result.get().getName());
        verify(userDao, times(1)).findById(1L);
    }

    @Test
    void testGetUserById_NotFound() {
        when(userDao.findById(1L)).thenReturn(Optional.empty());

        Optional<User> result = userService.getUserById(1L);

        assertFalse(result.isPresent());
        verify(userDao, times(1)).findById(1L);
    }

    @Test
    void testGetUserById_InvalidId() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.getUserById(null));
        assertThrows(IllegalArgumentException.class,
                () -> userService.getUserById(0L));
        assertThrows(IllegalArgumentException.class,
                () -> userService.getUserById(-1L));

        verify(userDao, never()).findById(anyLong());
    }

    @Test
    void testGetAllUsers() {
        // Given
        List<User> users = Arrays.asList(
                new User("John Doe", "New York", 30),
                new User("Jane Smith", "Boston", 25)
        );
        when(userDao.findAll()).thenReturn(users);

        List<User> result = userService.getAllUsers();

        assertEquals(2, result.size());
        verify(userDao, times(1)).findAll();
    }

    @Test
    void testUpdateUser_Success() {
        User existingUser = new User("Old Name", "Old City", 30);
        when(userDao.findById(1L)).thenReturn(Optional.of(existingUser));

        userService.updateUser(1L, "New Name", "New City", 35);

        assertEquals("New Name", existingUser.getName());
        assertEquals("New City", existingUser.getHometown());
        assertEquals(35, existingUser.getAge());
        verify(userDao, times(1)).update(existingUser);
    }

    @Test
    void testUpdateUser_UserNotFound() {
        when(userDao.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> userService.updateUser(1L, "New Name", "New City", 35));

        verify(userDao, never()).update(any(User.class));
    }

    @Test
    void testUpdateUser_PartialUpdate() {
        User existingUser = new User("Old Name", "Old City", 30);
        when(userDao.findById(1L)).thenReturn(Optional.of(existingUser));

        userService.updateUser(1L, "New Name", null, null);

        assertEquals("New Name", existingUser.getName());
        assertEquals("Old City", existingUser.getHometown()); // осталось прежним
        assertEquals(30, existingUser.getAge()); // осталось прежним
        verify(userDao, times(1)).update(existingUser);
    }

    @Test
    void testDeleteUser_Success() {
        User user = new User("John Doe", "New York", 30);
        when(userDao.findById(1L)).thenReturn(Optional.of(user));

        userService.deleteUser(1L);

        verify(userDao, times(1)).delete(1L);
    }

    @Test
    void testDeleteUser_UserNotFound() {
        when(userDao.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> userService.deleteUser(1L));

        verify(userDao, never()).delete(anyLong());
    }

    @Test
    void testFindUserByHometown_Success() {
        User user = new User("John Doe", "New York", 30);
        when(userDao.findByHometown("New York")).thenReturn(Optional.of(user));

        Optional<User> result = userService.findUserByHometown("New York");

        assertTrue(result.isPresent());
        assertEquals("John Doe", result.get().getName());
        verify(userDao, times(1)).findByHometown("New York");
    }

    @Test
    void testFindUserByHometown_InvalidHometown() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.findUserByHometown(""));
        assertThrows(IllegalArgumentException.class,
                () -> userService.findUserByHometown(null));

        verify(userDao, never()).findByHometown(anyString());
    }
}

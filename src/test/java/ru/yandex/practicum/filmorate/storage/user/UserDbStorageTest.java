package ru.yandex.practicum.filmorate.storage.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@Import(UserDbStorage.class)
class UserDbStorageTest {

    @Autowired
    private UserDbStorage userDbStorage;

    // ----------- Helpers -----------

    // Create a sample user with valid data
    private User createSampleUser() {
        User user = new User();
        user.setEmail("user@mail.com");
        user.setLogin("userLogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        return user;
    }

    // ----------- Tests -----------

    // Test: User should be added successfully
    @Test
    void shouldAddUserSuccessfully() {
        User user = createSampleUser();
        User savedUser = userDbStorage.addUser(user);

        assertNotNull(savedUser.getId());
        assertEquals(user.getEmail(), savedUser.getEmail());
        assertEquals(user.getLogin(), savedUser.getLogin());
        assertEquals(user.getName(), savedUser.getName());
        assertEquals(user.getBirthday(), savedUser.getBirthday());
    }

    // Test: User should be found by its ID
    @Test
    void shouldFindUserById() {
        User user = createSampleUser();
        User savedUser = userDbStorage.addUser(user);

        Optional<User> loadedUser = userDbStorage.getUserById(savedUser.getId());

        assertTrue(loadedUser.isPresent());
        assertEquals(savedUser.getId(), loadedUser.get().getId());
    }

    // Test: All users should be returned
    @Test
    void shouldReturnAllUsers() {
        User u1 = userDbStorage.addUser(createSampleUser());
        User u2 = createSampleUser();
        u2.setEmail("other@mail.com");
        u2.setLogin("otherLogin");
        userDbStorage.addUser(u2);

        List<User> users = userDbStorage.getAllUsers();

        assertTrue(users.size() >= 2);
        assertTrue(users.stream().anyMatch(u -> u.getId() == u1.getId()));
    }

    // Test: User should be updated successfully
    @Test
    void shouldUpdateUserSuccessfully() {
        User user = createSampleUser();
        User savedUser = userDbStorage.addUser(user);

        savedUser.setName("Updated Name");
        savedUser.setEmail("updated@mail.com");

        User updatedUser = userDbStorage.updateUser(savedUser);

        assertEquals("Updated Name", updatedUser.getName());
        assertEquals("updated@mail.com", updatedUser.getEmail());
    }

    // Test: Updating non-existing user should throw NotFoundException
    @Test
    void shouldThrowWhenUpdatingNonexistentUser() {
        User user = createSampleUser();
        user.setId(9999);

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> userDbStorage.updateUser(user)
        );

        assertEquals("User with id=9999 not found.", ex.getMessage());
    }
}
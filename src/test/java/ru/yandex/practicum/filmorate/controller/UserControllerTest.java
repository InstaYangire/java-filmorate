package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {
    private UserController controller;

    @BeforeEach
    void setUp() {
        controller = new UserController();
    }

    // ____________Helpers___________

    // Creates a valid User object with default correct values
    private User makeValidUser() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("user1234");
        user.setName("Name Testovich");
        user.setBirthday(LocalDate.of(2000, 2, 2));
        return user;
    }

    // Asserts that validation fails with the expected error message
    private void assertValidationFails(User user, String expectedMessage) {
        ValidationException exception = assertThrows(ValidationException.class,
                () -> controller.createUser(user));
        assertEquals(expectedMessage, exception.getMessage());
    }

    // __________Tests___________

    // Test: Valid user should be added without throwing an exception
    @Test
    void shouldAddUserWhenDataIsValid() {
        User user = makeValidUser();
        assertDoesNotThrow(() -> controller.createUser(user));
    }

    // Test: Should fail when email is empty
    @Test
    void shouldFailValidationWhenEmailIsBlank() {
        User user = makeValidUser();
        user.setEmail(" ");
        assertValidationFails(user, "Email cannot be empty and must contain the @ symbol.");
    }

    // Test: Should fail when email does not contain '@'
    @Test
    void shouldFailValidationWhenEmailHasNoAtSymbol() {
        User user = makeValidUser();
        user.setEmail("invalidemail.com");
        assertValidationFails(user, "Email cannot be empty and must contain the @ symbol.");
    }

    // Test: Should fail when login is empty
    @Test
    void shouldFailValidationWhenLoginIsBlank() {
        User user = makeValidUser();
        user.setLogin("   ");
        assertValidationFails(user, "Login cannot be empty or contain spaces.");
    }

    // Test: Should fail when login contains spaces
    @Test
    void shouldFailValidationWhenLoginContainsSpaces() {
        User user = makeValidUser();
        user.setLogin("user name");
        assertValidationFails(user, "Login cannot be empty or contain spaces.");
    }

    // Test: Should allow birthday to be today
    @Test
    void shouldAddUserWhenBirthdayIsToday() {
        User user = makeValidUser();
        user.setBirthday(LocalDate.now());
        assertDoesNotThrow(() -> controller.createUser(user));
    }

    // Test: Should fail when birthday is in the future
    @Test
    void shouldFailValidationWhenBirthdayIsInFuture() {
        User user = makeValidUser();
        user.setBirthday(LocalDate.now().plusDays(1));
        assertValidationFails(user, "The date of birth cannot be in the future.");
    }

    // Test: Should set login as name when name is null
    @Test
    void shouldSetLoginAsNameWhenNameIsNull() {
        User user = makeValidUser();
        user.setName(null);
        controller.createUser(user);
        assertEquals(user.getLogin(), user.getName());
    }

    // Test: Should set login as name when name is blank
    @Test
    void shouldSetLoginAsNameWhenNameIsBlank() {
        User user = makeValidUser();
        user.setName("   ");
        controller.createUser(user);
        assertEquals(user.getLogin(), user.getName());
    }

    // Test: Should update existing User
    @Test
    void shouldUpdateExistingUser() {
        User user = makeValidUser();
        User created = controller.createUser(user);
        created.setName("Updated Name");
        User updated = controller.updateUser(created);
        assertEquals("Updated Name", updated.getName());
    }

    // Test: Should fail when updating non-existent User
    @Test
    void shouldFailWhenUpdatingNonexistentUser() {
        User user = makeValidUser();
        user.setId(999);
        ValidationException exception = assertThrows(ValidationException.class,
                () -> controller.updateUser(user));
        assertEquals("User with id=999 not found.", exception.getMessage());
    }

    // Test: Should return all Users
    @Test
    void shouldReturnAllUsers() {
        User u1 = makeValidUser();
        User u2 = makeValidUser();
        u2.setEmail("another@example.com");
        u2.setLogin("user4321");

        controller.createUser(u1);
        controller.createUser(u2);

        assertEquals(2, controller.getAllUsers().size());
    }

    // Test: Should return all Users with correct Ids
    @Test
    void shouldReturnAllUsersWithCorrectIds() {
        User u1 = controller.createUser(makeValidUser());
        User u2 = makeValidUser();
        u2.setLogin("secondUser");
        u2.setEmail("second@example.com");
        User u2Created = controller.createUser(u2);

        List<User> users = controller.getAllUsers();
        assertEquals(2, users.size());
        assertEquals(1, u1.getId());
        assertEquals(2, u2Created.getId());
    }
}
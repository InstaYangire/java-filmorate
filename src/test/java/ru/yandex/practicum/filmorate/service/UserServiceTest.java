package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {
    private UserService service;

    // ____________Helpers___________

    // Creates a valid User object with default correct values
    private User makeValidUser(String login, String email) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName("Default Name");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        return user;
    }

    // Registers a user in the service and returns the registered object
    private User registerUser(String login, String email) {
        return service.addUser(makeValidUser(login, email));
    }

    @BeforeEach
    void setUp() {
        service = new UserService(new InMemoryUserStorage());
    }

    // ____________Tests___________

    // Test: Should add friend correctly
    @Test
    void shouldAddFriendSuccessfully() {
        User user1 = registerUser("user1", "user1@example.com");
        User user2 = registerUser("user2", "user2@example.com");

        service.addFriend(user1.getId(), user2.getId());

        assertTrue(user1.getFriends().contains(user2.getId()));
        assertTrue(user2.getFriends().contains(user1.getId()));
    }

    // Test: Should remove friend correctly
    @Test
    void shouldRemoveFriendSuccessfully() {
        User user1 = registerUser("user1", "user1@example.com");
        User user2 = registerUser("user2", "user2@example.com");

        service.addFriend(user1.getId(), user2.getId());
        service.removeFriend(user1.getId(), user2.getId());

        assertFalse(user1.getFriends().contains(user2.getId()));
        assertFalse(user2.getFriends().contains(user1.getId()));
    }

    // Test: Should return correct list of friends
    @Test
    void shouldReturnFriendsList() {
        User user1 = registerUser("user1", "user1@example.com");
        User user2 = registerUser("user2", "user2@example.com");

        service.addFriend(user1.getId(), user2.getId());
        List<User> friends = service.getFriends(user1.getId());

        assertEquals(1, friends.size());
        assertEquals(user2.getId(), friends.get(0).getId());
    }

    // Test: Should return common friends correctly
    @Test
    void shouldReturnCommonFriends() {
        User user1 = registerUser("user1", "user1@example.com");
        User user2 = registerUser("user2", "user2@example.com");
        User user3 = registerUser("user3", "user3@example.com");

        service.addFriend(user1.getId(), user3.getId());
        service.addFriend(user2.getId(), user3.getId());

        List<User> common = service.getCommonFriends(user1.getId(), user2.getId());

        assertEquals(1, common.size());
        assertEquals(user3.getId(), common.get(0).getId());
    }
}
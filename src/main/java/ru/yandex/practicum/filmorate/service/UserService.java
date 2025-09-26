package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;

import static ru.yandex.practicum.filmorate.validator.UserValidator.validate;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;
    private final FriendshipService friendshipService;
    private final FeedService feedService;

    @Autowired
    public UserService(@Qualifier("userDbStorage") UserStorage userStorage,
                       FriendshipService friendshipService,
                       FeedService feedService) {
        this.userStorage = userStorage;
        this.friendshipService = friendshipService;
        this.feedService = feedService;
    }

    //___________User____________
    public User addUser(User user) {
        log.info("Received a request to add a new user: {}", user);
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        validate(user);
        User createdUser = userStorage.addUser(user);
        log.info("User added successfully: {}", createdUser);
        return createdUser;
    }

    public User updateUser(User user) {
        log.info("Received a request to update user: {}", user);
        validate(user);
        userStorage.getUserById(user.getId())
                .orElseThrow(() -> new NotFoundException("User with id=" + user.getId() + " not found."));
        User updatedUser = userStorage.updateUser(user);
        log.info("User with id={} updated successfully.", updatedUser.getId());
        return updatedUser;
    }

    public List<User> getAllUsers() {
        List<User> users = userStorage.getAllUsers();
        log.info("Request for list of all users received. Quantity: {}", users.size());
        return users;
    }

    public User getUserById(int id) {
        return userStorage.getUserById(id)
                .orElseThrow(() -> new NotFoundException("User not found."));
    }

    //_________Friends_________
    public void addFriend(int userId, int friendId) {
        if (userId == friendId) {
            throw new ValidationException("User cannot add themselves as a friend.");
        }

        userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " not found."));
        userStorage.getUserById(friendId)
                .orElseThrow(() -> new NotFoundException("User with id=" + friendId + " not found."));

        friendshipService.addFriend(userId, friendId);

        feedService.addFeed(userId, EventType.FRIEND, Operation.ADD, friendId);

        log.info("User with id={} added user with id={} as a friend.", userId, friendId);
    }

    public User removeFriend(int userId, int friendId) {

        friendshipService.removeFriend(userId, friendId);

        User user = userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " not found."));

        feedService.addFeed(userId, EventType.FRIEND, Operation.REMOVE, friendId);
        log.info("User with id={} removed user with id={} from friends.", userId, friendId);
        return user;
    }

    public List<User> getFriends(int userId) {
        User user = userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " not found."));
        List<User> friends = friendshipService.getFriends(userId);

        log.info("Request to get friends of user with id={}. Quantity: {}", userId, friends.size());
        return friends;
    }

    public List<User> getCommonFriends(int userId, int otherId) {
        User user = userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " not found."));

        User other = userStorage.getUserById(otherId)
                .orElseThrow(() -> new NotFoundException("User with id=" + otherId + " not found."));

        List<User> commonFriends = friendshipService.getCommonFriends(userId, otherId);

        log.info("Request for common friends of users with id={} and id={}. Quantity: {}", userId, otherId, commonFriends.size());
        return commonFriends;
    }

    public void deleteUser(int id) {
        log.info("Received request to delete user with id={}", id);
        if (id <= 0) {
            throw new ValidationException("User id must be more than 0.");
        }
        getUserById(id);
        userStorage.deleteUser(id);
        log.info("User with id={} deleted successfully", id);
    }
}
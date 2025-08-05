package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    //___________User____________
    // Creating a new user
    public User addUser(User user) {
        log.info("Received a request to add a new user: {}", user);
        User createdUser = userStorage.addUser(user);
        log.info("User added successfully: {}", createdUser);
        return createdUser;
    }

    // Updating an existing user by id
    public User updateUser(User user) {
        log.info("Received a request to update user: {}", user);
        if (userStorage.getUserById(user.getId()) == null) {
            throw new NotFoundException("User with id=" + user.getId() + " not found.");
        }
        User updatedUser = userStorage.updateUser(user);
        log.info("User with id={} updated successfully.", updatedUser.getId());
        return updatedUser;
    }

    // Getting a list of all users
    public List<User> getAllUsers() {
        List<User> users = userStorage.getAllUsers();
        log.info("Request for list of all users received. Quantity: {}", users.size());
        return users;
    }

    // Getting a user by id
    public User getUserById(int id) {
        User user = userStorage.getUserById(id);
        if (user == null) {
            throw new NotFoundException("User with id=" + id + " not found.");
        }
        return user;
    }

    //_________Friends_________
    // Adding a new friend
    public void addFriend(int userId, int friendId) {
        if (userId == friendId) {
            throw new ValidationException("User cannot add themselves as a friend.");
        }

        User user = userStorage.getUserById(userId);
        if (user == null) {
            throw new NotFoundException("User with id=" + userId + " not found.");
        }

        User friend = userStorage.getUserById(friendId);
        if (friend == null) {
            throw new NotFoundException("Friend with id=" + friendId + " not found.");
        }

        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
        log.info("User with id={} added user with id={} as a friend.", userId, friendId);
    }

    // Removing a friend
    public User removeFriend(int userId, int friendId) {
        User user = userStorage.getUserById(userId);
        if (user == null) {
            throw new NotFoundException("User with id=" + userId + " not found.");
        }

        User friend = userStorage.getUserById(friendId);
        if (friend == null) {
            throw new NotFoundException("Friend with id=" + friendId + " not found.");
        }

        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
        log.info("User with id={} removed user with id={} from friends.", userId, friendId);
        return user;
    }

    // Getting a list of friends
    public List<User> getFriends(int userId) {
        User user = userStorage.getUserById(userId);
        if (user == null) {
            throw new NotFoundException("User with id=" + userId + " not found.");
        }

        List<User> friends = user.getFriends().stream()
                .map(userStorage::getUserById)
                .filter(Objects::nonNull)
                .toList();
        log.info("Request to get friends of user with id={}. Quantity: {}", userId, friends.size());
        return friends;
    }

    // Getting a list of common friends
    public List<User> getCommonFriends(int userId, int otherId) {
        User user = userStorage.getUserById(userId);
        if (user == null) {
            throw new NotFoundException("User with id=" + userId + " not found.");
        }

        User other = userStorage.getUserById(otherId);
        if (other == null) {
            throw new NotFoundException("User with id=" + otherId + " not found.");
        }

        Set<Integer> commonIds = new HashSet<>(user.getFriends());
        commonIds.retainAll(other.getFriends());

        List<User> commonFriends = commonIds.stream()
                .map(userStorage::getUserById)
                .filter(Objects::nonNull)
                .toList();

        log.info("Request for common friends of users with id={} and id={}. Quantity: {}", userId, otherId, commonFriends.size());
        return commonFriends;
    }
}

package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import java.util.*;
import static ru.yandex.practicum.filmorate.validator.UserValidator.validate;

@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users = new HashMap<>();
    private int nextId = 1;

    // Creating a new user
    @Override
    public User addUser(User user) {
        validate(user);
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        user.setId(nextId++);
        users.put(user.getId(), user);
        return user;
    }

    // Updating an existing user by id
    @Override
    public User updateUser(User user) {
        if (!users.containsKey(user.getId())) {
            throw new NotFoundException("User with id=" + user.getId() + " not found.");
        }
        users.put(user.getId(), user);
        return user;
    }

    // Getting a user by id
    @Override
    public Optional<User> getUserById(int id) {
        return Optional.ofNullable(users.get(id));
    }

    // Getting a list of all users
    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }
}
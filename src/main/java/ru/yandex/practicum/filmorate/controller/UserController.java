package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Integer, User> users = new HashMap<>();
    private int nextId = 1;

    // Creating a new user
    @PostMapping
    public User createUser(@RequestBody User user) {
        log.info("Received a request to add a new user: {}", user);
        validateUser(user);
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        user.setId(nextId++);
        users.put(user.getId(), user);
        log.info("User added successfully: {}", user);
        return user;
    }

    // Update existing user by id
    @PutMapping
    public User updateUser(@RequestBody User user) {
        log.info("Received a request to update user: {}", user);
        validateUser(user);
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        if (!users.containsKey(user.getId())) {
            log.warn("User with id={} not found.", user.getId());
            throw new ValidationException("User with id=" + user.getId() + " not found.");
        }
        users.put(user.getId(), user);
        log.info("User with id={} updated successfully.", user.getId());
        return user;
    }

    // Getting a list of all users
    @GetMapping
    public List<User> getAllUsers() {
        log.info("Request for list of all users received. Quantity: {}", users.size());
        return new ArrayList<>(users.values());
    }

    // User data validation
    private void validateUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.warn("Validation error: email does not contain the @ symbol or is empty.");
            throw new ValidationException("Email cannot be empty and must contain the @ symbol.");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            log.warn("Validation error: login is empty or contains spaces.");
            throw new ValidationException("Login cannot be empty or contain spaces.");
        }
        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Validation error: date of birth in the future.");
            throw new ValidationException("The date of birth cannot be in the future.");
        }
    }
}

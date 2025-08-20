package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Friendship {
    private final int userId;      // Who sent the friend request
    private final int friendId;    // Who received the request
    private final boolean confirmed; // Whether the friendship is confirmed
}
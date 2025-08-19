package ru.yandex.practicum.filmorate.model;

import lombok.Data;

@Data
public class Friendship {
    private int userId;          // The one who initiated the friendship
    private int friendId;        // To whom the friend request was sent
    private FriendshipStatus status;
}

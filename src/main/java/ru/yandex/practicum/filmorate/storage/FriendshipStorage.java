package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Friendship;

import java.util.List;

public interface FriendshipStorage {
    void addFriendship(Friendship friendship);

    void confirmFriendship(int userId, int friendId);

    void removeFriendship(int userId, int friendId);

    List<Friendship> getFriendshipsByUser(int userId);
}

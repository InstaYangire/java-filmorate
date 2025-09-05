package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Friendship;

import java.util.List;

public interface FriendshipStorage {

    void add(Friendship friendship);              // Create

    void update(Friendship friendship);           // Update

    void remove(Friendship friendship);           // Delete

    List<Friendship> getFriendshipsByUserId(int userId); // Read
}
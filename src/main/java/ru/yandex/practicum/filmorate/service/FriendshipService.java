package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.storage.FriendshipStorage;
import java.util.List;

public class FriendshipService {

    private final FriendshipStorage friendshipStorage;

    public FriendshipService(FriendshipStorage friendshipStorage) {
        this.friendshipStorage = friendshipStorage;
    }

    // Add a friend (creates a friend request)
    public void addFriend(int userId, int friendId) {
        Friendship friendship = new Friendship(userId, friendId, false); // false = not confirmed
        friendshipStorage.add(friendship);
    }

    // Confirm a friendship request
    public void confirmFriendship(int userId, int friendId) {
        Friendship friendship = new Friendship(userId, friendId, true); // true = confirmed
        friendshipStorage.update(friendship);
    }

    // Remove a friendship
    public void removeFriend(int userId, int friendId) {
        Friendship friendship = new Friendship(userId, friendId, false); // status doesn't matter here
        friendshipStorage.remove(friendship);
    }

    // Get the list of friends for a user
    public List<Friendship> getFriends(int userId) {
        return friendshipStorage.getFriendshipsByUserId(userId);
    }
}

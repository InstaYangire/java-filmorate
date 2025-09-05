package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FriendshipStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// Service for managing friendships between users
@Service
public class FriendshipService {

    private final FriendshipStorage friendshipStorage;
    private final UserStorage userStorage;

    public FriendshipService(FriendshipStorage friendshipStorage, @Qualifier("userDbStorage") UserStorage userStorage) {
        this.friendshipStorage = friendshipStorage;
        this.userStorage = userStorage;
    }

    // Add a friend (creates a friend request)
    public void addFriend(int userId, int friendId) {
        if (userId == friendId) {
            throw new IllegalArgumentException("You cannot add yourself as a friend.");
        }

        // Check if the target user exists
        userStorage.getUserById(friendId)
                .orElseThrow(() -> new NotFoundException("User not found: " + friendId));

        Friendship friendship = new Friendship(userId, friendId, false); // false = not confirmed
        friendshipStorage.add(friendship);
    }

    // Confirm a friendship request
    public void confirmFriendship(int userId, int friendId) {
        // Check if there's a pending request from friendId to userId
        List<Friendship> pending = friendshipStorage.getFriendshipsByUserId(friendId).stream()
                .filter(f -> f.getFriendId() == userId && !f.isConfirmed())
                .toList();

        if (pending.isEmpty()) {
            throw new NotFoundException("No pending friend request from user " + friendId);
        }

        // Update both directions as confirmed
        friendshipStorage.update(new Friendship(friendId, userId, true));
        friendshipStorage.add(new Friendship(userId, friendId, true));
    }

    // Remove a friendship
    public void removeFriend(int userId, int friendId) {
        userStorage.getUserById(friendId)
                .orElseThrow(() -> new NotFoundException("User not found: " + friendId));

        Friendship friendship = new Friendship(userId, friendId, false);
        friendshipStorage.remove(friendship);
    }

    // Get the list of friends for a user
    public List<User> getFriends(int userId) {
        List<Friendship> friendships = friendshipStorage.getFriendshipsByUserId(userId);
        return friendships.stream()
                .map(f -> userStorage.getUserById(f.getFriendId())
                        .orElseThrow(() -> new NotFoundException("User not found: " + f.getFriendId())))
                .toList();
    }

    // Get the list of common friends between two users
    public List<User> getCommonFriends(int userId, int otherUserId) {
        Set<Integer> user1FriendIds = friendshipStorage.getFriendshipsByUserId(userId).stream()
                .map(Friendship::getFriendId)
                .collect(Collectors.toSet());

        Set<Integer> user2FriendIds = friendshipStorage.getFriendshipsByUserId(otherUserId).stream()
                .map(Friendship::getFriendId)
                .collect(Collectors.toSet());

        user1FriendIds.retainAll(user2FriendIds); // keep only common IDs

        return user1FriendIds.stream()
                .map(id -> userStorage.getUserById(id)
                        .orElseThrow(() -> new NotFoundException("User not found: " + id)))
                .toList();
    }
}
package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.storage.FriendshipStorage;

import java.util.*;

@Component
public class InMemoryFriendshipStorage implements FriendshipStorage {

    private final Set<Friendship> friendships = new HashSet<>();

    @Override
    public void add(Friendship friendship) {
        friendships.add(friendship);
    }

    @Override
    public void update(Friendship friendship) {
        friendships.remove(friendship);
        friendships.add(friendship);
    }

    @Override
    public void remove(Friendship friendship) {
        friendships.remove(friendship);
    }

    @Override
    public List<Friendship> getFriendshipsByUserId(int userId) {
        List<Friendship> result = new ArrayList<>();
        for (Friendship f : friendships) {
            if (f.getUserId() == userId) {
                result.add(f);
            }
        }
        return result;
    }
}
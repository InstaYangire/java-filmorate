package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.storage.FeedStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedService {

    private final FeedStorage feedStorage;
    private final UserStorage userStorage;

    public void addFeed(int userId, EventType eventType, Operation operation, int entityId) {
        userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " not found."));

        Feed feed = new Feed();
        feed.setTimestamp(Instant.now().toEpochMilli());
        feed.setUserId(userId);
        feed.setEventType(eventType);
        feed.setOperation(operation);
        feed.setEntityId(entityId);

        feedStorage.addFeed(feed);
        log.info("Feed recorded: user={}, type={}, operation={}, entity={}",
                userId, eventType, operation, entityId);
    }

    public List<Feed> getFeedByUserId(int userId) {
        userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " not found."));
        return feedStorage.getFeedByUserId(userId);
    }

    public void removeUserFeed(int userId) {
        feedStorage.removeFeedByUserId(userId);
        log.info("All feed events for user {} removed", userId);
    }
}

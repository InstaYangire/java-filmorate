package ru.yandex.practicum.filmorate.storage.feed;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.Operation;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@Import(FeedDbStorage.class)
class FeedDbStorageTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private FeedDbStorage feedDbStorage;

    @BeforeEach
    void setUp() {
        // Создаем таблицу для тестов
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS feed (" +
                "event_id INT PRIMARY KEY AUTO_INCREMENT, " +
                "timestamp BIGINT NOT NULL, " +
                "user_id INT NOT NULL, " +
                "event_type VARCHAR NOT NULL, " +
                "operation VARCHAR NOT NULL, " +
                "entity_id INT NOT NULL)");

        // Очищаем таблицу перед каждым тестом
        jdbcTemplate.execute("DELETE FROM feed");
        jdbcTemplate.execute("DELETE FROM friendships");
        jdbcTemplate.execute("DELETE FROM film_likes");
        jdbcTemplate.execute("DELETE FROM reviews");
        jdbcTemplate.execute("DELETE FROM films");
        jdbcTemplate.execute("DELETE FROM users");
    }

    // ----------- Helpers -----------

    private Feed createSampleFeed(int userId, EventType eventType, Operation operation, int entityId) {
        Feed feed = new Feed();
        feed.setTimestamp(System.currentTimeMillis());
        feed.setUserId(userId);
        feed.setEventType(eventType);
        feed.setOperation(operation);
        feed.setEntityId(entityId);
        return feed;
    }

    // ----------- Tests -----------

    @Test
    void shouldAddFeedSuccessfully() {
        Feed feed = createSampleFeed(1, EventType.LIKE, Operation.ADD, 100);

        Feed savedFeed = feedDbStorage.addFeed(feed);

        assertNotNull(savedFeed.getEventId());
        assertEquals(feed.getUserId(), savedFeed.getUserId());
        assertEquals(feed.getEventType(), savedFeed.getEventType());
        assertEquals(feed.getOperation(), savedFeed.getOperation());
        assertEquals(feed.getEntityId(), savedFeed.getEntityId());
        assertEquals(feed.getTimestamp(), savedFeed.getTimestamp());
    }

    @Test
    void shouldReturnFeedByUserId() {
        Feed feed1 = feedDbStorage.addFeed(createSampleFeed(1, EventType.LIKE, Operation.ADD, 100));
        Feed feed2 = feedDbStorage.addFeed(createSampleFeed(1, EventType.FRIEND, Operation.ADD, 200));
        feedDbStorage.addFeed(createSampleFeed(2, EventType.REVIEW, Operation.ADD, 300)); // Different user

        List<Feed> user1Feeds = feedDbStorage.getFeedByUserId(1);

        assertEquals(2, user1Feeds.size());
        assertTrue(user1Feeds.stream().anyMatch(f -> f.getEventId() == feed1.getEventId()));
        assertTrue(user1Feeds.stream().anyMatch(f -> f.getEventId() == feed2.getEventId()));
    }

    @Test
    void shouldReturnEmptyListWhenNoFeedsForUser() {
        feedDbStorage.addFeed(createSampleFeed(1, EventType.LIKE, Operation.ADD, 100));

        List<Feed> user999Feeds = feedDbStorage.getFeedByUserId(999);

        assertNotNull(user999Feeds);
        assertTrue(user999Feeds.isEmpty());
    }

    @Test
    void shouldReturnFeedsInAscendingTimestampOrder() {
        Feed feed1 = createSampleFeed(1, EventType.LIKE, Operation.ADD, 100);
        feed1.setTimestamp(1000L);
        feedDbStorage.addFeed(feed1);

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Feed feed2 = createSampleFeed(1, EventType.FRIEND, Operation.ADD, 200);
        feed2.setTimestamp(2000L);
        feedDbStorage.addFeed(feed2);

        List<Feed> feeds = feedDbStorage.getFeedByUserId(1);

        assertEquals(2, feeds.size());
        assertEquals(feed1.getEventType(), feeds.get(0).getEventType());
        assertEquals(feed2.getEventType(), feeds.get(1).getEventType());
    }

    @Test
    void shouldHandleMultipleEventTypes() {
        Feed likeFeed = feedDbStorage.addFeed(createSampleFeed(1, EventType.LIKE, Operation.ADD, 100));
        Feed reviewFeed = feedDbStorage.addFeed(createSampleFeed(1, EventType.REVIEW, Operation.ADD, 200));
        Feed friendFeed = feedDbStorage.addFeed(createSampleFeed(1, EventType.FRIEND, Operation.ADD, 300));

        List<Feed> feeds = feedDbStorage.getFeedByUserId(1);
        assertEquals(3, feeds.size());

        assertTrue(feeds.stream().anyMatch(f -> f.getEventType() == EventType.LIKE));
        assertTrue(feeds.stream().anyMatch(f -> f.getEventType() == EventType.REVIEW));
        assertTrue(feeds.stream().anyMatch(f -> f.getEventType() == EventType.FRIEND));
    }

    @Test
    void shouldHandleMultipleOperations() {
        Feed addFeed = feedDbStorage.addFeed(createSampleFeed(1, EventType.LIKE, Operation.ADD, 100));
        Feed removeFeed = feedDbStorage.addFeed(createSampleFeed(1, EventType.LIKE, Operation.REMOVE, 100));
        Feed updateFeed = feedDbStorage.addFeed(createSampleFeed(1, EventType.REVIEW, Operation.UPDATE, 200));

        List<Feed> feeds = feedDbStorage.getFeedByUserId(1);
        assertEquals(3, feeds.size());

        assertTrue(feeds.stream().anyMatch(f -> f.getOperation() == Operation.ADD));
        assertTrue(feeds.stream().anyMatch(f -> f.getOperation() == Operation.REMOVE));
        assertTrue(feeds.stream().anyMatch(f -> f.getOperation() == Operation.UPDATE));
    }

    @Test
    void shouldGenerateUniqueEventIds() {
        Feed feed1 = feedDbStorage.addFeed(createSampleFeed(1, EventType.LIKE, Operation.ADD, 100));
        Feed feed2 = feedDbStorage.addFeed(createSampleFeed(1, EventType.FRIEND, Operation.ADD, 200));
        Feed feed3 = feedDbStorage.addFeed(createSampleFeed(2, EventType.REVIEW, Operation.ADD, 300));

        assertNotEquals(feed1.getEventId(), feed2.getEventId());
        assertNotEquals(feed1.getEventId(), feed3.getEventId());
        assertNotEquals(feed2.getEventId(), feed3.getEventId());
    }
}

package ru.yandex.practicum.filmorate.storage.feed;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.storage.FeedStorage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class FeedDbStorage implements FeedStorage {

    private final JdbcTemplate jdbcTemplate;

    // Adding feed
    @Override
    public Feed addFeed(Feed feed) {
        String sql = "INSERT INTO feed (timestamp, user_id, event_type, operation, entity_id) " +
                "VALUES (?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setLong(1, feed.getTimestamp());
            stmt.setInt(2, feed.getUserId());
            stmt.setString(3, feed.getEventType().name());
            stmt.setString(4, feed.getOperation().name());
            stmt.setInt(5, feed.getEntityId());
            return stmt;
        }, keyHolder);

        feed.setEventId(Objects.requireNonNull(keyHolder.getKey()).intValue());
        return feed;
    }

    // List user feed
    @Override
    public List<Feed> getFeedByUserId(int userId) {
        String sql = "SELECT * FROM feed WHERE user_id = ? ORDER BY timestamp ASC";
        return jdbcTemplate.query(sql, this::mapRowToFeed, userId);
    }

    // Removing feed by user ID
    @Override
    public void removeFeedByUserId(int userId) {
        String sql = "DELETE FROM feed WHERE user_id = ?";
        jdbcTemplate.update(sql, userId);
    }

    private Feed mapRowToFeed(ResultSet rs, int rowNum) throws SQLException {
        return new Feed(
                rs.getInt("event_id"),
                rs.getLong("timestamp"),
                rs.getInt("user_id"),
                EventType.valueOf(rs.getString("event_type")),
                Operation.valueOf(rs.getString("operation")),
                rs.getInt("entity_id")
        );
    }
}

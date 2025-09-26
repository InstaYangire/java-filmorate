package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.storage.FriendshipStorage;

import java.util.List;

@Component
@Primary
@RequiredArgsConstructor
public class FriendshipDbStorage implements FriendshipStorage {

    private final JdbcTemplate jdbcTemplate;

    // ----------- Private helpers -----------

    // Convert boolean "confirmed" into DB string value
    private String toDbStatus(boolean confirmed) {
        return confirmed ? "CONFIRMED" : "UNCONFIRMED";
    }

    // Convert DB string value into boolean "confirmed"
    private boolean fromDbStatus(String status) {
        return "CONFIRMED".equals(status);
    }

    // ----------- CRUD methods -----------

    @Override
    public void add(Friendship friendship) {
        String sql = "INSERT INTO friendships (user_id, friend_id, status) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql,
                friendship.getUserId(),
                friendship.getFriendId(),
                toDbStatus(friendship.isConfirmed()));
    }

    @Override
    public void update(Friendship friendship) {
        String sql = "UPDATE friendships SET status = ? WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql,
                toDbStatus(friendship.isConfirmed()),
                friendship.getUserId(),
                friendship.getFriendId());
    }

    @Override
    public void remove(Friendship friendship) {
        String sql = "DELETE FROM friendships WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql,
                friendship.getUserId(),
                friendship.getFriendId());
    }

    @Override
    public List<Friendship> getFriendshipsByUserId(int userId) {
        String sql = "SELECT * FROM friendships WHERE user_id = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new Friendship(
                rs.getInt("user_id"),
                rs.getInt("friend_id"),
                fromDbStatus(rs.getString("status"))
        ), userId);
    }
}
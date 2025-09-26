package ru.yandex.practicum.filmorate.storage.review;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReviewDbStorage implements ReviewStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Review create(Review review) {
        String sql = "INSERT INTO reviews (content, is_positive, user_id, film_id, useful) VALUES (?, ?, ?, ?, 0)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"review_id"});
            ps.setString(1, review.getContent());
            ps.setBoolean(2, review.getIsPositive());
            ps.setInt(3, review.getUserId());
            ps.setInt(4, review.getFilmId());
            return ps;
        }, keyHolder);

        review.setReviewId(keyHolder.getKey().intValue());
        review.setUseful(0);
        return review;
    }

    @Override
    public Review update(Review review) {
        String sql = "UPDATE reviews SET content = ?, is_positive = ? WHERE review_id = ?";
        int updated = jdbcTemplate.update(sql, review.getContent(), review.getIsPositive(), review.getReviewId());

        if (updated == 0) {
            throw new NotFoundException("Review not found with id=" + review.getReviewId());
        }
        return findById(review.getReviewId()).get();
    }

    @Override
    public void delete(int reviewId) {
        int deleted = jdbcTemplate.update("DELETE FROM reviews WHERE review_id = ?", reviewId);
        if (deleted == 0) {
            throw new NotFoundException("Review not found with id=" + reviewId);
        }
    }

    @Override
    public Optional<Review> findById(int reviewId) {
        String sql = "SELECT * FROM reviews WHERE review_id = ?";
        List<Review> reviews = jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToReview(rs), reviewId);
        return reviews.stream().findFirst();
    }

    @Override
    public List<Review> findByFilmId(Integer filmId, int count) {
        String sql = """
                SELECT r.review_id, r.content, r.is_positive, r.user_id, r.film_id, r.useful
                FROM reviews r
                WHERE (? IS NULL OR r.film_id = ?)
                ORDER BY r.useful DESC
                LIMIT ?
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> mapRowToReview(rs),
                filmId, filmId, count
        );
    }

    @Override
    @Transactional
    public void addLike(int reviewId, int userId) {
        Boolean isPositive = jdbcTemplate.query(
                "SELECT is_positive FROM review_likes WHERE review_id = ? AND user_id = ?",
                (rs, rowNum) -> rs.getBoolean("is_positive"),
                reviewId, userId
        ).stream().findFirst().orElse(null);

        if (isPositive == null) {
            jdbcTemplate.update(
                    "INSERT INTO review_likes (review_id, user_id, is_positive) VALUES (?, ?, TRUE)",
                    reviewId, userId
            );
            jdbcTemplate.update(
                    "UPDATE reviews SET useful = useful + 1 WHERE review_id = ?",
                    reviewId
            );
        } else if (!isPositive) {
            jdbcTemplate.update(
                    "UPDATE review_likes SET is_positive = TRUE WHERE review_id = ? AND user_id = ?",
                    reviewId, userId
            );
            jdbcTemplate.update(
                    "UPDATE reviews SET useful = useful + 2 WHERE review_id = ?",
                    reviewId
            );
        }
    }

    @Transactional
    public void addDislike(int reviewId, int userId) {
        Boolean isPositive = jdbcTemplate.query(
                "SELECT is_positive FROM review_likes WHERE review_id = ? AND user_id = ?",
                (rs, rowNum) -> rs.getBoolean("is_positive"),
                reviewId, userId
        ).stream().findFirst().orElse(null);

        if (isPositive == null) {
            jdbcTemplate.update(
                    "INSERT INTO review_likes (review_id, user_id, is_positive) VALUES (?, ?, FALSE)",
                    reviewId, userId
            );
            jdbcTemplate.update(
                    "UPDATE reviews SET useful = useful - 1 WHERE review_id = ?",
                    reviewId
            );
        } else if (isPositive) {
            jdbcTemplate.update(
                    "UPDATE review_likes SET is_positive = FALSE WHERE review_id = ? AND user_id = ?",
                    reviewId, userId
            );
            jdbcTemplate.update(
                    "UPDATE reviews SET useful = useful - 2 WHERE review_id = ?",
                    reviewId
            );
        }
    }

    @Override
    @Transactional
    public void removeLike(int reviewId, int userId) {
        int deleted = jdbcTemplate.update(
                "DELETE FROM review_likes WHERE review_id = ? AND user_id = ? AND is_positive = TRUE",
                reviewId, userId
        );
        if (deleted > 0) {
            jdbcTemplate.update("UPDATE reviews SET useful = useful - 1 WHERE review_id = ?", reviewId);
        }
    }

    @Override
    @Transactional
    public void removeDislike(int reviewId, int userId) {
        int deleted = jdbcTemplate.update(
                "DELETE FROM review_likes WHERE review_id = ? AND user_id = ? AND is_positive = FALSE",
                reviewId, userId
        );
        if (deleted > 0) {
            jdbcTemplate.update("UPDATE reviews SET useful = useful + 1 WHERE review_id = ?", reviewId);
        }
    }

    private Review mapRowToReview(ResultSet rs) throws SQLException {
        return new Review(
                rs.getInt("review_id"),
                rs.getString("content"),
                rs.getBoolean("is_positive"),
                rs.getInt("user_id"),
                rs.getInt("film_id"),
                rs.getInt("useful")
        );
    }
}
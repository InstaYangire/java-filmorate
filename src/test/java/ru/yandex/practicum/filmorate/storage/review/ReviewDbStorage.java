package ru.yandex.practicum.filmorate.storage.review;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component("reviewDbStorage")
@RequiredArgsConstructor
public class ReviewDbStorage implements ReviewStorage {

    private final JdbcTemplate jdbcTemplate;
    // private final UserStorage userStorage; <-- УДАЛЕНО
    // private final FilmStorage filmStorage; <-- УДАЛЕНО

    @Override
    public Review createReview(Review review) {
        // ВАЛИДАЦИЯ УДАЛЕНА ОТСЮДА. Она теперь в ReviewService.

        String sql = "INSERT INTO reviews (content, is_positive, user_id, film_id) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, review.getContent());
            ps.setBoolean(2, review.getIsPositive());
            ps.setInt(3, Math.toIntExact(review.getUserId())); // <-- Исправлено с setLong на setInt
            ps.setInt(4, Math.toIntExact(review.getFilmId())); // <-- Исправлено с setLong на setInt
            return ps;
        }, keyHolder);

        int id = Objects.requireNonNull(keyHolder.getKey()).intValue(); // <-- Исправлено с longValue() на intValue()
        review.setReviewId((long) id);
        review.setUseful(0);
        return review;
    }

    @Override
    public Review updateReview(Review review) {
        String sql = "UPDATE reviews SET content = ?, is_positive = ?, user_id = ?, film_id = ? WHERE review_id = ?";
        int rowsUpdated = jdbcTemplate.update(sql,
                review.getContent(),
                review.getIsPositive(),
                review.getUserId(),
                review.getFilmId(),
                review.getReviewId());

        if (rowsUpdated == 0) {
            throw new NotFoundException("Review with id=" + review.getReviewId() + " not found for update.");
        }

        return getReviewById(review.getReviewId())
                .orElseThrow(() -> new IllegalStateException("Review vanished after update."));
    }

    @Override
    public void deleteReview(Long id) {

    }

    @Override
    public Optional<Review> getReviewById(Long id) {
        return Optional.empty();
    }

    @Override
    public void deleteReview(int id) {
        String sql = "DELETE FROM reviews WHERE review_id = ?";
        int rowsDeleted = jdbcTemplate.update(sql, id);
        if (rowsDeleted == 0) {
            throw new NotFoundException("Review with id=" + id + " not found for deletion.");
        }
        jdbcTemplate.update("DELETE FROM review_reactions WHERE review_id = ?", id);
    }

    @Override
    public Optional<Review> getReviewById(int id) {
        String sql = "SELECT * FROM reviews WHERE review_id = ?";
        List<Review> reviews = jdbcTemplate.query(sql, this::mapRowToReview, id);
        return reviews.stream().findFirst();
    }

    @Override
    public List<Review> getAllReviews() {
        String sql = "SELECT * FROM reviews";
        return jdbcTemplate.query(sql, this::mapRowToReview);
    }

    @Override
    public List<Review> getReviewsByFilmId(Long filmId) {
        return List.of();
    }

    @Override
    public void addLike(Long reviewId, Long userId) {

    }

    @Override
    public void addDislike(Long reviewId, Long userId) {

    }

    @Override
    public void deleteLike(Long reviewId, Long userId) {

    }

    @Override
    public List<Review> getReviewsByFilmId(int filmId) {
        String sql = "SELECT * FROM reviews WHERE film_id = ?";
        return jdbcTemplate.query(sql, this::mapRowToReview, filmId);
    }

    @Override
    public void addLike(int reviewId, int userId) {
        updateReaction(reviewId, userId, true);
    }

    @Override
    public void addDislike(int reviewId, int userId) {
        updateReaction(reviewId, userId, false);
    }

    @Override
    public void deleteLike(int reviewId, int userId) {
        String sql = "DELETE FROM review_reactions WHERE review_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, reviewId, userId);
        updateUsefulCount(reviewId);
    }

    private void updateReaction(int reviewId, int userId, boolean isLike) {
        deleteLike(reviewId, userId);
        String sql = "INSERT INTO review_reactions (review_id, user_id, is_like) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, reviewId, userId, isLike);
        updateUsefulCount(reviewId);
    }

    private void updateUsefulCount(int reviewId) {
        String sql = "SELECT SUM(CASE WHEN is_like THEN 1 ELSE -1 END) AS useful_count " +
                "FROM review_reactions WHERE review_id = ?";
        Integer useful = jdbcTemplate.queryForObject(sql, Integer.class, reviewId);
        useful = useful == null ? 0 : useful;

        String updateSql = "UPDATE reviews SET useful = ? WHERE review_id = ?";
        jdbcTemplate.update(updateSql, useful, reviewId);
    }

    private Review mapRowToReview(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        Review review = new Review();
        review.setReviewId((long) rs.getInt("review_id"));
        review.setContent(rs.getString("content"));
        review.setIsPositive(rs.getBoolean("is_positive"));
        review.setUserId((long) rs.getInt("user_id"));
        review.setFilmId((long) rs.getInt("film_id"));
        review.setUseful(rs.getInt("useful"));
        return review;
    }
}
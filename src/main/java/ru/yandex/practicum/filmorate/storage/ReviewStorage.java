package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewStorage {
    Review createReview(Review review);
    Review updateReview(Review review);
    void deleteReview(Long id);
    Optional<Review> getReviewById(Long id);
    List<Review> getAllReviews();
    List<Review> getReviewsByFilmId(Long filmId);
    void addLike(Long reviewId, Long userId);
    void addDislike(Long reviewId, Long userId);
    void deleteLike(Long reviewId, Long userId);
}
package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewStorage {
    Review createReview(Review review);

    Review updateReview(Review review);

    void deleteReview(int id);

    Optional<Review> getReviewById(int id);

    List<Review> getAllReviews();

    List<Review> getReviewsByFilmId(int filmId);

    void addLike(int reviewId, int userId);

    void addDislike(int reviewId, int userId);

    void deleteLike(int reviewId, int userId);

}
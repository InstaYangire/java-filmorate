package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Optional;

// Storage interface for reviews
public interface ReviewStorage {

    // Create a new review
    Review create(Review review);

    // Update an existing review
    Review update(Review review);

    // Delete a review by ID
    void delete(int reviewId);

    // Find review by ID
    Optional<Review> findById(int reviewId);

    // Find all reviews for a film (or all reviews if filmId = null)
    List<Review> findByFilmId(Integer filmId, int count);

    // Add like to review
    void addLike(int reviewId, int userId);

    // Add dislike to review
    void addDislike(int reviewId, int userId);

    // Remove like from review
    void removeLike(int reviewId, int userId);

    // Remove dislike from review
    void removeDislike(int reviewId, int userId);
}
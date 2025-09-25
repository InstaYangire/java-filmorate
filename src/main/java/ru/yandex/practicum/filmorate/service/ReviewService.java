package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;

// Service layer for reviews
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewStorage reviewStorage;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;
    private final FeedService feedService;

    // Create a new review
    public Review create(Review review) {
        validateUserAndFilm(review.getUserId(), review.getFilmId());
        validateReviewProps(review);

        Review createdReview = reviewStorage.create(review);

        feedService.addFeed(createdReview.getUserId(),
                EventType.REVIEW,
                Operation.ADD,
                createdReview.getReviewId());

        return createdReview;
    }

    // Update review
    public Review update(Review review) {
        validateUserAndFilm(review.getUserId(), review.getFilmId());
        Review existingReview = getById(review.getReviewId());
        Review updatedReview = reviewStorage.update(review);
        feedService.addFeed(existingReview.getUserId(), EventType.REVIEW, Operation.UPDATE, review.getReviewId());
        return updatedReview;
    }

    // Delete review by ID
    public void delete(int reviewId) {
        checkReview(reviewId);
        Review existingReview = getById(reviewId);
        reviewStorage.delete(reviewId);
        feedService.addFeed(existingReview.getUserId(), EventType.REVIEW, Operation.REMOVE, reviewId);
    }

    // Get review by ID
    public Review getById(int reviewId) {
        return reviewStorage.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Review with id=" + reviewId + " not found"));
    }

    // Get reviews for a film (or all if filmId == null)
    public List<Review> getByFilm(Integer filmId, int count) {
        return reviewStorage.findByFilmId(filmId, count);
    }

    // Add like
    public void addLike(int reviewId, int userId) {
        checkUser(userId);
        checkReview(reviewId);
        reviewStorage.addLike(reviewId, userId);
    }

    // Add dislike
    public void addDislike(int reviewId, int userId) {
        checkUser(userId);
        checkReview(reviewId);
        reviewStorage.addDislike(reviewId, userId);
    }

    // Remove like
    public void removeLike(int reviewId, int userId) {
        checkUser(userId);
        checkReview(reviewId);
        reviewStorage.removeLike(reviewId, userId);
    }

    // Remove dislike
    public void removeDislike(int reviewId, int userId) {
        checkUser(userId);
        checkReview(reviewId);
        reviewStorage.removeDislike(reviewId, userId);
    }

    // Helpers
    private void validateUserAndFilm(Integer userId, Integer filmId) {
        checkUser(userId);
        checkFilm(filmId);
    }

    private void checkUser(Integer userId) {
        if (userId == null) {
            throw new ValidationException("User id cannot be null");
        }
        userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " not found"));
    }

    private void checkFilm(Integer filmId) {
        if (filmId == null) {
            throw new ValidationException("Film id cannot be null");
        }
        filmStorage.getFilmById(filmId)
                .orElseThrow(() -> new NotFoundException("Film with id=" + filmId + " not found"));
    }

    private void checkReview(Integer reviewId) {
        if (reviewId == null) {
            throw new ValidationException("Review id cannot be null");
        }
        reviewStorage.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Review with id=" + reviewId + " not found"));
    }

    private void validateReviewProps(Review review) {
        if (review.getContent() == null) {
            throw new ValidationException("Empty content is not allowed");
        }

        if (review.getIsPositive() == null) {
            throw new ValidationException("Positivity should be specified");
        }
    }
}
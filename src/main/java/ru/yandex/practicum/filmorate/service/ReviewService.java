package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewStorage reviewStorage;

    public Review createReview(Review review) {
        validateReview(review);
        return reviewStorage.createReview(review);
    }

    public Review updateReview(Review review) {
        if (review.getReviewId() == null) {
            throw new IllegalArgumentException("Review ID cannot be null for update.");
        }
        validateReview(review);
        return reviewStorage.updateReview(review);
    }

    public void deleteReview(Long id) {
        reviewStorage.deleteReview(id);
    }

    public Review getReviewById(Long id) {
        return reviewStorage.getReviewById(id)
                .orElseThrow(() -> new NotFoundException("Review with id=" + id + " not found."));
    }

    public List<Review> getReviewsByFilmId(Long filmId, int count) {
        List<Review> reviews = filmId == null ?
                reviewStorage.getAllReviews() :
                reviewStorage.getReviewsByFilmId(filmId);

        return reviews.stream()
                .sorted(Comparator.comparingInt(Review::getUseful).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }

    public void addLike(Long reviewId, Long userId) {
        reviewStorage.addLike(reviewId, userId);
    }

    public void addDislike(Long reviewId, Long userId) {
        reviewStorage.addDislike(reviewId, userId);
    }

    public void deleteLike(Long reviewId, Long userId) {
        reviewStorage.deleteLike(reviewId, userId);
    }

    private void validateReview(Review review) {
        if (review.getContent() == null || review.getContent().isBlank()) {
            throw new IllegalArgumentException("Review content cannot be empty.");
        }
        if (review.getUserId() == null) {
            throw new IllegalArgumentException("User ID cannot be null.");
        }
        if (review.getFilmId() == null) {
            throw new IllegalArgumentException("Film ID cannot be null.");
        }
    }
}
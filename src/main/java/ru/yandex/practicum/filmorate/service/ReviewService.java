package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewStorage reviewStorage;
    private final UserStorage userStorage; // <-- Добавлена зависимость для валидации
    private final FilmStorage filmStorage; // <-- Добавлена зависимость для валидации

    public Review createReview(Review review) {
        validateReview(review);
        // Валидация существования пользователя и фильма ПЕРЕНЕСЕНА СЮДА
        if (!userStorage.getUserById(review.getUserId()).isPresent()) {
            throw new NotFoundException("User with id=" + review.getUserId() + " not found.");
        }
        if (!filmStorage.getFilmById(review.getFilmId()).isPresent()) {
            throw new NotFoundException("Film with id=" + review.getFilmId() + " not found.");
        }
        return reviewStorage.createReview(review);
    }

    public Review updateReview(Review review) {
        if (review.getReviewId() == 0) {
            throw new IllegalArgumentException("Review ID cannot be null or zero for update.");
        }
        validateReview(review);
        return reviewStorage.updateReview(review);
    }

    public void deleteReview(int id) {
        reviewStorage.deleteReview(id);
    }

    public Review getReviewById(int id) {
        return reviewStorage.getReviewById(id)
                .orElseThrow(() -> new NotFoundException("Review with id=" + id + " not found."));
    }

    public List<Review> getReviewsByFilmId(Integer filmId, int count) {
        List<Review> reviews = filmId == null ?
                reviewStorage.getAllReviews() :
                reviewStorage.getReviewsByFilmId(filmId);

        return reviews.stream()
                .sorted(Comparator.comparingInt(Review::getUseful).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }

    public void addLike(int reviewId, int userId) {
        reviewStorage.addLike(reviewId, userId);
    }

    public void addDislike(int reviewId, int userId) {
        reviewStorage.addDislike(reviewId, userId);
    }

    public void deleteLike(int reviewId, int userId) {
        reviewStorage.deleteLike(reviewId, userId);
    }

    private void validateReview(Review review) {
        if (review.getContent() == null || review.getContent().isBlank()) {
            throw new IllegalArgumentException("Review content cannot be empty.");
        }
        if (review.getUserId() == 0) {
            throw new IllegalArgumentException("User ID cannot be null or zero.");
        }
        if (review.getFilmId() == 0) {
            throw new IllegalArgumentException("Film ID cannot be null or zero.");
        }
    }
}
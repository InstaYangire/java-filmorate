package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.List;

// Controller for reviews
@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // Create review
    @PostMapping
    public Review create(@RequestBody Review review) {
        return reviewService.create(review);
    }

    // Update review
    @PutMapping
    public Review update(@RequestBody Review review) {
        return reviewService.update(review);
    }

    // Delete review
    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id) {
        reviewService.delete(id);
    }

    // Get review by ID
    @GetMapping("/{id}")
    public Review getById(@PathVariable int id) {
        return reviewService.getById(id);
    }

    // Get reviews for a film (or all if filmId not provided)
    @GetMapping
    public List<Review> getByFilm(
            @RequestParam(required = false) Integer filmId,
            @RequestParam(defaultValue = "10") int count) {
        return reviewService.getByFilm(filmId, count);
    }

    // Add like
    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable int id, @PathVariable int userId) {
        reviewService.addLike(id, userId);
    }

    // Add dislike
    @PutMapping("/{id}/dislike/{userId}")
    public void addDislike(@PathVariable int id, @PathVariable int userId) {
        reviewService.addDislike(id, userId);
    }

    // Remove like
    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable int id, @PathVariable int userId) {
        reviewService.removeLike(id, userId);
    }

    // Remove dislike
    @DeleteMapping("/{id}/dislike/{userId}")
    public void removeDislike(@PathVariable int id, @PathVariable int userId) {
        reviewService.removeDislike(id, userId);
    }
}
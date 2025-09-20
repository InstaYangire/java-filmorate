package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReviewServiceTest {

    @Mock
    private ReviewStorage reviewStorage;

    @InjectMocks
    private ReviewService reviewService;

    private Review sampleReview;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        sampleReview = new Review();
        sampleReview.setContent("Great film!");
        sampleReview.setIsPositive(true);
        sampleReview.setUserId(1L);
        sampleReview.setFilmId(1L);
        sampleReview.setUseful(0);
    }

    @Test
    void shouldCreateReview() {
        when(reviewStorage.createReview(any(Review.class))).thenReturn(sampleReview);

        Review result = reviewService.createReview(sampleReview);

        assertNotNull(result);
        verify(reviewStorage).createReview(any(Review.class));
    }

    @Test
    void shouldThrowOnEmptyContent() {
        sampleReview.setContent("");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> reviewService.createReview(sampleReview));
        assertEquals("Review content cannot be empty.", ex.getMessage());
    }

    @Test
    void shouldUpdateReview() {
        sampleReview.setReviewId(1L);
        when(reviewStorage.updateReview(any(Review.class))).thenReturn(sampleReview);

        Review result = reviewService.updateReview(sampleReview);

        assertNotNull(result);
        verify(reviewStorage).updateReview(any(Review.class));
    }

    @Test
    void shouldGetReviewById() {
        when(reviewStorage.getReviewById(1L)).thenReturn(Optional.of(sampleReview));

        Review result = reviewService.getReviewById(1L);

        assertEquals(sampleReview, result);
    }

    @Test
    void shouldThrowOnNotFound() {
        when(reviewStorage.getReviewById(999L)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> reviewService.getReviewById(999L));
        assertEquals("Review with id=999 not found.", ex.getMessage());
    }

    @Test
    void shouldSortReviewsByUseful() {
        Review r1 = new Review(); r1.setReviewId(1L); r1.setUseful(10);
        Review r2 = new Review(); r2.setReviewId(2L); r2.setUseful(5);
        Review r3 = new Review(); r3.setReviewId(3L); r3.setUseful(15);

        when(reviewStorage.getAllReviews()).thenReturn(List.of(r1, r2, r3));

        List<Review> result = reviewService.getReviewsByFilmId(null, 10);

        assertEquals(3, result.size());
        assertEquals(15, result.get(0).getUseful()); // Самый полезный первым
        assertEquals(10, result.get(1).getUseful());
        assertEquals(5, result.get(2).getUseful());
    }
}
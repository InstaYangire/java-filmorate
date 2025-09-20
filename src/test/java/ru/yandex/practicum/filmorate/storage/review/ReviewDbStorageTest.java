package ru.yandex.practicum.filmorate.storage.review;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@Import({ReviewDbStorage.class, UserDbStorage.class})
class ReviewDbStorageTest {

    @Autowired
    private ReviewStorage reviewStorage;

    @Autowired
    private UserDbStorage userDbStorage;

    private Review sampleReview;
    private Long testUserId;
    private final Long testFilmId = 1L; // Предполагаем, что фильм с ID=1 существует

    ReviewDbStorageTest(ReviewStorage reviewStorage) {
        this.reviewStorage = reviewStorage;
    }

    @BeforeEach
    void setUp() {
        // Создаем тестового пользователя
        ru.yandex.practicum.filmorate.model.User user = new ru.yandex.practicum.filmorate.model.User();
        user.setEmail("testuser@example.com");
        user.setLogin("tester");
        user.setName("Test User");
        user.setBirthday(java.time.LocalDate.of(1990, 1, 1));
        testUserId = (long) userDbStorage.addUser(user).getId();

        sampleReview = new Review();
        sampleReview.setContent("Great film!");
        sampleReview.setIsPositive(true);
        sampleReview.setUserId(testUserId);
        sampleReview.setFilmId(testFilmId);
    }

    @Test
    void shouldCreateAndGetReview() {
        Review created = reviewStorage.createReview(sampleReview);
        assertNotNull(created.getReviewId());
        assertEquals(sampleReview.getContent(), created.getContent());

        Review retrieved = reviewStorage.getReviewById(created.getReviewId()).orElseThrow();
        assertEquals(created.getReviewId(), retrieved.getReviewId());
        assertEquals(created.getContent(), retrieved.getContent());
    }

    @Test
    void shouldUpdateReview() {
        Review created = reviewStorage.createReview(sampleReview);
        created.setContent("Updated content");
        Review updated = reviewStorage.updateReview(created);

        assertEquals("Updated content", updated.getContent());
        assertEquals(created.getReviewId(), updated.getReviewId());
    }

    @Test
    void shouldDeleteReview() {
        Review created = reviewStorage.createReview(sampleReview);
        Long id = created.getReviewId();

        reviewStorage.deleteReview(id);

        assertThrows(NotFoundException.class, () -> reviewStorage.getReviewById(id).orElseThrow());
    }

    @Test
    void shouldAddAndDeleteLike() {
        Review created = reviewStorage.createReview(sampleReview);

        reviewStorage.addLike(created.getReviewId(), testUserId);
        Review afterLike = reviewStorage.getReviewById(created.getReviewId()).orElseThrow();
        assertEquals(1, afterLike.getUseful());

        reviewStorage.deleteLike(created.getReviewId(), testUserId);
        Review afterDelete = reviewStorage.getReviewById(created.getReviewId()).orElseThrow();
        assertEquals(0, afterDelete.getUseful());
    }

    @Test
    void shouldAddDislike() {
        Review created = reviewStorage.createReview(sampleReview);

        reviewStorage.addDislike(created.getReviewId(), testUserId);
        Review afterDislike = reviewStorage.getReviewById(created.getReviewId()).orElseThrow();
        assertEquals(-1, afterDislike.getUseful());
    }

    @Test
    void shouldGetReviewsByFilmId() {
        reviewStorage.createReview(sampleReview);
        List<Review> reviews = reviewStorage.getReviewsByFilmId(testFilmId);
        assertFalse(reviews.isEmpty());
    }
}
package ru.yandex.practicum.filmorate.storage.review;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.film.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.film.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@Import({
        ReviewDbStorage.class,   // Тестируемый компонент
        UserDbStorage.class,     // Зависимость ReviewDbStorage
        FilmDbStorage.class,     // Зависимость ReviewDbStorage
        GenreDbStorage.class,    // Зависимость FilmDbStorage
        MpaDbStorage.class       // Зависимость FilmDbStorage
})
class ReviewDbStorageTest {

    @Autowired
    private ReviewStorage reviewStorage;

    @Autowired
    private UserDbStorage userDbStorage;

    private Review sampleReview;
    private int testUserId;
    private final int testFilmId = 1; // Предполагаем, что фильм с ID=1 существует

    @BeforeEach
    void setUp() {
        // Создаем тестового пользователя
        ru.yandex.practicum.filmorate.model.User user = new ru.yandex.practicum.filmorate.model.User();
        user.setEmail("testuser@example.com");
        user.setLogin("testUser");
        user.setName("Test User");
        user.setBirthday(java.time.LocalDate.of(1990, 1, 1));
        testUserId = userDbStorage.addUser(user).getId();

        sampleReview = new Review();
        sampleReview.setContent("Great film!");
        sampleReview.setIsPositive(true);
        sampleReview.setUserId((long) testUserId);
        sampleReview.setFilmId((long) testFilmId);
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
        int id = Math.toIntExact(created.getReviewId());

        reviewStorage.deleteReview((long) id);

        assertThrows(NotFoundException.class, () -> reviewStorage.getReviewById((long) id).orElseThrow());
    }

    @Test
    void shouldAddAndDeleteLike() {
        Review created = reviewStorage.createReview(sampleReview);

        reviewStorage.addLike(created.getReviewId(), (long) testUserId);
        Review afterLike = reviewStorage.getReviewById(created.getReviewId()).orElseThrow();
        assertEquals(1, afterLike.getUseful());

        reviewStorage.deleteLike(created.getReviewId(), (long) testUserId);
        Review afterDelete = reviewStorage.getReviewById(created.getReviewId()).orElseThrow();
        assertEquals(0, afterDelete.getUseful());
    }

    @Test
    void shouldAddDislike() {
        Review created = reviewStorage.createReview(sampleReview);

        reviewStorage.addDislike(created.getReviewId(), (long) testUserId);
        Review afterDislike = reviewStorage.getReviewById(created.getReviewId()).orElseThrow();
        assertEquals(-1, afterDislike.getUseful());
    }

    @Test
    void shouldGetReviewsByFilmId() {
        reviewStorage.createReview(sampleReview);
        List<Review> reviews = reviewStorage.getReviewsByFilmId((long) testFilmId);
        assertFalse(reviews.isEmpty());
    }
}
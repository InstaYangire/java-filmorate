package ru.yandex.practicum.filmorate.storage.review;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.film.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.film.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@Import({ReviewDbStorage.class, UserDbStorage.class, FilmDbStorage.class, GenreDbStorage.class, MpaDbStorage.class})
class ReviewDbStorageTest {

    @Autowired
    private ReviewDbStorage reviewDbStorage;

    @Autowired
    private UserDbStorage userDbStorage;

    @Autowired
    private FilmDbStorage filmDbStorage;

    // --- Helpers ---

    private User createSampleUser() {
        User user = new User();
        user.setEmail("user@mail.com");
        user.setLogin("userLogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return userDbStorage.addUser(user);
    }

    private Film createSampleFilm() {
        Film film = new Film();
        film.setName("Inception");
        film.setDescription("Test description");
        film.setReleaseDate(LocalDate.of(2010, 7, 16));
        film.setDuration(148);
        film.setMpa(new MpaRating(1, null));
        return filmDbStorage.addFilm(film);
    }

    private Review createSampleReview(int userId, int filmId) {
        Review review = new Review();
        review.setContent("Great movie!");
        review.setIsPositive(true);
        review.setUserId(userId);
        review.setFilmId(filmId);
        review.setUseful(0);
        return reviewDbStorage.create(review);
    }

    // Test: review should be created
    @Test
    void shouldCreateReview() {
        User user = createSampleUser();
        Film film = createSampleFilm();

        Review saved = createSampleReview(user.getId(), film.getId());

        assertNotNull(saved.getReviewId());
        assertEquals("Great movie!", saved.getContent());
        assertEquals(user.getId(), saved.getUserId());
        assertEquals(film.getId(), saved.getFilmId());
    }

    // Test: review should be found by id
    @Test
    void shouldFindReviewById() {
        User user = createSampleUser();
        Film film = createSampleFilm();
        Review saved = createSampleReview(user.getId(), film.getId());

        Optional<Review> found = reviewDbStorage.findById(saved.getReviewId());

        assertTrue(found.isPresent());
        assertEquals(saved.getContent(), found.get().getContent());
    }

    // Test: update review
    @Test
    void shouldUpdateReview() {
        User user = createSampleUser();
        Film film = createSampleFilm();
        Review saved = createSampleReview(user.getId(), film.getId());

        saved.setContent("Not so great...");
        saved.setIsPositive(false);
        Review updated = reviewDbStorage.update(saved);

        assertEquals("Not so great...", updated.getContent());
        assertFalse(updated.getIsPositive());
    }

    // Test: delete review
    @Test
    void shouldDeleteReview() {
        User user = createSampleUser();
        Film film = createSampleFilm();
        Review saved = createSampleReview(user.getId(), film.getId());

        reviewDbStorage.delete(saved.getReviewId());

        Optional<Review> found = reviewDbStorage.findById(saved.getReviewId());
        assertTrue(found.isEmpty());
    }

    // Test: find reviews by film, sorted by useful
    @Test
    void shouldFindReviewsByFilmSortedByUseful() {
        User user = createSampleUser();
        Film film = createSampleFilm();
        Review review1 = createSampleReview(user.getId(), film.getId());
        Review review2 = createSampleReview(user.getId(), film.getId());

        reviewDbStorage.addLike(review2.getReviewId(), user.getId());

        List<Review> reviews = reviewDbStorage.findByFilmId(film.getId(), 10);

        assertEquals(2, reviews.size());
        assertEquals(review2.getReviewId(), reviews.get(0).getReviewId());
    }

    // Test: add like should increase useful
    @Test
    void shouldAddLikeToReview() {
        User user = createSampleUser();
        Film film = createSampleFilm();
        Review review = createSampleReview(user.getId(), film.getId());

        reviewDbStorage.addLike(review.getReviewId(), user.getId());

        Review updated = reviewDbStorage.findById(review.getReviewId()).get();
        assertEquals(1, updated.getUseful());
    }

    // Test: add dislike should decrease useful
    @Test
    void shouldAddDislikeToReview() {
        User user = createSampleUser();
        Film film = createSampleFilm();
        Review review = createSampleReview(user.getId(), film.getId());

        reviewDbStorage.addDislike(review.getReviewId(), user.getId());

        Review updated = reviewDbStorage.findById(review.getReviewId()).get();
        assertEquals(-1, updated.getUseful());
    }

    // Test: remove like should decrease useful
    @Test
    void shouldRemoveLikeFromReview() {
        User user = createSampleUser();
        Film film = createSampleFilm();
        Review review = createSampleReview(user.getId(), film.getId());

        reviewDbStorage.addLike(review.getReviewId(), user.getId());
        reviewDbStorage.removeLike(review.getReviewId(), user.getId());

        Review updated = reviewDbStorage.findById(review.getReviewId()).get();
        assertEquals(0, updated.getUseful());
    }

    // Test: remove dislike should increase useful
    @Test
    void shouldRemoveDislikeFromReview() {
        User user = createSampleUser();
        Film film = createSampleFilm();
        Review review = createSampleReview(user.getId(), film.getId());

        reviewDbStorage.addDislike(review.getReviewId(), user.getId());
        reviewDbStorage.removeDislike(review.getReviewId(), user.getId());

        Review updated = reviewDbStorage.findById(review.getReviewId()).get();
        assertEquals(0, updated.getUseful());
    }

    // Test: request for nonexistent review should throw
    @Test
    void shouldReturnEmptyWhenReviewNotFound() {
        Optional<Review> review = reviewDbStorage.findById(999);
        assertTrue(review.isEmpty());
    }
}
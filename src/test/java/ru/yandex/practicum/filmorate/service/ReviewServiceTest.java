package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ReviewServiceTest {

    private JdbcTemplate jdbcTemplate;
    private UserStorage userStorage;
    private FilmStorage filmStorage;
    private ReviewStorage reviewStorage;
    private ReviewService reviewService;

    private User makeValidUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setLogin("login_" + email);
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return userStorage.addUser(user);
    }

    private Film makeValidFilm(String name) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2010, 1, 1));
        film.setDuration(120);
        film.setMpa(new MpaRating(1, "G"));
        return filmStorage.addFilm(film);
    }

    private Review makeValidReview(int userId, int filmId) {
        Review review = new Review();
        review.setContent("Great movie!");
        review.setIsPositive(true);
        review.setUserId(userId);
        review.setFilmId(filmId);
        review.setUseful(0);
        return reviewService.create(review);
    }

    @BeforeEach
    void setUp() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        dataSource.setUsername("sa");
        dataSource.setPassword("");

        jdbcTemplate = new JdbcTemplate(dataSource);

        jdbcTemplate.execute("DROP ALL OBJECTS");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS mpa_ratings (" +
                "id INT PRIMARY KEY AUTO_INCREMENT, " +
                "name VARCHAR NOT NULL)");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS genres (" +
                "id INT PRIMARY KEY AUTO_INCREMENT, " +
                "name VARCHAR NOT NULL)");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS users (" +
                "id INT PRIMARY KEY AUTO_INCREMENT, " +
                "email VARCHAR NOT NULL, " +
                "login VARCHAR NOT NULL, " +
                "name VARCHAR, " +
                "birthday DATE)");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS films (" +
                "id INT PRIMARY KEY AUTO_INCREMENT, " +
                "name VARCHAR NOT NULL, " +
                "description VARCHAR, " +
                "release_date DATE, " +
                "duration INT, " +
                "mpa_id INT, " +
                "CONSTRAINT fk_mpa FOREIGN KEY (mpa_id) REFERENCES mpa_ratings(id))");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS reviews (" +
                "review_id INT PRIMARY KEY AUTO_INCREMENT, " +
                "content VARCHAR NOT NULL, " +
                "is_positive BOOLEAN NOT NULL, " +
                "user_id INT NOT NULL, " +
                "film_id INT NOT NULL, " +
                "useful INT DEFAULT 0, " +
                "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (film_id) REFERENCES films(id) ON DELETE CASCADE)");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS review_reactions (" +
                "review_id INT NOT NULL, " +
                "user_id INT NOT NULL, " +
                "is_like BOOLEAN NOT NULL, " +
                "PRIMARY KEY (review_id, user_id), " +
                "FOREIGN KEY (review_id) REFERENCES reviews(review_id) ON DELETE CASCADE, " +
                "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE)");
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS film_genres (" +
                "film_id INT NOT NULL, " +
                "genre_id INT NOT NULL, " +
                "PRIMARY KEY (film_id, genre_id), " +
                "FOREIGN KEY (film_id) REFERENCES films(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (genre_id) REFERENCES genres(id) ON DELETE CASCADE)");
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS film_likes (" +
                "film_id INT NOT NULL, " +
                "user_id INT NOT NULL, " +
                "PRIMARY KEY (film_id, user_id), " +
                "FOREIGN KEY (film_id) REFERENCES films(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE)");
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS directors (" +
                "id INT PRIMARY KEY AUTO_INCREMENT, " +
                "name VARCHAR NOT NULL)");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS film_directors (" +
                "film_id INT NOT NULL, " +
                "director_id INT NOT NULL, " +
                "PRIMARY KEY (film_id, director_id), " +
                "FOREIGN KEY (film_id) REFERENCES films(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (director_id) REFERENCES directors(id) ON DELETE CASCADE)");

        jdbcTemplate.execute("MERGE INTO mpa_ratings (id, name) KEY (id) VALUES (1, 'G')");
        jdbcTemplate.execute("MERGE INTO mpa_ratings (id, name) KEY (id) VALUES (2, 'PG')");
        jdbcTemplate.execute("MERGE INTO mpa_ratings (id, name) KEY (id) VALUES (3, 'PG-13')");
        jdbcTemplate.execute("MERGE INTO mpa_ratings (id, name) KEY (id) VALUES (4, 'R')");
        jdbcTemplate.execute("MERGE INTO mpa_ratings (id, name) KEY (id) VALUES (5, 'NC-17')");

        jdbcTemplate.execute("MERGE INTO genres (id, name) KEY (id) VALUES (1, 'Комедия')");
        jdbcTemplate.execute("MERGE INTO genres (id, name) KEY (id) VALUES (2, 'Драма')");
        jdbcTemplate.execute("MERGE INTO genres (id, name) KEY (id) VALUES (3, 'Мультфильм')");
        jdbcTemplate.execute("MERGE INTO genres (id, name) KEY (id) VALUES (4, 'Триллер')");
        jdbcTemplate.execute("MERGE INTO genres (id, name) KEY (id) VALUES (5, 'Документальный')");
        jdbcTemplate.execute("MERGE INTO genres (id, name) KEY (id) VALUES (6, 'Боевик')");

        userStorage = new UserDbStorage(jdbcTemplate);
        filmStorage = new FilmDbStorage(jdbcTemplate);
        reviewStorage = new ReviewDbStorage(jdbcTemplate);

        reviewService = new ReviewService(reviewStorage, userStorage, filmStorage);
    }


    // Should throw when review not found
    @Test
    void shouldThrowWhenReviewNotFound() {
        assertThrows(NotFoundException.class, () -> reviewService.getById(999));
    }

    // Should create review successfully
    @Test
    void shouldCreateReviewSuccessfully() {
        User user = makeValidUser("user1@mail.com");
        Film film = makeValidFilm("Film 1");
        Review review = new Review();
        review.setContent("Excellent film!");
        review.setIsPositive(true);
        review.setUserId(user.getId());
        review.setFilmId(film.getId());

        Review createdReview = reviewService.create(review);

        assertNotNull(createdReview.getReviewId());
        assertEquals("Excellent film!", createdReview.getContent());
        assertTrue(createdReview.getIsPositive());
        assertEquals(user.getId(), createdReview.getUserId());
        assertEquals(film.getId(), createdReview.getFilmId());
    }

    // Should update review successfully
    @Test
    void shouldUpdateReviewSuccessfully() {
        User user = makeValidUser("user2@mail.com");
        Film film = makeValidFilm("Film 2");
        Review review = makeValidReview(user.getId(), film.getId());

        review.setContent("Updated content");
        review.setIsPositive(false);
        Review updatedReview = reviewService.update(review);

        assertEquals(review.getReviewId(), updatedReview.getReviewId());
        assertEquals("Updated content", updatedReview.getContent());
        assertFalse(updatedReview.getIsPositive());
    }

    // Should delete review successfully
    @Test
    void shouldDeleteReviewSuccessfully() {
        User user = makeValidUser("user3@mail.com");
        Film film = makeValidFilm("Film 3");
        Review review = makeValidReview(user.getId(), film.getId());

        assertDoesNotThrow(() -> reviewService.delete(review.getReviewId()));
        assertThrows(NotFoundException.class, () -> reviewService.getById(review.getReviewId()));
    }

    // Should get reviews by film id successfully
    @Test
    void shouldGetReviewsByFilmIdSuccessfully() {
        User user1 = makeValidUser("user4@mail.com");
        User user2 = makeValidUser("user5@mail.com");
        Film film = makeValidFilm("Film 4");

        Review review1 = makeValidReview(user1.getId(), film.getId());
        Review review2 = makeValidReview(user2.getId(), film.getId());

        List<Review> reviews = reviewService.getByFilm(film.getId(), 10);

        assertEquals(2, reviews.size());
        assertTrue(reviews.stream().anyMatch(r -> r.getReviewId() == review1.getReviewId()));
        assertTrue(reviews.stream().anyMatch(r -> r.getReviewId() == review2.getReviewId()));
    }

    // Should throw when creating review with non-existing user
    @Test
    void shouldThrowWhenCreatingReviewWithInvaldiUSer() {
        Film film = makeValidFilm("Test Film");
        Review review = new Review();
        review.setContent("This should fail");
        review.setIsPositive(true);
        review.setUserId(999); // non-existing user
        review.setFilmId(film.getId());

        assertThrows(NotFoundException.class, () -> reviewService.create(review));
    }

    // Should maintain correct user-review relationship
    @Test
    void shouldMaintainCorrectUserReviewRelationship() {
        User user1 = makeValidUser("user1@mail.com");
        User user2 = makeValidUser("user2@mail.com");
        Film film = makeValidFilm("Test Film");

        Review review1 = makeValidReview(user1.getId(), film.getId());
        Review review2 = makeValidReview(user2.getId(), film.getId());

        Review foundReview1 = reviewService.getById(review1.getReviewId());
        Review foundReview2 = reviewService.getById(review2.getReviewId());

        assertEquals(user1.getId(), foundReview1.getUserId(), "Review 1 should belong to user1");
        assertEquals(user2.getId(), foundReview2.getUserId(), "Review 2 should belong to user2");
        assertNotEquals(foundReview1.getUserId(), foundReview2.getUserId(), "Reviews should have different authors");

        assertEquals(review1.getContent(), foundReview1.getContent());
        assertEquals(review1.getIsPositive(), foundReview1.getIsPositive());

        assertEquals(review2.getContent(), foundReview2.getContent());
        assertEquals(review2.getIsPositive(), foundReview2.getIsPositive());
    }

    // Should correctly delete review
    @Test
    void shouldCorrectlyDeleteReview() {
        User user = makeValidUser("user@mail.com");
        Film film = makeValidFilm("Test Film");
        Review review = makeValidReview(user.getId(), film.getId());

        assertDoesNotThrow(() -> reviewService.getById(review.getReviewId()));

        reviewService.delete(review.getReviewId());

        assertThrows(NotFoundException.class, () -> reviewService.getById(review.getReviewId()));
    }

    // Should throw when creating review with non-existing film
    @Test
    void shouldThrowWhenCreatingReviewWithInvalidFilm() {
        User user = makeValidUser("user10@mail.com");
        Review review = new Review();
        review.setContent("Bad case 2");
        review.setIsPositive(true);
        review.setUserId(user.getId());
        review.setFilmId(999);
        assertThrows(NotFoundException.class, () -> reviewService.create(review));

    }

    // Should throw when adding like for non-existing review
    @Test
    void shouldThrowWhenAddingLikeToInvalidReview() {
        User user = makeValidUser("user11@mail.com");
        assertThrows(NotFoundException.class, () -> reviewService.addLike(999, user.getId()));
    }

}
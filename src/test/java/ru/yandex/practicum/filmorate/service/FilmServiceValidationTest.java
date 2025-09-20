package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.storage.film.DirectorDbStorage;
import ru.yandex.practicum.filmorate.storage.film.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.film.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmServiceValidationTest {
    private FilmService filmService;

    @BeforeEach
    void setUp() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:file:./db/filmorate");
        dataSource.setUsername("sa");
        dataSource.setPassword("password");

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        MpaDbStorage mpaDbStorage = new MpaDbStorage(jdbcTemplate);
        GenreDbStorage genreDbStorage = new GenreDbStorage(jdbcTemplate);
        MpaService mpaService = new MpaService(mpaDbStorage);
        GenreService genreService = new GenreService(genreDbStorage);

        FilmStorage filmStorage = new InMemoryFilmStorage();
        UserStorage userStorage = new InMemoryUserStorage();

        DirectorStorage directorStorage = new DirectorDbStorage(jdbcTemplate); // Нужно создать этот класс
        DirectorService directorService = new DirectorService(directorStorage);

        filmService = new FilmService(
                filmStorage,
                userStorage,
                mpaService,
                genreService,
                directorService,
                jdbcTemplate
        );

    }

    // ____________Helpers___________

    // Creates a valid Film object with default values
    private Film makeValidFilm() {
        Film film = new Film();
        film.setName("Matrix");
        film.setDescription("Interesting sci-fi movie.");
        film.setReleaseDate(LocalDate.of(1999, 3, 31));
        film.setDuration(136);
        return film;
    }

    // Asserts that validation fails with the expected message
    private void assertValidationFails(Film film, String expectedMessage) {
        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmService.addFilm(film));
        assertEquals(expectedMessage, exception.getMessage());
    }

    // __________Tests___________

    // Test: Valid film should be added without throwing an exception
    @Test
    void shouldAddFilmWhenDataIsValid() {
        Film film = makeValidFilm();
        assertDoesNotThrow(() -> filmService.addFilm(film));
    }

    // Test: Should fail when the film name is blank
    @Test
    void shouldFailValidationWhenNameIsBlank() {
        Film film = makeValidFilm();
        film.setName("   ");
        assertValidationFails(film, "Movie title cannot be empty.");
    }

    // Test: Should allow description with exactly 200 characters
    @Test
    void shouldAddFilmWhenDescriptionIsExactly200Characters() {
        Film film = makeValidFilm();
        film.setDescription("y".repeat(200));
        assertDoesNotThrow(() -> filmService.addFilm(film));
    }

    // Test: Should fail when description exceeds 200 characters
    @Test
    void shouldFailValidationWhenDescriptionIsTooLong() {
        Film film = makeValidFilm();
        film.setDescription("n".repeat(201));
        assertValidationFails(film, "The description must not exceed 200 characters.");
    }

    // Test: Should fail when release date is before December 28, 1895
    @Test
    void shouldFailValidationWhenReleaseDateIsBeforeCinemaBirthday() {
        Film film = makeValidFilm();
        film.setReleaseDate(LocalDate.of(1666, 6, 6));
        assertValidationFails(film, "The release date cannot be earlier than December 28, 1895.");
    }

    // Test: Should fail when duration is zero
    @Test
    void shouldFailValidationWhenDurationIsZero() {
        Film film = makeValidFilm();
        film.setDuration(0);
        assertValidationFails(film, "The duration of the movie must be positive and not equal to zero.");
    }

    // Test: Should fail when duration is negative
    @Test
    void shouldFailValidationWhenDurationIsNegative() {
        Film film = makeValidFilm();
        film.setDuration(-60);
        assertValidationFails(film, "The duration of the movie must be positive and not equal to zero.");
    }

    // Test: Should update existing film
    @Test
    void shouldUpdateExistingFilm() {
        Film film = makeValidFilm();
        Film added = filmService.addFilm(film);
        added.setName("Updated Name");
        Film updated = filmService.updateFilm(added);
        assertEquals("Updated Name", updated.getName());
    }

    // Test: Should add film when updating non-existent film
    @Test
    void shouldAddFilmWhenUpdatingNonexistentFilm() {
        Film film = makeValidFilm();
        film.setId(999);
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> filmService.updateFilm(film));
        assertEquals("Movie with id=999 not found.", exception.getMessage());
    }

    // Test: Should return all films
    @Test
    void shouldReturnAllFilms() {
        Film f1 = makeValidFilm();
        Film f2 = makeValidFilm();
        f2.setName("Interstellar");

        filmService.addFilm(f1);
        filmService.addFilm(f2);
        assertEquals(2, filmService.getAllFilms().size());
    }
}
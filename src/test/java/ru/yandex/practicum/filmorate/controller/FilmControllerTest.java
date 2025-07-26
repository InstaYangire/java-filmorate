package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTest {
    private FilmController controller;

    @BeforeEach
    void setUp() {
        controller = new FilmController();
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
                () -> controller.addFilm(film));
        assertEquals(expectedMessage, exception.getMessage());
    }

    // __________Tests___________

    // Test: Valid film should be added without throwing an exception
    @Test
    void shouldAddFilmWhenDataIsValid() {
        Film film = makeValidFilm();
        assertDoesNotThrow(() -> controller.addFilm(film));
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
        assertDoesNotThrow(() -> controller.addFilm(film));
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
        Film added = controller.addFilm(film);
        added.setName("Updated Name");
        Film updated = controller.updateFilm(added);
        assertEquals("Updated Name", updated.getName());
    }

    // Test: Should add film when updating non-existent film
    @Test
    void shouldAddFilmWhenUpdatingNonexistentFilm() {
        Film film = makeValidFilm();
        film.setId(999);
        ValidationException exception = assertThrows(ValidationException.class,
                () -> controller.updateFilm(film));
        assertEquals("Movie with id=999 not found.", exception.getMessage());
    }

    // Test: Should return all films
    @Test
    void shouldReturnAllFilms() {
        Film f1 = makeValidFilm();
        Film f2 = makeValidFilm();
        f2.setName("Interstellar");

        controller.addFilm(f1);
        controller.addFilm(f2);
        assertEquals(2, controller.getAllFilms().size());
    }
}
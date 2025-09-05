package ru.yandex.practicum.filmorate.storage.film;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmDbStorage.class, GenreDbStorage.class, MpaDbStorage.class, UserDbStorage.class})
class FilmDbStorageTest {

    @Autowired
    private FilmDbStorage filmDbStorage;

    @Autowired
    private UserDbStorage userDbStorage;

    // ----------- Helpers -----------

    // Create a sample film with minimal valid data
    private Film createSampleFilm() {
        Film film = new Film();
        film.setName("Inception");
        film.setDescription("A mind-bending thriller");
        film.setReleaseDate(LocalDate.of(2010, 7, 16));
        film.setDuration(148);
        film.setMpa(new MpaRating(1, null)); // Only ID is needed
        film.setGenres(Set.of(new Genre(1, null))); // Only IDs are needed
        return film;
    }

    // Create and save a sample user in DB
    private User createSampleUser() {
        User user = new User();
        user.setEmail("user@mail.com");
        user.setLogin("userLogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        return userDbStorage.addUser(user);
    }

    // ----------- Tests -----------

    // Test: Film should be added successfully
    @Test
    void shouldAddFilmSuccessfully() {
        Film film = createSampleFilm();
        Film savedFilm = filmDbStorage.addFilm(film);

        assertEquals(film.getName(), savedFilm.getName());
        assertEquals(film.getDescription(), savedFilm.getDescription());
        assertEquals(film.getReleaseDate(), savedFilm.getReleaseDate());
        assertEquals(film.getDuration(), savedFilm.getDuration());
        assertEquals(film.getMpa().getId(), savedFilm.getMpa().getId());
    }

    // Test: Film should be found by its ID
    @Test
    void shouldFindFilmById() {
        Film film = createSampleFilm();
        Film savedFilm = filmDbStorage.addFilm(film);
        Optional<Film> loadedFilm = filmDbStorage.getFilmById(savedFilm.getId());

        assertTrue(loadedFilm.isPresent());
        assertEquals(savedFilm.getId(), loadedFilm.get().getId());
    }

    // Test: All films should be returned
    @Test
    void shouldReturnAllFilms() {
        Film film = createSampleFilm();
        filmDbStorage.addFilm(film);

        List<Film> films = filmDbStorage.getAllFilms();
        assertFalse(films.isEmpty());
    }

    // Test: Film should be updated successfully
    @Test
    void shouldUpdateFilmSuccessfully() {
        Film film = createSampleFilm();
        Film savedFilm = filmDbStorage.addFilm(film);

        savedFilm.setName("Inception Updated");
        savedFilm.setDescription("Updated description");
        savedFilm.setDuration(150);

        Film updatedFilm = filmDbStorage.updateFilm(savedFilm);

        assertEquals("Inception Updated", updatedFilm.getName());
        assertEquals("Updated description", updatedFilm.getDescription());
        assertEquals(150, updatedFilm.getDuration());
    }

    // Test: Like should be added successfully
    @Test
    void shouldAddLikeSuccessfully() {
        Film film = createSampleFilm();
        Film savedFilm = filmDbStorage.addFilm(film);
        User savedUser = createSampleUser();

        filmDbStorage.addLike(savedFilm.getId(), savedUser.getId());

        Set<Integer> likes = filmDbStorage.getFilmById(savedFilm.getId()).get().getLikes();
        assertTrue(likes.contains(savedUser.getId()));
    }

    // Test: Adding a duplicate like should throw ValidationException
    @Test
    void shouldThrowWhenAddingDuplicateLike() {
        Film film = createSampleFilm();
        Film savedFilm = filmDbStorage.addFilm(film);
        User savedUser = createSampleUser();

        // First like should pass
        filmDbStorage.addLike(savedFilm.getId(), savedUser.getId());

        // Second like should fail
        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> filmDbStorage.addLike(savedFilm.getId(), savedUser.getId())
        );

        assertEquals(
                "User with id=" + savedUser.getId() + " has already liked film with id=" + savedFilm.getId(),
                ex.getMessage()
        );
    }

    // Test: Like should be removed successfully
    @Test
    void shouldRemoveLikeSuccessfully() {
        Film film = createSampleFilm();
        Film savedFilm = filmDbStorage.addFilm(film);
        User savedUser = createSampleUser();

        filmDbStorage.addLike(savedFilm.getId(), savedUser.getId());
        filmDbStorage.removeLike(savedFilm.getId(), savedUser.getId());

        Set<Integer> likes = filmDbStorage.getFilmById(savedFilm.getId()).get().getLikes();
        assertFalse(likes.contains(savedUser.getId()));
    }

    // Test: Removing a like that doesn't exist should throw NotFoundException
    @Test
    void shouldThrowWhenRemovingNonexistentLike() {
        Film film = createSampleFilm();
        Film savedFilm = filmDbStorage.addFilm(film);
        User savedUser = createSampleUser();

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> filmDbStorage.removeLike(savedFilm.getId(), savedUser.getId())
        );

        assertEquals(
                "Like not found: filmId=" + savedFilm.getId() + ", userId=" + savedUser.getId(),
                ex.getMessage()
        );
    }
}
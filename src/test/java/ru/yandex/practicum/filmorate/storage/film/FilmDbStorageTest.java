package ru.yandex.practicum.filmorate.storage.film;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmDbStorage.class, GenreDbStorage.class, MpaDbStorage.class, UserDbStorage.class, DirectorDbStorage.class})
class FilmDbStorageTest {

    @Autowired
    private FilmDbStorage filmDbStorage;

    @Autowired
    private UserDbStorage userDbStorage;

    @Autowired
    private DirectorDbStorage directorDbStorage;

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
        film.setDirectors(new ArrayList<>());
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

    private Director createSampleDirector() {
        Director director = new Director();
        director.setName("Test Director");
        return directorDbStorage.create(director);
    }

    private Director createSampleDirector(String name) {
        Director director = new Director();
        director.setName(name);
        return directorDbStorage.create(director);
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
        assertNotNull(savedFilm.getDirectors());
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

    // Test: Common films should be returned
    @Test
    void shouldReturnCommonFilms() {
        User user = createSampleUser();
        User friend = createSampleUser();
        Film film = createSampleFilm();
        Film savedFilm = filmDbStorage.addFilm(film);
        filmDbStorage.addLike(savedFilm.getId(), user.getId());
        filmDbStorage.addLike(savedFilm.getId(), friend.getId());

        List<Film> films = filmDbStorage.getCommonFilms(user.getId(), friend.getId());
        Set<Integer> likes = filmDbStorage.getFilmById(savedFilm.getId()).get().getLikes();
        assertFalse(films.isEmpty());
        assertTrue(likes.contains(user.getId()));
        assertTrue(likes.contains(friend.getId()));
    }

    // Test: Few common films should be returned
    @Test
    void shouldReturnFewCommonFilms() {
        User user = createSampleUser();
        User friend = createSampleUser();
        Film film1 = createSampleFilm();
        Film film2 = createSampleFilm();
        Film film3 = createSampleFilm();

        Film savedFilm1 = filmDbStorage.addFilm(film1);
        Film savedFilm2 = filmDbStorage.addFilm(film2);
        Film savedFilm3 = filmDbStorage.addFilm(film3);

        // Both users liked all three films
        filmDbStorage.addLike(savedFilm1.getId(), user.getId());
        filmDbStorage.addLike(savedFilm1.getId(), friend.getId());
        filmDbStorage.addLike(savedFilm2.getId(), user.getId());
        filmDbStorage.addLike(savedFilm2.getId(), friend.getId());
        filmDbStorage.addLike(savedFilm3.getId(), user.getId());
        filmDbStorage.addLike(savedFilm3.getId(), friend.getId());

        List<Film> commonFilms = filmDbStorage.getCommonFilms(user.getId(), friend.getId());

        // Checking that 3 films returned
        assertEquals(3, commonFilms.size());

        Set<Integer> returnedFilmIds = commonFilms.stream()
                .map(Film::getId)
                .collect(Collectors.toSet());

        // Checking that all films returned
        assertTrue(returnedFilmIds.contains(savedFilm1.getId()));
        assertTrue(returnedFilmIds.contains(savedFilm2.getId()));
        assertTrue(returnedFilmIds.contains(savedFilm3.getId()));
    }

    // Test: Only common films should be returned
    @Test
    void shouldReturnOnlyCommonFilms() {
        User user = createSampleUser();
        User friend = createSampleUser();

        // Common films
        Film commonFilm1 = createSampleFilm();
        Film commonFilm2 = createSampleFilm();

        // Film that likes only user/friend
        Film userOnlyFilm = createSampleFilm();
        Film friendOnlyFilm = createSampleFilm();

        Film savedCommon1 = filmDbStorage.addFilm(commonFilm1);
        Film savedCommon2 = filmDbStorage.addFilm(commonFilm2);
        Film savedUserOnly = filmDbStorage.addFilm(userOnlyFilm);
        Film savedFriendOnly = filmDbStorage.addFilm(friendOnlyFilm);
        filmDbStorage.addLike(savedCommon1.getId(), user.getId());
        filmDbStorage.addLike(savedCommon2.getId(), user.getId());
        filmDbStorage.addLike(savedUserOnly.getId(), user.getId());
        filmDbStorage.addLike(savedCommon1.getId(), friend.getId());
        filmDbStorage.addLike(savedCommon2.getId(), friend.getId());
        filmDbStorage.addLike(savedFriendOnly.getId(), friend.getId());

        List<Film> commonFilms = filmDbStorage.getCommonFilms(user.getId(), friend.getId());

        // Checking that there should be only 2 common films
        assertEquals(2, commonFilms.size());

        Set<Integer> commonFilmIds = commonFilms.stream()
                .map(Film::getId)
                .collect(Collectors.toSet());

        // Checking that only common films returned
        assertTrue(commonFilmIds.contains(savedCommon1.getId()));
        assertTrue(commonFilmIds.contains(savedCommon2.getId()));
        assertFalse(commonFilmIds.contains(savedUserOnly.getId()));
        assertFalse(commonFilmIds.contains(savedFriendOnly.getId()));
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

    @Test
    void shouldHandleFilmWithDirectors() {
        Director director = createSampleDirector();
        Film film = createSampleFilm();
        film.setDirectors(new ArrayList<>(List.of(director)));

        Film savedFilm = filmDbStorage.addFilm(film);

        assertNotNull(savedFilm.getDirectors());
        assertEquals(1, savedFilm.getDirectors().size());
        assertEquals(director.getId(), savedFilm.getDirectors().get(0).getId());
    }

    @Test
    void shouldHandleFilmWithMultipleDirectors() {
        Director director1 = createSampleDirector("Christopher Nolan");
        Director director2 = createSampleDirector("Hans Zimmer");
        Director director3 = createSampleDirector("Emma Thomas");

        Film film = createSampleFilm();
        film.setName("Interstellar");
        film.setDirectors(new ArrayList<>(List.of(director1, director2, director3)));

        Film savedFilm = filmDbStorage.addFilm(film);

        assertNotNull(savedFilm.getDirectors());
        assertEquals(3, savedFilm.getDirectors().size());
        assertTrue(savedFilm.getDirectors().stream()
                .anyMatch(d -> d.getName().equals("Christopher Nolan")));
        assertTrue(savedFilm.getDirectors().stream()
                .anyMatch(d -> d.getName().equals("Hans Zimmer")));
        assertTrue(savedFilm.getDirectors().stream()
                .anyMatch(d -> d.getName().equals("Emma Thomas")));

        Optional<Film> retrievedFilmOpt = filmDbStorage.getFilmById(savedFilm.getId());
        assertTrue(retrievedFilmOpt.isPresent());

        Film retrievedFilm = retrievedFilmOpt.get();
        assertEquals(3, retrievedFilm.getDirectors().size());
    }

    @Test
    void shouldUpdateFilmWithEmptyDirectorsList() {
        Director director = createSampleDirector("Test Director");
        Film film = createSampleFilm();
        film.setDirectors(new ArrayList<>(List.of(director)));
        Film savedFilm = filmDbStorage.addFilm(film);

        assertEquals(1, savedFilm.getDirectors().size());

        savedFilm.setDirectors(new ArrayList<>());
        Film updatedFilm = filmDbStorage.updateFilm(savedFilm);

        assertNotNull(updatedFilm.getDirectors());
        assertTrue(updatedFilm.getDirectors().isEmpty());

        Optional<Film> retrievedFilmOpt = filmDbStorage.getFilmById(updatedFilm.getId());
        assertTrue(retrievedFilmOpt.isPresent());

        Film retrievedFilm = retrievedFilmOpt.get();
        assertTrue(retrievedFilm.getDirectors().isEmpty());
    }

    @Test
    void shouldUpdateFilmWithNewDirectors() {
        Director director1 = createSampleDirector("Director 1");
        Film film = createSampleFilm();
        film.setDirectors(new ArrayList<>(List.of(director1)));
        Film savedFilm = filmDbStorage.addFilm(film);

        Director director2 = createSampleDirector("New Director 1");
        Director director3 = createSampleDirector("New Director 2");

        savedFilm.setDirectors(new ArrayList<>(List.of(director2, director3)));
        Film updatedFilm = filmDbStorage.updateFilm(savedFilm);

        assertEquals(2, updatedFilm.getDirectors().size());
        assertTrue(updatedFilm.getDirectors().stream()
                .anyMatch(d -> d.getName().equals("New Director 1")));
        assertTrue(updatedFilm.getDirectors().stream()
                .anyMatch(d -> d.getName().equals("New Director 2")));
        assertFalse(updatedFilm.getDirectors().stream()
                .anyMatch(d -> d.getName().equals("Director 1")));
    }
}
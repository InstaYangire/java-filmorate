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

    // Test: Should return popular films filtered by genre
    @Test
    void shouldReturnPopularFilmsFilteredByGenre() {
        User user = createSampleUser();

        Film film1 = createSampleFilm(); // жанр 1
        Film film2 = createSampleFilm(); // жанр 1
        Film film3 = createSampleFilm();
        film3.setGenres(Set.of(new Genre(2, null))); // жанр 2

        Film saved1 = filmDbStorage.addFilm(film1);
        Film saved2 = filmDbStorage.addFilm(film2);
        Film saved3 = filmDbStorage.addFilm(film3);

        filmDbStorage.addLike(saved1.getId(), user.getId());
        filmDbStorage.addLike(saved2.getId(), user.getId());
        filmDbStorage.addLike(saved3.getId(), user.getId());

        List<Film> results = filmDbStorage.getPopularFilms(10, 1, null);

        assertFalse(results.isEmpty());
        assertTrue(results.stream().allMatch(f ->
                f.getGenres().stream().anyMatch(g -> g.getId() == 1)));
    }

    // Test: Should return popular films filtered by year
    @Test
    void shouldReturnPopularFilmsFilteredByYear() {
        User user = createSampleUser();

        Film film1 = createSampleFilm();
        film1.setReleaseDate(LocalDate.of(2020, 1, 1));

        Film film2 = createSampleFilm();
        film2.setReleaseDate(LocalDate.of(2020, 5, 5));

        Film film3 = createSampleFilm();
        film3.setReleaseDate(LocalDate.of(2021, 1, 1));

        Film saved1 = filmDbStorage.addFilm(film1);
        Film saved2 = filmDbStorage.addFilm(film2);
        Film saved3 = filmDbStorage.addFilm(film3);

        filmDbStorage.addLike(saved1.getId(), user.getId());
        filmDbStorage.addLike(saved2.getId(), user.getId());
        filmDbStorage.addLike(saved3.getId(), user.getId());

        List<Film> results = filmDbStorage.getPopularFilms(10, null, 2020);

        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(f -> f.getReleaseDate().getYear() == 2020));
    }

    // Test: Should return popular films filtered by genre and year
    @Test
    void shouldReturnPopularFilmsFilteredByGenreAndYear() {
        User user = createSampleUser();

        Film film1 = createSampleFilm();
        film1.setReleaseDate(LocalDate.of(2019, 3, 3));
        film1.setGenres(Set.of(new Genre(1, null)));

        Film film2 = createSampleFilm();
        film2.setReleaseDate(LocalDate.of(2019, 6, 6));
        film2.setGenres(Set.of(new Genre(1, null)));

        Film film3 = createSampleFilm();
        film3.setReleaseDate(LocalDate.of(2020, 1, 1));
        film3.setGenres(Set.of(new Genre(2, null)));

        Film saved1 = filmDbStorage.addFilm(film1);
        Film saved2 = filmDbStorage.addFilm(film2);
        Film saved3 = filmDbStorage.addFilm(film3);

        filmDbStorage.addLike(saved1.getId(), user.getId());
        filmDbStorage.addLike(saved2.getId(), user.getId());
        filmDbStorage.addLike(saved3.getId(), user.getId());

        List<Film> results = filmDbStorage.getPopularFilms(10, 1, 2019);

        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(f ->
                f.getGenres().stream().anyMatch(g -> g.getId() == 1)
                        && f.getReleaseDate().getYear() == 2019));
    }

    // ----------- Search Tests -----------

    // Test: Film should be found by title
    @Test
    void shouldFindFilmByTitle() {
        Film film = createSampleFilm();
        film.setName("Love Actually");
        filmDbStorage.addFilm(film);

        List<Film> results = filmDbStorage.searchFilms("love", List.of("title"));
        assertEquals(1, results.size());
        assertEquals("Love Actually", results.get(0).getName());
    }

    // Test: Film should be found by description
    @Test
    void shouldFindFilmByDescription() {
        Film film = createSampleFilm();
        film.setDescription("A romantic love story");
        filmDbStorage.addFilm(film);

        List<Film> results = filmDbStorage.searchFilms("romantic", List.of("description"));
        assertEquals(1, results.size());
        assertTrue(results.get(0).getDescription().contains("romantic"));
    }

    // Test: Film should be found by director
    @Test
    void shouldFindFilmByDirector() {
        Director director = createSampleDirector("Christopher Nolan");
        Film film = createSampleFilm();
        film.setDirectors(new ArrayList<>(List.of(director)));
        filmDbStorage.addFilm(film);

        List<Film> results = filmDbStorage.searchFilms("nolan", List.of("director"));
        assertEquals(1, results.size());
        assertTrue(results.get(0).getDirectors().stream()
                .anyMatch(d -> d.getName().equals("Christopher Nolan")));
    }

    // Test: Film should be found by title and director
    @Test
    void shouldFindFilmByTitleAndDirector() {
        Director director = createSampleDirector("Steven Spielberg");
        Film film = createSampleFilm();
        film.setName("Jurassic Park");
        film.setDirectors(new ArrayList<>(List.of(director)));
        filmDbStorage.addFilm(film);

        // Search by both title and director
        List<Film> resultsByTitle = filmDbStorage.searchFilms("Jurassic", List.of("title", "director"));
        List<Film> resultsByDirector = filmDbStorage.searchFilms("Spielberg", List.of("title", "director"));

        assertEquals(1, resultsByTitle.size());
        assertEquals(1, resultsByDirector.size());
    }

    // Test: No films should be found if no matches
    @Test
    void shouldReturnEmptyWhenNoMatch() {
        Film film = createSampleFilm();
        film.setName("Inception");
        filmDbStorage.addFilm(film);

        List<Film> results = filmDbStorage.searchFilms("Matrix", List.of("title"));
        assertTrue(results.isEmpty());
    }

    // Test: Search should be case-insensitive
    @Test
    void shouldHandleCaseInsensitiveSearch() {
        Film film = createSampleFilm();
        film.setName("Avatar");
        filmDbStorage.addFilm(film);

        List<Film> results = filmDbStorage.searchFilms("AVATAR", List.of("title"));
        assertEquals(1, results.size());
    }

    // Test: Search with blank query should return empty list
    @Test
    void shouldReturnEmptyWhenQueryIsBlank() {
        Film film = createSampleFilm();
        film.setName("Titanic");
        filmDbStorage.addFilm(film);

        List<Film> results = filmDbStorage.searchFilms("   ", List.of("title"));
        assertTrue(results.isEmpty());
    }

    // Test: Invalid 'by' parameter should throw ValidationException
    @Test
    void shouldThrowWhenByParameterIsInvalid() {
        Film film = createSampleFilm();
        filmDbStorage.addFilm(film);

        assertThrows(ValidationException.class, () ->
                filmDbStorage.searchFilms("test", List.of("wrong"))
        );
    }

    // Test: Film should not be duplicated when matches multiple fields
    @Test
    void shouldNotDuplicateFilmWhenMatchesMultipleFields() {
        Film film = createSampleFilm();
        film.setName("Unique Movie");
        film.setDescription("This Unique Movie is great");
        filmDbStorage.addFilm(film);

        List<Film> results = filmDbStorage.searchFilms("unique", List.of("title", "description"));
        assertEquals(1, results.size());
        assertEquals("Unique Movie", results.get(0).getName());
    }

    // Test: Film should be found by description and director
    @Test
    void shouldFindFilmByDescriptionAndDirector() {
        Director director = createSampleDirector("James Cameron");
        Film film = createSampleFilm();
        film.setDescription("Epic sci-fi adventure");
        film.setDirectors(new ArrayList<>(List.of(director)));
        filmDbStorage.addFilm(film);

        List<Film> resultsByDescription = filmDbStorage.searchFilms("sci-fi", List.of("description", "director"));
        List<Film> resultsByDirector = filmDbStorage.searchFilms("Cameron", List.of("description", "director"));

        assertEquals(1, resultsByDescription.size());
        assertEquals(1, resultsByDirector.size());
    }

    // ----------- Recommendations Tests -----------
    // Test: Should return empty list (0 recommendations) when user has no likes
    @Test
    void shouldReturnEmptyListWhenUserHasNoLikes() {
        User userOne = createSampleUser();
        User userTwo = createSampleUser();
        Film film = createSampleFilm();
        Film savedFilm = filmDbStorage.addFilm(film);

        filmDbStorage.addLike(savedFilm.getId(), userTwo.getId());

        Set<Integer> likes = filmDbStorage.getFilmById(savedFilm.getId()).get().getLikes();
        assertFalse(likes.contains(userOne.getId()));
        assertTrue(likes.contains(userTwo.getId()));

        List<Film> recommendations = filmDbStorage.getRecommendations(userOne.getId());
        assertTrue(recommendations.isEmpty());
    }

    // Should return empty list when user has likes but no similar users
    @Test
    void shouldReturnEmptyListWhenUserHasLikesButNoSimilarUsers() {
        User userOne = createSampleUser();
        User userTwo = createSampleUser();
        Film filmOne = createSampleFilm();
        Film filmTwo = createSampleFilm();
        Film savedFilmOne = filmDbStorage.addFilm(filmOne);
        Film savedFilmTwo = filmDbStorage.addFilm(filmTwo);

        filmDbStorage.addLike(savedFilmOne.getId(), userOne.getId());
        filmDbStorage.addLike(savedFilmTwo.getId(), userTwo.getId());

        Set<Integer> likesOne = filmDbStorage.getFilmById(savedFilmOne.getId()).get().getLikes();
        Set<Integer> likesTwo = filmDbStorage.getFilmById(savedFilmTwo.getId()).get().getLikes();
        assertTrue(likesOne.contains(userOne.getId()));
        assertFalse(likesOne.contains(userTwo.getId()));
        assertFalse(likesTwo.contains(userOne.getId()));
        assertTrue(likesTwo.contains(userTwo.getId()));

        List<Film> recommendations = filmDbStorage.getRecommendations(userOne.getId());
        assertTrue(recommendations.isEmpty());
    }

    // Should return recommendations when similar users exist
    @Test
    void shouldReturnRecommendationsWhenSimilarUsersExist() {
        User userOne = createSampleUser();
        User userTwo = createSampleUser();
        Film filmOne = createSampleFilm();
        Film filmTwo = createSampleFilm();
        filmTwo.setName("Recommended Film");
        Film savedFilmOne = filmDbStorage.addFilm(filmOne);
        Film savedFilmTwo = filmDbStorage.addFilm(filmTwo);

        filmDbStorage.addLike(savedFilmOne.getId(), userOne.getId());
        filmDbStorage.addLike(savedFilmOne.getId(), userTwo.getId());
        filmDbStorage.addLike(savedFilmTwo.getId(), userTwo.getId());

        List<Film> recommendations = filmDbStorage.getRecommendations(userOne.getId());
        assertFalse(recommendations.isEmpty());
        assertEquals(1, recommendations.size());
        assertEquals("Recommended Film", recommendations.get(0).getName());
    }

    // Should not return already liked films
    @Test
    void shouldNotReturnAlreadyLikedFilms() {
        User userOne = createSampleUser();
        User userTwo = createSampleUser();
        Film filmOne = createSampleFilm();
        Film filmTwo = createSampleFilm();
        Film savedFilmOne = filmDbStorage.addFilm(filmOne);
        Film savedFilmTwo = filmDbStorage.addFilm(filmTwo);

        filmDbStorage.addLike(savedFilmOne.getId(), userOne.getId());
        filmDbStorage.addLike(savedFilmOne.getId(), userTwo.getId());
        filmDbStorage.addLike(savedFilmTwo.getId(), userOne.getId());
        filmDbStorage.addLike(savedFilmTwo.getId(), userTwo.getId());

        List<Film> recommendationsOne = filmDbStorage.getRecommendations(userOne.getId());
        List<Film> recommendationsTwo = filmDbStorage.getRecommendations(userTwo.getId());
        assertTrue(recommendationsOne.isEmpty());
        assertTrue(recommendationsTwo.isEmpty());
    }

    // Should throw exception when deleting film that doesn't exist
    @Test
    void shouldThrowNotFoundExceptionWhenDeletingNonExistentFilm() {
        int nonExistentFilmId = 9999;

        assertFalse(filmDbStorage.getFilmById(nonExistentFilmId).isPresent());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> filmDbStorage.deleteFilm(nonExistentFilmId)
        );

        assertEquals("Film with id=" + nonExistentFilmId + " not found.", exception.getMessage());
    }

    // Should maintain database integrity after film deletion
    @Test
    void shouldMaintainDatabaseIntegrityAfterFilmDeletion() {
        Film film1 = createSampleFilm();
        film1.setName("First Film");
        Film film2 = createSampleFilm();
        film2.setName("Second Film");

        Film savedFilm1 = filmDbStorage.addFilm(film1);
        Film savedFilm2 = filmDbStorage.addFilm(film2);

        User user = createSampleUser();
        filmDbStorage.addLike(savedFilm1.getId(), user.getId());
        filmDbStorage.addLike(savedFilm2.getId(), user.getId());

        List<Film> allFilmsBefore = filmDbStorage.getAllFilms();
        assertEquals(2, allFilmsBefore.size());

        Set<Integer> userLikesBefore = filmDbStorage.getFilmById(savedFilm1.getId()).get().getLikes();
        assertTrue(userLikesBefore.contains(user.getId()));

        filmDbStorage.deleteFilm(savedFilm1.getId());

        assertTrue(filmDbStorage.getFilmById(savedFilm2.getId()).isPresent());
        Film remainingFilm = filmDbStorage.getFilmById(savedFilm2.getId()).get();
        assertEquals("Second Film", remainingFilm.getName());

        Set<Integer> remainingFilmLikes = remainingFilm.getLikes();
        assertTrue(remainingFilmLikes.contains(user.getId()));
        assertEquals(1, remainingFilmLikes.size());

        assertTrue(userDbStorage.getUserById(user.getId()).isPresent());

        List<Film> allFilmsAfter = filmDbStorage.getAllFilms();
        assertEquals(1, allFilmsAfter.size());
        assertEquals("Second Film", allFilmsAfter.get(0).getName());
    }

    //Should handle deletion of film with genres correctly
    @Test
    void shouldHandleDeletionOfFilmWithGenres() {
        Film film = createSampleFilm();
        film.setGenres(Set.of(new Genre(1, "Комедия"), new Genre(2, "Драма")));

        Film savedFilm = filmDbStorage.addFilm(film);
        int filmId = savedFilm.getId();

        Optional<Film> filmBeforeDelete = filmDbStorage.getFilmById(filmId);
        assertTrue(filmBeforeDelete.isPresent());
        assertEquals(2, filmBeforeDelete.get().getGenres().size());

        filmDbStorage.deleteFilm(filmId);

        assertFalse(filmDbStorage.getFilmById(filmId).isPresent());

        Film newFilm = createSampleFilm();
        newFilm.setName("New Film with Same Genres");
        newFilm.setGenres(Set.of(new Genre(1, "Комедия"), new Genre(2, "Драма")));

        Film savedNewFilm = filmDbStorage.addFilm(newFilm);
        assertTrue(filmDbStorage.getFilmById(savedNewFilm.getId()).isPresent());
        assertEquals(2, filmDbStorage.getFilmById(savedNewFilm.getId()).get().getGenres().size());
    }
}
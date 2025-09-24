package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.film.DirectorDbStorage;
import ru.yandex.practicum.filmorate.storage.film.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.film.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryFriendshipStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FilmServiceTest {
    private FilmService filmService;
    private UserService userService;
    private FeedService feedService;

    // ____________Helpers___________

    // Creates a valid film with default values
    private Film makeValidFilm(String name) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        return film;
    }

    // Registers and returns film
    private Film registerFilm(String name) {
        return filmService.addFilm(makeValidFilm(name));
    }

    // Creates a valid user with unique login/email
    private User makeValidUser(String login, String email) {
        User user = new User();
        user.setLogin(login);
        user.setEmail(email);
        user.setName("Name");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        return user;
    }

    // Registers user and returns the result
    private User registerUser(String login, String email) {
        return userService.addUser(makeValidUser(login, email));
    }

    @BeforeEach
    void setUp() {
        InMemoryUserStorage userStorage = new InMemoryUserStorage();
        InMemoryFilmStorage filmStorage = new InMemoryFilmStorage();

        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:file:./db/filmorate");
        dataSource.setUsername("sa");
        dataSource.setPassword("password");

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        MpaDbStorage mpaDbStorage = new MpaDbStorage(jdbcTemplate);
        GenreDbStorage genreDbStorage = new GenreDbStorage(jdbcTemplate);
        DirectorStorage directorStorage = new DirectorDbStorage(jdbcTemplate);

        MpaService mpaService = new MpaService(mpaDbStorage);
        GenreService genreService = new GenreService(genreDbStorage);

        FilmService filmServiceWithoutDirector = new FilmService(
                filmStorage, userStorage, mpaService, genreService, null, jdbcTemplate, feedService);

        DirectorService directorService = new DirectorService(directorStorage);

        filmService = new FilmService(
                filmStorage, userStorage, mpaService, genreService, directorService, jdbcTemplate, feedService);

        FeedService feedService = new FeedService(null, null) {
            public void addFeed(int userId, EventType eventType, Operation operation, int entityId) {

            }

            public List<Feed> getFeedByUserId(int userId) {
                return List.of();
            }

            public void removeUserFeed(int userId) {
            }
        };

        InMemoryFriendshipStorage friendshipStorage = new InMemoryFriendshipStorage();
        FriendshipService friendshipService = new FriendshipService(friendshipStorage, userStorage);

        userService = new UserService(userStorage, friendshipService, feedService);
        filmService = new FilmService(filmStorage, userStorage, mpaService, genreService, directorService, jdbcTemplate,
                feedService);
    }

    // ____________Tests___________

    // Test: Should add like successfully
    @Test
    void shouldAddLikeSuccessfully() {
        Film film = registerFilm("Interstellar");
        User user = registerUser("user1", "u1@mail.com");

        filmService.addLike(film.getId(), user.getId());

        assertTrue(film.getLikes().contains(user.getId()));
    }

    // Test: Should remove like successfully
    @Test
    void shouldRemoveLikeSuccessfully() {
        Film film = registerFilm("Inception");
        User user = registerUser("user1", "u1@mail.com");

        filmService.addLike(film.getId(), user.getId());
        filmService.removeLike(film.getId(), user.getId());

        assertFalse(film.getLikes().contains(user.getId()));
    }

    // Test: Should return top films sorted by likes
    @Test
    void shouldReturnPopularFilmsInOrder() {
        Film f1 = registerFilm("Film 1");
        Film f2 = registerFilm("Film 2");
        Film f3 = registerFilm("Film 3");

        User u1 = registerUser("u1", "u1@mail.com");
        User u2 = registerUser("u2", "u2@mail.com");
        User u3 = registerUser("u3", "u3@mail.com");

        filmService.addLike(f2.getId(), u1.getId());
        filmService.addLike(f2.getId(), u2.getId());
        filmService.addLike(f1.getId(), u3.getId());

        List<Film> popular = filmService.getPopular(3);

        assertEquals(f2.getId(), popular.get(0).getId());
        assertEquals(f1.getId(), popular.get(1).getId());
        assertEquals(f3.getId(), popular.get(2).getId());
    }

    // Test: Should return popular films with filters
    @Test
    void shouldReturnPopularFilmsWithFilters() {
        Film f1 = registerFilm("Film 1");
        Film f2 = registerFilm("Film 2");
        Film f3 = registerFilm("Film 3");

        User u1 = registerUser("u1", "u1@mail.com");
        User u2 = registerUser("u2", "u2@mail.com");

        filmService.addLike(f1.getId(), u1.getId());
        filmService.addLike(f1.getId(), u2.getId());

        List<Film> popular = filmService.getPopular(5, null, null);

        assertFalse(popular.isEmpty());
        assertEquals(f1.getId(), popular.get(0).getId());
    }

    // Test: Should throw when adding duplicate like
    @Test
    void shouldThrowWhenAddingDuplicateLike() {
        Film film = registerFilm("Avatar");
        User user = registerUser("userX", "x@mail.com");

        filmService.addLike(film.getId(), user.getId());

        ValidationException ex = assertThrows(ValidationException.class, () ->
                filmService.addLike(film.getId(), user.getId()));
        assertEquals("User with id=" + user.getId() + " has already liked film with id=" + film.getId(), ex.getMessage());
    }

    // ----------- Search Tests -----------
    // Test: Should find film by title
    @Test
    void shouldFindFilmByTitle() {
        Film film = registerFilm("Love Actually");
        List<Film> results = filmService.searchFilms("love", List.of("title"));

        assertEquals(1, results.size());
        assertEquals(film.getId(), results.get(0).getId());
    }

    // Test: Should find film by description
    @Test
    void shouldFindFilmByDescription() {
        Film film = makeValidFilm("Random");
        film.setDescription("Epic adventure story");
        filmService.addFilm(film);

        List<Film> results = filmService.searchFilms("adventure", List.of("description"));
        assertEquals(1, results.size());
    }

    // Test: Should find film by director
    @Test
    void shouldFindFilmByDirector() {
        assertDoesNotThrow(() -> filmService.searchFilms("Nolan", List.of("director")));
    }

    // Test: Should find film by title and description
    @Test
    void shouldFindFilmByTitleAndDescription() {
        Film film = makeValidFilm("Matrix");
        film.setDescription("Sci-fi revolution");
        filmService.addFilm(film);

        List<Film> results = filmService.searchFilms("sci-fi", List.of("title", "description"));
        assertEquals(1, results.size());
    }

    // Test: Should return empty list when no matches found
    @Test
    void shouldReturnEmptyWhenNoMatches() {
        registerFilm("Interstellar");
        List<Film> results = filmService.searchFilms("Comedy", List.of("title"));
        assertTrue(results.isEmpty());
    }

    // Test: Should throw when invalid search parameter provided
    @Test
    void shouldThrowWhenInvalidByParameter() {
        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> filmService.searchFilms("something", List.of("invalidField"))
        );

        assertEquals("Invalid search parameter: invalidField", ex.getMessage());
    }

    // ----------- Recommendations Tests -----------
    // Test: Should return empty list (0 recommendations) when user has no likes
    @Test
    void shouldReturnEmptyListWhenUserHasNoLikes() {
        User u1 = registerUser("u1", "u1@mail.com");
        User u2 = registerUser("u2", "u2@mail.com");
        Film f1 = registerFilm("Film 1");

        filmService.addLike(f1.getId(), u2.getId());

        Set<Integer> likes = filmService.getFilmById(f1.getId()).getLikes();
        assertFalse(likes.contains(u1.getId()));
        assertTrue(likes.contains(u2.getId()));

        List<Film> recommendations = filmService.getRecommendations(u1.getId());
        assertTrue(recommendations.isEmpty());
    }

    // Should return empty list when user has likes but no similar users
    @Test
    void shouldReturnEmptyListWhenUserHasLikesButNoSimilarUsers() {
        User u1 = registerUser("u1", "u1@mail.com");
        User u2 = registerUser("u2", "u2@mail.com");
        Film f1 = registerFilm("Film 1");
        Film f2 = registerFilm("Film 2");

        filmService.addLike(f1.getId(), u1.getId());
        filmService.addLike(f2.getId(), u2.getId());

        Set<Integer> likes1 = filmService.getFilmById(f1.getId()).getLikes();
        Set<Integer> likes2 = filmService.getFilmById(f2.getId()).getLikes();
        assertTrue(likes1.contains(u1.getId()));
        assertFalse(likes1.contains(u2.getId()));
        assertFalse(likes2.contains(u1.getId()));
        assertTrue(likes2.contains(u2.getId()));

        List<Film> recommendations = filmService.getRecommendations(u1.getId());
        assertTrue(recommendations.isEmpty());
    }

    // Should return recommendations when similar users exist
    @Test
    void shouldReturnRecommendationsWhenSimilarUsersExist() {
        User u1 = registerUser("u1", "u1@mail.com");
        User u2 = registerUser("u2", "u2@mail.com");
        Film f1 = registerFilm("Film 1");
        Film f2 = registerFilm("Film 2");
        f2.setName("Recommended Film");

        filmService.addLike(f1.getId(), u1.getId());
        filmService.addLike(f1.getId(), u2.getId());
        filmService.addLike(f2.getId(), u2.getId());

        List<Film> recommendations = filmService.getRecommendations(u1.getId());
        assertFalse(recommendations.isEmpty());
        assertEquals(1, recommendations.size());
        assertEquals("Recommended Film", recommendations.get(0).getName());
    }

    // Should not return already liked films
    @Test
    void shouldNotReturnAlreadyLikedFilms() {
        User u1 = registerUser("u1", "u1@mail.com");
        User u2 = registerUser("u2", "u2@mail.com");
        Film f1 = registerFilm("Film 1");
        Film f2 = registerFilm("Film 2");

        filmService.addLike(f1.getId(), u1.getId());
        filmService.addLike(f1.getId(), u2.getId());
        filmService.addLike(f2.getId(), u1.getId());
        filmService.addLike(f2.getId(), u2.getId());

        List<Film> recommendationsOne = filmService.getRecommendations(u1.getId());
        List<Film> recommendationsTwo = filmService.getRecommendations(u2.getId());
        assertTrue(recommendationsOne.isEmpty());
        assertTrue(recommendationsTwo.isEmpty());
    }

    // Should delete film successfully
    @Test
    void shouldDeleteFilmSuccessfully() {
        Film film = registerFilm("Film to delete");
        int filmId = film.getId();

        assertDoesNotThrow(() -> filmService.getFilmById(filmId));

        assertDoesNotThrow(() -> filmService.deleteFilm(filmId));

        assertThrows(ru.yandex.practicum.filmorate.exception.NotFoundException.class,
                () -> filmService.getFilmById(filmId));
    }

    // Should throw exception when deleting non-existent film
    @Test
    void shouldThrowWhenDeletingNonExistentFilm() {
        int nonExistentFilmId = 9999;

        assertThrows(ru.yandex.practicum.filmorate.exception.NotFoundException.class,
                () -> filmService.getFilmById(nonExistentFilmId));

        assertThrows(ru.yandex.practicum.filmorate.exception.NotFoundException.class,
                () -> filmService.deleteFilm(nonExistentFilmId));
    }

    // hould remove film likes when film is deleted
    @Test
    void shouldRemoveFilmLikesWhenFilmIsDeleted() {
        // Создаем фильм и пользователей
        Film film = registerFilm("Film with likes");
        User user1 = registerUser("user1", "user1@mail.com");
        User user2 = registerUser("user2", "user2@mail.com");

        int filmId = film.getId();
        int userId1 = user1.getId();
        int userId2 = user2.getId();

        filmService.addLike(filmId, userId1);
        filmService.addLike(filmId, userId2);

        Film filmBeforeDelete = filmService.getFilmById(filmId);
        assertEquals(2, filmBeforeDelete.getLikes().size());
        assertTrue(filmBeforeDelete.getLikes().contains(userId1));
        assertTrue(filmBeforeDelete.getLikes().contains(userId2));

        assertDoesNotThrow(() -> filmService.deleteFilm(filmId));

        assertThrows(ru.yandex.practicum.filmorate.exception.NotFoundException.class,
                () -> filmService.getFilmById(filmId));

        assertDoesNotThrow(() -> userService.getUserById(userId1));
        assertDoesNotThrow(() -> userService.getUserById(userId2));

        Film newFilm = registerFilm("New film after deletion");
        assertDoesNotThrow(() -> filmService.addLike(newFilm.getId(), userId1));
        assertDoesNotThrow(() -> filmService.addLike(newFilm.getId(), userId2));

        Film updatedNewFilm = filmService.getFilmById(newFilm.getId());
        assertEquals(2, updatedNewFilm.getLikes().size());
        assertTrue(updatedNewFilm.getLikes().contains(userId1));
        assertTrue(updatedNewFilm.getLikes().contains(userId2));
    }
}
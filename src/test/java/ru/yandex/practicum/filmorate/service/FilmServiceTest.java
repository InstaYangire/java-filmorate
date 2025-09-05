package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.film.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryFriendshipStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FilmServiceTest {
    private FilmService filmService;
    private UserService userService;

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

        MpaService mpaService = new MpaService(mpaDbStorage);
        GenreService genreService = new GenreService(genreDbStorage);

        InMemoryFriendshipStorage friendshipStorage = new InMemoryFriendshipStorage();
        FriendshipService friendshipService = new FriendshipService(friendshipStorage, userStorage);

        userService = new UserService(userStorage, friendshipService);
        filmService = new FilmService(filmStorage, userStorage, mpaService, genreService);
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
}
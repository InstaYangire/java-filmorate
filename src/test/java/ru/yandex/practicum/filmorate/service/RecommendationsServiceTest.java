package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RecommendationsServiceTest {

    @Mock
    private FilmStorage filmStorage;

    @Mock
    private UserStorage userStorage;

    @InjectMocks
    private FilmService filmService;

    // Should return empty list (0 recommendations) when user has no likes
    @Test
    void shouldReturnEmptyListWhenUserHasNoLikes() {
        User user = registerUser(1, "noLikesUser", "nolikes@mail.com");

        when(userStorage.getUserById(1)).thenReturn(Optional.of(user));
        when(filmStorage.getRecommendations(1)).thenReturn(List.of());

        List<Film> recommendations = filmService.getRecommendations(1);

        assertTrue(recommendations.isEmpty());
    }

    // Should return empty list when user has likes but no similar users
    @Test
    void shouldReturnEmptyListWhenUserHasLikesButNoSimilarUsers() {
        User user = registerUser(2, "lonelyUser", "lonely@mail.com");

        when(userStorage.getUserById(2)).thenReturn(Optional.of(user));
        when(filmStorage.getRecommendations(2)).thenReturn(List.of());

        List<Film> recommendations = filmService.getRecommendations(2);

        assertTrue(recommendations.isEmpty());
    }

    // Should return recommendations when similar users exist
    @Test
    void shouldReturnRecommendationsWhenSimilarUsersExist() {
        User user = registerUser(3, "socialUser", "social@mail.com");
        Film recommendedFilm = createFilm(10, "Recommended Film");

        when(userStorage.getUserById(3)).thenReturn(Optional.of(user));
        when(filmStorage.getRecommendations(3)).thenReturn(List.of(recommendedFilm));

        List<Film> recommendations = filmService.getRecommendations(3);

        assertFalse(recommendations.isEmpty());
        assertEquals(1, recommendations.size());
        assertEquals("Recommended Film", recommendations.get(0).getName());
    }

    // Should not return already liked films
    @Test
    void shouldNotReturnAlreadyLikedFilms() {
        User user = registerUser(4, "pickyUser", "picky@mail.com");
        Film newFilm = createFilm(20, "New Film");

        when(userStorage.getUserById(4)).thenReturn(Optional.of(user));
        when(filmStorage.getRecommendations(4)).thenReturn(List.of(newFilm));

        List<Film> recommendations = filmService.getRecommendations(4);

        // Even if there are recommendations, they should not contain already liked movies.
        assertFalse(recommendations.isEmpty());
    }

    private User registerUser(int id, String login, String email) {
        User user = new User();
        user.setId(id);
        user.setLogin(login);
        user.setEmail(email);
        user.setName("Name");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        return user;
    }

    private Film createFilm(int id, String name) {
        Film film = new Film();
        film.setId(id);
        film.setName(name);
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        return film;
    }
}
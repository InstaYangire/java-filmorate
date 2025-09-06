package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.LinkedHashSet;

import static ru.yandex.practicum.filmorate.validator.FilmValidator.validateFilm;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final MpaService mpaService;
    private final GenreService genreService;

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage,
                       @Qualifier("mpaService") MpaService mpaService,
                       @Qualifier("genreService") GenreService genreService) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.mpaService = mpaService;
        this.genreService = genreService;
    }

    // ___________Films___________
    // Adding a new movie
    public Film addFilm(Film film) {
        log.info("Request received to add movie: {}", film);
        validateFilm(film);
        validateAndSetMpaAndGenres(film);
        Film createdFilm = filmStorage.addFilm(film);
        log.info("Movie added successfully: {}", createdFilm);
        return createdFilm;
    }

    // Updating an existing movie by id
    public Film updateFilm(Film film) {
        log.info("Received a request to update film: {}", film);
        validateFilm(film);
        filmStorage.getFilmById(film.getId())
                .orElseThrow(() -> new NotFoundException("Movie with id=" + film.getId() + " not found."));
        validateAndSetMpaAndGenres(film);
        Film updatedFilm = filmStorage.updateFilm(film);
        log.info("Movie with id={} updated successfully.", updatedFilm.getId());
        return updatedFilm;
    }

    // Getting a list of all movies
    public List<Film> getAllFilms() {
        List<Film> films = filmStorage.getAllFilms();
        log.info("Request for list of all movies received. Quantity: {}", films.size());
        return films;
    }

    // Getting a movie by id
    public Film getFilmById(int id) {
        return filmStorage.getFilmById(id)
                .orElseThrow(() -> new NotFoundException("Movie with id=" + id + " not found."));
    }

    //___________Likes__________
    // Adding a like to a movie
    public void addLike(int filmId, int userId) {
        filmStorage.getFilmById(filmId)
                .orElseThrow(() -> new NotFoundException("Film with id=" + filmId + " not found."));
        userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " not found."));

        filmStorage.addLike(filmId, userId);
        log.info("User with id={} liked film with id={}", userId, filmId);
    }

    // Removing a like from a movie
    public void removeLike(int filmId, int userId) {
        filmStorage.getFilmById(filmId)
                .orElseThrow(() -> new NotFoundException("Film with id=" + filmId + " not found."));
        userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " not found."));

        filmStorage.removeLike(filmId, userId); // Удаляем из базы

        log.info("User with id={} removed like from film with id={}", userId, filmId);
    }

    // Getting a list of the most popular movies
    public List<Film> getPopular(int count) {
        List<Film> allFilms = filmStorage.getAllFilms();
        List<Film> sorted = allFilms.stream()
                .sorted((f1, f2) -> Integer.compare(f2.getLikes().size(), f1.getLikes().size()))
                .limit(count)
                .toList();
        log.info("Request for top {} popular films received. Found: {}", count, sorted.size());
        return sorted;
    }

    // Validate and replace MPA and genres from services
    private void validateAndSetMpaAndGenres(Film film) {
        if (film.getMpa() != null) {
            int mpaId = film.getMpa().getId();
            film.setMpa(mpaService.getMpaRatingById(mpaId));
        }

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Genre> validatedGenres = film.getGenres().stream()
                    .map(genre -> genreService.getGenreById(genre.getId()))
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            film.setGenres(validatedGenres);
        }
    }
}
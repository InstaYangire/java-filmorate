package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;

import static ru.yandex.practicum.filmorate.validator.FilmValidator.validateFilm;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    // ___________Films___________
    // Adding a new movie
    public Film addFilm(Film film) {
        log.info("Request received to add movie: {}", film);
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
        Film film = filmStorage.getFilmById(filmId)
                .orElseThrow(() -> new NotFoundException("Film with id=" + filmId + " not found."));
        userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " not found."));

        if (!film.getLikes().add(userId)) {
            throw new ValidationException("User with id=" + userId + " has already liked film with id=" + filmId);
        }

        log.info("User with id={} liked film with id={}", userId, filmId);
    }

    // Removing a like from a movie
    public void removeLike(int filmId, int userId) {
        Film film = filmStorage.getFilmById(filmId)
                .orElseThrow(() -> new NotFoundException("Film with id=" + filmId + " not found."));
        userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " not found."));

        if (!film.getLikes().remove(userId)) {
            throw new ValidationException("Like from user not found.");
        }

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
}

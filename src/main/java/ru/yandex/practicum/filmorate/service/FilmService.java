package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;

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
        if (filmStorage.getFilmById(film.getId()) == null) {
            throw new NotFoundException("Movie with id=" + film.getId() + " not found.");
        }
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
        return filmStorage.getFilmById(id);
    }

    //___________Likes__________
    // Adding a like to a movie
    public void addLike(int filmId, int userId) {
        if (filmStorage.getFilmById(filmId) == null) {
            throw new NotFoundException("Film with id=" + filmId + " not found.");
        }
        if (userStorage.getUserById(userId) == null) {
            throw new NotFoundException("User with id=" + userId + " not found.");
        }
        filmStorage.addLike(filmId, userId);
    }

    // Removing a like from a movie
    public void removeLike(int filmId, int userId) {
        if (filmStorage.getFilmById(filmId) == null) {
            throw new NotFoundException("Film with id=" + filmId + " not found.");
        }
        if (userStorage.getUserById(userId) == null) {
            throw new NotFoundException("User with id=" + userId + " not found.");
        }
        filmStorage.removeLike(filmId, userId);
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

package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.time.LocalDate;
import java.util.*;

@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Integer, Film> films = new HashMap<>();
    private int nextId = 1;
    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, 12, 28);

    // Adding a new movie
    @Override
    public Film addFilm(Film film) {
        validateFilm(film);
        film.setId(nextId++);
        films.put(film.getId(), film);
        return film;
    }

    // Updating an existing movie by id
    @Override
    public Film updateFilm(Film film) {
        if (!films.containsKey(film.getId())) {
            throw new NotFoundException("Film with id=" + film.getId() + " not found.");
        }
        films.put(film.getId(), film);
        return film;
    }

    // Getting a movie by id
    @Override
    public Film getFilmById(int id) {
        return films.get(id);
    }

    // Getting a list of all movies
    @Override
    public List<Film> getAllFilms() {
        return new ArrayList<>(films.values());
    }

    // Movie data validation
    private void validateFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            throw new ValidationException("Movie title cannot be empty.");
        }
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            throw new ValidationException("The description must not exceed 200 characters.");
        }
        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(CINEMA_BIRTHDAY)) {
            throw new ValidationException("The release date cannot be earlier than December 28, 1895.");
        }
        if (film.getDuration() <= 0) {
            throw new ValidationException("The duration of the movie must be positive and not equal to zero.");
        }
    }

    // __________Likes_____________
    @Override
    public void addLike(int filmId, int userId) {
        Film film = getFilmById(filmId);
        if (film != null) {
            film.getLikes().add(userId);
        }
    }

    @Override
    public void removeLike(int filmId, int userId) {
        Film film = getFilmById(filmId);
        if (film != null) {
            film.getLikes().remove(userId);
        }
    }
}

package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;


import java.util.*;

import static ru.yandex.practicum.filmorate.validator.FilmValidator.validateFilm;

@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Integer, Film> films = new HashMap<>();
    private int nextId = 1;

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
    public Optional<Film> getFilmById(int id) {
        return Optional.ofNullable(films.get(id));
    }

    // Getting a list of all movies
    @Override
    public List<Film> getAllFilms() {
        return new ArrayList<>(films.values());
    }

    // __________Likes_____________
    // Adding like
    @Override
    public void addLike(int filmId, int userId) {
        getFilmById(filmId).ifPresentOrElse(
                film -> film.getLikes().add(userId),
                () -> {
                    throw new NotFoundException("Film with id=" + filmId + " not found.");
                }
        );
    }

    // Removing like
    @Override
    public void removeLike(int filmId, int userId) {
        getFilmById(filmId).ifPresentOrElse(
                film -> film.getLikes().remove(userId),
                () -> {
                    throw new NotFoundException("Film with id=" + filmId + " not found.");
                }
        );
    }
}

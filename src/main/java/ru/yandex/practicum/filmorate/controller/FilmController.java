package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Integer, Film> films = new HashMap<>();
    private int nextId = 1;
    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, 12, 28);

    // Adding a new movie
    @PostMapping
    public Film addFilm(@RequestBody Film film) {
        log.info("Request received to add movie: {}", film);
        validateFilm(film);
        film.setId(nextId++);
        films.put(film.getId(), film);
        log.info("Movie added successfully: {}", film);
        return film;
    }

    // Updating an existing movie by id
    @PutMapping
    public Film updateFilm(@RequestBody Film film) {
        log.info("Request received to update movie: {}", film);
        validateFilm(film);
        if (!films.containsKey(film.getId())) {
            log.warn("Movie with id={} not found.", film.getId());
            throw new ValidationException("Movie with id=" + film.getId() + " not found.");
        }
        films.put(film.getId(), film);
        log.info("Movie with id={} updated successfully.", film.getId());
        return film;
    }

    // Getting a list of all movies
    @GetMapping
    public List<Film> getAllFilms() {
        log.info("Request for list of all movies received. Quantity: {}", films.size());
        return List.copyOf(films.values());
    }

    // Movie data validation
    private void validateFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("Validation error: empty movie title.");
            throw new ValidationException("Movie title cannot be empty.");
        }
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            log.warn("Validation error: description is longer than 200 characters.");
            throw new ValidationException("The description must not exceed 200 characters.");
        }
        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(CINEMA_BIRTHDAY)) {
            log.warn("Validation error: release before December 28, 1895.");
            throw new ValidationException("The release date cannot be earlier than December 28, 1895.");
        }
        if (film.getDuration() <= 0) {
            log.warn("Validation error: duration is less than or equal to 0.");
            throw new ValidationException("The duration of the movie must be positive and not equal to zero.");
        }
    }
}

package ru.yandex.practicum.filmorate.validator;

import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

public class FilmValidator {
    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, 12, 28);

    // Movie data validation
    public static void validateFilm(Film film) {
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
}

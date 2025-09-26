package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
@Validated
public class FilmController {
    private final FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    // ___________Films___________

    @PostMapping
    public Film addFilm(@Valid @RequestBody Film film) {
        return filmService.addFilm(film);
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        return filmService.updateFilm(film);
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable @Positive int id) {
        return filmService.getFilmById(id);
    }

    @GetMapping
    public List<Film> getAllFilms() {
        return filmService.getAllFilms();
    }

    @GetMapping("/common")
    public List<Film> getCommonFilms(@RequestParam("userId") @Positive int userId,
                                     @RequestParam("friendId") @Positive int friendId) {
        return filmService.getCommonFilms(userId, friendId);
    }

    @GetMapping("/director/{directorId}")
    public List<Film> getFilmsByDirector(@PathVariable @Positive int directorId,
                                         @RequestParam(defaultValue = "likes") String sortBy) {
        return filmService.getFilmsByDirector(directorId, sortBy);
    }

    @GetMapping("/search")
    public List<Film> searchFilms(
            @RequestParam @NotBlank String query,
            @RequestParam(name = "by") @NotEmpty List<String> by) {
        return filmService.searchFilms(query, by);
    }

    //___________Likes__________

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable @Positive int id, @PathVariable @Positive int userId) {
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable @Positive int id, @PathVariable @Positive int userId) {
        filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopular(@RequestParam(defaultValue = "10") @Positive int count,
                                 @RequestParam(required = false) @Positive Integer genreId,
                                 @RequestParam(required = false) @Positive Integer year) {
        return filmService.getPopular(count, genreId, year);
    }

    @DeleteMapping("/{id}")
    public void deleteFilm(@PathVariable @Positive int id) {
        filmService.deleteFilm(id);
    }
}
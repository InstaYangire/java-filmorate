package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    // ___________Films___________
    // Adding a new movie
    @PostMapping
    public Film addFilm(@RequestBody Film film) {
        return filmService.addFilm(film);
    }

    // Updating an existing movie by id
    @PutMapping
    public Film updateFilm(@RequestBody Film film) {
        return filmService.updateFilm(film);
    }

    // Getting a list of all movies
    @GetMapping
    public List<Film> getAllFilms() {
        return filmService.getAllFilms();
    }

    //___________Likes__________
    // Adding a like to a movie
    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable int id, @PathVariable int userId) {
        filmService.addLike(id, userId);
    }

    // Removing a like from a movie
    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable int id, @PathVariable int userId) {
        filmService.removeLike(id, userId);
    }

    // Getting a list of the most popular movies
    @GetMapping("/popular")
    public List<Film> getPopular(@RequestParam(defaultValue = "10") int count) {
        return filmService.getPopular(count);
    }
}

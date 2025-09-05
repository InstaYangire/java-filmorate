package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.GenreService;

import java.util.List;

@RestController
@RequestMapping("/genres")
@RequiredArgsConstructor
public class GenreController {

    private final GenreService genreService;

    // Getting a list of all genres
    @GetMapping
    public List<Genre> getAllGenres() {
        return genreService.getAllGenres();
    }

    // Getting a genre by id
    @GetMapping("/{id}")
    public Genre getGenreById(@PathVariable int id) {
        return genreService.getGenreById(id);
    }
}
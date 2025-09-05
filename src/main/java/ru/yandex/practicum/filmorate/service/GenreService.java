package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.GenreDbStorage;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GenreService {

    private final GenreDbStorage genreDbStorage;

    // Getting a list of all genres
    public List<Genre> getAllGenres() {
        return genreDbStorage.getAllGenres();
    }

    // Getting a genre by id
    public Genre getGenreById(int id) {
        return genreDbStorage.getGenreById(id);
    }
}
package ru.yandex.practicum.filmorate.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DirectorService {

    private final DirectorStorage directorStorage;
    private final FilmService filmService;

    public List<Director> getAllDirectors() {
        log.info("Request for all directors received");
        return directorStorage.findAll();
    }

    public Director getDirectorById(int id) {
        log.info("Request for director with id={}", id);
        return directorStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Director with id=" + id + " not found."));
    }

    public Director createDirector(Director director) {
        log.info("Request to create director: {}", director);
        validateDirector(director);
        return directorStorage.create(director);
    }

    public Director updateDirector(Director director) {
        log.info("Request to update director: {}", director);
        validateDirector(director);
        getDirectorById(director.getId());
        return directorStorage.update(director);
    }

    public void deleteDirector(int id) {
        log.info("Request to delete director with id={}", id);
        getDirectorById(id);
        directorStorage.delete(id);
    }

    public List<Film> getDirectorFilms(int directorId, String sortBy) {
        log.info("Request for films of director id={} sorted by {}", directorId, sortBy);
        getDirectorById(directorId);

        List<Film> allFilms = filmService.getAllFilms();
        return allFilms.stream()
                .filter(film -> film.getDirectorId() != null && film.getDirectorId() == directorId)
                .sorted((f1, f2) -> {
                    if ("year".equalsIgnoreCase(sortBy)) {
                        return f1.getReleaseDate().compareTo(f2.getReleaseDate());
                    } else { // default sort by likes
                        return Integer.compare(f2.getLikes().size(), f1.getLikes().size());
                    }
                })
                .toList();
    }

    private void validateDirector(Director director) {
        if (director.getName() == null || director.getName().isBlank()) {
            throw new IllegalArgumentException("Director name cannot be empty");
        }
    }
}

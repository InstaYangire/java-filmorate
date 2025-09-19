package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/directors")
@RequiredArgsConstructor
public class DirectorController {

    private final DirectorService directorService;

    // Getting all directors
    @GetMapping
    public List<Director> getAllDirectors() {
        return directorService.getAllDirectors();
    }

    // Getting director by ID
    @GetMapping("/{id}")
    public Director getDirectorById(@PathVariable int id) {
        return directorService.getDirectorById(id);
    }

    // Creating director
    @PostMapping
    public Director createDirector(@RequestBody Director director) {
        return directorService.createDirector(director);
    }

    // Updating director
    @PutMapping
    public Director updateDirector(@RequestBody Director director) {
        return directorService.updateDirector(director);
    }

    // Deleting director
    @DeleteMapping("/{id}")
    public void deleteDirector(@PathVariable int id) {
        directorService.deleteDirector(id);

    }
}

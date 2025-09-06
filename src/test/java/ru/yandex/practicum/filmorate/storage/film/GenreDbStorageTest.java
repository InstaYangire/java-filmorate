package ru.yandex.practicum.filmorate.storage.film;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@Import(GenreDbStorage.class)
class GenreDbStorageTest {

    @Autowired
    private GenreDbStorage genreDbStorage;

    // ----------- Tests -----------

    // Test: All genres should be returned and not empty
    @Test
    void shouldReturnAllGenres() {
        List<Genre> genres = genreDbStorage.getAllGenres();

        assertFalse(genres.isEmpty());
        assertTrue(genres.size() >= 6); // Expected at least 6 standard genres
    }

    // Test: Genres should be sorted by ID
    @Test
    void shouldReturnGenresSortedById() {
        List<Genre> genres = genreDbStorage.getAllGenres();

        for (int i = 1; i < genres.size(); i++) {
            assertTrue(genres.get(i).getId() > genres.get(i - 1).getId());
        }
    }

    // Test: Genre should be found by its ID
    @Test
    void shouldFindGenreById() {
        Genre genre = genreDbStorage.getGenreById(1);

        assertNotNull(genre);
        assertEquals(1, genre.getId());
        assertEquals("Комедия", genre.getName()); // Matches data.sql
    }

    // Test: Getting non-existing genre should throw NotFoundException
    @Test
    void shouldThrowWhenGenreNotFound() {
        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> genreDbStorage.getGenreById(999)
        );

        assertEquals("Genre with id=999 not found.", ex.getMessage());
    }
}
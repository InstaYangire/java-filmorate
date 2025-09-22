package ru.yandex.practicum.filmorate.storage.film;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@Import(DirectorDbStorage.class)
class DirectorDbStorageTest {

    @Autowired
    private DirectorDbStorage directorDbStorage;

    // ----------- Helpers -----------

    // Create a sample director with default name
    private Director createSampleDirector() {
        Director director = new Director();
        director.setName("Test Director");
        return directorDbStorage.create(director);
    }

    // Create a sample director with custom name
    private Director createSampleDirector(String name) {
        Director director = new Director();
        director.setName(name);
        return directorDbStorage.create(director);
    }

    // ----------- Tests -----------

    // Test: All directors should be found
    @Test
    void shouldFindAllDirectors() {
        createSampleDirector("Director 1");
        createSampleDirector("Director 2");

        List<Director> directors = directorDbStorage.findAll();
        assertEquals(2, directors.size());
    }

    // Test: Director should be found by ID
    @Test
    void shouldFindDirectorById() {
        Director director = createSampleDirector("Christopher Nolan");

        Optional<Director> found = directorDbStorage.findById(director.getId());

        assertTrue(found.isPresent());
        assertEquals("Christopher Nolan", found.get().getName());
    }

    // Test: Should return empty when director not found
    @Test
    void shouldReturnEmptyWhenDirectorNotFound() {
        Optional<Director> director = directorDbStorage.findById(999);
        assertFalse(director.isPresent());
    }

    // Test: Director should be created successfully
    @Test
    void shouldCreateDirector() {
        Director created = createSampleDirector("New Director");

        assertNotNull(created.getId());
        assertEquals("New Director", created.getName());

        Optional<Director> found = directorDbStorage.findById(created.getId());
        assertTrue(found.isPresent());
        assertEquals("New Director", found.get().getName());
    }

    // Test: Director should be updated successfully
    @Test
    void shouldUpdateDirector() {
        Director director = createSampleDirector("Old Name");

        Director toUpdate = new Director(director.getId(), "Updated Name");
        Director updated = directorDbStorage.update(toUpdate);

        assertEquals("Updated Name", updated.getName());

        Optional<Director> found = directorDbStorage.findById(director.getId());
        assertTrue(found.isPresent());
        assertEquals("Updated Name", found.get().getName());
    }

    // Test: Updating non-existent director should throw NotFoundException
    @Test
    void shouldThrowWhenUpdatingNonExistentDirector() {
        Director director = new Director(999, "Non-existent");

        assertThrows(NotFoundException.class, () -> directorDbStorage.update(director));
    }

    // Test: Director should be deleted successfully
    @Test
    void shouldDeleteDirector() {
        Director director = createSampleDirector("To Delete");

        directorDbStorage.delete(director.getId());

        Optional<Director> found = directorDbStorage.findById(director.getId());
        assertFalse(found.isPresent());
    }

    // Test: Should generate unique IDs for directors
    @Test
    void shouldGenerateUniqueIds() {
        Director d1 = createSampleDirector("Director 1");
        Director d2 = createSampleDirector("Director 2");

        assertNotEquals(d1.getId(), d2.getId());
    }
}
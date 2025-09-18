package ru.yandex.practicum.filmorate.storage.film;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class DirectorDbStorageTest {
    private DirectorStorage directorStorage;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        dataSource.setUsername("sa");
        dataSource.setPassword("");

        jdbcTemplate = new JdbcTemplate(dataSource);

        // Initialize database schema
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS directors (" +
                "id INT PRIMARY KEY AUTO_INCREMENT, " +
                "name VARCHAR NOT NULL)");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS films (" +
                "id INT PRIMARY KEY AUTO_INCREMENT, " +
                "name VARCHAR NOT NULL, " +
                "description VARCHAR, " +
                "release_date DATE, " +
                "duration INT, " +
                "mpa_id INT, " +
                "director_id INT)");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS film_directors (" +
                "film_id INT NOT NULL, " +
                "director_id INT NOT NULL, " +
                "PRIMARY KEY (film_id, director_id))");

        directorStorage = new DirectorDbStorage(jdbcTemplate);

        // Clean up before each test
        jdbcTemplate.update("DELETE FROM film_directors");
        jdbcTemplate.update("DELETE FROM directors");
    }

    @Test
    void shouldFindAllDirectors() {
        jdbcTemplate.update("INSERT INTO directors (name) VALUES ('Director 1')");
        jdbcTemplate.update("INSERT INTO directors (name) VALUES ('Director 2')");

        List<Director> directors = directorStorage.findAll();
        assertEquals(2, directors.size());
    }

    @Test
    void shouldFindDirectorById() {
        // Insert a director and get the generated ID
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO directors (name) VALUES (?)",
                    new String[]{"id"}
            );
            ps.setString(1, "Test Director");
            return ps;
        }, keyHolder);

        int generatedId = keyHolder.getKey().intValue();

        Optional<Director> director = directorStorage.findById(generatedId);
        assertTrue(director.isPresent());
        assertEquals("Test Director", director.get().getName());
    }

    @Test
    void shouldReturnEmptyWhenDirectorNotFound() {
        Optional<Director> director = directorStorage.findById(999);
        assertFalse(director.isPresent());
    }

    @Test
    void shouldCreateDirector() {
        Director director = new Director();
        director.setName("New Director");

        Director created = directorStorage.create(director);
        assertNotNull(created.getId());
        assertEquals("New Director", created.getName());

        // Verify it's in database
        Optional<Director> found = directorStorage.findById(created.getId());
        assertTrue(found.isPresent());
        assertEquals("New Director", found.get().getName());
    }

    @Test
    void shouldUpdateDirector() {
        jdbcTemplate.update("INSERT INTO directors (name) VALUES ('Old Name')");

        Director director = new Director(1, "Updated Name");
        Director updated = directorStorage.update(director);

        assertEquals("Updated Name", updated.getName());

        // Verify update in database
        Optional<Director> found = directorStorage.findById(1);
        assertTrue(found.isPresent());
        assertEquals("Updated Name", found.get().getName());
    }

    @Test
    void shouldThrowWhenUpdatingNonExistentDirector() {
        Director director = new Director(999, "Non-existent");

        assertThrows(NotFoundException.class, () -> directorStorage.update(director));
    }

    @Test
    void shouldDeleteDirector() {
        jdbcTemplate.update("INSERT INTO directors (name) VALUES ('To Delete')");

        directorStorage.delete(1);

        Optional<Director> found = directorStorage.findById(1);
        assertFalse(found.isPresent());
    }

    @Test
    void shouldReturnEmptyListWhenNoDirectorsForFilm() {
        jdbcTemplate.update("INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES " +
                "('Film 1', 'Desc', '2020-01-01', 120, 1)");

        List<Director> directors = directorStorage.getDirectorByFilmId(1);
        assertTrue(directors.isEmpty());
    }

    @Test
    void shouldGenerateUniqueIds() {
        Director director1 = new Director();
        director1.setName("Director 1");

        Director director2 = new Director();
        director2.setName("Director 2");

        Director created1 = directorStorage.create(director1);
        Director created2 = directorStorage.create(director2);

        assertNotEquals(created1.getId(), created2.getId());
    }
}
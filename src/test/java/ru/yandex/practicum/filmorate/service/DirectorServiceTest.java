package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.film.DirectorDbStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class DirectorServiceTest {

    @Autowired
    private DirectorService directorService;

    @Autowired
    private FilmService filmService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Director makeValidDirector(String name) {
        Director director = new Director();
        director.setName(name);
        return director;
    }

    private Director registerDirector(String name) {
        return directorService.createDirector(makeValidDirector(name));
    }

    private Film makeValidFilm(String name) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        MpaRating mpa = new MpaRating(3, "PG-13");
        film.setMpa(mpa);
        return film;
    }

    private Film registerFilm(String name) {
        return filmService.addFilm(makeValidFilm(name));
    }

    @BeforeEach
    void setUp() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        dataSource.setUsername("sa");
        dataSource.setPassword("");

        jdbcTemplate = new JdbcTemplate(dataSource);

        jdbcTemplate.execute("DROP TABLE IF EXISTS film_directors");
        jdbcTemplate.execute("DROP TABLE IF EXISTS directors");
        jdbcTemplate.execute("DROP TABLE IF EXISTS films");
        // Initialize database schema for tests
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

        DirectorStorage directorStorage = new DirectorDbStorage(jdbcTemplate);
        directorService = new DirectorService(directorStorage, filmService);
    }

    @Test
    void shouldGetAllDirectors() {
        registerDirector("Director 1");
        registerDirector("Director 2");

        List<Director> directors = directorService.getAllDirectors();
        assertEquals(2, directors.size());
    }

    @Test
    void shouldGetDirectorById() {
        Director director = registerDirector("Test Director");

        Director found = directorService.getDirectorById(director.getId());
        assertEquals("Test Director", found.getName());
    }

    @Test
    void shouldThrowWhenDirectorNotFound() {
        assertThrows(NotFoundException.class, () -> directorService.getDirectorById(999));
    }

    @Test
    void shouldCreateDirector() {
        Director director = makeValidDirector("New Director");
        Director created = directorService.createDirector(director);

        assertNotNull(created.getId());
        assertEquals("New Director", created.getName());
    }

    @Test
    void shouldUpdateDirector() {
        Director director = registerDirector("Old Name");
        director.setName("Updated Name");

        Director updated = directorService.updateDirector(director);
        assertEquals("Updated Name", updated.getName());
    }

    @Test
    void shouldDeleteDirector() {
        Director director = registerDirector("To Delete");

        directorService.deleteDirector(director.getId());
        assertThrows(NotFoundException.class, () -> directorService.getDirectorById(director.getId()));
    }

    @Test
    void shouldThrowWhenCreatingDirectorWithEmptyName() {
        Director director = makeValidDirector("   ");
        assertThrows(IllegalArgumentException.class, () -> directorService.createDirector(director));
    }
}


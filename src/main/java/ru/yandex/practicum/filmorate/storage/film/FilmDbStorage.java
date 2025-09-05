package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

@Component("filmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    // Adding a new film
    @Override
    public Film addFilm(Film film) {
        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, film.getName());
            stmt.setString(2, film.getDescription());
            stmt.setObject(3, film.getReleaseDate());
            stmt.setInt(4, film.getDuration());
            stmt.setInt(5, film.getMpa().getId());
            return stmt;
        }, keyHolder);

        int filmId = Objects.requireNonNull(keyHolder.getKey()).intValue();
        film.setId(filmId);

        updateFilmGenres(film); // Save genres
        return getFilmById(filmId).orElseThrow(() -> new NotFoundException("Film not found after creation."));
    }

    // Updating an existing film
    @Override
    public Film updateFilm(Film film) {
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE id = ?";

        int rows = jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());

        if (rows == 0) {
            throw new NotFoundException("Film with id=" + film.getId() + " not found.");
        }

        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
        updateFilmGenres(film); // Update genres
        return getFilmById(film.getId()).orElseThrow(() -> new NotFoundException("Film not found after update."));
    }

    // Getting film by ID
    @Override
    public Optional<Film> getFilmById(int id) {
        String sql = "SELECT * FROM films WHERE id = ?";
        List<Film> films = jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToFilm(rs.getInt("id")), id);
        return films.stream().findFirst();
    }

    // Getting all films
    @Override
    public List<Film> getAllFilms() {
        String sql = "SELECT id FROM films ORDER BY id"; // whatever ID comes first after INSERT is bad, so ORDER BY is needed
        List<Integer> ids = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getInt("id"));

        List<Film> films = new ArrayList<>();
        for (int id : ids) {
            getFilmById(id).ifPresent(films::add);
        }

        return films;
    }

    // Adding a like to a film
    @Override
    public void addLike(int filmId, int userId) {
        String sql = "SELECT COUNT(*) FROM film_likes WHERE film_id = ? AND user_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, filmId, userId);

        if (count > 0) {
            throw new ValidationException("User with id=" + userId + " has already liked film with id=" + filmId);
        }

        jdbcTemplate.update("INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)", filmId, userId);
    }

    // Removing a like from a film
    @Override
    public void removeLike(int filmId, int userId) {
        String checkSql = "SELECT COUNT(*) FROM film_likes WHERE film_id = ? AND user_id = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, filmId, userId);

        if (count == 0) {
            throw new NotFoundException("Like not found: filmId=" + filmId + ", userId=" + userId);
        }

        jdbcTemplate.update("DELETE FROM film_likes WHERE film_id = ? AND user_id = ?", filmId, userId);
    }

    // Mapping film by ID (used to load genres and mpa)
    private Film mapRowToFilm(int filmId) {
        String sql = "SELECT * FROM films WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            Film film = new Film();
            film.setId(rs.getInt("id"));
            film.setName(rs.getString("name"));
            film.setDescription(rs.getString("description"));
            film.setReleaseDate(rs.getDate("release_date").toLocalDate());
            film.setDuration(rs.getInt("duration"));
            film.setMpa(getMpaById(rs.getInt("mpa_id")));
            film.setGenres(new HashSet<>(getGenresByFilmId(filmId)));
            film.setLikes(getLikesByFilmId(filmId));
            return film;
        }, filmId);
    }

    // Saving genres to film_genres table
    private void updateFilmGenres(Film film) {
        Set<Genre> genres = film.getGenres();
        if (genres == null) return;

        for (Genre genre : genres) {
            jdbcTemplate.update("INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)",
                    film.getId(), genre.getId());
        }
    }

    // Getting genres of a film
    private List<Genre> getGenresByFilmId(int filmId) {
        String sql = "SELECT g.id, g.name FROM genres g JOIN film_genres fg ON g.id = fg.genre_id WHERE fg.film_id = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new Genre(rs.getInt("id"), rs.getString("name")), filmId);
    }

    // Getting MPA rating by ID
    private MpaRating getMpaById(int mpaId) {
        String sql = "SELECT * FROM mpa_ratings WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, (rs, rowNum) ->
                new MpaRating(rs.getInt("id"), rs.getString("name")), mpaId);
    }

    // Getting likes of a film
    private Set<Integer> getLikesByFilmId(int filmId) {
        String sql = "SELECT user_id FROM film_likes WHERE film_id = ?";
        return new HashSet<>(jdbcTemplate.query(sql, (rs, rowNum) -> rs.getInt("user_id"), filmId));
    }
}
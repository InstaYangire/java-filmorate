package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GenreDbStorage {

    private final JdbcTemplate jdbcTemplate;

    // Getting a list of all genres
    public List<Genre> getAllGenres() {
        String sql = "SELECT * FROM genres ORDER BY id";
        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new Genre(rs.getInt("id"), rs.getString("name")));
    }

    // Getting a genre by id
    public Genre getGenreById(int id) {
        String sql = "SELECT * FROM genres WHERE id = ?";
        List<Genre> result = jdbcTemplate.query(sql, (rs, rowNum) ->
                new Genre(rs.getInt("id"), rs.getString("name")), id);

        if (result.isEmpty()) {
            throw new NotFoundException("Genre with id=" + id + " not found.");
        }

        return result.get(0);
    }
}
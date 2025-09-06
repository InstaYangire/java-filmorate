package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MpaDbStorage {

    private final JdbcTemplate jdbcTemplate;

    // Getting a list of all MPA ratings
    public List<MpaRating> getAllRatings() {
        String sql = "SELECT * FROM mpa_ratings ORDER BY id";
        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new MpaRating(rs.getInt("id"), rs.getString("name")));
    }

    // Getting an MPA rating by id
    public MpaRating getRatingById(int id) {
        String sql = "SELECT * FROM mpa_ratings WHERE id = ?";
        List<MpaRating> result = jdbcTemplate.query(sql, (rs, rowNum) ->
                new MpaRating(rs.getInt("id"), rs.getString("name")), id);

        if (result.isEmpty()) {
            throw new NotFoundException("MPA rating with id=" + id + " not found.");
        }

        return result.get(0);
    }
}
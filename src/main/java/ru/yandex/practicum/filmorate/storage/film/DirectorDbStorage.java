package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DirectorDbStorage implements DirectorStorage {

    private final JdbcTemplate jdbcTemplate;

    //get all directors
    @Override
    public List<Director> findAll() {
        String sql = "SELECT * FROM directors ORDER BY id";
        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new Director(rs.getInt("id"), rs.getString("name")));
    }

    //find director by id
    @Override
    public Optional<Director> findById(int id) {
        String sql = "SELECT * FROM directors WHERE id = ?";
        List<Director> directors = jdbcTemplate.query(sql, (rs, rowNum) ->
                new Director(rs.getInt("id"), rs.getString("name")), id);
        return directors.stream().findFirst();
    }

    //create new director
    @Override
    public Director create(Director director) {
        String sql = "INSERT INTO directors (name) VALUES (?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, director.getName());
            return stmt;
        }, keyHolder);

        director.setId(keyHolder.getKey().intValue());
        return director;
    }

    //update director
    @Override
    public Director update(Director director) {
        String sql = "UPDATE directors SET name = ? WHERE id = ?";

        int rows = jdbcTemplate.update(sql, director.getName(), director.getId());

        if (rows == 0) {
            throw new NotFoundException("Director with id=" + director.getId() + " not found.");
        }

        return director;
    }

    //delete director
    @Override
    public void delete(int id) {
        String sql = "DELETE FROM directors WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    //get director by film's id
    @Override
    public List<Director> getDirectorByFilmId(int filmId) {
        String sql = "SELECT d.* FROM directors d " +
                "JOIN film_directors fd ON d.id = fd.director_id " +
                "WHERE fd.film_id = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new Director(rs.getInt("id"), rs.getString("name")), filmId);
    }

}

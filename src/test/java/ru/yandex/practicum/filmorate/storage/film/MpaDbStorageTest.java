package ru.yandex.practicum.filmorate.storage.film;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@Import(MpaDbStorage.class)
class MpaDbStorageTest {

    @Autowired
    private MpaDbStorage mpaDbStorage;

    // ---------- Tests ----------

    // Test: Should return all MPA ratings from database
    @Test
    void shouldReturnAllRatings() {
        List<MpaRating> ratings = mpaDbStorage.getAllRatings();

        assertNotNull(ratings);
        assertFalse(ratings.isEmpty(), "Expected non-empty list of ratings");
    }

    // Test: Should return MPA rating by id when it exists
    @Test
    void shouldReturnRatingById() {
        MpaRating rating = mpaDbStorage.getRatingById(1);

        assertNotNull(rating);
        assertEquals(1, rating.getId());
        assertNotNull(rating.getName());
    }

    // Test: Should throw when rating with given id does not exist
    @Test
    void shouldThrowWhenRatingNotFound() {
        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> mpaDbStorage.getRatingById(999)
        );

        assertEquals("MPA rating with id=999 not found.", ex.getMessage());
    }
}
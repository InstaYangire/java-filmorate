package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.film.MpaDbStorage;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MpaService {

    private final MpaDbStorage mpaDbStorage;

    // Getting a list of all MPA ratings
    public List<MpaRating> getAllMpaRatings() {
        return mpaDbStorage.getAllRatings();
    }

    // Getting an MPA rating by id
    public MpaRating getMpaRatingById(int id) {
        return mpaDbStorage.getRatingById(id);
    }
}
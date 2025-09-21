package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Review {
    private int reviewId;
    private String content;
    private Boolean isPositive;
    private int userId;
    private int filmId;
    private int useful; // счетчик полезности (лайки - дизлайки)
}
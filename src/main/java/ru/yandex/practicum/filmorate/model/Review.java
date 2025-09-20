// File: src/main/java/ru/yandex/practicum/filmorate/model/Review.java
package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Review {
    private Long reviewId; // Используем Long, так как ID отзыва может быть большим
    private String content;
    private Boolean isPositive;
    private Long userId;
    private Long filmId;
    private int useful; // Счетчик полезности (лайки - дизлайк и)
}
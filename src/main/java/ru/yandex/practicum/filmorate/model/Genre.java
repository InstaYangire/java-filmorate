package ru.yandex.practicum.filmorate.model;

import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

@Getter
@ToString
public class Genre {
    private final int id;
    private final String name;

    public Genre(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Genre genre)) return false;
        return id == genre.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
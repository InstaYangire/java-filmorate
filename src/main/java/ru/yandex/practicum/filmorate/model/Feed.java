package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Feed {
    private int eventId;
    private long timestamp;
    private int userId;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private EventType eventType;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Operation operation;
    private int entityId;
}
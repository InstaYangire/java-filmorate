package ru.yandex.practicum.filmorate.model;

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
    private EventType eventType;
    private Operation operation;
    private int entityId;
}




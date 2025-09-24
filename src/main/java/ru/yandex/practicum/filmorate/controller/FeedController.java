package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.service.FeedService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class FeedController {

    private final FeedService feedService;

    // Getting feed
    @GetMapping("/{id}/feed")
    public List<Feed> getFeed(@PathVariable int id) {
        log.info("Get feed request for user id={}", id);
        return feedService.getFeedByUserId(id);
    }
}

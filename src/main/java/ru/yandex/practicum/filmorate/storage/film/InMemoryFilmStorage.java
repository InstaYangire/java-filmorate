package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.storage.FilmStorage;


import java.util.*;
import java.util.stream.Collectors;

import static ru.yandex.practicum.filmorate.validator.FilmValidator.validateFilm;

@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Integer, Film> films = new HashMap<>();
    // Storing likes as key pairs in the Map: userId -> Set<filmId>
    private final Map<Integer, Set<Integer>> userToFilms = new HashMap<>();
    // Storing likes as key pairs in the Map: filmId -> Set<userId> for quick access
    private final Map<Integer, Set<Integer>> filmToUsers = new HashMap<>();
    private int nextId = 1;

    // Adding a new movie
    @Override
    public Film addFilm(Film film) {
        validateFilm(film);
        film.setId(nextId++);
        films.put(film.getId(), film);
        return film;
    }

    // Updating an existing movie by id
    @Override
    public Film updateFilm(Film film) {
        if (!films.containsKey(film.getId())) {
            throw new NotFoundException("Film with id=" + film.getId() + " not found.");
        }
        films.put(film.getId(), film);
        return film;
    }

    // Getting a movie by id
    @Override
    public Optional<Film> getFilmById(int id) {
        return Optional.ofNullable(films.get(id));
    }

    // Getting a list of all movies
    @Override
    public List<Film> getAllFilms() {
        return new ArrayList<>(films.values());
    }

    // Getting a list of common films
    @Override
    public List<Film> getCommonFilms(int userId, int friendId) {
        List<Film> commonFilms = new ArrayList<>();

        for (Film film : films.values()) {
            if (film.getLikes().contains(userId) && film.getLikes().contains(friendId))
                commonFilms.add(film);
        }
        return commonFilms;
    }

    // Getting films by director
    @Override
    public List<Film> getFilmsByDirector(int directorId) {
        List<Film> filmsByDirector = new ArrayList<>();

        for (Film film : films.values()) {
            if (film.getDirectors() != null) {
                for (Director director : film.getDirectors()) {
                    if (director.getId() == directorId) {
                        filmsByDirector.add(film);
                        break;
                    }
                }
            }
        }

        return filmsByDirector;
    }

    // Searching films by title, description or director
    @Override
    public List<Film> searchFilms(String query, List<String> by) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        String lowerQuery = query.toLowerCase();

        return films.values().stream()
                .filter(film -> {
                    boolean matches = false;
                    if (by.contains("title") && film.getName() != null &&
                            film.getName().toLowerCase().contains(lowerQuery)) {
                        matches = true;
                    }
                    if (by.contains("description") && film.getDescription() != null &&
                            film.getDescription().toLowerCase().contains(lowerQuery)) {
                        matches = true;
                    }
                    if (by.contains("director") && film.getDirectors() != null &&
                            film.getDirectors().stream()
                                    .anyMatch(d -> d.getName().toLowerCase().contains(lowerQuery))) {
                        matches = true;
                    }
                    return matches;
                })
                .toList();
    }

    // Getting popular films with optional filters (genreId, year)
    @Override
    public List<Film> getPopularFilms(int count, Integer genreId, Integer year) {
        return films.values().stream()
                // фильтр по жанру (если задан)
                .filter(film -> genreId == null ||
                        (film.getGenres() != null && film.getGenres().stream().anyMatch(g -> g.getId() == genreId)))
                // фильтр по году (если задан)
                .filter(film -> year == null ||
                        (film.getReleaseDate() != null && film.getReleaseDate().getYear() == year))
                // сортировка по количеству лайков
                .sorted((f1, f2) -> Integer.compare(f2.getLikes().size(), f1.getLikes().size()))
                .limit(count)
                .toList();
    }

    // __________Likes_____________
    // Adding like
    @Override
    public void addLike(int filmId, int userId) {
        getFilmById(filmId).ifPresentOrElse(
                film -> {
                    if (film.getLikes().contains(userId)) {
                        throw new ValidationException("User with id=" + userId +
                                " has already liked film with id=" + filmId);
                    }
                    film.getLikes().add(userId);
                    // Adding to userToFilms
                    userToFilms.computeIfAbsent(userId, k -> new HashSet<>()).add(filmId);
                    // Adding to filmToUsers
                    filmToUsers.computeIfAbsent(filmId, k -> new HashSet<>()).add(userId);
                },
                () -> {
                    throw new NotFoundException("Film with id=" + filmId + " not found.");
                }
        );
    }

    // Removing like
    @Override
    public void removeLike(int filmId, int userId) {
        getFilmById(filmId).ifPresentOrElse(
                film -> {
                    film.getLikes().remove(userId);

                    // Remove from userToFilms
                    if (userToFilms.containsKey(userId)) {
                        userToFilms.get(userId).remove(filmId);
                        if (userToFilms.get(userId).isEmpty()) {
                            userToFilms.remove(userId);
                        }
                    }
                    // Remove from filmToUsers
                    if (filmToUsers.containsKey(filmId)) {
                        filmToUsers.get(filmId).remove(userId);
                        if (filmToUsers.get(filmId).isEmpty()) {
                            filmToUsers.remove(filmId);
                        }
                    }
                },
                () -> {
                    throw new NotFoundException("Film with id=" + filmId + " not found.");
                }
        );
    }

    // Getting Recommendations
    @Override
    public List<Film> getRecommendations(int userId) {
        // Checking if the user has likes
        if (!userToFilms.containsKey(userId) || userToFilms.get(userId).isEmpty()) {
            return Collections.emptyList();
        }

        // Finding the most similar user
        Integer similarUserId = findMostSimilarUser(userId);

        if (similarUserId == null) {
            return Collections.emptyList();
        }

        // Getting recommendations
        Set<Integer> recommendedFilmIds = getRecommendedFilmIds(userId, similarUserId);
        return getFilmsSortedByPopularity(recommendedFilmIds);
    }

    private Integer findMostSimilarUser(int targetUserId) {
        Set<Integer> targetUserFilms = userToFilms.get(targetUserId);

        return userToFilms.entrySet().stream()
                .filter(entry -> entry.getKey() != targetUserId) // not the target user
                .filter(entry -> !entry.getValue().isEmpty()) // the user has likes
                .map(entry -> {
                    // Finding common films
                    Set<Integer> commonFilms = new HashSet<>(targetUserFilms);
                    commonFilms.retainAll(entry.getValue());
                    return new AbstractMap.SimpleEntry<>(entry.getKey(), commonFilms.size());
                })
                .filter(entry -> entry.getValue() > 0) // there are common likes
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    private Set<Integer> getRecommendedFilmIds(int targetUserId, int similarUserId) {
        Set<Integer> targetUserFilms = userToFilms.get(targetUserId);
        Set<Integer> similarUserFilms = userToFilms.get(similarUserId);

        // Films of a similar user that were not liked by the target user
        Set<Integer> recommended = new HashSet<>(similarUserFilms);
        recommended.removeAll(targetUserFilms);
        return recommended;
    }

    private List<Film> getFilmsSortedByPopularity(Set<Integer> filmIds) {
        return filmIds.stream()
                .map(films::get)
                .filter(Objects::nonNull)
                .sorted((f1, f2) -> {
                    int likes1 = filmToUsers.getOrDefault(f1.getId(), Collections.emptySet()).size();
                    int likes2 = filmToUsers.getOrDefault(f2.getId(), Collections.emptySet()).size();
                    return Integer.compare(likes2, likes1); // descending order
                })
                .collect(Collectors.toList());
    }
}
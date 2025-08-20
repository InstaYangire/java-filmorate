# java-filmorate
Template repository for Filmorate project.

##  Схема базы данных

Ниже представлена диаграмма базы данных, отражающая структуру проекта Filmorate:

![Схема базы данных](./Filmorate.png)

## Примеры SQL-запросов

**Получить все фильмы:**

```sql
SELECT * 
FROM films;
```

**Получить фильм по ID:**

```sql
SELECT * 
FROM films 
WHERE id = 1;
```

**Получить всех пользователей:**

```sql
SELECT * 
FROM users;
```

**Получить пользователя по ID:**

```sql
SELECT * 
FROM users 
WHERE id = 1;
```

**Получить топ-10 популярных фильмов по количеству лайков:**

```sql
SELECT f.*, COUNT(fl.user_id) AS likes
FROM films AS f
LEFT JOIN film_likes AS fl ON f.id = fl.film_id
GROUP BY f.id
ORDER BY likes DESC
LIMIT 10;
```

**Получить общих друзей двух пользователей:**

```sql
SELECT u.*
FROM users AS u
JOIN friendships AS f1 ON u.id = f1.friend_id
JOIN friendships AS f2 ON u.id = f2.friend_id
WHERE f1.user_id = 1 AND f2.user_id = 2 AND f1.status = 'CONFIRMED' AND f2.status = 'CONFIRMED';
```

**Получить все жанры фильма:**

```sql
SELECT g.*
FROM genres AS g
JOIN film_genres AS fg ON g.id = fg.genre_id
WHERE fg.film_id = 1;
```

**Получить рейтинг фильма:**

```sql
SELECT mr.*
FROM mpa_ratings AS mr
JOIN films AS f ON f.mpa_id = mr.id
WHERE f.id = 1;
```


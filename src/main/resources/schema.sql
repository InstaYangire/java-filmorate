CREATE TABLE IF NOT EXISTS mpa_ratings (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR NOT NULL
);

CREATE TABLE IF NOT EXISTS genres (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR NOT NULL
);

CREATE TABLE IF NOT EXISTS directors (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR NOT NULL
);

ALTER TABLE directors ALTER COLUMN id RESTART WITH 1;

CREATE TABLE IF NOT EXISTS users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR NOT NULL,
    login VARCHAR NOT NULL,
    name VARCHAR,
    birthday DATE
);

CREATE TABLE IF NOT EXISTS films (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR NOT NULL,
    description VARCHAR,
    release_date DATE,
    duration INT,
    mpa_id INT,
    CONSTRAINT fk_mpa FOREIGN KEY (mpa_id) REFERENCES mpa_ratings(id)
);

CREATE TABLE IF NOT EXISTS film_likes (
    film_id INT NOT NULL,
    user_id INT NOT NULL,
    PRIMARY KEY (film_id, user_id),
    FOREIGN KEY (film_id) REFERENCES films(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS film_genres (
    film_id INT NOT NULL,
    genre_id INT NOT NULL,
    PRIMARY KEY (film_id, genre_id),
    FOREIGN KEY (film_id) REFERENCES films(id) ON DELETE CASCADE,
    FOREIGN KEY (genre_id) REFERENCES genres(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS friendships (
    user_id INT NOT NULL,
    friend_id INT NOT NULL,
    status VARCHAR NOT NULL,
    PRIMARY KEY (user_id, friend_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (friend_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS film_directors (
    film_id INTEGER REFERENCES films(id) ON DELETE CASCADE,
    director_id INTEGER REFERENCES directors(id) ON DELETE CASCADE,
    PRIMARY KEY (film_id, director_id)
);
-- Drop all tables before creating them
DROP TABLE IF EXISTS review_likes CASCADE;
DROP TABLE IF EXISTS reviews CASCADE;
DROP TABLE IF EXISTS film_directors CASCADE;
DROP TABLE IF EXISTS friendships CASCADE;
DROP TABLE IF EXISTS film_genres CASCADE;
DROP TABLE IF EXISTS film_likes CASCADE;
DROP TABLE IF EXISTS films CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS directors CASCADE;
DROP TABLE IF EXISTS genres CASCADE;
DROP TABLE IF EXISTS mpa_ratings CASCADE;
DROP TABLE IF EXISTS feed CASCADE;

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

  CREATE TABLE IF NOT EXISTS users (
      id INTEGER PRIMARY KEY AUTO_INCREMENT,
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

  CREATE TABLE IF NOT EXISTS reviews (
      review_id INT PRIMARY KEY AUTO_INCREMENT,
      content VARCHAR NOT NULL,
      is_positive BOOLEAN NOT NULL,
      user_id INT NOT NULL,
      film_id INT NOT NULL,
      useful INT DEFAULT 0,
      FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
      FOREIGN KEY (film_id) REFERENCES films(id) ON DELETE CASCADE
  );

  CREATE TABLE IF NOT EXISTS review_likes (
      review_id INT NOT NULL,
      user_id INT NOT NULL,
      is_positive BOOLEAN NOT NULL,
      PRIMARY KEY (review_id, user_id),
      FOREIGN KEY (review_id) REFERENCES reviews(review_id) ON DELETE CASCADE,
      FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
  );

  CREATE TABLE IF NOT EXISTS feed (
      event_id INT PRIMARY KEY AUTO_INCREMENT,
      timestamp BIGINT NOT NULL,
      user_id INT NOT NULL,
      event_type VARCHAR NOT NULL,
      operation VARCHAR NOT NULL,
      entity_id INT NOT NULL
  );

-- Reset counters for deterministic tests
ALTER TABLE mpa_ratings ALTER COLUMN id RESTART WITH 1;
ALTER TABLE genres ALTER COLUMN id RESTART WITH 1;
ALTER TABLE directors ALTER COLUMN id RESTART WITH 1;
ALTER TABLE users ALTER COLUMN id RESTART WITH 1;
ALTER TABLE films ALTER COLUMN id RESTART WITH 1;
ALTER TABLE reviews ALTER COLUMN review_id RESTART WITH 1;
ALTER TABLE feed ALTER COLUMN event_id RESTART WITH 1;
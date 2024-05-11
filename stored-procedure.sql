DELIMITER //

CREATE PROCEDURE add_movie(
    IN movie_title VARCHAR(100),
    IN movie_year INT,
    IN movie_director VARCHAR(100),
    IN movie_genre VARCHAR(32),
    IN star_name VARCHAR(100),
    IN star_birthYear INT
)
BEGIN
    DECLARE genre_id INT;
    DECLARE movie_id VARCHAR(10);
    DECLARE star_id VARCHAR(10);
    DECLARE max_movie_id VARCHAR(10);
    DECLARE max_genre_id INT;
    DECLARE max_star_id VARCHAR(10);

    -- CHECK MOVIE EXISTS
    SELECT id INTO movie_id FROM movies WHERE title = movie_title LIMIT 1;

    IF movie_id IS NULL THEN
        -- GET MAX ID
        SELECT MAX(CAST(SUBSTRING(id, 7) AS UNSIGNED)) INTO max_movie_id FROM movies WHERE id LIKE 'insert%';

        IF max_movie_id IS NULL THEN
            SET movie_id = 'insert0';
        ELSE
            SET movie_id = CONCAT('insert', max_movie_id + 1);
        END IF;

        -- INSERT MOVIE
        INSERT INTO movies (id, title, director, year) VALUES (movie_id, movie_title, movie_director, movie_year);

        -- CHECK GENRE ID
        SELECT id INTO genre_id FROM genres WHERE name = movie_genre LIMIT 1;

        IF genre_id IS NULL THEN
            -- GET MAX GENRE ID
            SELECT MAX(id) INTO max_genre_id FROM genres;
            SET genre_id = max_genre_id + 1;
            INSERT INTO genres (id, name) VALUES (genre_id, movie_genre);
        END IF;

        -- INSERT INTO genres_in_movies
        INSERT INTO genres_in_movies (genreId, movieId) VALUES (genre_id, movie_id);

        -- CHECK STAR ID
        SELECT id INTO star_id FROM stars WHERE name = star_name LIMIT 1;

        IF star_id IS NULL THEN
            -- GET MAX STAR ID
            SELECT MAX(CAST(SUBSTRING(id, 7) AS UNSIGNED)) INTO max_star_id FROM stars WHERE id LIKE 'insert%';

            IF max_star_id IS NULL THEN
                SET star_id = 'insert0';
            ELSE
                SET star_id = CONCAT('insert', max_star_id + 1);
            END IF;

            INSERT INTO stars (id, name, birthYear) VALUES (star_id, star_name, star_birthYear);
        END IF;
        
        -- INSERT INTO stars_in_movies
        INSERT INTO stars_in_movies (starId, movieId) VALUES (star_id, movie_id);
        SELECT CONCAT("Added Successfully. Movie ID: ", movie_id, ", Star ID: ", star_id, ", Genre ID: ", genre_id) AS message;
    ELSE
        SELECT 'ERROR: Movie Already Exist' AS message;
    END IF;
END //

DELIMITER ;
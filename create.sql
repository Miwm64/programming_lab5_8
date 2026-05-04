CREATE TYPE movie_genre AS ENUM ('DRAMA', 'MUSICAL', 'TRAGEDY', 'THRILLER');
CREATE TYPE mpaa_rating AS ENUM ('PG_13', 'R', 'NC_17');
CREATE TYPE color AS ENUM ('GREEN', 'RED', 'YELLOW', 'ORANGE', 'BROWN');
CREATE TYPE country AS ENUM ('UNITED_KINGDOM', 'CHINA', 'INDIA', 'ITALY', 'THAILAND');

CREATE SEQUENCE IF NOT EXISTS person_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE IF NOT EXISTS movie_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE IF NOT EXISTS person (
  id BIGINT PRIMARY KEY DEFAULT nextval('person_id_seq'),
    name VARCHAR(255) NOT NULL CHECK (TRIM(name) <> ''),
    weight REAL CHECK (weight > 0),
    hair_color color NOT NULL,
    nationality country NOT NULL
    );


CREATE TABLE IF NOT EXISTS movie (
    id BIGINT PRIMARY KEY DEFAULT nextval('movie_id_seq'),
    coord_x REAL CHECK (coord_x  <= 274),
    coord_y BIGINT NOT NULL CHECK (coord_y  > -559),
    name VARCHAR(255) NOT NULL CHECK (TRIM(name) <> ''),
    creation_date TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    oscars_count INTEGER CHECK (oscars_count > 0),
    golden_palm_count BIGINT CHECK (golden_palm_count > 0),
    genre movie_genre,
    mpaa_rating mpaa_rating NOT NULL,
    operator_id BIGINT REFERENCES person(id)
    );


CREATE INDEX IF NOT EXISTS idx_movie_operator ON movie(operator_id);
CREATE INDEX IF NOT EXISTS idx_movie_genre ON movie(genre);
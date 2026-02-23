package ru.spb.miwm64.moviemanager.entities;

import ru.spb.miwm64.moviemanager.exceptions.InvalidValueException;

import java.time.ZonedDateTime;
import java.util.Objects;

public class Movie implements Comparable<Movie> {
    private Long id; //Поле не может быть null, Значение поля должно быть больше 0, Значение этого поля должно быть уникальным, Значение этого поля должно генерироваться автоматически
    private String name; //Поле не может быть null, Строка не может быть пустой
    private Coordinates coordinates; //Поле не может быть null
    private java.time.ZonedDateTime creationDate; //Поле не может быть null, Значение этого поля должно генерироваться автоматически
    private int oscarsCount; //Значение поля должно быть больше 0
    private long goldenPalmCount; //Значение поля должно быть больше 0
    private MovieGenre genre; //Поле может быть null
    private MpaaRating mpaaRating; //Поле не может быть null
    private Person operator; //Поле может быть null

    public Movie(Long id, String name, Coordinates coordinates, ZonedDateTime creationDate, int oscarsCount,
                 long goldenPalmCount, MovieGenre genre, MpaaRating mpaaRating,  Person operator ) {
        setCoordinates(coordinates);
        setCreationDate(creationDate);
        setGenre(genre);
        setGoldenPalmCount(goldenPalmCount);
        setId(id);
        setMpaaRating(mpaaRating);
        setName(name);
        setOperator(operator);
        setOscarsCount(oscarsCount);
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = Objects.requireNonNull(coordinates);
    }

    public ZonedDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(ZonedDateTime creationDate) {
        this.creationDate = Objects.requireNonNull(creationDate);
    }

    public MovieGenre getGenre() {
        return genre;
    }

    public void setGenre(MovieGenre genre) {
        this.genre = genre;
    }

    public long getGoldenPalmCount() {
        return goldenPalmCount;
    }

    public void setGoldenPalmCount(long goldenPalmCount) {
        if (goldenPalmCount <= 0){
            throw new InvalidValueException("goldenPalmCount must be greater than 0");
        }
        this.goldenPalmCount = goldenPalmCount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        Objects.requireNonNull(id);
        if (id <= 0){
            throw new InvalidValueException("movie id must be greater than 0");
        }
        this.id = id;
    }

    public MpaaRating getMpaaRating() {
        return mpaaRating;
    }

    public void setMpaaRating(MpaaRating mpaaRating) {
        this.mpaaRating = Objects.requireNonNull(mpaaRating);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        Objects.requireNonNull(name, "name cannot be null");
        if (name.isEmpty()){
            throw new InvalidValueException("name can not be empty");
        }
        this.name = name;
    }

    public Person getOperator() {
        return operator;
    }

    public void setOperator(Person operator) {
        this.operator = operator;
    }

    public int getOscarsCount() {
        return oscarsCount;
    }

    public void setOscarsCount(int oscarsCount) {
        if (oscarsCount <= 0){
            throw new InvalidValueException("oscarsCount must be greater than 0");
        }
        this.oscarsCount = oscarsCount;
    }

    @Override
    public int compareTo(Movie o) {
        if (this.goldenPalmCount < o.goldenPalmCount){
            return -1;
        }
        if (this.goldenPalmCount > o.goldenPalmCount){
            return 1;
        }
        if (this.oscarsCount < o.oscarsCount){
            return -1;
        }
        if (this.oscarsCount > o.oscarsCount){
            return 1;
        }
        return 0;
    }

    @Override
    public String toString() {
        return "Movie{" +
                "coordinates=" + coordinates +
                ", id=" + id +
                ", name='" + name + '\'' +
                ", creationDate=" + creationDate +
                ", oscarsCount=" + oscarsCount +
                ", goldenPalmCount=" + goldenPalmCount +
                ", genre=" + genre +
                ", mpaaRating=" + mpaaRating +
                ", operator=" + operator +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Movie movie = (Movie) o;
        return oscarsCount == movie.oscarsCount && goldenPalmCount == movie.goldenPalmCount &&
                Objects.equals(name, movie.name) && Objects.equals(coordinates, movie.coordinates) &&
                genre == movie.genre && mpaaRating == movie.mpaaRating && Objects.equals(operator, movie.operator);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, coordinates, oscarsCount, goldenPalmCount, genre, mpaaRating, operator);
    }
}


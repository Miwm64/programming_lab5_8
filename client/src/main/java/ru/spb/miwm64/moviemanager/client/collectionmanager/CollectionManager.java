package ru.spb.miwm64.moviemanager.client.collectionmanager;

import ru.spb.miwm64.moviemanager.client.entities.Movie;
import ru.spb.miwm64.moviemanager.client.entities.Person;

import java.util.ArrayList;

public interface CollectionManager {
    void append(Movie movie);
    boolean addIfMin(Movie movie);

    void setCollection(ArrayList<Movie> movies);
    void setById(Long id, Movie movie);

    Movie getById(Long id);
    Movie getByIndex(int index);
    ArrayList<Movie> getGreater(Person person);
    ArrayList<Movie> getAll();

    void removeById(Long id);
    void removeByIndex(int index);
    void removeGreater(Movie movie);
    void removeAll();

    long countByGoldenPalmCount(long count);
    ArrayList<Movie> filterGreaterThanOperatorCommand(Person p);
    ArrayList<Movie> printFieldAscendingGoldenPalmCountCommand();
}

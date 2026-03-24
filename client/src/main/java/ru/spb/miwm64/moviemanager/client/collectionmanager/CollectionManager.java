package ru.spb.miwm64.moviemanager.client.collectionmanager;

import ru.spb.miwm64.moviemanager.common.entities.Movie;
import ru.spb.miwm64.moviemanager.common.entities.Person;

import java.util.ArrayList;

public interface CollectionManager {
    int add(Movie movie);
    boolean addIfMin(Movie movie);

    void setById(Long id, Movie movie);

    Movie getById(Long id);
    Movie getByIndex(int index);
    ArrayList<Movie> getGreater(Person person);
    ArrayList<Movie> getAll();

    void removeById(Long id);
    void removeByIndex(int index);
    void removeGreater(Movie movie);
    void removeAll();
    void clear();

    long countByGoldenPalmCount(long count);
    ArrayList<Movie> filterGreaterThanOperatorCommand(Person p);
    ArrayList<Movie> printFieldAscendingGoldenPalmCountCommand();
}

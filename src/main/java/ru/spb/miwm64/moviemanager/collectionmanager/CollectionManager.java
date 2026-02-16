package ru.spb.miwm64.moviemanager.collectionmanager;

import ru.spb.miwm64.moviemanager.entities.Movie;
import ru.spb.miwm64.moviemanager.entities.Person;

import java.util.ArrayList;

public interface CollectionManager {
    void append();
    void updateId(int oldId, int newId);
    void addIfMin(Movie movie);

    Movie getById(int id);
    Movie getByIndex(int index);
    ArrayList<Movie> getGreater(Person person);
    ArrayList<Movie> getAll();

    void removeById(int id);
    void removeByIndex(int index);
    void removeGreater(Movie movie);
    void removeAll();
}

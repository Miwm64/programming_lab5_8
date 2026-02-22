package ru.spb.miwm64.moviemanager.collectionmanager;

import ru.spb.miwm64.moviemanager.entities.Movie;
import ru.spb.miwm64.moviemanager.entities.Person;

import java.util.ArrayList;

public class BasicCollectionManager implements CollectionManager {
    ArrayList<Movie> movies;

    public BasicCollectionManager() {
        movies = new ArrayList<>();
    }

    public BasicCollectionManager(ArrayList<Movie> movies) {
        this.movies = new ArrayList<>(movies);
    }

    @Override
    public void append() {

    }

    @Override
    public void updateId(int oldId, int newId) {

    }

    @Override
    public void addIfMin(Movie movie) {

    }

    @Override
    public Movie getById(int id) {
        return null;
    }

    @Override
    public Movie getByIndex(int index) {
        return null;
    }

    @Override
    public ArrayList<Movie> getGreater(Person person) {
        return null;
    }

    @Override
    public ArrayList<Movie> getAll() {
        return null;
    }

    @Override
    public void removeById(int id) {

    }

    @Override
    public void removeByIndex(int index) {

    }

    @Override
    public void removeGreater(Movie movie) {

    }

    @Override
    public void removeAll() {
        this.movies = new ArrayList<>();
    }

}

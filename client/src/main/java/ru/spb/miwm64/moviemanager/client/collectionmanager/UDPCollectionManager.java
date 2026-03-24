package ru.spb.miwm64.moviemanager.client.collectionmanager;

import ru.spb.miwm64.moviemanager.client.entities.Movie;
import ru.spb.miwm64.moviemanager.client.entities.Person;

import java.util.ArrayList;

public class UDPCollectionManager implements CollectionManager {
    @Override
    public void append(Movie movie) {

    }

    @Override
    public boolean addIfMin(Movie movie) {
        return false;
    }

    @Override
    public void setCollection(ArrayList<Movie> movies) {

    }

    @Override
    public void setById(Long id, Movie movie) {

    }

    @Override
    public Movie getById(Long id) {
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
    public void removeById(Long id) {

    }

    @Override
    public void removeByIndex(int index) {

    }

    @Override
    public void removeGreater(Movie movie) {

    }

    @Override
    public void removeAll() {

    }

    @Override
    public void clear() {

    }

    @Override
    public long countByGoldenPalmCount(long count) {
        return 0;
    }

    @Override
    public ArrayList<Movie> filterGreaterThanOperatorCommand(Person p) {
        return null;
    }

    @Override
    public ArrayList<Movie> printFieldAscendingGoldenPalmCountCommand() {
        return null;
    }
}
